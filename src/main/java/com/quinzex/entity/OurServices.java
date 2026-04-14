package com.quinzex.entity;


import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.time.Instant;

@Entity
@Table(name = "our_services")
@Data
@ToString(exclude ="content")
public class OurServices {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String title;
    @Column(length = 2000)
    private String description;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String content;

    private Instant createdAt;
    private Instant updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }
    @PreUpdate
    public void preUpdate() {
        updatedAt = Instant.now();
    }
}
