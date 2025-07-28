package com.example.authdemo.controller;
import java.util.List;

import com.example.authdemo.dto.ChangePasswordRequest;
import com.example.authdemo.dto.LoginRequest;
import com.example.authdemo.dto.OtpVerificationRequest;
import com.example.authdemo.model.User;
import com.example.authdemo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class UserController {

    @Autowired
    private UserService userService;

    @PutMapping("/change-password")
    public ResponseEntity<String> changePassword(@RequestBody ChangePasswordRequest request) {
        boolean result = userService.changeUserPassword(request.getEmail(), request.getNewPassword());

        if (result) {
            return ResponseEntity.ok("Password updated successfully.");
        } else {
            return ResponseEntity.badRequest().body("User not found or update failed.");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody User user) {
        String response = userService.registerUser(user);
        if (response.equals("Email already exists!")) {
            return ResponseEntity.badRequest().body("Registration failed. Email already exists.");
        } else if (response.equals("Error sending OTP email.")) {
            return ResponseEntity.internalServerError().body("Error sending OTP email.");
        }

        return ResponseEntity.ok(response); // OTP sent
    }

    @GetMapping("/admin/users/emails")
    public ResponseEntity<List<String>> getVerifiedEmails(@RequestParam String query) {
        List<String> emails = userService.getVerifiedEmailsMatching(query);
        return ResponseEntity.ok(emails);
    }


    @PostMapping("/login")
    public ResponseEntity<String> loginUser(@RequestBody LoginRequest loginRequest) {
        String result = userService.loginUser(loginRequest);
        if (result.startsWith("Login successful")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(401).body(result);
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<String> verifyOtp(@RequestBody OtpVerificationRequest request) {
        System.out.println("Received OTP verification request:");
        System.out.println("Email: " + request.getEmail());
        System.out.println("OTP: " + request.getOtp());
        System.out.println("Password: " + request.getPassword());

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        user.setRole("USER");

        String result = userService.verifyOtpAndCreateUser(user, request.getOtp());

        System.out.println("Final result: " + result);

        if (result != null && result.contains("successfully")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result != null ? result : "OTP verification failed.");
        }
    }

}
