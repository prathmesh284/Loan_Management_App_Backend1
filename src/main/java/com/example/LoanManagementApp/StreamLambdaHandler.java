package com.example.LoanManagementApp;

import com.amazonaws.serverless.proxy.spring.SpringBootLambdaContainerHandler;
import com.amazonaws.serverless.proxy.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

/**
 * Lambda handler for Spring Boot application running on AWS Lambda with HTTP API v2 (API Gateway v2)
 * 
 * Handler: com.example.LoanManagementApp.StreamLambdaHandler
 * 
 * Configuration:
 * - Memory: 512 MB (minimum recommended for Spring Boot)
 * - Timeout: 60 seconds
 * - Environment Variables: DB_HOST, DB_PORT, DB_NAME, DB_USERNAME, DB_PASSWORD, JWT_SECRET
 */
public class StreamLambdaHandler implements RequestStreamHandler {
    private static final Logger logger = LoggerFactory.getLogger(StreamLambdaHandler.class);
    
    // Spring Boot Lambda Container Handler for proxying requests to Spring Boot
    private static SpringBootLambdaContainerHandler<AwsProxyRequest, AwsProxyResponse> handler;

    static {
        try {
            logger.info("Initializing Spring Boot Lambda Container Handler");
            handler = SpringBootLambdaContainerHandler.getAwsProxyHandler(LoanManagementAppApplication.class);
        } catch (Exception e) {
            logger.error("Failed to initialize Spring Boot Lambda Container Handler", e);
            throw new RuntimeException("Could not initialize Spring Boot", e);
        }
    }

    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {
        logger.debug("Handling Lambda request - Request ID: {}", context.getAwsRequestId());
        
        try {
            // Proxy the request through Spring Boot Lambda Container Handler
            handler.proxyStream(inputStream, outputStream, context);
        } catch (Exception e) {
            logger.error("Error processing request - Request ID: {}", context.getAwsRequestId(), e);
            throw new IOException("Error processing request", e);
        }
    }
}