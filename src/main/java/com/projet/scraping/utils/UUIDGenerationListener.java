package com.projet.scraping.utils;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class UUIDGenerationListener {

    @PrePersist
    public void prePersist(Object entity) {
        if (entity instanceof BaseEntity) {
            BaseEntity baseEntity = (BaseEntity) entity;
            if (baseEntity.getPublicId() == null) {
                baseEntity.setPublicId(UUID.randomUUID());
            }
            if (baseEntity.getCreateDate() == null) {
                baseEntity.setCreateDate(Instant.now());
            }
        }
    }

    @PreUpdate
    public void preUpdate(Object entity) {
        if (entity instanceof BaseEntity) {
            BaseEntity baseEntity = (BaseEntity) entity;
            baseEntity.setDateM(Instant.now());
        }
    }
}
