package com.quinzex.entity;

import com.quinzex.enums.YoutubeCategory;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@Setter
@Getter
public class YoutubeUrls {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false,length = 2000)
    private String url;
    @Column(nullable = false,length = 2000)
    private String title;
    @Enumerated(EnumType.STRING)
    private YoutubeCategory category;
    @Column(nullable = false,updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected  void onCreate() {
        createdAt = LocalDateTime.now(ZoneId.of("Asia/Kolkata"));
    }
}
