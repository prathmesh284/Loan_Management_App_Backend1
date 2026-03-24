package com.example.LoanManagementApp.config;

import com.amazonaws.auth.*;
import com.amazonaws.services.s3.*;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.*;

@Configuration
public class AwsConfig {

    private final Dotenv dotenv = Dotenv.load();

    @Bean
    public AmazonS3 amazonS3() {

        String accessKey = dotenv.get("AWS_ACCESS_KEY");
        String secretKey = dotenv.get("AWS_SECRET_KEY");
        String region = dotenv.get("AWS_REGION");

        BasicAWSCredentials creds = new BasicAWSCredentials(accessKey, secretKey);

        return AmazonS3ClientBuilder.standard()
                .withRegion(region)
                .withCredentials(new AWSStaticCredentialsProvider(creds))
                .build();
    }
}