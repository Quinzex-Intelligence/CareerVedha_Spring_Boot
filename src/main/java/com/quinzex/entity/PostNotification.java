package com.quinzex.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(
        indexes = {
                @Index(
                        name = "idx_created_id",
                        columnList = "createdAt, notificationId"
                ),
                @Index(
                        name = "idx_role_created_id",
                        columnList = "receiverRole, createdAt, notificationId"
                )
        }
)
public class PostNotification {
@Id
@GeneratedValue(strategy= GenerationType.IDENTITY)
private Long notificationId;

private Long postId;

private String message; //message

private LocalDateTime createdAt; //notification created date

private String receiverRole;

    private Boolean seen;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now(ZoneId.of("Asia/Kolkata"));
        this.seen = false;
    }
}
