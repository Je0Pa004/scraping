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
public class SystemLogDTO {
    private String id;
    private LocalDateTime timestamp;
    private LogLevel level;
    private String source;
    private String message;
    private String user;

    public enum LogLevel {
        INFO, WARNING, ERROR, CRITICAL
    }
}
