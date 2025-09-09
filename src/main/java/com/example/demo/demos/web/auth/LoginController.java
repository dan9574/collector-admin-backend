package com.example.demo.demos.web.auth;

import com.example.demo.demos.web.user.UserRepository;
import com.example.demo.demos.web.user.PasswordService;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.util.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;

@RestController
public class LoginController {

    private final UserRepository userRepository;
    private final PasswordService passwordService;
    private final SessionStore sessionStore;

    public LoginController(UserRepository userRepository,
                           PasswordService passwordService,
                           SessionStore sessionStore) {
        this.userRepository = userRepository;
        this.passwordService = passwordService;
        this.sessionStore = sessionStore;
    }

    @PostMapping("/api/login")
    public ResponseEntity<?> login(@RequestBody LoginReq req, HttpServletResponse resp) {
        return userRepository.findByUsername(req.getUsername())
                .filter(u -> passwordService.matches(req.getPassword(), u.getPasswordHash()))
                .<ResponseEntity<?>>map(u -> {
                    String sid = sessionStore.create(u.getUsername());

                    // 用 ResponseCookie 写 SameSite=Lax（开发期 http 不要 secure）
                    ResponseCookie cookie = ResponseCookie.from("sid", sid)
                            .httpOnly(true)
                            .secure(false)             // 本地 http 开发必须 false
                            .path("/")
                            .maxAge(Duration.ofDays(7))
                            .sameSite("Lax")
                            .build();
                    resp.addHeader("Set-Cookie", cookie.toString());

                    Map<String, Object> okBody = new HashMap<>();
                    okBody.put("success", true);
                    okBody.put("username", u.getUsername());
                    // okBody.put("sid", sid); // 若不想暴露可去掉，仅调试时保留
                    return ResponseEntity.ok(okBody);
                })
                .orElseGet(() -> {
                    Map<String, Object> errBody = new HashMap<>();
                    errBody.put("success", false);
                    errBody.put("message", "用户名或密码错误");
                    return ResponseEntity.status(401).body(errBody);
                });
    }
    @PostMapping("/api/logout")
    public ResponseEntity<Void> logout(@CookieValue(value = "sid", required = false) String sid) {
        if (sid != null) sessionStore.remove(sid);            // 清内存会话
        ResponseCookie clear = ResponseCookie.from("sid", "")  // 让浏览器删除 Cookie
                .httpOnly(true).path("/").sameSite("Lax").maxAge(0).build();
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, clear.toString()).build();
    }
}
