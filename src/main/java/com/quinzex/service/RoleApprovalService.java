package com.quinzex.service;

import com.quinzex.entity.LmsLogin;
import com.quinzex.entity.RoleNotification;
import com.quinzex.repository.LmsLoginRepo;
import com.quinzex.repository.RoleNotificationRepo;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Set;

@Service
@Transactional
public class RoleApprovalService implements IRoleApprovalService {

    private final RoleNotificationRepo roleNotificationRepo;
    private final LmsLoginRepo lmsLoginRepo;
    private final IemailService iemailService;

    public RoleApprovalService(RoleNotificationRepo roleNotificationRepo,LmsLoginRepo lmsLoginRepo,IemailService iemailService) {
        this.roleNotificationRepo = roleNotificationRepo;
        this.lmsLoginRepo = lmsLoginRepo;
        this.iemailService = iemailService;
    }

    @Override
    public String approve(Long notificationId, Authentication authentication) {
        RoleNotification notification = roleNotificationRepo.findById(notificationId).orElseThrow(()->new RuntimeException(" Notification Not Found"));

        LmsLogin user = lmsLoginRepo.findByEmail(notification.getEmail()).orElseThrow(()->new RuntimeException("User Not Found"));
        String approverRole = authentication.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
        String requiredApprover =notification.getRole();

        if ("SUPER_ADMIN".equals(requiredApprover) && !"SUPER_ADMIN".equals(approverRole)) {
            throw new RuntimeException("Only Super Admin can approve/reject this request");
        }

        if (!Set.of("ADMIN", "SUPER_ADMIN").contains(approverRole)) {
            throw new RuntimeException("You are not authorized");
        }

        if (!"PENDING".equals(notification.getNotificationStatus())) {
            throw new RuntimeException("Already processed");
        }

        user.setIsAuthorized(true);
        user.setStatus("APPROVED");
        user.setTokenVersion(user.getTokenVersion()+1);
        lmsLoginRepo.save(user);
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Kolkata"));
        roleNotificationRepo.updateAllByEmail(
                user.getEmail(),
                "APPROVED",
                now,
                authentication.getName()
        );
        //email service
        iemailService.sendApprovalEmail(notification.getEmail(),user.getRole().getRoleName());
        return notification.getEmail()+" is approved successfully";
    }

    @Override
    public String reject(Long notificationId, String reason, Authentication authentication) {
        RoleNotification notification = roleNotificationRepo.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        LmsLogin user = lmsLoginRepo.findByEmail(notification.getEmail()).orElseThrow(()->new RuntimeException("User Not Found"));
        String approverRole = authentication.getAuthorities()
                .iterator()
                .next()
                .getAuthority()
                .replace("ROLE_", "");


        String requiredApprover =notification.getRole();

        if ("SUPER_ADMIN".equals(requiredApprover) && !"SUPER_ADMIN".equals(approverRole)) {
            throw new RuntimeException("Only Super Admin can approve/reject this request");
        }


        if (!Set.of("ADMIN", "SUPER_ADMIN").contains(approverRole)) {
            throw new RuntimeException("You are not authorized");
        }
        if (!"PENDING".equals(notification.getNotificationStatus())) {
            throw new RuntimeException("Already processed");
        }




        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Kolkata"));

        roleNotificationRepo.updateAllByEmail(
                user.getEmail(),
                "REJECTED",
                 now,
                authentication.getName()
        );

        //email service
        iemailService.sendRejectionEmail(notification.getEmail(), user.getRole().getRoleName(),  reason);
        lmsLoginRepo.delete(user);
        return notification.getEmail()+" rejected successfully";

    }
}
