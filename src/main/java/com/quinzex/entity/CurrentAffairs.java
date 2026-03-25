package com.quinzex.entity;

import com.quinzex.enums.Language;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@Table(name = "current_affairs")
@Getter
@Setter
public class CurrentAffairs {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String summary;
    @Column(length = 2000)
    private String description;

    @Column(nullable = false)
    private String bucketName;

    @Column(nullable = false, unique = true)
    private String s3Key;

    @Column(nullable = false)
    private String region;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Language language;

    private LocalDateTime creationorupdationDate;

    @PrePersist
    protected void onCreate() {
        this.creationorupdationDate = LocalDateTime.now(ZoneId.of("Asia/Kolkata"));

    }
}
