package com.quinzex.service;

import com.quinzex.entity.LmsLogin;
import com.quinzex.entity.NotificationSeen;
import com.quinzex.entity.RoleNotification;
import com.quinzex.repository.LmsLoginRepo;
import com.quinzex.repository.NotificationSeenRepository;
import com.quinzex.repository.RoleNotificationRepo;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;

@Service
public class NotificationService  implements INotificationService{

    private final RoleNotificationRepo roleNotificationRepo;
    private final NotificationSeenRepository notificationSeenRepository;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final LmsLoginRepo lmsLoginRepo;
    public NotificationService(RoleNotificationRepo roleNotificationRepo, SimpMessagingTemplate simpMessagingTemplate,LmsLoginRepo lmsLoginRepo,NotificationSeenRepository notificationSeenRepository) {
        this.roleNotificationRepo = roleNotificationRepo;
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.lmsLoginRepo = lmsLoginRepo;
        this.notificationSeenRepository=notificationSeenRepository;
    }

    @Override
    public RoleNotification sendNotification(String mail,String role,String message) {

       RoleNotification roleNotification = new RoleNotification();
       roleNotification.setRole(role);
       roleNotification.setEmail(mail);
       roleNotification.setMessage(message);
        RoleNotification savedNotification =  roleNotificationRepo.save(roleNotification);
        simpMessagingTemplate.convertAndSend("/topic/approvals/" +role,savedNotification);
        return savedNotification;
    }
    @Override
    public List<RoleNotification> getAllNotifications(LocalDateTime cursorTime,Long cursorId,int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Order.desc("localDateTime"),Sort.Order.desc("id")));
        if(cursorId==null||cursorTime==null){
            return roleNotificationRepo.findAll(pageable).getContent();
        }
        return roleNotificationRepo.findAllBefore(cursorTime,cursorId,pageable);

    }

    @Override
    public List<RoleNotification> getNotificationsByRole(Authentication authentication, LocalDateTime cursorTime,Long cursorId,int limit) {
        String username = authentication.getName();
        LmsLogin user= lmsLoginRepo.findByEmail(username).orElseThrow(()->new RuntimeException("Role Not found Contact Admins"));
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Order.desc("localDateTime"),Sort.Order.desc("id")));
        if(cursorTime==null||cursorId==null){
         return roleNotificationRepo.findByRole(user.getRole().getRoleName(),pageable).getContent();
        }
        return roleNotificationRepo.findByRoleBefore(user.getRole().getRoleName(),cursorTime,cursorId,pageable);
    }
    @Override
    public List<RoleNotification> getUnseenNotificationsByRole(Authentication authentication,LocalDateTime cursorTime,Long cursorId,int limit) {
        String username = authentication.getName();
        LmsLogin user= lmsLoginRepo.findByEmail(username).orElseThrow(()->new RuntimeException("Role Not found Contact Admins"));
        Pageable pageable = PageRequest.of(0, Math.min(limit, 50));
        if("SUPER_ADMIN".equals(user.getRole().getRoleName())){
            if(cursorTime==null||cursorId==null){
                return roleNotificationRepo
                        .findAllUnseenForSuperAdmin(user.getId(), pageable);
            }
            return roleNotificationRepo.findAllUnseenForSuperAdminWithCursor(user.getId(),cursorTime,cursorId,pageable);
        }
      if(cursorTime==null||cursorId==null){
          return roleNotificationRepo.findUnseenByRole(user.getRole().getRoleName(), user.getId(), pageable);
      }
      return roleNotificationRepo.findUnseenByRoleWithCursor(user.getRole().getRoleName(), user.getId(), cursorTime,cursorId,pageable);
    }


    @Override
    public List<RoleNotification> getNotificationsByStatus(String status,LocalDateTime cursorTime,Long cursorId,int limit,Authentication authentication) {
        Pageable pageable = PageRequest.of(0, Math.min(limit,50), Sort.by(Sort.Order.desc("localDateTime"),Sort.Order.desc("id")));
        LmsLogin user = lmsLoginRepo.findByEmail(authentication.getName()).orElseThrow(()->new RuntimeException("User Not found"));
        String role = user.getRole().getRoleName();
        if(cursorTime==null||cursorId==null){
            return roleNotificationRepo.findByNotificationStatusOrderByLocalDateTimeDesc(status,role,pageable);
        }
        return roleNotificationRepo.findByStatusWithCompositeCursor(status,role,cursorTime,cursorId,pageable);

    }

    @Transactional
@Override
    public String markAllAsSeen(Set<Long> notificationIDs, Authentication authentication){
        LmsLogin loggedUser = lmsLoginRepo.findByEmail(authentication.getName()).orElseThrow(()-> new RuntimeException("No user found"));
        Long loggedInUserID = loggedUser.getId();
        if(notificationIDs==null||notificationIDs.isEmpty()){
            return "No notification Found an error occurred";
        }
Set<Long> alreadySeen = notificationSeenRepository.findAlreadySeenIds(loggedInUserID,notificationIDs);
       List<Long> toBeMarked = notificationIDs.stream().filter(id->!alreadySeen.contains(id)).toList();
        if(toBeMarked.isEmpty()){
            return "No notification Found to be marked as seen";
        }
        List<NotificationSeen> batch = toBeMarked.stream()
                .map(id->{
                    NotificationSeen notificationSeen = new NotificationSeen();
                    notificationSeen.setNotificationId(id);
                    notificationSeen.setUserId(loggedInUserID);
                    notificationSeen.setSeenAt(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                    return notificationSeen;
                }).toList();
        notificationSeenRepository.saveAll(batch);
        return batch.size()+ "Hurrah marked seen..! ";
    }
}
