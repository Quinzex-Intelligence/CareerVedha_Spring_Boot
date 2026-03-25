package com.quinzex.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "notification_seen",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"notification_id", "user_id"})
        },
        indexes = {
                @Index(name = "idx_notification_user", columnList = "notification_id,user_id")
        }
)
public class NotificationSeen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "notification_id", nullable = false)
    private Long notificationId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "seen_at", nullable = false)
    private LocalDateTime seenAt = LocalDateTime.now(ZoneId.of("Asia/Kolkata"));
}
