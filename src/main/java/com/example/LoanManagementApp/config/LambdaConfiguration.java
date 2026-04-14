package com.example.LoanManagementApp.config;

import org.hibernate.jpa.HibernateHints;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.TransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import jakarta.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Lambda-specific Spring Boot configuration.
 * Optimizes the application for AWS Lambda cold starts and memory constraints.
 */
@Configuration
@EnableTransactionManagement
@EnableConfigurationProperties
public class LambdaConfiguration {
    
    private static final Logger logger = LoggerFactory.getLogger(LambdaConfiguration.class);

    /**
     * Configures transaction manager optimized for Lambda
     */
    @Bean
    public TransactionManager transactionManager(EntityManagerFactory emf) {
        logger.info("Initializing JPA Transaction Manager for Lambda");
        JpaTransactionManager tm = new JpaTransactionManager();
        tm.setEntityManagerFactory(emf);
        return tm;
    }

    /**
     * Lambda initialization hook - logs startup information
     */
    @Bean
    public LambdaInitializer lambdaInitializer() {
        return new LambdaInitializer();
    }

    /**
     * Inner class to handle Lambda initialization
     */
    public static class LambdaInitializer {
        private static final Logger logger = LoggerFactory.getLogger(LambdaInitializer.class);

        public LambdaInitializer() {
            logger.info("==================================================");
            logger.info("Lambda Environment Configuration:");
            logger.info("  Java Version: {}", System.getProperty("java.version"));
            logger.info("  Memory: {} MB", Runtime.getRuntime().maxMemory() / 1024 / 1024);
            logger.info("  Processors: {}", Runtime.getRuntime().availableProcessors());
            logger.info("  DB Host: {}", System.getenv("DB_HOST"));
            logger.info("  AWS Region: {}", System.getenv("My_AWS_REGION"));
            logger.info("==================================================");
        }
    }
}
