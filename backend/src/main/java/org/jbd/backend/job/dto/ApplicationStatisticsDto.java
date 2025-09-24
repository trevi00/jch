package org.jbd.backend.job.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationStatisticsDto {

    private Long totalApplications;
    private Long appliedCount;
    private Long documentPassedCount;
    private Long interviewScheduledCount;
    private Long interviewPassedCount;
    private Long finalPassedCount;
    private Long rejectedCount;
    
    private Double documentPassRate;
    private Double interviewPassRate;
    private Double finalPassRate;
}