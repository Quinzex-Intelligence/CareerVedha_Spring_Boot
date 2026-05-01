package com.quinzex.contoller;

import com.quinzex.entity.OurServices;
import com.quinzex.service.IourSevice;
import com.quinzex.service.S3PresignedUrlService;
import com.quinzex.service.S3UploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/spring/services")
@RequiredArgsConstructor
public class OurServiceController {

    private final IourSevice ourService;
    private final S3UploadService s3UploadService;
    private final S3PresignedUrlService presignedUrlService;

    // CREATE
    @PostMapping("/create-service")
    public OurServices createService(@RequestBody OurServices request) {
        return ourService.createOurService(request);
    }

    //  UPDATE
    @PutMapping("/{id}")
    public OurServices updateService(@PathVariable Long id,
                                     @RequestBody OurServices request) {
        return ourService.updateService(id, request);
    }

    // 🗑 DELETE
    @DeleteMapping("/{id}")
    public String deleteService(@PathVariable Long id) {
        ourService.deleteService(id);
        return "Service deleted successfully";
    }

    // GET ALL (with images + presigned URLs)
    @GetMapping("/get-all")
    public List<OurServices> getServices(
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "5") int size
    ) {
        return ourService.findServicesWithCursor(cursor, size);
    }
    @PostMapping("/upload")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<String> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("folder") String folder) throws IOException {

        String s3Key = s3UploadService.uploadFile(file, folder);
        return ResponseEntity.ok(s3Key);
    }
    @PutMapping("/update")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<String> updateFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("key") String s3Key) throws IOException {

        s3UploadService.updateFile(s3Key, file);
        return ResponseEntity.ok("File updated successfully");
    }



    // DELETE IMAGE FROM S3
    @DeleteMapping("/file")
    @PreAuthorize("hasAuthority('SERVICES') or hasRole('SUPER_ADMIN')")
    public String deleteImage(@RequestParam("key") String key) throws Exception {
        s3UploadService.deleteFile(key);
        return "Image deleted successfully";
    }
    @GetMapping("/get/{id}")
    public OurServices getServiceById(@PathVariable Long id) {
        return ourService.getServiceById(id);
    }
}