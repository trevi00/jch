package org.jbd.backend.community.repository;

import org.jbd.backend.community.domain.Category;
import org.jbd.backend.community.domain.Post;
import org.jbd.backend.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    Page<Post> findByIsDeletedFalseOrderByCreatedAtDesc(Pageable pageable);

    List<Post> findByCategoryAndIsDeletedFalseOrderByCreatedAtDesc(Category category, Pageable pageable);

    List<Post> findByTitleContainingIgnoreCaseAndIsDeletedFalseOrderByCreatedAtDesc(String title, Pageable pageable);

    List<Post> findByAuthorAndIsDeletedFalseOrderByCreatedAtDesc(User author, Pageable pageable);

    List<Post> findByCategoryAndIsDeletedFalseOrderByViewCountDesc(Category category, Pageable pageable);

    @Query("SELECT p FROM Post p " +
           "JOIN FETCH p.category " +
           "JOIN FETCH p.author " +
           "WHERE p.id = :id AND p.isDeleted = false")
    Optional<Post> findByIdWithCategoryAndAuthor(@Param("id") Long id);

    List<Post> findBySentimentLabelAndIsDeletedFalseOrderByCreatedAtDesc(String sentimentLabel, Pageable pageable);

    List<Post> findByCategoryAndSentimentLabelAndIsDeletedFalseOrderByCreatedAtDesc(
            Category category, String sentimentLabel, Pageable pageable);

    List<Post> findBySentimentScoreBetweenAndIsDeletedFalseOrderByCreatedAtDesc(
            Double minScore, Double maxScore, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.imageUrl IS NOT NULL AND p.isDeleted = false ORDER BY p.createdAt DESC")
    List<Post> findPostsWithImagesOrderByCreatedAtDesc(Pageable pageable);

    Long countByCategoryAndIsDeletedFalse(Category category);

    // AI Statistics queries
    @Query("SELECT COUNT(p) FROM Post p WHERE p.isDeleted = false AND p.sentimentLabel IS NOT NULL")
    Long countTotalSentimentAnalyses();

    @Query("SELECT COUNT(p) FROM Post p WHERE p.isDeleted = false AND p.sentimentLabel = 'positive'")
    Long countPositivePosts();

    @Query("SELECT COUNT(p) FROM Post p WHERE p.isDeleted = false AND p.sentimentLabel = 'neutral'")
    Long countNeutralPosts();

    @Query("SELECT COUNT(p) FROM Post p WHERE p.isDeleted = false AND p.sentimentLabel = 'negative'")
    Long countNegativePosts();

    @Query("SELECT COUNT(p) FROM Post p WHERE p.isDeleted = false AND p.imageUrl IS NOT NULL")
    Long countPostsWithImages();
}