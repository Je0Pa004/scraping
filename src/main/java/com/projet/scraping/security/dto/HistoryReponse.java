package com.projet.scraping.security.dto;

import java.time.LocalDateTime;

public class HistoryReponse {
    private Long id;
    private String action;
    private String description;
    private LocalDateTime timestamp;
    private String userId;

    public HistoryReponse() {
    }

    public HistoryReponse(Long id, String action, String description, LocalDateTime timestamp, String userId) {
        this.id = id;
        this.action = action;
        this.description = description;
        this.timestamp = timestamp;
        this.userId = userId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getUserId() {
        return userId;
    }

    public void setFullName(String fullName) {
        // This could map to userId or be a separate field for user's full name
        // For now, we'll leave it as is since the mapper is setting it from history.getUser().getNom()
    }

    public void setName(String name) {
        this.action = name;
    }

    public void setDateHistory(java.util.Date dateHistory) {
        // Convert Date to LocalDateTime if needed
        if (dateHistory != null) {
            this.timestamp = LocalDateTime.ofInstant(dateHistory.toInstant(), java.time.ZoneId.systemDefault());
        }
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}

