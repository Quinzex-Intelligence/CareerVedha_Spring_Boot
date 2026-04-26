package com.quinzex.repository;

import com.quinzex.entity.RoleNotification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

public interface RoleNotificationRepo extends JpaRepository<RoleNotification,Long> {

    @Query("""
    SELECT n FROM RoleNotification n
    WHERE n.role = :role
      AND (
           n.localDateTime < :cursorTime
           OR (n.localDateTime = :cursorTime AND n.id < :cursorId)
      )
    ORDER BY n.localDateTime DESC, n.id DESC
""")
    List<RoleNotification> findByRoleBefore(
            @Param("role") String role,
            @Param("cursorTime") LocalDateTime cursorTime,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );
    @Query("""
SELECT rn
FROM RoleNotification rn
WHERE rn.role = :role
AND rn.id NOT IN (
    SELECT ns.notificationId
    FROM NotificationSeen ns
    WHERE ns.userId = :userId
)
ORDER BY rn.localDateTime DESC, rn.id DESC
""")
    List<RoleNotification> findUnseenByRole(
            @Param("role") String role,
            @Param("userId") Long userId,
            Pageable pageable
    );


    @Query("""
SELECT rn
FROM RoleNotification rn
WHERE rn.role = :role
AND rn.id NOT IN (
    SELECT ns.notificationId
    FROM NotificationSeen ns
    WHERE ns.userId = :userId
)
AND (
      rn.localDateTime < :cursorTime
   OR (rn.localDateTime = :cursorTime AND rn.id < :cursorId)
)
ORDER BY rn.localDateTime DESC, rn.id DESC
""")
    List<RoleNotification> findUnseenByRoleWithCursor(
            @Param("role") String role,
            @Param("userId") Long userId,
            @Param("cursorTime") LocalDateTime cursorTime,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );




    @Query("""
    SELECT n FROM RoleNotification n
    WHERE n.localDateTime < :cursorTime
       OR (n.localDateTime = :cursorTime AND n.id < :cursorId)
    ORDER BY n.localDateTime DESC, n.id DESC
""")
    List<RoleNotification> findAllBefore(
            @Param("cursorTime") LocalDateTime cursorTime,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );
    Page<RoleNotification> findAll(Pageable pageable);
    Page<RoleNotification> findByRole(String role, Pageable pageable);
    @Query("""
SELECT rn FROM RoleNotification rn
WHERE rn.notificationStatus = :status
  AND (
        :role = 'SUPER_ADMIN'
     OR (:role = 'ADMIN' AND rn.message NOT LIKE '%ADMIN role%')
  )
ORDER BY rn.localDateTime DESC, rn.id DESC
""")
    List<RoleNotification> findByNotificationStatusOrderByLocalDateTimeDesc(
            @Param("status") String notificationStatus,
            @Param("role") String role,
            Pageable pageable
    );
    @Query("""
SELECT rn FROM RoleNotification rn
WHERE rn.notificationStatus = :status
  AND (
        :role = 'SUPER_ADMIN'
     OR (:role = 'ADMIN' AND rn.message NOT LIKE '%ADMIN role%')
  )
  AND (
        rn.localDateTime < :cursorTime
     OR (rn.localDateTime = :cursorTime AND rn.id < :cursorId)
  )
ORDER BY rn.localDateTime DESC, rn.id DESC
""")
    List<RoleNotification> findByStatusWithCompositeCursor(
            @Param("status") String status,
            @Param("role") String role,
            @Param("cursorTime") LocalDateTime cursorTime,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );
    @Query("""
SELECT rn
FROM RoleNotification rn
WHERE rn.id NOT IN (
    SELECT ns.notificationId
    FROM NotificationSeen ns
    WHERE ns.userId = :userId
)
AND (
      :cursorTime IS NULL
   OR rn.localDateTime < :cursorTime
   OR (rn.localDateTime = :cursorTime AND rn.id < :cursorId)
)
ORDER BY rn.localDateTime DESC, rn.id DESC
""")
    List<RoleNotification> findAllUnseenForSuperAdminWithCursor(
            @Param("userId") Long userId,
            @Param("cursorTime") LocalDateTime cursorTime,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );
    @Query("""
SELECT rn
FROM RoleNotification rn
WHERE rn.id NOT IN (
    SELECT ns.notificationId
    FROM NotificationSeen ns
    WHERE ns.userId = :userId
)
ORDER BY rn.localDateTime DESC, rn.id DESC
""")
    List<RoleNotification> findAllUnseenForSuperAdmin(
            @Param("userId") Long userId,
            Pageable pageable
    );
    @Modifying
    @Query("""
UPDATE RoleNotification r 
SET r.notificationStatus = :status,
    r.notificationApprovedOrRejectedDate = :date,
    r.userEmail = :approvedBy
WHERE r.email = :email
""")
    void updateAllByEmail(String email, String status, LocalDateTime date, String approvedBy);

}
