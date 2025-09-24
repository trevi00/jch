package org.jbd.backend.dashboard.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MetricType {
    API_CALLS("API 호출 수"),
    ACTIVE_USERS("활성 사용자 수"),
    SYSTEM_UPTIME("시스템 업타임"),
    ERROR_COUNT("에러 발생 횟수"),
    RESPONSE_TIME("응답 시간"),
    MEMORY_USAGE("메모리 사용률"),
    CPU_USAGE("CPU 사용률"),
    DATABASE_CONNECTIONS("데이터베이스 연결 수"),
    DISK_USAGE("디스크 사용률"),
    NETWORK_IO("네트워크 I/O");

    private final String description;
}