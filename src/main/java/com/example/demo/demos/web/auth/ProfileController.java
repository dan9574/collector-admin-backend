package com.example.demo.demos.web.auth;

import com.example.demo.demos.web.user.User;
import com.example.demo.demos.web.user.UserRepository;
import com.example.demo.demos.web.user.PasswordService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
public class ProfileController {

    private final TokenStore tokenStore;
    private final UserRepository userRepository;
    private final SessionStore sessionStore;
    private final PasswordService passwordService;

    public ProfileController(TokenStore tokenStore,
                             UserRepository userRepository,
                             SessionStore sessionStore,
                             PasswordService passwordService) {
        this.tokenStore = tokenStore;
        this.userRepository = userRepository;
        this.sessionStore = sessionStore;
        this.passwordService = passwordService;
    }

    private Optional<String> getCurrentUsername(String auth, String sid) {
        Optional<String> usernameOpt = Optional.empty();
        if (auth != null && auth.startsWith("Bearer ")) {
            String token = auth.substring(7);
            usernameOpt = tokenStore.getUsername(token);
        }
        if (!usernameOpt.isPresent() && sid != null && !sid.isEmpty()) {
            usernameOpt = sessionStore.get(sid).map(s -> s.username);
        }
        return usernameOpt;
    }

    @GetMapping("/api/profile")
    public ResponseEntity<?> profile(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @CookieValue(value = "sid", required = false) String sid) {

        Optional<String> usernameOpt = getCurrentUsername(auth, sid);

        return usernameOpt
                .flatMap(userRepository::findByUsername)
                .<ResponseEntity<?>>map(u -> ResponseEntity.ok(new FullProfile(u)))
                .orElseGet(() -> ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(java.util.Collections.singletonMap("error", "unauthorized")));
    }

    @PutMapping("/api/profile")
    public ResponseEntity<?> updateProfile(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @CookieValue(value = "sid", required = false) String sid,
            @RequestBody FullProfile req) {

        Optional<String> usernameOpt = getCurrentUsername(auth, sid);

        if (!usernameOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(java.util.Collections.singletonMap("error", "unauthorized"));
        }

        Optional<User> userOpt = userRepository.findByUsername(usernameOpt.get());
        if (!userOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(java.util.Collections.singletonMap("error", "user not found"));
        }

        User user = userOpt.get();
        user.setRealName(req.realName);
        user.setPosition(req.position);
        user.setEmail(req.email);
        user.setPhone(req.phone);
        user.setBio(req.bio);

        userRepository.save(user);

        return ResponseEntity.ok(new FullProfile(user));
    }

    /**
     * PUT /api/profile/password
     * 修改当前用户密码
     * 前端需传 newPassword 字段
     */
    @PutMapping("/api/profile/password")
    public ResponseEntity<?> changePassword(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @CookieValue(value = "sid", required = false) String sid,
            @RequestBody PasswordChangeReq req) {

        if (req == null || req.newPassword == null || req.newPassword.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(java.util.Collections.singletonMap("error", "新密码不能为空"));
        }

        Optional<String> usernameOpt = getCurrentUsername(auth, sid);

        if (!usernameOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(java.util.Collections.singletonMap("error", "unauthorized"));
        }

        Optional<User> userOpt = userRepository.findByUsername(usernameOpt.get());
        if (!userOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(java.util.Collections.singletonMap("error", "user not found"));
        }

        User user = userOpt.get();
        user.setPasswordHash(passwordService.encode(req.newPassword));
        userRepository.save(user);

        return ResponseEntity.ok(java.util.Collections.singletonMap("success", true));
    }

    // 完整资料
    static class FullProfile {
        public Long id;
        public String username;
        public String role;
        public String realName;
        public String position;
        public String email;
        public String phone;
        public String bio;

        public FullProfile(User u) {
            this.id = u.getId();
            this.username = u.getUsername();
            this.role = u.getRole();
            this.realName = u.getRealName();
            this.position = u.getPosition();
            this.email = u.getEmail();
            this.phone = u.getPhone();
            this.bio = u.getBio();
        }

        public FullProfile() {}
    }

    // 修改密码请求体
    static class PasswordChangeReq {
        public String newPassword;
        public PasswordChangeReq() {}
    }
}