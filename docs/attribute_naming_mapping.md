# 속성명 매핑 문서 (Attribute Naming Mapping)

## 변경 원칙
1. **Primary Key**: `id` → `{table_name}_id` (예: `user_id`, `post_id`)
2. **Foreign Key**: `{entity}Id` → `{entity}_id` (예: `userId` → `user_id`)
3. **날짜/시간**: camelCase → snake_case (예: `createdAt` → `created_at`)
4. **Boolean**: `is_` 또는 `has_` 접두사 유지
5. **Enum**: 대문자 snake_case 유지

## 테이블별 매핑

### 1. users 테이블
| 현재 속성명 | 변경 속성명 | 비고 |
|------------|------------|------|
| id | user_id | Primary Key |
| userType | user_type | Enum |
| emailVerified | email_verified | Boolean |
| companyEmailVerified | company_email_verified | Boolean |
| oauthId | oauth_id | |
| oauthProvider | oauth_provider | |
| profileImageUrl | profile_image_url | |
| birthDate | birth_date | |
| websiteUrl | website_url | |
| githubUrl | github_url | |
| linkedinUrl | linkedin_url | |
| currentJobTitle | current_job_title | |
| careerSummary | career_summary | |
| desiredSalaryMin | desired_salary_min | |
| desiredSalaryMax | desired_salary_max | |
| desiredJobType | desired_job_type | |
| desiredLocation | desired_location | |
| workExperienceYears | work_experience_years | |
| notificationEnabled | notification_enabled | |
| emailNotificationEnabled | email_notification_enabled | |
| smsNotificationEnabled | sms_notification_enabled | |
| jobAlertEnabled | job_alert_enabled | |
| marketingConsent | marketing_consent | |
| dataProcessingConsent | data_processing_consent | |
| thirdPartySharingConsent | third_party_sharing_consent | |
| lastLoginAt | last_login_at | |
| lastJobSearchAt | last_job_search_at | |
| profileCompletionRate | profile_completion_rate | |
| jobApplicationCount | job_application_count | |
| jobViewCount | job_view_count | |
| createdAt | created_at | |
| updatedAt | updated_at | |
| deletedAt | deleted_at | |
| isDeleted | is_deleted | |

### 2. companies 테이블
| 현재 속성명 | 변경 속성명 | 비고 |
|------------|------------|------|
| id | company_id | Primary Key |
| userId | user_id | Foreign Key |
| companyName | company_name | |
| businessNumber | business_number | |
| companySize | company_size | |
| establishmentDate | establishment_date | |
| companyDescription | company_description | |
| companyWebsite | company_website | |
| companyAddress | company_address | |
| companyLogoUrl | company_logo_url | |
| employeeCount | employee_count | |
| annualRevenue | annual_revenue | |
| companyCulture | company_culture | |
| createdAt | created_at | |
| updatedAt | updated_at | |
| deletedAt | deleted_at | |
| isDeleted | is_deleted | |

### 3. job_postings 테이블
| 현재 속성명 | 변경 속성명 | 비고 |
|------------|------------|------|
| id | job_id | Primary Key |
| companyUserId | company_user_id | Foreign Key |
| companyName | company_name | |
| jobType | job_type | |
| experienceLevel | experience_level | |
| requiredSkills | required_skills | |
| preferredSkills | preferred_skills | |
| minSalary | min_salary | |
| maxSalary | max_salary | |
| salaryType | salary_type | |
| applicationDeadline | application_deadline | |
| startDate | start_date | |
| viewCount | view_count | |
| applicationCount | application_count | |
| createdAt | created_at | |
| updatedAt | updated_at | |
| deletedAt | deleted_at | |
| isDeleted | is_deleted | |

### 4. job_applications 테이블
| 현재 속성명 | 변경 속성명 | 비고 |
|------------|------------|------|
| id | application_id | Primary Key |
| jobPostingId | job_posting_id | Foreign Key |
| applicantUserId | applicant_user_id | Foreign Key |
| coverLetter | cover_letter | |
| resumeUrl | resume_url | |
| expectedSalary | expected_salary | |
| availableStartDate | available_start_date | |
| createdAt | created_at | |
| updatedAt | updated_at | |
| deletedAt | deleted_at | |
| isDeleted | is_deleted | |

### 5. posts 테이블
| 현재 속성명 | 변경 속성명 | 비고 |
|------------|------------|------|
| id | post_id | Primary Key |
| userId | user_id | Foreign Key |
| categoryId | category_id | Foreign Key |
| imageUrl | image_url | |
| isPublished | is_published | |
| viewCount | view_count | |
| likeCount | like_count | |
| commentCount | comment_count | |
| sentimentScore | sentiment_score | |
| sentimentLabel | sentiment_label | |
| createdAt | created_at | |
| updatedAt | updated_at | |
| deletedAt | deleted_at | |
| isDeleted | is_deleted | |

### 6. comments 테이블
| 현재 속성명 | 변경 속성명 | 비고 |
|------------|------------|------|
| id | comment_id | Primary Key |
| postId | post_id | Foreign Key |
| userId | user_id | Foreign Key |
| parentCommentId | parent_comment_id | Foreign Key |
| likeCount | like_count | |
| createdAt | created_at | |
| updatedAt | updated_at | |
| deletedAt | deleted_at | |
| isDeleted | is_deleted | |

### 7. categories 테이블
| 현재 속성명 | 변경 속성명 | 비고 |
|------------|------------|------|
| id | category_id | Primary Key |
| displayOrder | display_order | |
| isActive | is_active | |
| createdAt | created_at | |
| updatedAt | updated_at | |

### 8. interviews 테이블
| 현재 속성명 | 변경 속성명 | 비고 |
|------------|------------|------|
| id | interview_id | Primary Key |
| userId | user_id | String type |
| sessionId | session_id | |
| experienceLevel | experience_level | |
| interviewType | interview_type | |
| totalScore | total_score | |
| startTime | start_time | |
| endTime | end_time | |
| durationMinutes | duration_minutes | |
| questionCount | question_count | |
| answeredCount | answered_count | |
| createdAt | created_at | |
| updatedAt | updated_at | |

### 9. interview_questions 테이블
| 현재 속성명 | 변경 속성명 | 비고 |
|------------|------------|------|
| id | question_id | Primary Key |
| interviewId | interview_id | Foreign Key |
| questionText | question_text | |
| questionType | question_type | |
| questionOrder | question_order | |
| userAnswer | user_answer | |
| aiFeedback | ai_feedback | |
| timeLimitSeconds | time_limit_seconds | |
| actualTimeSeconds | actual_time_seconds | |
| createdAt | created_at | |
| updatedAt | updated_at | |

## Java Entity 적용 전략

### @Column 어노테이션 사용
```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(name = "user_type")
    @Enumerated(EnumType.STRING)
    private UserType userType;

    @Column(name = "email_verified")
    private Boolean emailVerified;

    // ... 이하 동일 패턴
}
```

### JSON 직렬화 설정
```java
// application.yml
spring:
  jackson:
    property-naming-strategy: SNAKE_CASE  # API 응답도 snake_case로
    # 또는 CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES
```

또는 DTO에서 @JsonProperty 사용:
```java
public class UserDTO {
    @JsonProperty("user_id")
    private Long id;

    @JsonProperty("user_type")
    private String userType;
}
```

## 작업 순서
1. ✅ 매핑 문서 작성 (현재 문서)
2. ⏳ database_schema.sql 파일 업데이트
3. ⏳ Java Entity 클래스 @Column 어노테이션 추가
4. ⏳ Repository 메서드명 확인 (필요시 @Query 추가)
5. ⏳ Service 레이어 확인
6. ⏳ Controller/DTO 확인
7. ⏳ 테스트 실행 및 검증

## 주의사항
- H2 데이터베이스는 대소문자 구분 없음 (개발 환경)
- MySQL은 대소문자 구분 가능 (운영 환경)
- JPA가 자동 생성하는 쿼리 확인 필요
- 기존 데이터 마이그레이션 계획 필요 (운영 환경)