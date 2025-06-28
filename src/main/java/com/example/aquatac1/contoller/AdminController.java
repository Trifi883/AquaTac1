package com.example.aquatac1.contoller;

import com.example.aquatac1.dto.UserInfoResponse;
import com.example.aquatac1.model.User;
import com.example.aquatac1.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    @Autowired
    UserRepository userRepository;

    @GetMapping("/dashboard")
    public String adminDashboard() {
        return "Admin Dashboard";
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserInfoResponse>> getAllUsers() {
        List<UserInfoResponse> users = userRepository.findAll().stream()
                .map(user -> new UserInfoResponse(
                        user.getId(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getRoles().stream()
                                .map(role -> role.getName().name())
                                .collect(Collectors.toList()),
                        user.isApproved()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(users);
    }

    @GetMapping("/pending-approvals")
    public ResponseEntity<List<UserInfoResponse>> getPendingApprovals() {
        List<UserInfoResponse> pendingUsers = userRepository.findByIsApprovedFalse().stream()
                .map(user -> new UserInfoResponse(
                        user.getId(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getRoles().stream()
                                .map(role -> role.getName().name())
                                .collect(Collectors.toList()),
                        user.isApproved()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(pendingUsers);
    }

    @PostMapping("/approve-user/{userId}")
    public ResponseEntity<?> approveUser(@PathVariable Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Error: User not found."));

        user.setApproved(true);
        userRepository.save(user);

        return ResponseEntity.ok("User approved successfully");
    }

    @DeleteMapping("/delete-user/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
        userRepository.deleteById(userId);
        return ResponseEntity.ok("User deleted successfully");
    }
}