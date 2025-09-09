package com.example.demo.demos.web.user;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class PasswordService {
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    // 统一用 encode
    public String encode(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    public boolean matches(String rawPassword, String passwordHash) {
        return encoder.matches(rawPassword, passwordHash);
    }

    // 如果你懒得改 Controller 里 hash(...) 的调用，可以加个别名
    public String hash(String rawPassword) {
        return encode(rawPassword);
    }
}
