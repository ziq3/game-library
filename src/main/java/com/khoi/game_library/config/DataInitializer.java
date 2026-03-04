package com.khoi.game_library.config;

import com.khoi.game_library.model.User;
import com.khoi.game_library.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner seedDemoUser(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (!userRepository.existsByUsername("demo")) {
                User user = new User();
                user.setUsername("demo");
                user.setEmail("demo@example.com");
                user.setPassword(passwordEncoder.encode("password"));
                userRepository.save(user);
            }
        };
    }
}
