package com.server.server.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:8080")
public class AuthController {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthController(UserRepository userRepo, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        return userRepo.findByUsername(request.username())
                .map(u -> {
                    if (!u.isActive()) {
                        return ResponseEntity.status(403).body("Account is inactive. Please contact admin.");
                    }

                    if (passwordEncoder.matches(request.password(), u.getPassword())) {
                        String token = jwtUtil.generateToken(u.getUsername(), u.getRole());
                        return ResponseEntity.ok(new AuthResponse(u.getUsername(), u.getRole(), token));
                    } else {
                        return ResponseEntity.status(401).body("Invalid credentials");
                    }
                })
                .orElseGet(() -> ResponseEntity.status(401).body("Invalid credentials"));
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userRepo.findAll();
        return ResponseEntity.ok(users);
    }

    @PutMapping("/users/{id}/active")
    public ResponseEntity<?> updateUserActiveStatus(@PathVariable Long id, @RequestBody boolean active) {
        return userRepo.findById(id)
                .map(user -> {
                    user.setActive(active);
                    userRepo.save(user);
                    return ResponseEntity.ok("User active status updated");
                })
                .orElseGet(() -> ResponseEntity.status(404).body("User not found"));
    }

    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody User newUser) {
        newUser.setActive(true);
        newUser.setCreatedDate(LocalDateTime.now());
        newUser.setPassword(passwordEncoder.encode(newUser.getPassword()));

        if (newUser.getRole() == null || newUser.getRole().isEmpty()) {
            newUser.setRole("VIEWER");
        }

        User saved = userRepo.save(newUser);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        return userRepo.findById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).body("User not found"));
    }


    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User updatedUser) {
        Optional<User> optionalUser = userRepo.findById(id);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(404).build();
        }

        User user = optionalUser.get();
        user.setUsername(updatedUser.getUsername());

        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
        }

        user.setEmail(updatedUser.getEmail());
        user.setActive(updatedUser.isActive());
        userRepo.save(user);

        return ResponseEntity.ok(user);
    }
}
