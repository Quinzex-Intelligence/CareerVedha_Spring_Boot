package com.quinzex.entity;

import jakarta.persistence.*;
import lombok.*;


import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class RoleNotification {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;
    @Column(nullable = false)
    private String email;
    @Column(nullable = false)
    private String role;
    @Column(nullable = false)
    private String message;

    @Column(nullable = false,name = "local_date_time",updatable = false)
    private LocalDateTime localDateTime= LocalDateTime.now(ZoneId.of("Asia/Kolkata"));
    @Column(nullable = false,name = "notification_status")
    private String notificationStatus="PENDING";
    @Column(name = "approved_rejected_email")
    private String userEmail;
   @Column(name = "date_of_approved_or_rejected")
    private LocalDateTime notificationApprovedOrRejectedDate;


}
