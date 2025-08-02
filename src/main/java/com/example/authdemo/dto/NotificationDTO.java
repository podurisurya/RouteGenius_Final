package com.example.authdemo.dto;

public class NotificationDTO {
    private String content;

    public NotificationDTO(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}