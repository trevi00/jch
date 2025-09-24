package org.jbd.backend.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jbd.backend.dashboard.domain.SystemMetrics;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemMetricsDto {

    private Long id;
    private LocalDate metricDate;
    private SystemMetrics.MetricType metricType;
    private String metricTypeDescription;
    private Long value;
    private String description;
    private LocalDateTime createdAt;

    public static SystemMetricsDto from(SystemMetrics metrics) {
        return SystemMetricsDto.builder()
                .id(metrics.getId())
                .metricDate(metrics.getMetricDate())
                .metricType(metrics.getMetricType())
                .metricTypeDescription(metrics.getMetricType().getDescription())
                .value(metrics.getValue())
                .description(metrics.getDescription())
                .createdAt(metrics.getCreatedAt())
                .build();
    }
}