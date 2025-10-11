package com.projet.scraping.utils;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;

import java.time.Instant;
import java.util.UUID;

@MappedSuperclass
@NoArgsConstructor
@Getter
@Setter
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @UuidGenerator
    @Column(name = "public_id", nullable = false, unique = true, updatable = false)
    private UUID publicId;

    @CreationTimestamp
    @Column(name = "date_creation", nullable = false, updatable = false)
    private Instant createDate;

    @UpdateTimestamp
    @Column(name = "date_modification")
    private Instant dateM;

    @CreatedBy
    private String userCreate;

    @LastModifiedBy
    private String userUpdate;
}
