package com.example.pss.Security;

import com.example.pss.model.User;
import com.example.pss.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository; // Assuming you have a UserRepository

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Fetch the User entity from the database
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        // Since your User model implements UserDetails, you can return it directly.
        // Spring Security will then use the getAuthorities() method from your User
        // model
        // to determine the user's roles (e.g., ROLE_ADMIN, ROLE_STUDENT).
        return user;
    }
}