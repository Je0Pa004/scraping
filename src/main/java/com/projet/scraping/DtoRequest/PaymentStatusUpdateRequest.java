package com.projet.scraping.DtoRequest;

import com.projet.scraping.entities.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentStatusUpdateRequest {
    private PaymentStatus status;
    private Long typeAbonnementId;
}
