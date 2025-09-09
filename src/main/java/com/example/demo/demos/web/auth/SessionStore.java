// src/main/java/com/example/demo/demos/web/auth/SessionStore.java
package com.example.demo.demos.web.auth;

import com.example.demo.demos.web.user.User;
import com.example.demo.demos.web.user.UserRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SessionStore {

    public static class Session {
        public final String sid;
        public final String username;
        public final Instant createdAt;

        public Session(String sid, String username) {
            this.sid = sid;
            this.username = username;
            this.createdAt = Instant.now();
        }
    }

    // sid -> Session
    private final Map<String, Session> bySid = new ConcurrentHashMap<>();
    private final UserRepository userRepository; // ✨ 注入用于查询 User

    public SessionStore(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /** 创建会话，返回 sid（登录成功时调用） */
    public String create(String username) {
        String sid = UUID.randomUUID().toString().replace("-", "");
        bySid.put(sid, new Session(sid, username));
        return sid;
    }

    /** 取底层 Session 对象 */
    public Optional<Session> get(String sid) {
        return Optional.ofNullable(bySid.get(sid));
    }

    /** 直接拿 username（有时也会用到） */
    public Optional<String> getUsername(String sid) {
        return get(sid).map(s -> s.username);
    }

    /** ✨ 直接拿 User（给 Controller 用） */
    public Optional<User> getUser(String sid) {
        return getUsername(sid).flatMap(userRepository::findByUsername);
    }

    /** 注销/移除会话 */
    public void remove(String sid) {
        bySid.remove(sid);
    }
}
