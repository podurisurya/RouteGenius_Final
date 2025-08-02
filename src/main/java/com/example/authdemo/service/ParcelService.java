package com.example.authdemo.service;

import com.example.authdemo.dto.NotificationDTO;
import com.example.authdemo.dto.ParcelRequest;
import com.example.authdemo.model.Parcel;
import com.example.authdemo.repository.ParcelRepository;
import com.example.authdemo.repository.UserRepository;
import jakarta.mail.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ParcelService {

    private static final Logger logger = LoggerFactory.getLogger(ParcelService.class);

    private static final List<String> EMAIL_TRIGGER_STATUSES = Arrays.asList(
            "Dispatched", "In Transit", "Out for Delivery", "Delivered"
    );

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ParcelRepository parcelRepository;

    @Autowired
    private EmailService emailService;

    // Injected to send WebSocket messages
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public Parcel addParcel(ParcelRequest request) {
        // Verify the customer email exists and is verified
        userRepository.findByEmailAndVerifiedTrue(request.getCustomerEmail())
                .orElseThrow(() -> new IllegalArgumentException("Customer email is not registered or verified."));

        // Create and save the new parcel
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

        // Send traditional email notification
        try {
            emailService.sendParcelCreationEmail(savedParcel);
            logger.info("Creation email sent successfully for tracking ID: {}", savedParcel.getTrackingId());
        } catch (MessagingException e) {
            logger.error("Failed to send creation email for tracking ID {}: {}", savedParcel.getTrackingId(), e.getMessage());
        }

        // Send real-time WebSocket notification to the specific user
        String destination = "/queue/updates";
        NotificationDTO notification = new NotificationDTO("A new parcel " + savedParcel.getTrackingId() + " has been created for you.");
        messagingTemplate.convertAndSendToUser(savedParcel.getCustomerEmail(), destination, notification);
        logger.info("Sent WebSocket notification to user {} at destination {}", savedParcel.getCustomerEmail(), destination);

        return savedParcel;
    }

    public List<Parcel> getAllParcels() {
        return parcelRepository.findAll();
    }

    public List<Parcel> getParcelsByCustomerEmail(String email) {
        return parcelRepository.findByCustomerEmailOrderByCreatedAtDesc(email);
    }

    public Parcel updateParcelStatus(String trackingId, String newStatus) {
        return parcelRepository.findByTrackingId(trackingId).map(parcel -> {
            String oldStatus = parcel.getStatus();
            parcel.setStatus(newStatus);
            Parcel updatedParcel = parcelRepository.save(parcel);

            // Send email for key status milestones
            if (!oldStatus.equals(newStatus) && EMAIL_TRIGGER_STATUSES.contains(newStatus)) {
                try {
                    emailService.sendParcelUpdateEmail(updatedParcel);
                    logger.info("Status update email sent for tracking ID: {}", updatedParcel.getTrackingId());
                } catch (MessagingException e) {
                    logger.error("Failed to send status update email for tracking ID {}: {}", updatedParcel.getTrackingId(), e.getMessage());
                }
            }

            // Send real-time WebSocket notification on status update
            String destination = "/queue/updates";
            NotificationDTO notification = new NotificationDTO("The status of your parcel " + updatedParcel.getTrackingId() + " is now " + newStatus + ".");
            messagingTemplate.convertAndSendToUser(updatedParcel.getCustomerEmail(), destination, notification);
            logger.info("Sent WebSocket notification to user {} at destination {}", updatedParcel.getCustomerEmail(), destination);

            return updatedParcel;
        }).orElse(null);
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
        return parcelRepository.findByTrackingId(trackingId).map(parcel -> {
            String oldStatus = parcel.getStatus();

            parcel.setSenderName(updatedData.getSenderName());
            parcel.setSenderAddress(updatedData.getSenderAddress());
            parcel.setRecipientName(updatedData.getRecipientName());
            parcel.setRecipientAddress(updatedData.getRecipientAddress());
            parcel.setParcelDescription(updatedData.getParcelDescription());
            parcel.setStatus(updatedData.getStatus());

            Parcel savedParcel = parcelRepository.save(parcel);

            // Send email for key status milestones
            if (!oldStatus.equals(savedParcel.getStatus()) && EMAIL_TRIGGER_STATUSES.contains(savedParcel.getStatus())) {
                try {
                    emailService.sendParcelUpdateEmail(savedParcel);
                    logger.info("Update email (from edit) sent for tracking ID: {}", savedParcel.getTrackingId());
                } catch (MessagingException e) {
                    logger.error("Failed to send update email for tracking ID {}: {}", savedParcel.getTrackingId(), e.getMessage());
                }
            }

            // Send real-time WebSocket notification on full update
            String destination = "/queue/updates";
            NotificationDTO notification = new NotificationDTO("Your parcel " + savedParcel.getTrackingId() + " has been updated.");
            messagingTemplate.convertAndSendToUser(savedParcel.getCustomerEmail(), destination, notification);
            logger.info("Sent WebSocket notification to user {} at destination {}", savedParcel.getCustomerEmail(), destination);

            return savedParcel;
        }).orElse(null);
    }

    public Optional<Parcel> getParcelByTrackingId(String trackingId) {
        return parcelRepository.findByTrackingId(trackingId);
    }
}