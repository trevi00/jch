import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class HashPassword {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "TestPassword123!";
        String hashedPassword = encoder.encode(rawPassword);
        System.out.println("BCrypt hash: " + hashedPassword);
        System.out.println("Verification: " + encoder.matches(rawPassword, hashedPassword));
    }
}