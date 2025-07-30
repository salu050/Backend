package com.example.pss.controller;

import com.example.pss.model.User;
import com.example.pss.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/users")
// FIX: Updated CrossOrigin to allow both HTTP and HTTPS from localhost:3000
@CrossOrigin(origins = { "http://localhost:3000", "https://localhost:3000" })
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Handles user login.
     * Authenticates a user with provided username and password.
     * Returns the full User object (including role and hasPaidApplicationFee) upon
     * successful login.
     *
     * @param loginData A map containing "username" and "password".
     * @return ResponseEntity with User object if successful, or 401 if invalid
     *         credentials.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginData) {
        String username = loginData.get("username");
        String password = loginData.get("password");
        Optional<User> user = userService.login(username, password);

        if (user.isPresent()) {
            // Returning the full User object is good because it includes
            // hasPaidApplicationFee
            // which the frontend uses for conditional rendering/navigation.
            return ResponseEntity.ok(user.get());
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Invalid credentials"));
        }
    }

    /**
     * Registers a new user.
     * The role is typically set to "STUDENT" by default in the service or here.
     * Returns key user details including the initial hasPaidApplicationFee status.
     *
     * @param user The User object containing username, password, and optionally
     *             role.
     * @return ResponseEntity with registered user details if successful, or 400 if
     *         user already exists.
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        Optional<User> registered = userService.register(user);

        if (registered.isPresent()) {
            User regUser = registered.get();
            Map<String, Object> response = new HashMap<>();
            response.put("id", regUser.getId());
            response.put("username", regUser.getUsername());
            response.put("role", regUser.getRole());
            response.put("createdAt", regUser.getCreatedAt());
            response.put("hasPaidApplicationFee", regUser.isHasPaidApplicationFee());
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "User already exists with this username"));
        }
    }

    /**
     * Resets a user's password.
     *
     * @param data A map containing "username" and "newPassword".
     * @return ResponseEntity with success message, or 404 if user not found.
     */
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> data) {
        String username = data.get("username");
        String newPassword = data.get("newPassword");
        boolean updated = userService.resetPassword(username, newPassword);

        if (updated) {
            return ResponseEntity.ok(Map.of("message", "Password updated successfully!"));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "User not found."));
        }
    }

    /**
     * Handles the request to send a password reset link (or initiate reset
     * process).
     * This endpoint is called by the frontend when the user requests to reset their
     * password.
     * It typically simulates sending an email or performs initial validation.
     *
     * @param data A map containing "username".
     * @return ResponseEntity with a message indicating success or failure of the
     *         request.
     */
    @PostMapping("/reset-password-request")
    public ResponseEntity<?> requestPasswordReset(@RequestBody Map<String, String> data) {
        String username = data.get("username");
        // In a real application, you would:
        // 1. Validate the username.
        // 2. Generate a unique token.
        // 3. Store the token with an expiration time associated with the user.
        // 4. Send an email to the user with a link containing the token.
        // For this demo, we'll just simulate success.

        boolean userExists = userService.findByUsername(username).isPresent(); // Assuming findByUsername exists

        if (userExists) {
            // Simulate sending email (replace with actual email sending logic)
            System.out.println("Simulating password reset link sent to: " + username);
            return ResponseEntity.ok(Map.of("message", "Password reset link sent to your email (simulated)."));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "User with that username not found."));
        }
    }

    /**
     * Retrieves user details by ID.
     * Restricted to ADMIN role for security.
     *
     * @param id The ID of the user to retrieve.
     * @return ResponseEntity with user details, or 404 if user not found.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')") // Restrict to ADMINs
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        Optional<User> userOpt = userService.findById(id);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            Map<String, Object> map = new HashMap<>();
            map.put("id", user.getId());
            map.put("username", user.getUsername());
            map.put("role", user.getRole());
            map.put("createdAt", user.getCreatedAt());
            map.put("hasPaidApplicationFee", user.isHasPaidApplicationFee());
            return ResponseEntity.ok(map);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "User not found"));
        }
    }

    /**
     * Retrieves a list of all users.
     * Restricted to ADMIN role for security.
     *
     * @return ResponseEntity with a list of user details.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')") // Restrict to ADMINs
    public ResponseEntity<List<Map<String, Object>>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        List<Map<String, Object>> result = new ArrayList<>();
        for (User u : users) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", u.getId());
            map.put("username", u.getUsername());
            map.put("role", u.getRole());
            map.put("createdAt", u.getCreatedAt());
            map.put("hasPaidApplicationFee", u.isHasPaidApplicationFee());
            result.add(map);
        }
        return ResponseEntity.ok(result);
    }
}
