package com.example.backend.config;
import org.aspectj.weaver.patterns.HasThisTypePatternTriedToSneakInSomeGenericOrParameterizedTypePatternMatchingStuffAnywhereVisitor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.services.s3.S3Client;

import software.amazon.awssdk.regions.Region;

@Configuration
public class S3Config {
    @Value("${aws.region}")
    String region;

    @Value("${aws.accessKeyId}")
    String accessKeyId;

    @Value("${aws.secretAccessKey}")
    String secretAccessKey;

     @Bean
    public Region awsRegion() {
        return Region.of(region);
    }

    @Bean
    public AwsCredentialsProvider awsCredentialsProvider() {
        return StaticCredentialsProvider.create(
            AwsBasicCredentials.create(accessKeyId, secretAccessKey)
        );
    }

    
    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .region(software.amazon.awssdk.regions.Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create
                    (software.amazon.awssdk.auth.credentials.AwsBasicCredentials.create(accessKeyId, secretAccessKey)))
                .build();
    }
}