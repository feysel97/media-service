package com.microservice.mediaservice.service;

import com.microservice.mediaservice.dto.FileUploadEvent;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.http.Method;

import java.io.InputStream;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MediaStorageService {

    private final MinioClient minioClient;
    private final RabbitTemplate rabbitTemplate;

    @Value("${minio.bucket-name}")
    private String bucketName;

    @Value("${rabbitmq.exchange.media}")
    private String exchange;

    @Value("${rabbitmq.routing.file-uploaded}")
    private String routingKey;

    @PostConstruct
    public void initBucket() {
        try {
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                log.info("Bucket '{}' created successfully.", bucketName);
            } else {
                log.info("Bucket '{}' already exists.", bucketName);
            }
        } catch (Exception e) {
            log.error("Error initializing MinIO bucket: ", e);
            throw new RuntimeException("Failed to initialize MinIO storage", e);
        }
    }

    public String uploadFile(MultipartFile file, String username) {
        try {
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : "";
            String uniqueFilename = UUID.randomUUID().toString() + extension;

            InputStream inputStream = file.getInputStream();
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(uniqueFilename)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            log.info("File uploaded successfully to MinIO: {}", uniqueFilename);

            // Broadcast the event with the username attached
            FileUploadEvent event = new FileUploadEvent(username, uniqueFilename, file.getContentType(), file.getSize());
            rabbitTemplate.convertAndSend(exchange, routingKey, event);

            return uniqueFilename;
        } catch (Exception e) {
            log.error("Error uploading file: ", e);
            throw new RuntimeException("Failed to upload file", e);
        }
    }

    public String getFileUrl(String filename) {
        try {
            // Generates a secure, temporary URL valid for 24 hours
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(filename)
                            .expiry(60 * 60 * 24)
                            .build()
            );
        } catch (Exception e) {
            log.error("Error generating pre-signed URL: ", e);
            throw new RuntimeException("Failed to generate file URL", e);
        }
    }


}