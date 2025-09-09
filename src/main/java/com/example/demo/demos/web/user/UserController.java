package com.example.demo.demos.web.user;

import com.example.demo.demos.web.auth.TokenStore;
import com.example.demo.demos.web.auth.SessionStore;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository repo;
    private final PasswordService passwordService;
    private final TokenStore tokenStore;
    private final SessionStore sessionStore;

    public UserController(UserRepository repo,
                          PasswordService passwordService,
                          TokenStore tokenStore,
                          SessionStore sessionStore) {
        this.repo = repo;
        this.passwordService = passwordService;
        this.tokenStore = tokenStore;
        this.sessionStore = sessionStore;
    }

    // 同时支持 Bearer 与 Cookie(sid)
    private User getCurrentUser(String auth, String sid) {
        if (auth != null && auth.startsWith("Bearer ")) {
            String token = auth.substring(7);
            return tokenStore.getUser(token)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        }
        if (sid != null) {
            return sessionStore.getUser(sid)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
    }

    // 兼容 ids=1,2,3 或 ids=1&ids=2&ids=3，登录即可用
    @GetMapping("/lookup")
    public ResponseEntity<Map<Long, String>> lookupByIds(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @CookieValue(value = "sid", required = false) String sid,
            @RequestParam(value = "ids", required = false) List<String> idsParam
    ) {
        getCurrentUser(auth, sid); // 仅校验已登录

        // 解析 ID（兼容逗号与重复参数）
        LinkedHashSet<Long> ids = new LinkedHashSet<Long>();
        if (idsParam != null) {
            for (String item : idsParam) {
                if (item == null) continue;
                String[] parts = item.split(",");
                for (String p : parts) {
                    String t = p.trim();
                    if (!t.isEmpty()) {
                        try { ids.add(Long.valueOf(t)); } catch (NumberFormatException ignore) {}
                    }
                }
            }
        }
        if (ids.isEmpty()) return ResponseEntity.ok(Collections.<Long, String>emptyMap());

        Map<Long, String> out = new LinkedHashMap<Long, String>();
        for (User u : repo.findAllById(ids)) {
            out.put(u.getId(), displayName(u));
        }
        // 不存在的 ID 兜底
        for (Long id : ids) {
            if (!out.containsKey(id)) out.put(id, "用户#" + id);
        }
        return ResponseEntity.ok(out);
    }

    @GetMapping
    public ResponseEntity<?> listUsers(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @CookieValue(value = "sid", required = false) String sid) {
        User current = getCurrentUser(auth, sid);
        if ("ADMIN".equalsIgnoreCase(current.getRole())) {
            return ResponseEntity.ok(repo.findAll());
        } else {
            return ResponseEntity.ok(Collections.singletonList(current));
        }
    }

    @PostMapping
    public ResponseEntity<?> createUser(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @CookieValue(value = "sid", required = false) String sid,
            @RequestBody User newUser) {

        User current = getCurrentUser(auth, sid);
        if (!"ADMIN".equalsIgnoreCase(current.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No permission");
        }

        newUser.setRole("USER");
        newUser.setPasswordHash(passwordService.hash(newUser.getPasswordHash()));
        newUser.setCreatedAt(Instant.now());
        return ResponseEntity.status(HttpStatus.CREATED).body(repo.save(newUser));
    }

    // —— helpers ——
    private static String displayName(User u) {
        if (u.getRealName() != null && u.getRealName().trim().length() > 0) return u.getRealName();
        if (u.getUsername() != null && u.getUsername().trim().length() > 0) return u.getUsername();
        return "用户#" + u.getId();
    }
}
