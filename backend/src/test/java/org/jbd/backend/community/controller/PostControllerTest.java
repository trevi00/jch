package org.jbd.backend.community.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jbd.backend.community.dto.PostDto;
import org.jbd.backend.community.service.PostService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Post Controller 테스트")
class PostControllerTest {

    @Mock
    private PostService postService;

    @InjectMocks
    private PostController postController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(postController)
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("게시글을 성공적으로 생성할 수 있다")
    void 게시글을_성공적으로_생성할_수_있다() throws Exception {
        // given
        PostDto.CreateRequest request = new PostDto.CreateRequest(
                "테스트 게시글",
                "테스트 내용",
                1L
        );

        PostDto.Response expectedResponse = createMockResponse(
                1L, "테스트 게시글", "테스트 내용", "테스트사용자", "Java", 0L, 0L
        );

        given(postService.createPost(any(PostDto.CreateRequest.class), eq("test@example.com")))
                .willReturn(expectedResponse);

        // when & then
        mockMvc.perform(post("/api/posts")
                        .header("X-User-Email", "test@example.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("테스트 게시글"))
                .andExpect(jsonPath("$.content").value("테스트 내용"))
                .andExpect(jsonPath("$.authorName").value("테스트사용자"))
                .andExpect(jsonPath("$.categoryName").value("Java"));
    }

    @Test
    @DisplayName("ID로 게시글을 성공적으로 조회할 수 있다")
    void ID로_게시글을_성공적으로_조회할_수_있다() throws Exception {
        // given
        Long postId = 1L;
        PostDto.Response expectedResponse = createMockResponse(
                postId, "테스트 게시글", "테스트 내용", "테스트사용자", "Java", 10L, 5L
        );

        given(postService.getPostById(postId)).willReturn(expectedResponse);

        // when & then
        mockMvc.perform(get("/api/posts/{id}", postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(postId))
                .andExpect(jsonPath("$.title").value("테스트 게시글"))
                .andExpect(jsonPath("$.viewCount").value(10))
                .andExpect(jsonPath("$.likeCount").value(5));
    }

    @Test
    @DisplayName("카테고리별 게시글 목록을 성공적으로 조회할 수 있다")
    void 카테고리별_게시글_목록을_성공적으로_조회할_수_있다() throws Exception {
        // given
        Long categoryId = 1L;
        PostDto.PageResponse expectedPageResponse = createMockPageResponse();

        given(postService.getPostsByCategory(eq(categoryId), any(Pageable.class)))
                .willReturn(expectedPageResponse);

        // when & then
        mockMvc.perform(get("/api/posts/category/{categoryId}", categoryId)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posts").isArray())
                .andExpect(jsonPath("$.posts.length()").value(2))
                .andExpect(jsonPath("$.posts[0].title").value("게시글1"))
                .andExpect(jsonPath("$.posts[1].title").value("게시글2"));
    }

    @Test
    @DisplayName("제목으로 게시글을 검색할 수 있다")
    void 제목으로_게시글을_검색할_수_있다() throws Exception {
        // given
        String keyword = "Java";
        PostDto.PageResponse expectedPageResponse = createMockSearchPageResponse();

        given(postService.searchPostsByTitle(eq(keyword), any(Pageable.class)))
                .willReturn(expectedPageResponse);

        // when & then
        mockMvc.perform(get("/api/posts/search")
                        .param("keyword", keyword)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posts").isArray())
                .andExpect(jsonPath("$.posts.length()").value(1))
                .andExpect(jsonPath("$.posts[0].title").value("Java 기초"));
    }

    @Test
    @DisplayName("게시글을 성공적으로 수정할 수 있다")
    void 게시글을_성공적으로_수정할_수_있다() throws Exception {
        // given
        Long postId = 1L;
        PostDto.UpdateRequest request = new PostDto.UpdateRequest(
                "수정된 제목",
                "수정된 내용"
        );

        PostDto.Response expectedResponse = createMockResponse(
                postId, "수정된 제목", "수정된 내용", "테스트사용자", "Java", 10L, 5L
        );

        given(postService.updatePost(eq(postId), any(PostDto.UpdateRequest.class), eq("test@example.com")))
                .willReturn(expectedResponse);

        // when & then
        mockMvc.perform(put("/api/posts/{id}", postId)
                        .header("X-User-Email", "test@example.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("수정된 제목"))
                .andExpect(jsonPath("$.content").value("수정된 내용"));
    }

    @Test
    @DisplayName("게시글을 성공적으로 삭제할 수 있다")
    void 게시글을_성공적으로_삭제할_수_있다() throws Exception {
        // given
        Long postId = 1L;
        doNothing().when(postService).deletePost(postId, "test@example.com");

        // when & then
        mockMvc.perform(delete("/api/posts/{id}", postId)
                        .header("X-User-Email", "test@example.com"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("게시글 조회수를 증가시킬 수 있다")
    void 게시글_조회수를_증가시킬_수_있다() throws Exception {
        // given
        Long postId = 1L;
        PostDto.Response expectedResponse = createMockResponse(
                postId, "테스트 게시글", "테스트 내용", "테스트사용자", "Java", 11L, 5L
        );

        given(postService.incrementViewCount(postId)).willReturn(expectedResponse);

        // when & then
        mockMvc.perform(patch("/api/posts/{id}/view", postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.viewCount").value(11));
    }

    // Helper methods for creating mock data
    private PostDto.Response createMockResponse(Long id, String title, String content, String authorName, String categoryName, Long viewCount, Long likeCount) {
        PostDto.Response response = new PostDto.Response();
        response.setId(id);
        response.setTitle(title);
        response.setContent(content);
        response.setAuthorName(authorName);
        response.setCategoryName(categoryName);
        response.setViewCount(viewCount);
        response.setLikeCount(likeCount);
        response.setNoticePost(false);
        response.setPinned(false);
        response.setCreatedAt(LocalDateTime.now());
        response.setUpdatedAt(LocalDateTime.now());
        return response;
    }

    private PostDto.PageResponse createMockPageResponse() {
        PostDto.PageResponse pageResponse = new PostDto.PageResponse();
        List<PostDto.Response> posts = Arrays.asList(
                createMockResponse(1L, "게시글1", "내용1", "사용자1", "Java", 10L, 2L),
                createMockResponse(2L, "게시글2", "내용2", "사용자2", "Java", 5L, 1L)
        );
        pageResponse.setPosts(posts);
        pageResponse.setPageNumber(0);
        pageResponse.setPageSize(10);
        pageResponse.setTotalElements(2);
        pageResponse.setTotalPages(1);
        pageResponse.setFirst(true);
        pageResponse.setLast(true);
        return pageResponse;
    }

    private PostDto.PageResponse createMockSearchPageResponse() {
        PostDto.PageResponse pageResponse = new PostDto.PageResponse();
        List<PostDto.Response> posts = Arrays.asList(
                createMockResponse(1L, "Java 기초", "Java 기초 내용", "사용자1", "Java", 20L, 3L)
        );
        pageResponse.setPosts(posts);
        pageResponse.setPageNumber(0);
        pageResponse.setPageSize(10);
        pageResponse.setTotalElements(1);
        pageResponse.setTotalPages(1);
        pageResponse.setFirst(true);
        pageResponse.setLast(true);
        return pageResponse;
    }
}