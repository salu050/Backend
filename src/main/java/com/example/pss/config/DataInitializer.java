package com.example.pss.config;

import com.example.pss.model.User;
import com.example.pss.model.Role; // Import your custom Role enum
import com.example.pss.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    // It's better to inject PasswordEncoder if it's a Spring Bean,
    // but for a simple initializer, instantiating it directly is acceptable.
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public void run(String... args) {
        // Set admin user: username = admin1@gmail.com, password = admin1password
        if (userRepository.findByUsername("abourumaysah@gmail.com").isEmpty()) {
            User admin = new User();
            admin.setUsername("abourumaysah@gmail.com");
            admin.setPassword(passwordEncoder.encode("Salim@6919"));
            // FIX: Use the Role enum constant
            admin.setRole(Role.ADMIN);
            userRepository.save(admin);
        }

        // Set ministry user: username = ministry1@gmail.com, password = ministry123
        if (userRepository.findByUsername("educational@gmail.com").isEmpty()) {
            User ministry = new User();
            ministry.setUsername("educational@gmail.com");
            ministry.setPassword(passwordEncoder.encode("Salim@6919"));
            // FIX: Use the Role enum constant
            ministry.setRole(Role.MINISTRY);
            userRepository.save(ministry);
        }
    }
}