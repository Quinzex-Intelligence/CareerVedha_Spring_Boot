package com.quinzex.service;

import com.quinzex.dto.PostNotificationDto;
import com.quinzex.entity.PostNotification;
import com.quinzex.repository.PostNotificationRepo;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
public class PostNotificationService implements IPostNotificationService {
    private final PostNotificationRepo postNotificationRepo;
    private final SimpMessagingTemplate simpMessagingTemplate;


    public PostNotificationService(PostNotificationRepo postNotificationRepo, SimpMessagingTemplate simpMessagingTemplate) {
        this.postNotificationRepo = postNotificationRepo;
        this.simpMessagingTemplate = simpMessagingTemplate;

    }
    @Override
    @Transactional
    public void postNotification(PostNotificationDto postNotificationDto) {
        PostNotification notification = new PostNotification();
        String receiverRole = postNotificationDto.getReceiverRole();
        notification.setPostId(postNotificationDto.getPostId());
        notification.setMessage(postNotificationDto.getMessage());
        notification.setReceiverRole(receiverRole);
        postNotificationRepo.save(notification);
        //role based notification
        simpMessagingTemplate.convertAndSend("/topic/notifications/"+receiverRole,notification );

        //super admin targeted notifications should not be pushed to admin topic
        if (!"SUPER_ADMIN".equals(receiverRole)) {
            //admin notification
            simpMessagingTemplate.convertAndSend(
                    "/topic/notifications/ADMIN",
                    notification
            );
        }
        //super admin notification
        simpMessagingTemplate.convertAndSend(
                "/topic/notifications/SUPER_ADMIN",
                notification
        );
    }
@Override
    public List<PostNotification> getPostNotifications(Authentication authentication, LocalDateTime createdAt,Long cursorId,int size) {


        String loggedInUserRole = authentication.getAuthorities().stream()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .filter(r -> r.equals("ADMIN") || r.equals("SUPER_ADMIN"))
                .findFirst()
                .orElse(
                        authentication.getAuthorities()
                                .iterator()
                                .next()
                                .getAuthority()
                                .replace("ROLE_", "")
                );


        if(createdAt==null&& cursorId==null){
            createdAt = LocalDateTime.now(ZoneId.of("Asia/Kolkata"));
            cursorId=Long.MAX_VALUE;
        }
    else if ((createdAt == null) != (cursorId == null)) {
        throw new IllegalArgumentException(
                "Both createdAt and cursorId must be provided together"
        );
    }
    Pageable pageable = PageRequest.of(0, size);
        if ("SUPER_ADMIN".equals(loggedInUserRole)) {
            return postNotificationRepo.findAllByCursor(
                    createdAt,
                    cursorId,
                    pageable
            );
        }

        if ("ADMIN".equals(loggedInUserRole)) {
            return postNotificationRepo.findAllExcludingSuperAdminByCursor(
                    createdAt,
                    cursorId,
                    pageable
            );
        }


    return postNotificationRepo.findByRoleWithCursor(
            loggedInUserRole,
            createdAt,
            cursorId,
            pageable
    );
    }

    @Transactional
    public void markNotificationAsSeen(Long notificationId) {
        int updated = postNotificationRepo.markAsSeen(notificationId);

        if (updated == 0) {
            throw new RuntimeException("Notification not found");
        }
    }


}

