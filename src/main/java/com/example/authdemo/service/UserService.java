package com.example.authdemo.service;

import com.example.authdemo.dto.EditUserRequest;
import com.example.authdemo.dto.LoginRequest;
import com.example.authdemo.model.User;
import com.example.authdemo.repository.UserRepository;
import com.example.authdemo.util.OtpUtil;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

// ✅ IMPLEMENTS UserDetailsService to act as the single source for authentication
@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final Map<String, String> otpStore = new HashMap<>();

    /**
     * ✅ THIS IS THE NEW UNIFIED AUTHENTICATION METHOD
     * It's called by Spring Security to find a user during login.
     * It handles both the special 'admin' case and regular database users.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // First, check if the login attempt is for the special admin user
        if ("admin".equals(username)) {
            return org.springframework.security.core.userdetails.User
                    .withUsername("admin")
                    .password(passwordEncoder.encode("admin123")) // Password must be encoded
                    .roles("ADMIN")
                    .build();
        }

        // If not admin, treat the username as an email and load from the database
        User user = userRepository.findByEmail(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found with email: " + username);
        }

        // Return the database user's details for Spring Security to use
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword()) // Assumes password in DB is already encoded
                .roles(user.getRole())
                .build();
    }


    // --- ALL YOUR EXISTING METHODS REMAIN BELOW ---

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
            user.setVerified(true);
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


    public String loginUser(LoginRequest loginRequest) {
        User user = userRepository.findByEmail(loginRequest.getEmail());
        if (user != null && passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            return "Login successful: " + user.getName() + "|" + user.getEmail();
        }
        return "Invalid credentials!";
    }

    public Optional<User> updateUser(String email, EditUserRequest request) {
        User existingUser = userRepository.findByEmail(email);
        if (existingUser != null) {
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
            return false;
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
