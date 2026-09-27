package com.ahmtcnmn.manager.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.ahmtcnmn.manager.model.User;
import com.ahmtcnmn.manager.repository.UserRepository;

import lombok.RequiredArgsConstructor;


@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.initial-username}")
    private String adminUsername;

    @Value("${admin.initial-password}")
    private String adminPassword;

    @Override
    public void run(String... args) {
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = new User();
            admin.setId(1L);
            admin.setUsername("admin");
            admin.setPasswordHash(passwordEncoder.encode(adminPassword));
            admin.setRole("ADMIN");
            userRepository.save(admin);
            System.out.println(">>> Admin kullanıcı oluşturuldu");
        }
    }
}
