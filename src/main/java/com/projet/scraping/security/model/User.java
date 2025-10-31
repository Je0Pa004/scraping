package com.projet.scraping.security.model;
import com.projet.scraping.entities.enums.AccountType;
import com.projet.scraping.entities.enums.UserStatus;
import com.projet.scraping.utils.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User extends BaseEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @UuidGenerator
    @Column(name = "public_id", nullable = false, unique = true, updatable = false)
    private UUID publicId;

    @Column(name = "nom",nullable = false)
    private String nom;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "mot_de_passe", nullable = false)
    private String motDePasse;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_compte", nullable = false)
    private AccountType typeCompte;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false)
    private UserStatus statut;

    @Column(name = "roles", nullable = false)
    private String roles = "USER";

    @Column(name = "enable",nullable = false)
    private boolean enable = true;

    public User() {
        this.statut = UserStatus.ACTIVE;
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

    public String getPassword() {
        return motDePasse;
    }

    public void setPassword(String password) {
        this.motDePasse = password;
    }

    public String getUsername() {
        return email;
    }

    public void setUsername(String username) {
        this.email = username;
    }

    public String getRoles() {
        return roles;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }

    public AccountType getTypeCompte() {
        return typeCompte;
    }

    public void setTypeCompte(AccountType typeCompte) {
        this.typeCompte = typeCompte;
    }

    public LocalDateTime getDateCreation() {
        return getCreateDate() != null ? getCreateDate().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime() : null;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        if (dateCreation != null) {
            setCreateDate(dateCreation.atZone(java.time.ZoneId.systemDefault()).toInstant());
        }
    }

    public UserStatus getStatut() {
        return statut;
    }

    public void setStatut(UserStatus statut) {
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
