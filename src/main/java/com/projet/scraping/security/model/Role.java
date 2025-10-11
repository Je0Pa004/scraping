package com.projet.scraping.security.model;

import com.projet.scraping.security.enums.UserRole;
import com.projet.scraping.utils.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;

import java.io.Serializable;
import java.util.UUID;


@Entity
@Table(name = "roles")
public class Role extends BaseEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "public_id", nullable = false, unique = true)
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private UUID publicId;

    @Enumerated(EnumType.STRING)
    @Column(name = "name", nullable = false)
    private UserRole name;

    public Role() {
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UUID getPublicId() {
        return publicId;
    }

    public void setPublicId(UUID publicId) {
        this.publicId = publicId;
    }

    public Object getName() {
        return name;
    }

    public void setName(UserRole name) {
        this.name = name;
    }
}