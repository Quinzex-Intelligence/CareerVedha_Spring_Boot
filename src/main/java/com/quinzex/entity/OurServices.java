package com.quinzex.entity;


import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import java.time.LocalDateTime;
import java.time.ZoneId;

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


    @Column(columnDefinition = "TEXT")
    private String content;


    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now(ZoneId.of("Asia/Kolkata"));
      updatedAt = LocalDateTime.now(ZoneId.of("Asia/Kolkata"));
    }
    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now(ZoneId.of("Asia/Kolkata"));
    }
}
