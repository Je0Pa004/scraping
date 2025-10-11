package com.projet.scraping.security.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class UserRoleReponse {
    private Long id;
    private String nom;
    private String email;
    private String typeCompte;
    private String statut;
    private boolean enable;
    private UUID publicId;
    private LocalDateTime dateCreation;
    private String roles;

    public UserRoleReponse() {
    }

    public UserRoleReponse(Long id, String nom, String email, String typeCompte,
                          String statut, boolean enable, UUID publicId, LocalDateTime dateCreation, String roles) {
        this.id = id;
        this.nom = nom;
        this.email = email;
        this.typeCompte = typeCompte;
        this.statut = statut;
        this.enable = enable;
        this.publicId = publicId;
        this.dateCreation = dateCreation;
        this.roles = roles;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTypeCompte() {
        return typeCompte;
    }

    public void setTypeCompte(String typeCompte) {
        this.typeCompte = typeCompte;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public UUID getPublicId() {
        return publicId;
    }

    public void setPublicId(UUID publicId) {
        this.publicId = publicId;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    public String getFullName() {
        return nom;
    }

    public void setFullName(String fullName) {
        this.nom = fullName;
    }

    public String getUsername() {
        return email;
    }

    public void setUsername(String username) {
        this.email = username;
    }

    public LocalDateTime getCreateDate() {
        return dateCreation;
    }

    public void setCreateDate(LocalDateTime createDate) {
        this.dateCreation = createDate;
    }

    public String getRoles() {
        return roles;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }
}
