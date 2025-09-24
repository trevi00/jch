package org.jbd.backend.community.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jbd.backend.community.dto.CommentDto;
import org.jbd.backend.community.service.CommentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
@DisplayName("Comment Controller 테스트")
class CommentControllerTest {

    @Mock
    private CommentService commentService;

    @InjectMocks
    private CommentController commentController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(commentController)
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("댓글을 성공적으로 생성할 수 있다")
    void 댓글을_성공적으로_생성할_수_있다() throws Exception {
        // given
        CommentDto.CreateRequest request = new CommentDto.CreateRequest(
                "테스트 댓글 내용",
                1L,
                null
        );

        CommentDto.Response expectedResponse = createMockResponse(
                1L, "테스트 댓글 내용", "테스트사용자", 1L, null, 0L
        );

        given(commentService.createComment(any(CommentDto.CreateRequest.class), eq("test@example.com")))
                .willReturn(expectedResponse);

        // when & then
        mockMvc.perform(post("/api/comments")
                        .header("X-User-Email", "test@example.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value("테스트 댓글 내용"))
                .andExpect(jsonPath("$.authorName").value("테스트사용자"))
                .andExpect(jsonPath("$.postId").value(1));
    }

    @Test
    @DisplayName("대댓글을 성공적으로 생성할 수 있다")
    void 대댓글을_성공적으로_생성할_수_있다() throws Exception {
        // given
        CommentDto.CreateRequest request = new CommentDto.CreateRequest(
                "테스트 대댓글 내용",
                1L,
                1L
        );

        CommentDto.Response expectedResponse = createMockResponse(
                2L, "테스트 대댓글 내용", "테스트사용자", 1L, 1L, 0L
        );

        given(commentService.createComment(any(CommentDto.CreateRequest.class), eq("test@example.com")))
                .willReturn(expectedResponse);

        // when & then
        mockMvc.perform(post("/api/comments")
                        .header("X-User-Email", "test@example.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value("테스트 대댓글 내용"))
                .andExpect(jsonPath("$.parentCommentId").value(1));
    }

    @Test
    @DisplayName("ID로 댓글을 성공적으로 조회할 수 있다")
    void ID로_댓글을_성공적으로_조회할_수_있다() throws Exception {
        // given
        Long commentId = 1L;
        CommentDto.Response expectedResponse = createMockResponse(
                commentId, "테스트 댓글", "테스트사용자", 1L, null, 5L
        );

        given(commentService.getCommentById(commentId)).willReturn(expectedResponse);

        // when & then
        mockMvc.perform(get("/api/comments/{id}", commentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(commentId))
                .andExpect(jsonPath("$.content").value("테스트 댓글"))
                .andExpect(jsonPath("$.likeCount").value(5));
    }

    @Test
    @DisplayName("게시글별 댓글 목록을 성공적으로 조회할 수 있다")
    void 게시글별_댓글_목록을_성공적으로_조회할_수_있다() throws Exception {
        // given
        Long postId = 1L;
        List<CommentDto.Response> expectedComments = Arrays.asList(
                createMockResponse(1L, "첫 번째 댓글", "사용자1", postId, null, 2L),
                createMockResponse(2L, "두 번째 댓글", "사용자2", postId, null, 1L)
        );

        given(commentService.getCommentsByPost(postId)).willReturn(expectedComments);

        // when & then
        mockMvc.perform(get("/api/comments/post/{postId}", postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].content").value("첫 번째 댓글"))
                .andExpect(jsonPath("$[1].content").value("두 번째 댓글"));
    }

    @Test
    @DisplayName("작성자별 댓글 목록을 성공적으로 조회할 수 있다")
    void 작성자별_댓글_목록을_성공적으로_조회할_수_있다() throws Exception {
        // given
        String authorEmail = "test@example.com";
        List<CommentDto.Response> expectedComments = Arrays.asList(
                createMockResponse(1L, "내 댓글1", "테스트사용자", 1L, null, 3L),
                createMockResponse(2L, "내 댓글2", "테스트사용자", 2L, null, 1L)
        );

        given(commentService.getCommentsByAuthor(authorEmail)).willReturn(expectedComments);

        // when & then
        mockMvc.perform(get("/api/comments/author")
                        .header("X-User-Email", authorEmail))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].authorName").value("테스트사용자"))
                .andExpect(jsonPath("$[1].authorName").value("테스트사용자"));
    }

    @Test
    @DisplayName("댓글을 성공적으로 수정할 수 있다")
    void 댓글을_성공적으로_수정할_수_있다() throws Exception {
        // given
        Long commentId = 1L;
        CommentDto.UpdateRequest request = new CommentDto.UpdateRequest("수정된 댓글 내용");

        CommentDto.Response expectedResponse = createMockResponse(
                commentId, "수정된 댓글 내용", "테스트사용자", 1L, null, 5L
        );

        given(commentService.updateComment(eq(commentId), any(CommentDto.UpdateRequest.class), eq("test@example.com")))
                .willReturn(expectedResponse);

        // when & then
        mockMvc.perform(put("/api/comments/{id}", commentId)
                        .header("X-User-Email", "test@example.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("수정된 댓글 내용"));
    }

    @Test
    @DisplayName("댓글을 성공적으로 삭제할 수 있다")
    void 댓글을_성공적으로_삭제할_수_있다() throws Exception {
        // given
        Long commentId = 1L;
        doNothing().when(commentService).deleteComment(commentId, "test@example.com");

        // when & then
        mockMvc.perform(delete("/api/comments/{id}", commentId)
                        .header("X-User-Email", "test@example.com"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("부모 댓글의 대댓글 목록을 조회할 수 있다")
    void 부모_댓글의_대댓글_목록을_조회할_수_있다() throws Exception {
        // given
        Long parentCommentId = 1L;
        List<CommentDto.Response> expectedReplies = Arrays.asList(
                createMockResponse(2L, "대댓글1", "사용자1", 1L, parentCommentId, 1L),
                createMockResponse(3L, "대댓글2", "사용자2", 1L, parentCommentId, 0L)
        );

        given(commentService.getRepliesByParentComment(parentCommentId)).willReturn(expectedReplies);

        // when & then
        mockMvc.perform(get("/api/comments/parent/{parentCommentId}/replies", parentCommentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].parentCommentId").value(parentCommentId))
                .andExpect(jsonPath("$[1].parentCommentId").value(parentCommentId));
    }

    @Test
    @DisplayName("게시글의 댓글 수를 조회할 수 있다")
    void 게시글의_댓글_수를_조회할_수_있다() throws Exception {
        // given
        Long postId = 1L;
        long expectedCount = 15L;

        given(commentService.getCommentCountByPost(postId)).willReturn(expectedCount);

        // when & then
        mockMvc.perform(get("/api/comments/post/{postId}/count", postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(expectedCount));
    }

    // Helper method for creating mock comment responses
    private CommentDto.Response createMockResponse(Long id, String content, String authorName, Long postId, Long parentCommentId, Long likeCount) {
        CommentDto.Response response = new CommentDto.Response();
        response.setId(id);
        response.setContent(content);
        response.setAuthorName(authorName);
        response.setPostId(postId);
        response.setParentCommentId(parentCommentId);
        response.setLikeCount(likeCount);
        response.setCreatedAt(LocalDateTime.now());
        response.setUpdatedAt(LocalDateTime.now());
        return response;
    }
}