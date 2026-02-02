package com.finallms.backend.service;

import com.amazonaws.HttpMethod;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;

@Service
public class S3Service {

    @Value("${aws.s3.access-key}")
    private String accessKey;

    @Value("${aws.s3.secret-key}")
    private String secretKey;

    @Value("${aws.s3.region}")
    private String region;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    private AmazonS3 s3Client;
    private boolean isMockMode = false;

    @PostConstruct
    public void init() {
        if (accessKey == null || accessKey.contains("placeholder")) {
            System.out.println("AWS Credentials not found. Using Mock S3 Mode.");
            isMockMode = true;
            return;
        }

        BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
        s3Client = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(region)
                .build();
    }

    public String uploadFile(MultipartFile file) throws IOException {
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

        if (isMockMode) {
            System.out.println("MOCK UPLOAD: " + fileName);
            return fileName;
        }

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(file.getContentType());

        s3Client.putObject(new PutObjectRequest(bucketName, fileName, file.getInputStream(), metadata));
        return fileName;
    }

    public String generatePresignedUrl(String fileName) {
        if (isMockMode) {
            String sampleVideo = "https://www.w3schools.com/html/mov_bbb.mp4";
            String sampleImage = "https://via.placeholder.com/350x220/667eea/ffffff?text=Media";
            if (fileName == null || fileName.isBlank())
                return sampleVideo;
            String lower = fileName.toLowerCase();
            if (lower.endsWith(".mp4") || lower.endsWith(".mov") || lower.endsWith(".mkv") || lower.endsWith(".webm"))
                return sampleVideo;
            return sampleImage;
        }

        if (fileName == null)
            return "";

        Date expiration = new Date();
        long expTimeMillis = expiration.getTime();
        expTimeMillis += 1000 * 60 * 60 * 2;
        expiration.setTime(expTimeMillis);

        GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(bucketName, fileName)
                .withMethod(HttpMethod.GET)
                .withExpiration(expiration);

        return s3Client.generatePresignedUrl(generatePresignedUrlRequest).toString();
    }

    public void deleteFile(String fileName) {
        if (isMockMode)
            return;
        s3Client.deleteObject(bucketName, fileName);
    }

    public boolean fileExists(String fileName) {
        if (isMockMode)
            return true;
        return s3Client.doesObjectExist(bucketName, fileName);
    }
}
