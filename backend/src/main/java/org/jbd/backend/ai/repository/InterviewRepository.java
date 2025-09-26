package org.jbd.backend.ai.repository;

import org.jbd.backend.ai.domain.Interview;
import org.jbd.backend.ai.domain.InterviewStatus;
import org.jbd.backend.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InterviewRepository extends JpaRepository<Interview, Long> {
    
    // 사용자별 면접 목록 조회 (페이징)
    Page<Interview> findByUserAndIsDeletedFalseOrderByCreatedAtDesc(User user, Pageable pageable);

    // 사용자별 면접 목록 조회 with JOIN FETCH (Lazy loading 이슈 해결)
    @Query("SELECT i FROM Interview i LEFT JOIN FETCH i.questions LEFT JOIN FETCH i.user WHERE i.user = :user AND i.isDeleted = false ORDER BY i.createdAt DESC")
    Page<Interview> findByUserWithQuestionsAndIsDeletedFalse(@Param("user") User user, Pageable pageable);
    
    // 사용자별 완료된 면접 목록 조회
    Page<Interview> findByUserAndStatusAndIsDeletedFalseOrderByCompletedAtDesc(
        User user, InterviewStatus status, Pageable pageable);
    
    // 사용자별 진행중인 면접 조회
    Optional<Interview> findByUserAndStatusAndIsDeletedFalse(User user, InterviewStatus status);
    
    // 사용자의 총 면접 횟수
    long countByUserAndStatusAndIsDeletedFalse(User user, InterviewStatus status);
    
    // 사용자의 평균 점수
    @Query("SELECT AVG(i.overallScore) FROM Interview i WHERE i.user = :user AND i.status = :status AND i.isDeleted = false")
    Double findAverageScoreByUserAndStatus(@Param("user") User user, @Param("status") InterviewStatus status);
    
    // 최근 면접 기록 조회
    List<Interview> findTop5ByUserAndStatusAndIsDeletedFalseOrderByCompletedAtDesc(
        User user, InterviewStatus status);
    
    // 특정 기간 내 면접 기록 조회
    @Query("SELECT i FROM Interview i WHERE i.user = :user AND i.completedAt BETWEEN :startDate AND :endDate AND i.isDeleted = false ORDER BY i.completedAt DESC")
    List<Interview> findByUserAndCompletedAtBetween(
        @Param("user") User user, 
        @Param("startDate") LocalDateTime startDate, 
        @Param("endDate") LocalDateTime endDate);
    
    // 면접 유형별 통계
    @Query("SELECT i.interviewType, COUNT(i), AVG(i.overallScore) FROM Interview i WHERE i.user = :user AND i.status = :status AND i.isDeleted = false GROUP BY i.interviewType")
    List<Object[]> findInterviewStatsByUserAndStatus(@Param("user") User user, @Param("status") InterviewStatus status);

    // 관리자 대시보드용 전체 면접 통계
    @Query("SELECT COUNT(i) FROM Interview i WHERE i.isDeleted = false")
    Long countTotalInterviews();

    @Query("SELECT COUNT(i) FROM Interview i WHERE i.status = :status AND i.isDeleted = false")
    Long countInterviewsByStatus(@Param("status") InterviewStatus status);

    @Query("SELECT AVG(i.overallScore) FROM Interview i WHERE i.status = :status AND i.isDeleted = false")
    Double findAverageScoreByStatus(@Param("status") InterviewStatus status);
}