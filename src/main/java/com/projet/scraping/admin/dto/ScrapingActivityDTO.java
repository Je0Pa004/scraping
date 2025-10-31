package com.projet.scraping.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScrapingActivityDTO {
    private String id;
    private String user;
    private String email;
    private String criteria;
    private String location;
    private String source;
    private LocalDateTime date;
    private Integer profilesFound;
    private ScrapingStatus status;

    public enum ScrapingStatus {
        COMPLETED, IN_PROGRESS, FAILED
    }
}
