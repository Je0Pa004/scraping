package com.projet.scraping.security.dto;

public class RoleDTO {
    private Long id;
    private String nom;
    private String description;

    public RoleDTO() {
    }

    public RoleDTO(Long id, String nom, String description) {
        this.id = id;
        this.nom = nom;
        this.description = description;
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

    public void setName(String name) {
        this.nom = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
