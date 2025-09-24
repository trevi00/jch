package org.jbd.backend.util;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PasswordHashGeneratorTest {
    
    private static final Logger logger = LoggerFactory.getLogger(PasswordHashGeneratorTest.class);
    
    @Test
    public void generatePasswordHash() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // Generate multiple hashes to ensure we have working ones
        String password = "password";
        
        System.out.println("=== GENERATING BCRYPT HASHES ===");
        for (int i = 1; i <= 3; i++) {
            String hash = encoder.encode(password);
            boolean matches = encoder.matches(password, hash);
            System.out.println("Hash " + i + ": " + hash);
            System.out.println("Verification " + i + ": " + matches);
            System.out.println("---");
            
            if (i == 1) {
                // Use the first hash for test-data.sql
                System.out.println("USE THIS HASH FOR test-data.sql:");
                System.out.println(hash);
                System.out.println("====================================");
            }
        }
        
        // Also test the one from our test data
        String existingHash = "$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HHZpliWvZ6mOQIllZ5Bc.";
        boolean existingMatches = encoder.matches(password, existingHash);
        System.out.println("Existing hash works: " + existingMatches);
    }
}