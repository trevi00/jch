package org.jbd.backend.job.specification;

import org.jbd.backend.job.domain.JobPosting;
import org.jbd.backend.job.domain.enums.ExperienceLevel;
import org.jbd.backend.job.domain.enums.JobStatus;
import org.jbd.backend.job.domain.enums.JobType;
import org.jbd.backend.job.dto.JobPostingSearchDto;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class JobPostingSpecification {

    public static Specification<JobPosting> withSearchCriteria(JobPostingSearchDto searchDto) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 키워드 검색 (제목, 설명, 회사명)
            if (searchDto.getKeyword() != null && !searchDto.getKeyword().trim().isEmpty()) {
                String keyword = "%" + searchDto.getKeyword().toLowerCase() + "%";
                Predicate titlePredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("title")), keyword);
                Predicate descriptionPredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("description")), keyword);
                Predicate companyNamePredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("companyName")), keyword);

                predicates.add(criteriaBuilder.or(titlePredicate, descriptionPredicate, companyNamePredicate));
            }

            // 제목 검색
            if (searchDto.getTitle() != null && !searchDto.getTitle().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("title")),
                    "%" + searchDto.getTitle().toLowerCase() + "%"));
            }

            // 회사명 검색
            if (searchDto.getCompanyName() != null && !searchDto.getCompanyName().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("companyName")),
                    "%" + searchDto.getCompanyName().toLowerCase() + "%"));
            }

            // 지역 검색
            if (searchDto.getLocation() != null && !searchDto.getLocation().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("location")),
                    "%" + searchDto.getLocation().toLowerCase() + "%"));
            }

            // 고용 형태
            if (searchDto.getJobType() != null) {
                predicates.add(criteriaBuilder.equal(root.get("jobType"), searchDto.getJobType()));
            }

            // 경력 수준
            if (searchDto.getExperienceLevel() != null) {
                predicates.add(criteriaBuilder.equal(root.get("experienceLevel"), searchDto.getExperienceLevel()));
            }

            // 급여 범위
            if (searchDto.getSalaryMin() != null) {
                predicates.add(criteriaBuilder.or(
                    criteriaBuilder.isNull(root.get("salaryMax")),
                    criteriaBuilder.greaterThanOrEqualTo(root.get("salaryMax"), searchDto.getSalaryMin())
                ));
            }

            if (searchDto.getSalaryMax() != null) {
                predicates.add(criteriaBuilder.or(
                    criteriaBuilder.isNull(root.get("salaryMin")),
                    criteriaBuilder.lessThanOrEqualTo(root.get("salaryMin"), searchDto.getSalaryMax())
                ));
            }

            // 급여 협의 가능 여부
            if (searchDto.getSalaryNegotiable() != null) {
                predicates.add(criteriaBuilder.equal(root.get("salaryNegotiable"), searchDto.getSalaryNegotiable()));
            }

            // 부서
            if (searchDto.getDepartment() != null && !searchDto.getDepartment().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("department")),
                    "%" + searchDto.getDepartment().toLowerCase() + "%"));
            }

            // 분야
            if (searchDto.getField() != null && !searchDto.getField().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("field")),
                    "%" + searchDto.getField().toLowerCase() + "%"));
            }

            // 원격 근무 가능 여부
            if (searchDto.getIsRemotePossible() != null) {
                predicates.add(criteriaBuilder.equal(root.get("isRemotePossible"), searchDto.getIsRemotePossible()));
            }

            // 필요 기술
            if (searchDto.getRequiredSkills() != null && !searchDto.getRequiredSkills().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("requiredSkills")),
                    "%" + searchDto.getRequiredSkills().toLowerCase() + "%"));
            }

            // 기업 유저 ID 목록
            if (searchDto.getCompanyUserIds() != null && !searchDto.getCompanyUserIds().isEmpty()) {
                predicates.add(root.get("companyUser").get("id").in(searchDto.getCompanyUserIds()));
            }

            // 상태
            if (searchDto.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), searchDto.getStatus()));
            } else {
                // 기본적으로 게시된 공고만 조회
                predicates.add(criteriaBuilder.equal(root.get("status"), JobStatus.PUBLISHED));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<JobPosting> isPublished() {
        return (root, query, criteriaBuilder) ->
            criteriaBuilder.equal(root.get("status"), JobStatus.PUBLISHED);
    }

    public static Specification<JobPosting> hasKeyword(String keyword) {
        return (root, query, criteriaBuilder) -> {
            if (keyword == null || keyword.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }

            String searchKeyword = "%" + keyword.toLowerCase() + "%";
            return criteriaBuilder.or(
                criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), searchKeyword),
                criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), searchKeyword),
                criteriaBuilder.like(criteriaBuilder.lower(root.get("companyName")), searchKeyword)
            );
        };
    }

    public static Specification<JobPosting> hasLocation(String location) {
        return (root, query, criteriaBuilder) -> {
            if (location == null || location.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(
                criteriaBuilder.lower(root.get("location")),
                "%" + location.toLowerCase() + "%"
            );
        };
    }

    public static Specification<JobPosting> hasJobType(JobType jobType) {
        return (root, query, criteriaBuilder) -> {
            if (jobType == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("jobType"), jobType);
        };
    }

    public static Specification<JobPosting> hasExperienceLevel(ExperienceLevel experienceLevel) {
        return (root, query, criteriaBuilder) -> {
            if (experienceLevel == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("experienceLevel"), experienceLevel);
        };
    }
}