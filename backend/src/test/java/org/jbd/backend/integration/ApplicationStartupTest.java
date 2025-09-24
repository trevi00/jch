package org.jbd.backend.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("애플리케이션 시작 테스트")
class ApplicationStartupTest {
    
    @Test
    @DisplayName("Spring Boot 애플리케이션이 정상적으로 시작되는지 테스트")
    void contextLoads() {
        // 이 테스트가 통과하면 Spring 컨텍스트가 정상적으로 로드됨을 의미
    }
}