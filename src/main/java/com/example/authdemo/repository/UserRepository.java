package com.example.authdemo.repository;

import com.example.authdemo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // ✅ To find one user by email
    User findByEmail(String email);

    // ✅ To find verified users by partial email (for suggestions)
    List<User> findByEmailContainingIgnoreCaseAndVerifiedTrue(String emailPart);

    // ✅ To fetch a verified user exactly
    Optional<User> findByEmailAndVerifiedTrue(String email);
}
