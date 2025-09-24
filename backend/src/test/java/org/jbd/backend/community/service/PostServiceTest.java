package org.jbd.backend.community.service;

import org.jbd.backend.ai.client.AIServiceClient;
import org.jbd.backend.community.domain.Category;
import org.jbd.backend.community.domain.Post;
import org.jbd.backend.community.dto.PostDto;
import org.jbd.backend.community.repository.CategoryRepository;
import org.jbd.backend.community.repository.PostRepository;
import org.jbd.backend.community.service.impl.PostServiceImpl;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("PostService 테스트")
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AIServiceClient aiServiceClient;

    private PostService postService;

    @BeforeEach
    void setUp() {
        postService = new PostServiceImpl(postRepository, categoryRepository, userRepository, aiServiceClient);
    }

    @Test
    @DisplayName("게시글을 성공적으로 생성한다")
    void 게시글을_성공적으로_생성한다() {
        // given
        String authorEmail = "test@example.com";
        PostDto.CreateRequest request = new PostDto.CreateRequest(
                "테스트 제목", "테스트 내용", 1L
        );

        User author = new User(authorEmail, "password", "테스트사용자", UserType.GENERAL);
        Category category = new Category("Java", "Java 프로그래밍");
        Post savedPost = new Post("테스트 제목", "테스트 내용", author, category);

        given(userRepository.findByEmail(authorEmail)).willReturn(Optional.of(author));
        given(categoryRepository.findById(1L)).willReturn(Optional.of(category));
        given(postRepository.save(any(Post.class))).willReturn(savedPost);

        // when
        PostDto.Response response = postService.createPost(request, authorEmail);

        // then
        assertThat(response.getTitle()).isEqualTo("테스트 제목");
        assertThat(response.getContent()).isEqualTo("테스트 내용");
        assertThat(response.getCategoryName()).isEqualTo("Java");
        assertThat(response.getAuthorName()).isEqualTo("테스트사용자");
        verify(postRepository, times(1)).save(any(Post.class));
    }

    @Test
    @DisplayName("ID로 게시글을 조회한다")
    void ID로_게시글을_조회한다() {
        // given
        Long postId = 1L;
        User author = new User("test@example.com", "password", "테스트사용자", UserType.GENERAL);
        Category category = new Category("Java", "Java 프로그래밍");
        Post post = new Post("테스트 제목", "테스트 내용", author, category);

        given(postRepository.findByIdWithCategoryAndAuthor(postId)).willReturn(Optional.of(post));

        // when
        PostDto.Response response = postService.getPostById(postId);

        // then
        assertThat(response.getTitle()).isEqualTo("테스트 제목");
        assertThat(response.getViewCount()).isEqualTo(0L);
        verify(postRepository, times(1)).findByIdWithCategoryAndAuthor(postId);
    }

    @Test
    @DisplayName("게시글 조회수를 증가시킨다")
    void 게시글_조회수를_증가시킨다() {
        // given
        Long postId = 1L;
        User author = new User("test@example.com", "password", "테스트사용자", UserType.GENERAL);
        Category category = new Category("Java", "Java 프로그래밍");
        Post post = new Post("테스트 제목", "테스트 내용", author, category);

        given(postRepository.findByIdWithCategoryAndAuthor(postId)).willReturn(Optional.of(post));
        given(postRepository.save(post)).willReturn(post);

        // when
        PostDto.Response response = postService.incrementViewCount(postId);

        // then
        assertThat(response.getViewCount()).isEqualTo(1L);
        verify(postRepository, times(1)).findByIdWithCategoryAndAuthor(postId);
        verify(postRepository, times(1)).save(post);
    }

    @Test
    @DisplayName("존재하지 않는 ID로 게시글 조회시 예외가 발생한다")
    void 존재하지_않는_ID로_게시글_조회시_예외가_발생한다() {
        // given
        Long postId = 1L;
        given(postRepository.findByIdWithCategoryAndAuthor(postId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> postService.getPostById(postId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("카테고리별 게시글을 페이징 조회한다")
    void 카테고리별_게시글을_페이징_조회한다() {
        // given
        Long categoryId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        
        User author = new User("test@example.com", "password", "테스트사용자", UserType.GENERAL);
        Category category = new Category("Java", "Java 프로그래밍");
        
        List<Post> posts = Arrays.asList(
                new Post("제목1", "내용1", author, category),
                new Post("제목2", "내용2", author, category)
        );

        given(categoryRepository.findById(categoryId)).willReturn(Optional.of(category));
        given(postRepository.findByCategoryOrderByCreatedAtDesc(eq(category), eq(pageable)))
                .willReturn(posts);

        // when
        PostDto.PageResponse response = postService.getPostsByCategory(categoryId, pageable);

        // then
        assertThat(response.getPosts()).hasSize(2);
        assertThat(response.getPosts().get(0).getTitle()).isEqualTo("제목1");
        verify(postRepository, times(1)).findByCategoryOrderByCreatedAtDesc(category, pageable);
    }

    @Test
    @DisplayName("게시글을 업데이트한다")
    void 게시글을_업데이트한다() {
        // given
        Long postId = 1L;
        String authorEmail = "test@example.com";
        PostDto.UpdateRequest request = new PostDto.UpdateRequest("수정된 제목", "수정된 내용");

        User author = new User(authorEmail, "password", "테스트사용자", UserType.GENERAL);
        Category category = new Category("Java", "Java 프로그래밍");
        Post post = new Post("원본 제목", "원본 내용", author, category);

        given(postRepository.findByIdWithCategoryAndAuthor(postId)).willReturn(Optional.of(post));
        given(userRepository.findByEmail(authorEmail)).willReturn(Optional.of(author));
        given(postRepository.save(post)).willReturn(post);

        // when
        PostDto.Response response = postService.updatePost(postId, request, authorEmail);

        // then
        assertThat(response.getTitle()).isEqualTo("수정된 제목");
        assertThat(response.getContent()).isEqualTo("수정된 내용");
        verify(postRepository, times(1)).save(post);
    }

    @Test
    @DisplayName("게시글을 삭제한다")
    void 게시글을_삭제한다() {
        // given
        Long postId = 1L;
        String authorEmail = "test@example.com";

        User author = new User(authorEmail, "password", "테스트사용자", UserType.GENERAL);
        Category category = new Category("Java", "Java 프로그래밍");
        Post post = new Post("테스트 제목", "테스트 내용", author, category);

        given(postRepository.findByIdWithCategoryAndAuthor(postId)).willReturn(Optional.of(post));
        given(userRepository.findByEmail(authorEmail)).willReturn(Optional.of(author));

        // when
        postService.deletePost(postId, authorEmail);

        // then
        verify(postRepository, times(1)).findByIdWithCategoryAndAuthor(postId);
        verify(postRepository, times(1)).save(post);
    }

    @Test
    @DisplayName("인기 게시글을 조회한다")
    void 인기_게시글을_조회한다() {
        // given
        Long categoryId = 1L;
        int limit = 5;
        Pageable pageable = PageRequest.of(0, limit);

        User author = new User("test@example.com", "password", "테스트사용자", UserType.GENERAL);
        Category category = new Category("Java", "Java 프로그래밍");
        
        Post popularPost1 = new Post("인기글1", "인기내용1", author, category);
        popularPost1.incrementViewCount();
        popularPost1.incrementViewCount();
        
        Post popularPost2 = new Post("인기글2", "인기내용2", author, category);
        popularPost2.incrementViewCount();

        List<Post> popularPosts = Arrays.asList(popularPost1, popularPost2);

        given(categoryRepository.findById(categoryId)).willReturn(Optional.of(category));
        given(postRepository.findByCategoryOrderByViewCountDesc(category, pageable))
                .willReturn(popularPosts);

        // when
        List<PostDto.Response> responses = postService.getPopularPosts(categoryId, limit);

        // then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getViewCount()).isEqualTo(2L);
        assertThat(responses.get(1).getViewCount()).isEqualTo(1L);
        verify(postRepository, times(1)).findByCategoryOrderByViewCountDesc(category, pageable);
    }
}