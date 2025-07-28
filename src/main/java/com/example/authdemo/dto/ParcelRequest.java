package com.example.authdemo.dto;

public class ParcelRequest {

    // ✅ CHANGED: All fields updated to match the new form
    private String customerEmail;
    private String senderName;
    private String senderAddress;
    private String recipientName;
    private String recipientAddress;
    private String parcelDescription;

    // --- Getters and Setters ---

    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getSenderAddress() { return senderAddress; }
    public void setSenderAddress(String senderAddress) { this.senderAddress = senderAddress; }

    public String getRecipientName() { return recipientName; }
    public void setRecipientName(String recipientName) { this.recipientName = recipientName; }

    public String getRecipientAddress() { return recipientAddress; }
    public void setRecipientAddress(String recipientAddress) { this.recipientAddress = recipientAddress; }

    public String getParcelDescription() { return parcelDescription; }
    public void setParcelDescription(String parcelDescription) { this.parcelDescription = parcelDescription; }
}
