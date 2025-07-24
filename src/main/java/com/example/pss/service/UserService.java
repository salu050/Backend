package com.example.pss.service;

import com.example.pss.model.User;
import java.util.List;
import java.util.Optional;

public interface UserService {
    Optional<User> register(User user);

    Optional<User> login(String username, String password);

    boolean resetPassword(String username, String newPassword);

    Optional<User> findById(Long id);

    Optional<User> findByUsername(String username);

    List<User> getAllUsers();
}
