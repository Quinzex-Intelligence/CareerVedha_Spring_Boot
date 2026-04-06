package com.quinzex.contoller;

import com.quinzex.FeignClient.ActuatorFeignClient;
import com.quinzex.dto.*;
import com.quinzex.entity.PostNotification;
import com.quinzex.entity.Questions;
import com.quinzex.entity.RoleNotification;
import com.quinzex.entity.YoutubeUrls;
import com.quinzex.enums.Category;
import com.quinzex.enums.Language;
import com.quinzex.enums.YoutubeCategory;
import com.quinzex.service.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping(value = "/api/spring", produces = "application/json")
public class Controller {
    private final ContactService contactService;
    private final IemailService iemailService;
    private final IRegistrationLogin iRegistrationLogin;
    private final IRolesAndPermissions rolesAndPermissions;
    private final IExamService examService;
    private final INotificationService notificationService;
    private final IRoleApprovalService roleApprovalService;
    private final IRoleAndPermissionNames   roleAndPermissionNames;
    private final IUsersService usersService;
    private final PostNotificationProducer producer;
    private final PostNotificationService postNotificationService;
    private final PostNotificationCounterService postNotificationCounterService;
    private final ActuatorFeignClient actuatorFeignClient;
    private final IDjangoCategories     djangoCategories;
    private final CurrentAffairsService currentAffairsService;
    private final QuestionPaperService questionPaperService;
    private final IotpService   iotpService;
    private final IYoutubeService youtubeService;

    public Controller(
            IemailService iemailService,
            IRegistrationLogin iRegistrationLogin,
            IRolesAndPermissions rolesAndPermissions,
            IExamService examService,
            INotificationService notificationService,
            IRoleApprovalService roleApprovalService,
            IRoleAndPermissionNames   roleAndPermissionNames,
            IUsersService usersService,
            PostNotificationProducer producer, PostNotificationService postNotificationService,
            PostNotificationCounterService postNotificationCounterService,
            ActuatorFeignClient actuatorFeignClient,
            IDjangoCategories djangoCategories,
            CurrentAffairsService currentAffairsService,
            QuestionPaperService questionPaperService,
            ContactService contactService,
            IotpService iotpService,
            IYoutubeService youtubeService) {

        this.iemailService = iemailService;
        this.iRegistrationLogin = iRegistrationLogin;
        this.rolesAndPermissions = rolesAndPermissions;
        this.examService = examService;
        this.notificationService = notificationService;
        this.roleApprovalService = roleApprovalService;
        this.roleAndPermissionNames =roleAndPermissionNames;
        this.usersService = usersService;
        this.producer = producer;
        this.postNotificationService = postNotificationService;
        this.postNotificationCounterService = postNotificationCounterService;
        this.actuatorFeignClient=actuatorFeignClient;
        this.djangoCategories = djangoCategories;
        this.currentAffairsService = currentAffairsService;
        this.questionPaperService = questionPaperService;
        this.contactService = contactService;
        this.iotpService = iotpService;
        this.youtubeService = youtubeService;
    }



    @PostMapping("/registersendotp")
    public ResponseEntity<Map<String, Object>> sendOtp(@RequestParam String email) {
       String otp =iotpService.generateOtp(email);
       iemailService.sendEmail(email,otp);
        return ResponseEntity.ok(
                Map.of("message", "OTP sent to mail", "email", email)
        );
    }

    @PostMapping("/registeruser")
    public ResponseEntity<Map<String, Object>> registerUser(
            @RequestBody RegisterUser registerUser) {

        String result = iRegistrationLogin.RegisterUser(registerUser);
        return ResponseEntity.ok(
                Map.of("message", result)
        );
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @RequestBody LoginRequest loginRequest,
            HttpServletResponse response) {

        return ResponseEntity.ok(
                iRegistrationLogin.login(loginRequest, response)
        );
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(
            @CookieValue("refreshToken") String refreshToken,
            HttpServletResponse response) {

        return ResponseEntity.ok(
                iRegistrationLogin.refresh(refreshToken, response)
        );
    }

    @PostMapping("/log-out")
    public ResponseEntity<Map<String, Object>> logout(
            @CookieValue(value = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response) {

        iRegistrationLogin.Logout(refreshToken, response);
        return ResponseEntity.ok(
                Map.of("message", "Logged out successfully")
        );
    }

    @PostMapping("/login/send-otp")
    public ResponseEntity<Map<String, Object>> sendLoginOtp(@RequestParam String email) {
        iRegistrationLogin.sendLoginOtp(email);
        return ResponseEntity.ok(
                Map.of("message", "Login OTP sent", "email", email)
        );
    }



    @PostMapping("/create-permission")
    public ResponseEntity<Map<String, Object>> createPermission(
            @RequestBody CreatePermissionRequest request) {

        return ResponseEntity.ok(
                Map.of("message", rolesAndPermissions.createPermission(request))
        );
    }

    @DeleteMapping("/delete-permission")
    public ResponseEntity<Map<String, Object>> deletePermission(
            @RequestBody CreatePermissionRequest request) {

        return ResponseEntity.ok(
                Map.of("message", rolesAndPermissions.deletePermission(request))
        );
    }

    @PostMapping("/create-role")
    @PreAuthorize("hasAuthority('LOGIN')")
    public ResponseEntity<Map<String, Object>> createRole(
            @RequestBody CreateRoleRequest request) {

        return ResponseEntity.ok(
                Map.of("message",
                        rolesAndPermissions.createRole(request.getRoleName()))
        );
    }

    @DeleteMapping("/inactive-role")
    public ResponseEntity<Map<String, Object>> deleteRole(
            @RequestBody CreateRoleRequest request) {

        return ResponseEntity.ok(
                Map.of("message",
                        rolesAndPermissions.inactiveRole(request.getRoleName()))
        );
    }

    @PostMapping("/add-permission")
    public ResponseEntity<Map<String, Object>> addPermissionToRole(
            @RequestBody RolePermissionRequest request) {

        return ResponseEntity.ok(
                Map.of("message",
                        rolesAndPermissions.addPermissionToRole(request))
        );
    }

    @PostMapping("/remove-permission")
    public ResponseEntity<Map<String, Object>> removePermissionFromRole(
            @RequestBody RolePermissionRequest request) {

        return ResponseEntity.ok(
                Map.of("message",
                        rolesAndPermissions.removePermissionFromRole(request))
        );
    }

    /* ---------------- TEST ---------------- */

    @GetMapping("/welcome")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> welcome() {
        return ResponseEntity.ok(
                Map.of("message", "welcome you are authenticated")
        );
    }





    @PostMapping("/post-question")
    public ResponseEntity<Map<String, Object>> createQuestion(
            @RequestBody List<CreateQuestion> createQuestions) {

        return ResponseEntity.status(HttpStatus.CREATED).body(
                Map.of("message",
                        examService.createQuestion(createQuestions))
        );
    }

    @PostMapping("/submit-exam")
    public ResponseEntity<ScoreWithAnswers> submitExam(
            @RequestBody List<AnswerRequest> answers) {

        return ResponseEntity.ok(examService.getScore(answers));

    }



    @GetMapping("/get-all-notifications")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<RoleNotification>> getAllNotifications(@RequestParam(required = false)LocalDateTime localDateTime,@RequestParam(required = false)Long cursorId,@RequestParam(defaultValue = "20")int limit) {
        return ResponseEntity.ok(
                notificationService.getAllNotifications(localDateTime, cursorId, limit)
        );
    }

    @GetMapping("/all-unseen-super-admin")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<RoleNotification>> getAllUnseenNotifications(
            Authentication authentication,
            @RequestParam(defaultValue = "20") int limit
    ) {
        return ResponseEntity.ok(
                notificationService.getUnseenNotificationsByRole(
                        authentication,
                        null,
                        null,
                        limit
                )
        );
    }


    @GetMapping("/get-all-notifcations-by-role")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<List<RoleNotification>> getNotificationsByRole(
            Authentication authentication ,@RequestParam(required = false)LocalDateTime localDateTime,@RequestParam(required = false)Long cursorId,@RequestParam(defaultValue = "20")int limit) {

        return ResponseEntity.ok(
                notificationService.getNotificationsByRole(authentication,localDateTime,cursorId,limit)
        );
    }

    @GetMapping("/unseen-notifications-by-role")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<List<RoleNotification>> getUnseenNotificationsByRole(
            Authentication authentication,
            @RequestParam(required = false) LocalDateTime cursorTime,
            @RequestParam(required = false) Long cursorId,
            @RequestParam(defaultValue = "20") int limit
    ) {
        return ResponseEntity.ok(
                notificationService.getUnseenNotificationsByRole(
                        authentication,
                        cursorTime,
                        cursorId,
                        limit
                )
        );
    }



    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    @PostMapping("/notifications-seen/-all")
    public ResponseEntity<String> markAllSeen(
            @RequestBody Set<Long> notificationIds,
            Authentication authentication) {

        return ResponseEntity.ok(
                notificationService.markAllAsSeen(notificationIds, authentication)
        );
    }


    @PutMapping("/{notificationId}/approve")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> approveNotification(
            @PathVariable long notificationId,
            Authentication authentication) {

        return ResponseEntity.ok(
                Map.of("message",
                        roleApprovalService.approve(notificationId, authentication))
        );
    }

    @PutMapping("/{notificationId}/reject")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> rejectNotification(
            @PathVariable long notificationId,
            @RequestBody RejectRequest rejectRequest,
            Authentication authentication) {

        return ResponseEntity.ok(
                Map.of("message",
                        roleApprovalService.reject(
                                notificationId,
                                rejectRequest.getReason(),
                                authentication))
        );
    }
    @GetMapping("/role-names")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<String>> getRoleNames() {
        return ResponseEntity.ok(
                roleAndPermissionNames.getRoleNames()
        );
    }
    @GetMapping("/permission-names")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<String>> getPermissionNames() {
        return ResponseEntity.ok(
                roleAndPermissionNames.getPermissionNames()
        );
    }
    @GetMapping("/get-active-roles")
    public ResponseEntity<List<String>> getAllActiveRoles() {
        return ResponseEntity.ok(
                rolesAndPermissions.getAllActiveRoles());
    }
    @GetMapping("/get-incative-roles")
    public ResponseEntity<List<String>> getAllInactiveRoles() {
        return ResponseEntity.ok(
                rolesAndPermissions.getAllInactiveRoles());
    }
    @PostMapping("/active-role")
    public ResponseEntity<String> activeRole(@RequestBody CreateRoleRequest RoleRequest) {
        return ResponseEntity.ok(rolesAndPermissions.activeRole(RoleRequest.getRoleName()));
    }
    @PutMapping("/edit-question/{id}")
    public ResponseEntity<String> editQuestion(
            @PathVariable Long id,
            @RequestBody CreateQuestion createQuestion
    ) {
        String response = examService.editQuestion(id, createQuestion);
        return ResponseEntity.ok(response);
    }


    @DeleteMapping("/delete-question/{id}")
    public ResponseEntity<String> deleteQuestion(
            @PathVariable Long id
    ) {
        String response = examService.deleteQuestion(List.of(id));
        return ResponseEntity.ok(response);
    }
    @GetMapping("/notifications-status")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<List<RoleNotification>> getByStatus(
            @RequestParam String status,
            @RequestParam(required = false) LocalDateTime cursorTime,
            @RequestParam(required = false) Long cursorId,
            @RequestParam(defaultValue = "20") int limit,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                notificationService.getNotificationsByStatus(
                        status.toUpperCase(),
                        cursorTime,
                        cursorId,
                        limit,
                        authentication
                )
        );
    }
    @GetMapping("/get-active-users")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Page<UserDTO>> getAllActiveUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return ResponseEntity.ok(usersService.getAllActiveUsers(pageable));
    }
    @GetMapping("/get-all-users")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Page<UserDTO>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return ResponseEntity.ok(usersService.getAllUsers(pageable));
    }
    @GetMapping("/get-inactive-users")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Page<UserDTO>> getAllInactiveUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return ResponseEntity.ok(usersService.getAllInactiveUsers(pageable));
    }
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Page<UserDTO>> searchUsers(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return ResponseEntity.ok(usersService.searchUsers(keyword, pageable));
    }
    @PutMapping("/activate-user")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<String> activateUser(
            @RequestParam String email,Authentication  authentication
    ) {
        return ResponseEntity.ok(usersService.activeUser(email,authentication));
    }
    @PutMapping("/inactivate-user")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<String> inactivateUser(
            @RequestParam String email,Authentication authentication
    ) {
        return ResponseEntity.ok(usersService.inactiveUser(email,authentication));
    }
    @GetMapping("/get-logged-user-details")
    public ResponseEntity<UserDTO> getUserDetails(Authentication authentication) {
        return ResponseEntity.ok(usersService.getLoggedInUserDetails(authentication));
    }
    @PutMapping("/edit-loggedin-user-details")
    public ResponseEntity<String> editLoggedInUser(@RequestBody UserDTO userDTO,Authentication authentication) {
        String msg = usersService.editLoggedInUser(userDTO.getFirstName(), userDTO.getLastName(), authentication);
        return ResponseEntity.ok(msg);
    }
    @PutMapping("/change-role")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<String> editRole(
            @RequestParam String email,
            @RequestParam String roleName
    ) {
        String response = usersService.editRole(email, roleName);
        return ResponseEntity.ok(response);
    }
    @PostMapping("/send-post-notification")
    public ResponseEntity<String> send(
            @RequestBody PostNotificationDto dto
    ) {
        producer.sendNotification(dto);   // ENTRY POINT
        return ResponseEntity.ok("Notification sent");
    }
    @GetMapping("/post-notifications")
    public List<PostNotification> getNotifications(Authentication authentication,   @RequestParam(required = false) LocalDateTime createdAt,
                                                   @RequestParam(required = false) Long cursorId,
                                                   @RequestParam(defaultValue = "20") int size) {

        return postNotificationService.getPostNotifications( authentication,createdAt,cursorId, size);
    }
    @GetMapping("/post-unseen-count")
    public long getUnseenCount(Authentication authentication) {

        String role = authentication.getAuthorities()
                .iterator()
                .next()
                .getAuthority()
                .replace("ROLE_", "")
                .toUpperCase();

        if ("SUPER_ADMIN".equals(role)) {
            return postNotificationCounterService.getCounterForSuperAdmin();
        }

        if ("ADMIN".equals(role)) {
            return postNotificationCounterService.getCounterForAdmin();
        }

        return postNotificationCounterService.getCounterForRole(role);
    }
    @PutMapping("/reset-unseen")
    public ResponseEntity<?> resetUnseen(Authentication authentication) {

        if (authentication == null || authentication.getAuthorities().isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "No role found"));
        }

        String role = authentication.getAuthorities()
                .iterator()
                .next()
                .getAuthority()
                .replace("ROLE_", "")
                .toUpperCase();

        if ("SUPER_ADMIN".equals(role)) {
            postNotificationCounterService.resetForSuperAdmin();
        } else if ("ADMIN".equals(role)) {
            postNotificationCounterService.resetForAdmin();
        } else {
            postNotificationCounterService.resetForRole(role);
        }

        return ResponseEntity.ok(Map.of("message", "Unseen counter reset"));
    }

    @GetMapping("/questions-random-chapterid")
    public ResponseEntity<List<?>> getRandomQuestions(
            @RequestParam Long chapterId,
            @RequestParam(defaultValue = "10") int count
    ) {
        List<?> questions =
                examService.getRandomQuestionsByChapterID(chapterId, count);

        if (questions.isEmpty()) {
            return ResponseEntity.noContent().build(); // 204
        }

        return ResponseEntity.ok(questions); // 200
    }
    @GetMapping("/questions-random-category")
    public ResponseEntity<List<?>> getRandomQuestions(
            @RequestParam String category,
            @RequestParam(defaultValue = "10") int count
    ) {
        List<?> questions =
                examService.getRandomQuestionsByCategory(category, count);

        if (questions.isEmpty()) {
            return ResponseEntity.noContent().build(); // 204
        }

        return ResponseEntity.ok(questions); // 200
    }

    @PatchMapping("/post-notifications/{id}/seen")
    public ResponseEntity<String> markAsPostSeen(@PathVariable Long id) {
        postNotificationService.markNotificationAsSeen(id);
        return ResponseEntity.ok("Notification marked as seen");
    }

    @GetMapping("/get-exam-categories")
    public ResponseEntity<List<String>> getAllExamCategories() {
        return ResponseEntity.ok(examService.getAllExamCategories());
    }
    @PostMapping(path = "/current-affairs",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> createCurrentAffairs( @RequestParam List<String> title,
                                                        @RequestParam(required = false) List<String> summary,
                                                        @RequestParam(required = false) List<String> description,
                                                        @RequestParam List<String> region,
                                                        @RequestParam(required = false) List<Language> language,
                                                        @RequestParam List<MultipartFile> file) throws IOException {
        List<CurrentAffairsCreateRequest> requests = new ArrayList<>();
        for(int i = 0; i < title.size(); i++){
            CurrentAffairsCreateRequest request = new CurrentAffairsCreateRequest();
            request.setTitle(title.get(i));
            request.setSummary(summary.get(i));
            request.setDescription(description.get(i));
            request.setRegion(region.get(i));
            request.setFile(file.get(i));
            request.setLanguage(language.get(i));
            requests.add(request);
        }
        return ResponseEntity.ok(
                currentAffairsService.createCurrentAffairs(requests)
        );
    }
    @GetMapping("/get-all-affairs")
    public ResponseEntity<List<CurrentAffairsResponse>> getAllCurrentAffairs(   @RequestParam(required = false) LocalDateTime cursorTime,
                                                                                @RequestParam(required = false) Long cursorId,
                                                                                @RequestParam(defaultValue = "20") int limit,
                                                                                            @RequestParam(required = false) Language language) {
        return ResponseEntity.ok(
                currentAffairsService.findAllWithLinks(cursorTime, language, cursorId, limit)
        );
    }
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(actuatorFeignClient.getHealth());
    }

    @GetMapping("/internal/academics/hierarchy")
    public List<ClassHierarchyDTO> getHierarchy() {
        return djangoCategories.fetchHierarchy();
    }

    @PutMapping(   path = "/update-ca/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> updateCurrentAffairs(  @PathVariable Long id,
                                                         @RequestParam String title,
                                                         @RequestParam(required = false) String summary,
                                                         @RequestParam(required = false) String description,
                                                         @RequestParam String region,
                                                         @RequestParam(required = false) MultipartFile file) throws IOException {
        CurrentAffairsCreateRequest request = new CurrentAffairsCreateRequest();
        request.setTitle(title);
        request.setSummary(summary);
        request.setDescription(description);
        request.setRegion(region);
        request.setFile(file);
        return  ResponseEntity.ok(currentAffairsService.updateCurrentAffairs(id, request));
    }
    @DeleteMapping(path = "/delete-ca/{id}")
    public ResponseEntity<String> deleteCurrentAffairs(@PathVariable Long id) throws IOException {
        return ResponseEntity.ok(currentAffairsService.deleteCurrentAffairs(id));
    }
    @GetMapping("/current-affairs/by-region")
    public ResponseEntity<List<CurrentAffairsResponse>> getByRegion(
            @RequestParam String region,
            @RequestParam(required = false) Language language,
            @RequestParam(required = false) LocalDateTime cursorTime,
            @RequestParam(required = false) Long cursorId,
            @RequestParam(defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(
                currentAffairsService.getByRegionWithCursor(
                        region,
                         language,
                        cursorTime,
                        cursorId,
                        limit
                )
        );
    }
    @PostMapping("/create-prev-papers/materials")
    public ResponseEntity<String> createPapers(
            @ModelAttribute BulkCreatePaperDto bulkRequests
    ) throws IOException {

        String response = questionPaperService.createPaper(bulkRequests.getRequests());
        return ResponseEntity.ok(response);
    }
    @GetMapping("/get-papers/bycategory")
    public ResponseEntity<List<PaperResponseDto>> getPapers(
            @RequestParam Category category,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime cursor,

            @RequestParam(defaultValue = "10")
            int limit
    ) {

        List<PaperResponseDto> papers =
                questionPaperService.getPapersByCategory(category, cursor, limit);

        return ResponseEntity.ok(papers);
    }
    @DeleteMapping("/delete-papers")
    public ResponseEntity<String> deletePapers(
            @RequestBody List<Long> ids
    ) throws IOException {

        String response = questionPaperService.deleteMultiplePapers(ids);
        return ResponseEntity.ok(response);
    }
    @PostMapping("/contact")
    public ResponseEntity<String> submitContact(
            @RequestBody ContactRequestDto request
    ) {
        String response = contactService.submitContact(request);
        return ResponseEntity.ok(response);
    }
    @PostMapping("/create-yt-urls")
    public String createYoutubeUrls(@RequestBody List<YoutubeUrls> youtubeUrls) {
        return youtubeService.createYoutubeUrls(youtubeUrls);
    }
    //  Get (Cursor Based Pagination)
    @GetMapping("/get-yt-urls-by-category")
    public List<YoutubeUrls> getYoutubeUrls(
            @RequestParam YoutubeCategory category,
            @RequestParam(required = false) Long cursorId
    ) {
        return youtubeService.getYoutubeUrls(category, cursorId);
    }
    @DeleteMapping("/delete-yt-urls")
    public String deleteYoutubeUrls(@RequestBody List<Long> ids) {
        return youtubeService.deleteYoutubeUrls(ids);
    }
    @PutMapping("/edit-yt-urls")
    public String updateYoutubeUrls(@RequestBody YoutubeUrls youtubeUrls) {
        return youtubeService.updateYoutubeUrls(youtubeUrls);
    }

}
