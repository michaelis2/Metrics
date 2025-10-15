package com.server.server.auth;

import java.time.LocalDateTime;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer
        implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void run(String ... args) {
        if (!this.userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(this.passwordEncoder.encode((CharSequence)"admin"));
            admin.setEmail("admin@example.com");
            admin.setActive(true);
            admin.setRole("ADMIN");
            admin.setCreatedDate(LocalDateTime.now());
            this.userRepository.save(admin);
            System.out.println("\u2705 Created default admin user: admin / admin");
        }
    }
}

