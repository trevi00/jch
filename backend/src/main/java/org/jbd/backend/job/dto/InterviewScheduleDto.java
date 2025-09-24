package org.jbd.backend.job.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InterviewScheduleDto {

    @NotNull(message = "면접 일시는 필수입니다")
    private LocalDateTime interviewDateTime;

    private String location;

    private String interviewerName;

    private String notes;
}