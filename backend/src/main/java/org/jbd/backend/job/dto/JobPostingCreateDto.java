package org.jbd.backend.job.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jbd.backend.job.domain.enums.ExperienceLevel;
import org.jbd.backend.job.domain.enums.JobType;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobPostingCreateDto {
    
    @NotBlank(message = "채용공고 제목은 필수입니다")
    @Size(min = 5, max = 100, message = "제목은 5자 이상 100자 이하여야 합니다")
    private String title;
    
    @NotBlank(message = "회사명은 필수입니다")
    @Size(max = 100, message = "회사명은 100자 이하여야 합니다")
    private String companyName;
    
    @NotBlank(message = "근무지역은 필수입니다")
    @Size(max = 100, message = "근무지역은 100자 이하여야 합니다")
    private String location;
    
    @NotNull(message = "고용형태는 필수입니다")
    private JobType jobType;
    
    @NotNull(message = "경력 수준은 필수입니다")
    private ExperienceLevel experienceLevel;
    
    @Size(max = 100, message = "부서명은 100자 이하여야 합니다")
    private String department;
    
    @Size(max = 100, message = "직무분야는 100자 이하여야 합니다")
    private String field;
    
    @Size(max = 3000, message = "직무설명은 3000자 이하여야 합니다")
    private String description;
    
    @Size(max = 2000, message = "자격요건은 2000자 이하여야 합니다")
    private String qualifications;
    
    @Size(max = 1500, message = "필요 기술은 1500자 이하여야 합니다")
    private String requiredSkills;
    
    @Size(max = 1000, message = "복리후생은 1000자 이하여야 합니다")
    private String benefits;
    
    @Min(value = 0, message = "최소 급여는 0 이상이어야 합니다")
    private Integer minSalary;
    
    @Min(value = 0, message = "최대 급여는 0 이상이어야 합니다")
    private Integer maxSalary;
    
    private Boolean salaryNegotiable = false;
    
    @Size(max = 100, message = "근무시간은 100자 이하여야 합니다")
    private String workingHours;
    
    private Boolean isRemotePossible = false;
    
    @Email(message = "유효한 이메일 형식이어야 합니다")
    private String contactEmail;
    
    @Pattern(regexp = "^[0-9-]+$", message = "전화번호는 숫자와 하이픈만 포함해야 합니다")
    private String contactPhone;
}