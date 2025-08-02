package com.example.authdemo.repository;

import com.example.authdemo.model.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    // This will help us check if feedback for a parcel already exists.
    boolean existsByTrackingId(String trackingId);
}