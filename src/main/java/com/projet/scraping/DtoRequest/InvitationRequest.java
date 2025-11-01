package com.projet.scraping.DtoRequest;

import lombok.Data;

@Data
public class InvitationRequest {
    private String email;
    private String nom;
    private String telephone;
    private String profilPublicId;
    private String notes;
}
