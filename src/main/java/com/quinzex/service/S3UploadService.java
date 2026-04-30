package com.quinzex.service;

import com.quinzex.dto.UploadResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3UploadService {
    private final S3Client s3Client;
private final S3PresignedUrlService s3PresignedUrlService;
    @Value("${aws.s3.bucket}")
    private String bucketName;

    public String uploadFile(MultipartFile file,String folder) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        String originalName = file.getOriginalFilename();
        String safeName = (originalName != null) ? originalName : "file";
        String s3Key = folder + "/" + UUID.randomUUID() + "-" + safeName;
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .contentType(file.getContentType())
                .build();
        s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
         return s3Key;
    }
    public void updateFile(String s3Key,MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
      PutObjectRequest putObjectRequest = PutObjectRequest.builder()
              .bucket(bucketName)
              .key(s3Key)
              .contentType(file.getContentType())
              .build();
        s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
    }

    public void deleteFile(String s3Key) throws IOException {
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .build();
        s3Client.deleteObject(deleteObjectRequest);
    }
    public String uploadFileAndGetUrl(MultipartFile file,String folder) throws IOException {
        if(file.isEmpty()){
            throw new IllegalArgumentException("file is empty");
        }
        String originalName=file.getOriginalFilename();
        String safeName =(originalName!=null)?originalName:"file";
        String s3Key = folder+"/"+ UUID.randomUUID()+"-" +safeName;
        PutObjectRequest putObjectRequest = PutObjectRequest.builder().bucket(bucketName).key(s3Key).contentType(file.getContentType()).build();
        s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        return s3Key;
    }

    public UploadResponse uploadAndGenerateUrl(MultipartFile file, String folder) throws IOException {

        String key = uploadFileAndGetUrl(file, folder);

        String url = s3PresignedUrlService.generateViewUrl(bucketName, key);

        return new UploadResponse(key, url);
    }
}
