package org.jbd.backend.dashboard.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "system_metrics", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"metric_date", "metric_type"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class SystemMetrics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "metric_date", nullable = false)
    private LocalDate metricDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "metric_type", nullable = false)
    private MetricType metricType;

    @Column(name = "metric_value", nullable = false)
    private Long value;

    private String description;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    public SystemMetrics(LocalDate metricDate, MetricType metricType, Long value, String description) {
        this.metricDate = metricDate;
        this.metricType = metricType;
        this.value = value;
        this.description = description;
    }

    public void updateValue(Long newValue) {
        this.value = newValue;
    }

    public static SystemMetrics createDailyActiveUsers(LocalDate date, Long count) {
        return new SystemMetrics(date, MetricType.DAILY_ACTIVE_USERS, count, "일일 활성 사용자 수");
    }

    public static SystemMetrics createApiCalls(LocalDate date, Long count) {
        return new SystemMetrics(date, MetricType.API_CALLS, count, "일일 API 호출 수");
    }

    public static SystemMetrics createErrorCount(LocalDate date, Long count) {
        return new SystemMetrics(date, MetricType.ERROR_COUNT, count, "일일 오류 발생 수");
    }

    public enum MetricType {
        DAILY_ACTIVE_USERS("일일 활성 사용자"),
        API_CALLS("API 호출 수"),
        ERROR_COUNT("오류 발생 수"),
        LOGIN_COUNT("로그인 수"),
        SIGNUP_COUNT("회원가입 수");

        private final String description;

        MetricType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}