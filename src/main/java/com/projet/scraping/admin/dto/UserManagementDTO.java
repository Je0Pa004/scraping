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
public class UserManagementDTO {
    private String id;
    private String fullName;
    private String email;
    private String roles;  // Rôles de l'utilisateur (ADMIN, USER, etc.)
    private String plan;   // Plan d'abonnement (Basic, Premium, etc.)
    private LocalDateTime registrationDate;
    private int searchesUsed;
    private int searchQuota;
    private double quotaPercentage;
    private String status;
    private String phone;
    private String company;
    private boolean emailVerified;
    private LocalDateTime lastLogin;
}
