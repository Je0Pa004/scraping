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
public class PaymentDTO {
    private String id;
    private String transactionId;
    private String user;
    private String email;
    private String plan;
    private Double amount;
    private LocalDateTime date;
    private PaymentStatus status;

    public enum PaymentStatus {
        SUCCEEDED, PENDING, FAILED, REFUNDED
    }
}
