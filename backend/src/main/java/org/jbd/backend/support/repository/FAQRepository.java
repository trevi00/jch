package org.jbd.backend.support.repository;

import org.jbd.backend.support.domain.FAQ;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FAQRepository extends JpaRepository<FAQ, Long> {

    // 활성화된 FAQ 조회 (카테고리별, 표시 순서대로)
    List<FAQ> findByIsActiveTrueOrderByDisplayOrderAscCreatedAtAsc();

    // 카테고리별 활성화된 FAQ 조회
    List<FAQ> findByCategoryAndIsActiveTrueOrderByDisplayOrderAscCreatedAtAsc(String category);

    // FAQ 조회수 증가
    @Modifying
    @Query("UPDATE FAQ f SET f.viewCount = f.viewCount + 1 WHERE f.id = :faqId")
    void incrementViewCount(@Param("faqId") Long faqId);

    // FAQ 도움됨 수 증가
    @Modifying
    @Query("UPDATE FAQ f SET f.helpfulCount = f.helpfulCount + 1 WHERE f.id = :faqId")
    void incrementHelpfulCount(@Param("faqId") Long faqId);

    // 카테고리별 FAQ 개수
    long countByCategoryAndIsActiveTrue(String category);

    // 인기 FAQ 조회 (조회수 기준)
    @Query("SELECT f FROM FAQ f WHERE f.isActive = true ORDER BY f.viewCount DESC, f.displayOrder ASC")
    List<FAQ> findPopularFAQs();

    // 키워드 검색
    @Query("SELECT f FROM FAQ f WHERE f.isActive = true AND " +
           "(LOWER(f.question) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(f.answer) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY f.displayOrder ASC, f.createdAt ASC")
    List<FAQ> searchByKeyword(@Param("keyword") String keyword);

    // 관리자용: 모든 FAQ 조회 (비활성화 포함)
    List<FAQ> findAllByOrderByDisplayOrderAscCreatedAtAsc();
}