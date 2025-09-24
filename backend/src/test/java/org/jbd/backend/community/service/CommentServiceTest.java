package org.jbd.backend.community.service;

import org.jbd.backend.community.domain.Category;
import org.jbd.backend.community.domain.Comment;
import org.jbd.backend.community.domain.Post;
import org.jbd.backend.community.dto.CommentDto;
import org.jbd.backend.community.repository.CommentRepository;
import org.jbd.backend.community.repository.PostRepository;
import org.jbd.backend.community.service.impl.CommentServiceImpl;
import org.jbd.backend.common.exception.ResourceNotFoundException;
import org.jbd.backend.user.domain.User;
import org.jbd.backend.user.domain.enums.UserType;
import org.jbd.backend.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("CommentService 테스트")
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    private CommentService commentService;

    @BeforeEach
    void setUp() {
        commentService = new CommentServiceImpl(commentRepository, postRepository, userRepository);
    }

    @Test
    @DisplayName("댓글을 성공적으로 생성한다")
    void 댓글을_성공적으로_생성한다() {
        // given
        String authorEmail = "test@example.com";
        CommentDto.CreateRequest request = new CommentDto.CreateRequest(
                "테스트 댓글", 1L, null
        );

        User author = new User(authorEmail, "password", "테스트사용자", UserType.GENERAL);
        Category category = new Category("Java", "Java 프로그래밍");
        Post post = new Post("테스트 게시글", "테스트 내용", author, category);
        Comment savedComment = new Comment("테스트 댓글", author, post);

        given(userRepository.findByEmail(authorEmail)).willReturn(Optional.of(author));
        given(postRepository.findById(1L)).willReturn(Optional.of(post));
        given(commentRepository.save(any(Comment.class))).willReturn(savedComment);

        // when
        CommentDto.Response response = commentService.createComment(request, authorEmail);

        // then
        assertThat(response.getContent()).isEqualTo("테스트 댓글");
        assertThat(response.getAuthorName()).isEqualTo("테스트사용자");
        assertThat(response.getPostId()).isEqualTo(post.getId());
        assertThat(response.getParentCommentId()).isNull();
        verify(commentRepository, times(1)).save(any(Comment.class));
    }

    @Test
    @DisplayName("대댓글을 성공적으로 생성한다")
    void 대댓글을_성공적으로_생성한다() {
        // given
        String authorEmail = "test@example.com";
        CommentDto.CreateRequest request = new CommentDto.CreateRequest(
                "테스트 대댓글", 1L, 1L
        );

        User author = new User(authorEmail, "password", "테스트사용자", UserType.GENERAL);
        Category category = new Category("Java", "Java 프로그래밍");
        Post post = new Post("테스트 게시글", "테스트 내용", author, category);
        Comment parentComment = new Comment("부모 댓글", author, post);
        Comment savedComment = new Comment("테스트 대댓글", author, post, parentComment);

        given(userRepository.findByEmail(authorEmail)).willReturn(Optional.of(author));
        given(postRepository.findById(1L)).willReturn(Optional.of(post));
        given(commentRepository.findById(1L)).willReturn(Optional.of(parentComment));
        given(commentRepository.save(any(Comment.class))).willReturn(savedComment);

        // when
        CommentDto.Response response = commentService.createComment(request, authorEmail);

        // then
        assertThat(response.getContent()).isEqualTo("테스트 대댓글");
        assertThat(response.getParentCommentId()).isEqualTo(parentComment.getId());
        verify(commentRepository, times(1)).save(any(Comment.class));
    }

    @Test
    @DisplayName("ID로 댓글을 조회한다")
    void ID로_댓글을_조회한다() {
        // given
        Long commentId = 1L;
        User author = new User("test@example.com", "password", "테스트사용자", UserType.GENERAL);
        Category category = new Category("Java", "Java 프로그래밍");
        Post post = new Post("테스트 게시글", "테스트 내용", author, category);
        Comment comment = new Comment("테스트 댓글", author, post);

        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));

        // when
        CommentDto.Response response = commentService.getCommentById(commentId);

        // then
        assertThat(response.getContent()).isEqualTo("테스트 댓글");
        assertThat(response.getAuthorName()).isEqualTo("테스트사용자");
        verify(commentRepository, times(1)).findById(commentId);
    }

    @Test
    @DisplayName("존재하지 않는 ID로 댓글 조회시 예외가 발생한다")
    void 존재하지_않는_ID로_댓글_조회시_예외가_발생한다() {
        // given
        Long commentId = 1L;
        given(commentRepository.findById(commentId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> commentService.getCommentById(commentId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("게시글별 댓글 목록을 조회한다")
    void 게시글별_댓글_목록을_조회한다() {
        // given
        Long postId = 1L;
        User author = new User("test@example.com", "password", "테스트사용자", UserType.GENERAL);
        Category category = new Category("Java", "Java 프로그래밍");
        Post post = new Post("테스트 게시글", "테스트 내용", author, category);
        
        Comment comment1 = new Comment("댓글1", author, post);
        Comment comment2 = new Comment("댓글2", author, post);
        
        given(postRepository.findById(postId)).willReturn(Optional.of(post));
        given(commentRepository.findByPostOrderByCreatedAtAsc(post))
                .willReturn(Arrays.asList(comment1, comment2));

        // when
        List<CommentDto.Response> responses = commentService.getCommentsByPost(postId);

        // then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getContent()).isEqualTo("댓글1");
        assertThat(responses.get(1).getContent()).isEqualTo("댓글2");
        verify(commentRepository, times(1)).findByPostOrderByCreatedAtAsc(post);
    }

    @Test
    @DisplayName("댓글을 업데이트한다")
    void 댓글을_업데이트한다() {
        // given
        Long commentId = 1L;
        String authorEmail = "test@example.com";
        CommentDto.UpdateRequest request = new CommentDto.UpdateRequest("수정된 댓글");

        User author = new User(authorEmail, "password", "테스트사용자", UserType.GENERAL);
        Category category = new Category("Java", "Java 프로그래밍");
        Post post = new Post("테스트 게시글", "테스트 내용", author, category);
        Comment comment = new Comment("원본 댓글", author, post);

        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));
        given(userRepository.findByEmail(authorEmail)).willReturn(Optional.of(author));
        given(commentRepository.save(comment)).willReturn(comment);

        // when
        CommentDto.Response response = commentService.updateComment(commentId, request, authorEmail);

        // then
        assertThat(response.getContent()).isEqualTo("수정된 댓글");
        verify(commentRepository, times(1)).save(comment);
    }

    @Test
    @DisplayName("댓글을 삭제한다")
    void 댓글을_삭제한다() {
        // given
        Long commentId = 1L;
        String authorEmail = "test@example.com";

        User author = new User(authorEmail, "password", "테스트사용자", UserType.GENERAL);
        Category category = new Category("Java", "Java 프로그래밍");
        Post post = new Post("테스트 게시글", "테스트 내용", author, category);
        Comment comment = new Comment("테스트 댓글", author, post);

        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));
        given(userRepository.findByEmail(authorEmail)).willReturn(Optional.of(author));

        // when
        commentService.deleteComment(commentId, authorEmail);

        // then
        verify(commentRepository, times(1)).findById(commentId);
        verify(commentRepository, times(1)).save(comment);
    }

    @Test
    @DisplayName("게시글의 댓글 수를 조회한다")
    void 게시글의_댓글_수를_조회한다() {
        // given
        Long postId = 1L;
        User author = new User("test@example.com", "password", "테스트사용자", UserType.GENERAL);
        Category category = new Category("Java", "Java 프로그래밍");
        Post post = new Post("테스트 게시글", "테스트 내용", author, category);

        given(postRepository.findById(postId)).willReturn(Optional.of(post));
        given(commentRepository.countByPost(post)).willReturn(5L);

        // when
        long count = commentService.getCommentCountByPost(postId);

        // then
        assertThat(count).isEqualTo(5L);
        verify(commentRepository, times(1)).countByPost(post);
    }
}