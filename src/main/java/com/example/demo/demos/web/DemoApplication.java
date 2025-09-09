package com.example.demo.demos.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;

import com.example.demo.demos.web.user.UserRepository;
import com.example.demo.demos.web.user.PasswordService;
import com.example.demo.demos.web.user.User;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    // 启动时播种 admin / admin123
    @Bean
    public CommandLineRunner seedAdmin(UserRepository userRepo,
                                       PasswordService passwordService) {
        return (args) -> {
            userRepo.findByUsername("admin").orElseGet(() -> {
                User u = new User();
                u.setUsername("admin");
                u.setPasswordHash(passwordService.encode("admin123"));
                u.setRole("ADMIN");
                u.setCreatedAt(java.time.Instant.now()); // ✅ 改成 Instant
                return userRepo.save(u);
            });
        };
    }
}
