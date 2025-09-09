package com.example.demo.demos.web.auth;

import com.example.demo.demos.web.user.User;
import com.example.demo.demos.web.user.UserRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class TokenStore {
    // token -> username
    private final ConcurrentMap<String, String> tokenToUser = new ConcurrentHashMap<>();
    private final UserRepository userRepository;

    public TokenStore(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void put(String token, String username) {
        tokenToUser.put(token, username);
    }

    // 新接口：直接返回 User
    public Optional<User> getUser(String token) {
        return Optional.ofNullable(tokenToUser.get(token))
                .flatMap(userRepository::findByUsername);
    }

    // 兼容老接口：返回用户名
    public Optional<String> getUsername(String token) {
        return Optional.ofNullable(tokenToUser.get(token));
    }

    public void revoke(String token) {
        tokenToUser.remove(token);
    }
}
