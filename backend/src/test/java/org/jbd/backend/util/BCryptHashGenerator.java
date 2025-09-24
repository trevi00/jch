package org.jbd.backend.util;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BCryptHashGenerator {
    
    @Test
    public void generateBCryptHash() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "TestPassword123!";
        
        // Generate a new hash for "TestPassword123!"
        String hashedPassword = encoder.encode(rawPassword);
        System.err.println("=== BCrypt Hash Generation ===");
        System.err.println("Raw password: " + rawPassword);
        System.err.println("BCrypt hash: " + hashedPassword);
        System.err.println("Verification: " + encoder.matches(rawPassword, hashedPassword));
        
        // Test with various known hashes
        String[] testHashes = {
            "$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.",
            "$2a$10$N.35BN7Jq3BkzEn.gkRkBOmm7p7zVQMq3RzKLyCiI4kSL2aZN1SDC",
            hashedPassword
        };
        
        for (int i = 0; i < testHashes.length; i++) {
            boolean matches = encoder.matches(rawPassword, testHashes[i]);
            System.err.println("Hash " + (i + 1) + " verification: " + matches);
            if (matches) {
                System.err.println("WORKING HASH: " + testHashes[i]);
            }
        }
        
        // Assert that at least our generated hash works
        assertTrue(encoder.matches(rawPassword, hashedPassword));
        
        System.err.println("=== End BCrypt Hash Generation ===");
    }
}