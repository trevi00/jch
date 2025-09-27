package org.jbd.backend.community.service;

import org.jbd.backend.community.dto.PostDto;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PostService {
    
    PostDto.PageResponse getAllPosts(Pageable pageable);
    
    PostDto.Response createPost(PostDto.CreateRequest request, String authorEmail);
    
    PostDto.Response getPostById(Long id);
    
    PostDto.PageResponse getPostsByCategory(Long categoryId, Pageable pageable);
    
    PostDto.PageResponse searchPostsByTitle(String keyword, Pageable pageable);
    
    PostDto.PageResponse getPostsByAuthor(String authorEmail, Pageable pageable);
    
    PostDto.Response updatePost(Long id, PostDto.UpdateRequest request, String authorEmail);
    
    void deletePost(Long id, String authorEmail);
    
    PostDto.Response incrementViewCount(Long id);
    
    List<PostDto.Response> getPopularPosts(Long categoryId, int limit);
    
    PostDto.PageResponse getPostsBySentiment(String sentimentLabel, Pageable pageable);
    
    PostDto.PageResponse getPostsByCategoryAndSentiment(Long categoryId, String sentimentLabel, Pageable pageable);
    
    PostDto.PageResponse getPostsBySentimentScore(Double minScore, Double maxScore, Pageable pageable);
    
    PostDto.PageResponse getPostsWithImages(Pageable pageable);

    // ===== 관리자용 메서드 =====
    void deletePostAsAdmin(Long id);
}