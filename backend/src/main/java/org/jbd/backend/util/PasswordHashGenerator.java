package org.jbd.backend.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * BCrypt 패스워드 해시 생성 및 검증 유틸리티 클래스
 *
 * 개발 및 테스트 환경에서 사용자 비밀번호의 BCrypt 해시를 생성하고 검증하는
 * 독립 실행형 유틸리티입니다. 주로 테스트 데이터 생성 시 사용됩니다.
 *
 * 주요 기능:
 * - BCrypt 해시 생성: 평문 비밀번호를 안전한 해시로 변환
 * - 해시 검증: 생성된 해시가 원본 비밀번호와 일치하는지 확인
 * - 기존 해시 호환성 검사: test-data.sql의 기존 해시 유효성 검증
 *
 * 사용 예시:
 * 1. 새로운 테스트 사용자 비밀번호 해시 생성
 * 2. 기존 테스트 데이터의 해시 검증
 * 3. 비밀번호 정책 변경 시 호환성 확인
 *
 * @author JBD Backend Team
 * @version 1.0
 * @since 2025-09-19
 * @see BCryptPasswordEncoder
 * @see org.jbd.backend.config.SecurityConfig#passwordEncoder()
 */
public class PasswordHashGenerator {

    /**
     * 프로그램 진입점 - BCrypt 해시 생성 및 검증을 수행합니다.
     *
     * 실행 과정:
     * 1. 지정된 평문 비밀번호("password")에 대한 BCrypt 해시 생성
     * 2. 생성된 해시가 원본 비밀번호와 일치하는지 검증
     * 3. test-data.sql에 저장된 기존 해시의 유효성 검사
     * 4. 기존 해시가 유효하지 않으면 새 해시 제안
     *
     * @param args 명령행 인수 (사용하지 않음)
     */
    public static void main(String[] args) {
        // Spring Security에서 사용하는 것과 동일한 BCrypt 인코더 생성
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        // 테스트에 사용할 기본 비밀번호
        String password = "password";

        System.out.println("=== GENERATING BCRYPT HASH ===");

        // 새로운 BCrypt 해시 생성
        // BCrypt는 매번 다른 솔트를 사용하므로 동일한 비밀번호도 다른 해시 생성
        String hash = encoder.encode(password);
        System.out.println("Password: " + password);
        System.out.println("BCrypt Hash: " + hash);

        // 생성된 해시가 올바른지 검증
        boolean matches = encoder.matches(password, hash);
        System.out.println("Hash verification: " + matches);

        // test-data.sql에 저장된 기존 해시와 호환성 검사
        // 이 해시는 "password"에 대한 BCrypt 해시입니다.
        String existingHash = "$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HHZpliWvZ6mOQIllZ5Bc.";
        boolean existingMatches = encoder.matches(password, existingHash);
        System.out.println("Existing test hash works: " + existingMatches);

        // 기존 해시 유효성에 따른 안내 메시지 출력
        if (existingMatches) {
            System.out.println("✓ Current test-data.sql hash is VALID! No changes needed.");
        } else {
            System.out.println("✗ Need to update test-data.sql with: " + hash);
        }
        System.out.println("===============================");
    }
}