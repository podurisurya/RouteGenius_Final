package com.example.authdemo.service;

import com.example.authdemo.dto.ParcelRequest;
import com.example.authdemo.model.Parcel;
import com.example.authdemo.repository.ParcelRepository;
import com.example.authdemo.repository.UserRepository;
import jakarta.mail.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ParcelService {

    // ✅ ADDED: Logger for handling potential email errors
    private static final Logger logger = LoggerFactory.getLogger(ParcelService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ParcelRepository parcelRepository;

    // ✅ ADDED: Inject the EmailService so we can use it
    @Autowired
    private EmailService emailService;

    public Parcel addParcel(ParcelRequest request) {
        // This part is correct
        userRepository.findByEmailAndVerifiedTrue(request.getCustomerEmail())
                .orElseThrow(() -> new IllegalArgumentException("Customer email is not registered or verified."));

        Parcel parcel = new Parcel();
        parcel.setCustomerEmail(request.getCustomerEmail());
        parcel.setSenderName(request.getSenderName());
        parcel.setSenderAddress(request.getSenderAddress());
        parcel.setRecipientName(request.getRecipientName());
        parcel.setRecipientAddress(request.getRecipientAddress());
        parcel.setParcelDescription(request.getParcelDescription());
        parcel.setTrackingId(UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        parcel.setStatus("Created");

        Parcel savedParcel = parcelRepository.save(parcel);

        // ✅ ADDED: Trigger the email after successfully creating the parcel
        try {
            emailService.sendParcelCreationEmail(savedParcel);
            logger.info("Creation email sent successfully for tracking ID: {}", savedParcel.getTrackingId());
        } catch (MessagingException e) {
            // This ensures if the email fails, the app doesn't crash
            logger.error("Failed to send creation email for tracking ID {}: {}", savedParcel.getTrackingId(), e.getMessage());
        }

        return savedParcel;
    }

    public List<Parcel> getAllParcels() {
        return parcelRepository.findAll();
    }
    // In src/main/java/com/example/authdemo/service/ParcelService.java
    public List<Parcel> getParcelsByCustomerEmail(String email) {
        return parcelRepository.findByCustomerEmailOrderByCreatedAtDesc(email);
    }
    public Parcel updateParcelStatus(String trackingId, String status) {
        Optional<Parcel> optionalParcel = parcelRepository.findByTrackingId(trackingId);
        if (optionalParcel.isPresent()) {
            Parcel parcel = optionalParcel.get();
            parcel.setStatus(status);
            Parcel updatedParcel = parcelRepository.save(parcel);

            // ✅ ADDED: Trigger email after updating the status
            try {
                emailService.sendParcelUpdateEmail(updatedParcel);
                logger.info("Status update email sent successfully for tracking ID: {}", updatedParcel.getTrackingId());
            } catch (MessagingException e) {
                logger.error("Failed to send status update email for tracking ID {}: {}", updatedParcel.getTrackingId(), e.getMessage());
            }

            return updatedParcel;
        }
        return null;
    }

    public boolean deleteParcelByTrackingId(String trackingId) {
        Optional<Parcel> parcelOpt = parcelRepository.findByTrackingId(trackingId);
        if (parcelOpt.isPresent()) {
            parcelRepository.delete(parcelOpt.get());
            return true;
        }
        return false;
    }

    public Parcel updateParcel(String trackingId, Parcel updatedData) {
        Optional<Parcel> existing = parcelRepository.findByTrackingId(trackingId);
        if (existing.isPresent()) {
            Parcel parcel = existing.get();
            String oldStatus = parcel.getStatus();

            parcel.setSenderName(updatedData.getSenderName());
            parcel.setSenderAddress(updatedData.getSenderAddress());
            parcel.setRecipientName(updatedData.getRecipientName());
            parcel.setRecipientAddress(updatedData.getRecipientAddress());
            parcel.setParcelDescription(updatedData.getParcelDescription());
            parcel.setStatus(updatedData.getStatus());

            Parcel savedParcel = parcelRepository.save(parcel);

            // ✅ ADDED: Send email only if the status has actually changed
            if (!oldStatus.equals(savedParcel.getStatus())) {
                try {
                    emailService.sendParcelUpdateEmail(savedParcel);
                    logger.info("Update email (from edit) sent successfully for tracking ID: {}", savedParcel.getTrackingId());
                } catch (MessagingException e) {
                    logger.error("Failed to send update email for tracking ID {}: {}", savedParcel.getTrackingId(), e.getMessage());
                }
            }

            return savedParcel;
        }
        return null;
    }

    public Optional<Parcel> getParcelByTrackingId(String trackingId) {
        return parcelRepository.findByTrackingId(trackingId);
    }
}
