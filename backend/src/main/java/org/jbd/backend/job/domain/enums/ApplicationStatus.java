package org.jbd.backend.job.domain.enums;

public enum ApplicationStatus {
    SUBMITTED("지원완료"),
    REVIEWED("검토중"),
    DOCUMENT_PASSED("서류통과"),
    INTERVIEW_SCHEDULED("면접예정"),
    INTERVIEW_PASSED("면접통과"),
    HIRED("최종합격"),
    REJECTED("불합격"),
    WITHDRAWN("지원취소");
    
    private final String description;
    
    ApplicationStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}