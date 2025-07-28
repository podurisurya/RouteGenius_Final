package com.example.authdemo.service;

import com.example.authdemo.dto.EditUserRequest;
import com.example.authdemo.model.User;
import com.example.authdemo.repository.UserRepository;
import com.example.authdemo.util.OtpUtil;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final Map<String, String> otpStore = new HashMap<>();

    public String registerUser(User user) {
        if (userRepository.findByEmail(user.getEmail()) != null) {
            return "Email already registered! Please login.";
        }

        String otp = OtpUtil.generateOtp();
        otpStore.put(user.getEmail(), otp);

        try {
            emailService.sendOtpEmail(user.getEmail(), user.getName(), otp);
            return "OTP sent to your email. Please verify.";
        } catch (MessagingException e) {
            e.printStackTrace();
            return "Error sending OTP email.";
        }
    }

    public String verifyOtpAndCreateUser(User user, String otp) {
        String storedOtp = otpStore.get(user.getEmail());
        System.out.println("Stored OTP: " + storedOtp + ", Entered OTP: " + otp);

        if (storedOtp == null) {
            return "No OTP found for this email. Please request OTP again.";
        }

        if (storedOtp.equals(otp.trim())) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setVerified(true); // ✅ ADD THIS LINE
            userRepository.save(user);
            otpStore.remove(user.getEmail());

            try {
                emailService.sendWelcomeEmail(user.getEmail(), user.getName());
            } catch (MessagingException e) {
                e.printStackTrace();
            }

            return "User registered successfully!";
        }

        return "Invalid OTP. Registration failed.";
    }


    public String loginUser(com.example.authdemo.dto.LoginRequest loginRequest) {
        User user = userRepository.findByEmail(loginRequest.getEmail());
        if (user != null && passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            return "Login successful: " + user.getName() + "|" + user.getEmail();
        }
        return "Invalid credentials!";
    }

    public Optional<User> updateUser(String email, EditUserRequest request) {
        User existingUser = userRepository.findByEmail(email);
        if (existingUser != null) {
            // For now, we only allow changing the name.
            // You could easily add other fields here in the future.
            existingUser.setName(request.getName());
            return Optional.of(userRepository.save(existingUser));
        }
        return Optional.empty();
    }

    public List<String> getVerifiedEmailsMatching(String query) {
        return userRepository.findByEmailContainingIgnoreCaseAndVerifiedTrue(query)
                .stream()
                .map(User::getEmail)
                .collect(Collectors.toList());
    }
    public boolean areValidAndDifferentVerifiedEmails(String senderEmail, String recipientEmail) {
        if (senderEmail.equalsIgnoreCase(recipientEmail)) {
            return false; // Can't be the same
        }

        boolean senderValid = userRepository.findByEmailAndVerifiedTrue(senderEmail).isPresent();
        boolean recipientValid = userRepository.findByEmailAndVerifiedTrue(recipientEmail).isPresent();

        return senderValid && recipientValid;
    }

    public boolean changeUserPassword(String email, String newPassword) {
        User user = userRepository.findByEmail(email);
        if (user != null) {
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            return true;
        }
        return false;
    }
}
