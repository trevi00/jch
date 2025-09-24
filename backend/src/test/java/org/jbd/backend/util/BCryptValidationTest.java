package org.jbd.backend.util;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class BCryptValidationTest {

    @Test
    public void testBCryptGeneration() {
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "password";
        
        // Generate fresh hash
        String newHash = encoder.encode(rawPassword);
        System.out.println("=== BCRYPT TEST RESULTS ===");
        System.out.println("Raw password: " + rawPassword);
        System.out.println("Generated hash: " + newHash);
        
        // Test the new hash
        boolean newHashMatches = encoder.matches(rawPassword, newHash);
        System.out.println("New hash matches: " + newHashMatches);
        assertTrue(newHashMatches, "New hash should match password");
        
        // Test the current test-data.sql hash
        String currentHash = "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbdOIig.5qjOwC2wC";
        boolean currentHashMatches = encoder.matches(rawPassword, currentHash);
        System.out.println("Current test-data hash: " + currentHash);
        System.out.println("Current hash matches: " + currentHashMatches);
        
        // Test some alternative known good hashes for "password"
        String[] knownHashes = {
            "$2a$10$E.dJmQexRcakrCjqMPGOxuQjav/ZpY9TJOgBUtP.JkLq5S2GNJpL6", // From Spring docs
            "$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HHZpliWvZ6mOQIllZ5Bc.", // From online generator
            "$2a$12$EXRkfkdmXn2gzds2SSitu.MW9.gAVqa9eLS1//RYtYCTqdWzsqB5W", // Another version
        };
        
        System.out.println("\nTesting known good hashes:");
        for (int i = 0; i < knownHashes.length; i++) {
            boolean matches = encoder.matches(rawPassword, knownHashes[i]);
            System.out.println("Hash " + (i+1) + ": " + knownHashes[i]);
            System.out.println("Matches: " + matches);
            
            if (matches) {
                System.out.println("*** FOUND WORKING HASH: " + knownHashes[i] + " ***");
                break;
            }
        }
        
        // Output final recommendation
        System.out.println("\n=== RECOMMENDATION ===");
        System.out.println("Use this hash in test-data.sql:");
        System.out.println(newHash);
        System.out.println("========================");
    }
}