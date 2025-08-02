package com.example.authdemo.service;

import com.example.authdemo.model.Feedback;
import com.example.authdemo.model.Parcel;
import com.example.authdemo.repository.FeedbackRepository;
import com.example.authdemo.repository.ParcelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FeedbackService {

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private ParcelRepository parcelRepository;

    @Transactional
    public Feedback submitFeedback(Feedback feedback) {
        // 1. Check if feedback already exists for this tracking ID
        if (feedbackRepository.existsByTrackingId(feedback.getTrackingId())) {
            throw new IllegalStateException("Feedback has already been submitted for this parcel.");
        }

        // 2. Find the corresponding parcel
        Parcel parcel = parcelRepository.findByTrackingId(feedback.getTrackingId())
                .orElseThrow(() -> new IllegalArgumentException("Parcel not found."));

        // 3. Ensure the parcel was actually delivered
        if (!"Delivered".equals(parcel.getStatus())) {
            throw new IllegalStateException("Feedback can only be provided for delivered parcels.");
        }

        // 4. Mark the parcel as having feedback submitted
        parcel.setFeedbackSubmitted(true);
        parcelRepository.save(parcel);

        // 5. Save and return the new feedback
        return feedbackRepository.save(feedback);
    }

    public List<Feedback> getAllFeedback() {
        return feedbackRepository.findAll();
    }
}