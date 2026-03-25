package com.quinzex.entity;

import com.quinzex.enums.Category;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@Setter
@Getter
@Table(name = "previous_questions")
public class PreviousQuestions {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long questionId;

    @Column(length = 100, nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    private Category category;

    @Column(length = 2000, nullable = false)
    private String description;

    @Column( nullable = false)
    private String bucketName;

    @Column(nullable = false)
    private String s3Key;

    @Column( nullable = false,updatable = false)
    private LocalDateTime creationDate;
    @PrePersist
    public void prePersist() {
        this.creationDate = LocalDateTime.now(ZoneId.of("Asia/Kolkata"));
    }

}
