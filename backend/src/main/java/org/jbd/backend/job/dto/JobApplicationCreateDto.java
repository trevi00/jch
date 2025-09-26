    package org.jbd.backend.job.dto;

    import jakarta.validation.constraints.NotBlank;
    import jakarta.validation.constraints.NotNull;
    import jakarta.validation.constraints.Size;
    import lombok.AllArgsConstructor;
    import lombok.Builder;
    import lombok.Getter;
    import lombok.NoArgsConstructor;
    import org.hibernate.validator.constraints.URL;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public class JobApplicationCreateDto {

        @NotNull(message = "채용공고 ID는 필수입니다")
        private Long jobPostingId;

        @NotBlank(message = "자기소개서는 필수입니다")
        @Size(min = 1, max = 2000, message = "자기소개서는 10자 이상 2000자 이하여야 합니다")
        private String coverLetter;

        @URL(message = "유효한 URL 형식이어야 합니다")
        private String resumeUrl;

        @Size(max = 1000, message = "포트폴리오 URL은 1000자 이하여야 합니다")
        private String portfolioUrls;
    }