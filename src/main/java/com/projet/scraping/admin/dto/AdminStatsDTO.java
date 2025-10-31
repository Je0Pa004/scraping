package com.projet.scraping.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminStatsDTO {
    private Long totalUsers;
    private Long totalSearches;
    private Long totalProfiles;
    private Double monthlyRevenue;
    private Long freeUsers;
    private Long basicUsers;
    private Long premiumUsers;
}
