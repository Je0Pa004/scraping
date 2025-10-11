package com.projet.scraping.security.dto;

import lombok.Builder;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public class UserDTO {

    private Long id;
    private String nom;
    @Email(message = "Email doit être valide")
    private String email;
    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 6, message = "Le mot de passe doit contenir au moins 6 caractères")
    private String motDePasse;
    private String typeCompte;
    private LocalDateTime dateCreation;
    private String statut;
    private boolean enable;
    private String roles;

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

    public String getPassword() {
        return motDePasse;
    }

    public void setPassword(String password) {
        this.motDePasse = password;
    }

    public String getRoles() {
        return roles;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }

    private UUID publicId;

    public UserDTO() {
        // No-argument constructor for frameworks like Spring
    }

    public UserDTO(Long id, String nom, String email, String motDePasse, String typeCompte,
                   LocalDateTime dateCreation, String statut, boolean enable, String roles, UUID publicId) {
        this.id = id;
        this.nom = nom;
        this.email = email;
        this.motDePasse = motDePasse;
        this.typeCompte = typeCompte;
        this.dateCreation = dateCreation;
        this.statut = statut;
        this.enable = enable;
        this.roles = roles;
        this.publicId = publicId;
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

    public String getMotDePasse() {
        return motDePasse;
    }

    public void setMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse;
    }

    public String getTypeCompte() {
        return typeCompte;
    }

    public void setTypeCompte(String typeCompte) {
        this.typeCompte = typeCompte;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
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
}
