package com.example.backend.service;

import java.io.IOException;
import java.time.Duration;

import org.apache.catalina.connector.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.core.signer.Presigner;
import software.amazon.awssdk.http.auth.spi.signer.HttpSigner;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.regions.Region;

@Service
public class S3Service {
    @Value("${aws.bucketName}")
    public String bucketName;

    @Autowired
    public S3Client s3Client;

    @Value("${aws.region}")
    private String region;

    public String getFileUrl(String fileName) {
        return "https://" + bucketName + ".s3." + region + ".amazonaws.com/" + fileName;
    }


    public String uploadFile(MultipartFile file,String name) throws IOException {
        s3Client.putObject(builder -> 
            builder.bucket(bucketName).key(name).build(),
            software.amazon.awssdk.core.sync.RequestBody.fromBytes(file.getBytes())

        );
        return getFileUrl(name);
    }

   
}