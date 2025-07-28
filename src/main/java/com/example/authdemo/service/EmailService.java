package com.example.authdemo.service;

import com.example.authdemo.model.Parcel;
import com.example.authdemo.model.User;
import com.example.authdemo.repository.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException; // ✅ ADDED: Import for the exception

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private UserRepository userRepository;

    // OTP and Welcome emails (no changes)
    public void sendOtpEmail(String to, String name, String otp) throws MessagingException {
        String subject = "RouteMax Email Verification OTP";
        String content = "<h3>Hello " + name + ",</h3>"
                + "<p>Your OTP for verifying email is:</p>"
                + "<h2>" + otp + "</h2>"
                + "<br><p>Use this to complete your registration.</p>";

        sendHtmlMail(to, subject, content);
    }

    public void sendWelcomeEmail(String to, String name) throws MessagingException {
        String subject = "Welcome to RouteMax!";
        String content = "<h3>Hi " + name + ",</h3>"
                + "<p>Thank you for registering with RouteMax!</p>"
                + "<p>We’re excited to have you onboard.</p>";

        sendHtmlMail(to, subject, content);
    }

    // Parcel creation email (no changes needed here)
    public void sendParcelCreationEmail(Parcel parcel) throws MessagingException {
        User customer = userRepository.findByEmail(parcel.getCustomerEmail());
        String customerName = (customer != null) ? customer.getName() : "Valued Customer";

        String subject = "Shipment Confirmed! Your RouteMax Tracking ID is " + parcel.getTrackingId();
        String content = "<h3>Hello " + customerName + ",</h3>"
                + "<p>Great news! A new shipment has been created for you via RouteMax.</p>"
                + "<p>Your Tracking ID is:</p>"
                + "<h2 style='color: #198754;'>" + parcel.getTrackingId() + "</h2>"
                + "<p><b>Details:</b></p>"
                + "<ul>"
                + "<li><b>Description:</b> " + parcel.getParcelDescription() + "</li>"
                + "<li><b>Recipient:</b> " + parcel.getRecipientName() + " at " + parcel.getRecipientAddress() + "</li>"
                + "</ul>"
                + "<p>You can use this tracking ID on our website to monitor its status in real-time. Thank you for using RouteMax!</p>";

        sendHtmlMail(parcel.getCustomerEmail(), subject, content);
    }

    // Parcel update email (no changes needed here)
    public void sendParcelUpdateEmail(Parcel parcel) throws MessagingException {
        User customer = userRepository.findByEmail(parcel.getCustomerEmail());
        String customerName = (customer != null) ? customer.getName() : "Valued Customer";

        String subject = "Shipment Update: Your RouteMax parcel is now " + parcel.getStatus();
        String content = "<h3>Hello " + customerName + ",</h3>"
                + "<p>The status of your shipment with Tracking ID <b>" + parcel.getTrackingId() + "</b> has been updated.</p>"
                + "<p>The new status is:</p>"
                + "<h2 style='background-color: #eafaf1; border-left: 5px solid #198754; padding: 10px;'>" + parcel.getStatus() + "</h2>"
                + "<p>You can continue to track this shipment on our website. Thank you for using RouteMax!</p>";

        sendHtmlMail(parcel.getCustomerEmail(), subject, content);
    }


    // ✅ MODIFIED: The sendHtmlMail method now handles the exception internally.
    private void sendHtmlMail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        try {
            // This line can throw an exception, so we wrap it.
            helper.setFrom("teamroutemax@gmail.com", "RouteMax Support");
        } catch (UnsupportedEncodingException e) {
            // This is a fallback in case the name is invalid, though "RouteMax Support" is safe.
            // It will log the error and send the email without the friendly name.
            e.printStackTrace();
            helper.setFrom("teamroutemax@gmail.com");
        }

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);
        mailSender.send(message);
    }
}
