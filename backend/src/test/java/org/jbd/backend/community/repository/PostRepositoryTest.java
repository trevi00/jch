package org.jbd.backend.community.repository;

import org.jbd.backend.community.domain.Category;
import org.jbd.backend.community.domain.Post;
import org.jbd.backend.user.domain.User;
import org.jbd.backend.user.domain.enums.UserType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Post Repository 테스트")
class PostRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PostRepository postRepository;

    @Test
    @DisplayName("카테고리별 게시글을 작성일 내림차순으로 조회할 수 있다")
    void 카테고리별_게시글을_작성일_내림차순으로_조회할_수_있다() {
        // given
        Category category = new Category("Java", "Java 프로그래밍 관련 게시판");
        entityManager.persistAndFlush(category);

        User user = new User("test@example.com", "password", "테스트사용자", UserType.GENERAL);
        entityManager.persistAndFlush(user);

        Post post1 = new Post("첫 번째 게시글", "첫 번째 내용", user, category);
        entityManager.persistAndFlush(post1);
        
        try { Thread.sleep(1); } catch (InterruptedException e) {}
        Post post2 = new Post("두 번째 게시글", "두 번째 내용", user, category);
        entityManager.persistAndFlush(post2);
        
        try { Thread.sleep(1); } catch (InterruptedException e) {}
        Post post3 = new Post("세 번째 게시글", "세 번째 내용", user, category);
        entityManager.persistAndFlush(post3);

        Pageable pageable = PageRequest.of(0, 10);

        // when
        List<Post> posts = postRepository.findByCategoryOrderByCreatedAtDesc(category, pageable);

        // then
        assertThat(posts).hasSize(3);
        assertThat(posts.get(0).getTitle()).isEqualTo("세 번째 게시글");
        assertThat(posts.get(1).getTitle()).isEqualTo("두 번째 게시글");
        assertThat(posts.get(2).getTitle()).isEqualTo("첫 번째 게시글");
    }

    @Test
    @DisplayName("제목으로 게시글을 검색할 수 있다")
    void 제목으로_게시글을_검색할_수_있다() {
        // given
        Category category = new Category("Java", "Java 프로그래밍 관련 게시판");
        entityManager.persistAndFlush(category);

        User user = new User("test@example.com", "password", "테스트사용자", UserType.GENERAL);
        entityManager.persistAndFlush(user);

        Post post1 = new Post("Java 기초 강의", "Java 기초 내용", user, category);
        Post post2 = new Post("Spring Boot 시작하기", "Spring Boot 내용", user, category);
        Post post3 = new Post("Java 고급 기법", "Java 고급 내용", user, category);

        entityManager.persistAndFlush(post1);
        entityManager.persistAndFlush(post2);
        entityManager.persistAndFlush(post3);

        Pageable pageable = PageRequest.of(0, 10);

        // when
        List<Post> posts = postRepository.findByTitleContainingIgnoreCaseOrderByCreatedAtDesc("java", pageable);

        // then
        assertThat(posts).hasSize(2);
        assertThat(posts.get(0).getTitle()).contains("Java");
        assertThat(posts.get(1).getTitle()).contains("Java");
    }

    @Test
    @DisplayName("작성자별 게시글을 조회할 수 있다")
    void 작성자별_게시글을_조회할_수_있다() {
        // given
        Category category = new Category("Java", "Java 프로그래밍 관련 게시판");
        entityManager.persistAndFlush(category);

        User user1 = new User("user1@example.com", "password", "사용자1", UserType.GENERAL);
        User user2 = new User("user2@example.com", "password", "사용자2", UserType.GENERAL);
        entityManager.persistAndFlush(user1);
        entityManager.persistAndFlush(user2);

        Post post1 = new Post("사용자1의 게시글1", "내용1", user1, category);
        Post post2 = new Post("사용자1의 게시글2", "내용2", user1, category);
        Post post3 = new Post("사용자2의 게시글1", "내용3", user2, category);

        entityManager.persistAndFlush(post1);
        entityManager.persistAndFlush(post2);
        entityManager.persistAndFlush(post3);

        Pageable pageable = PageRequest.of(0, 10);

        // when
        List<Post> posts = postRepository.findByAuthorOrderByCreatedAtDesc(user1, pageable);

        // then
        assertThat(posts).hasSize(2);
        assertThat(posts).allMatch(post -> post.getAuthor().equals(user1));
    }

    @Test
    @DisplayName("조회수가 높은 인기 게시글을 조회할 수 있다")
    void 조회수가_높은_인기_게시글을_조회할_수_있다() {
        // given
        Category category = new Category("Java", "Java 프로그래밍 관련 게시판");
        entityManager.persistAndFlush(category);

        User user = new User("test@example.com", "password", "테스트사용자", UserType.GENERAL);
        entityManager.persistAndFlush(user);

        Post post1 = new Post("인기 게시글1", "인기 내용1", user, category);
        Post post2 = new Post("일반 게시글", "일반 내용", user, category);
        Post post3 = new Post("인기 게시글2", "인기 내용2", user, category);

        // 조회수 설정 (증가 메서드 호출)
        for (int i = 0; i < 100; i++) {
            post1.incrementViewCount();
        }
        for (int i = 0; i < 10; i++) {
            post2.incrementViewCount();
        }
        for (int i = 0; i < 50; i++) {
            post3.incrementViewCount();
        }

        entityManager.persistAndFlush(post1);
        entityManager.persistAndFlush(post2);
        entityManager.persistAndFlush(post3);

        Pageable pageable = PageRequest.of(0, 2);

        // when
        List<Post> posts = postRepository.findByCategoryOrderByViewCountDesc(category, pageable);

        // then
        assertThat(posts).hasSize(2);
        assertThat(posts.get(0).getViewCount()).isEqualTo(100);
        assertThat(posts.get(1).getViewCount()).isEqualTo(50);
    }

    @Test
    @DisplayName("게시글 ID로 카테고리와 작성자 정보를 함께 조회할 수 있다")
    void 게시글_ID로_카테고리와_작성자_정보를_함께_조회할_수_있다() {
        // given
        Category category = new Category("Java", "Java 프로그래밍 관련 게시판");
        entityManager.persistAndFlush(category);

        User user = new User("test@example.com", "password", "테스트사용자", UserType.GENERAL);
        entityManager.persistAndFlush(user);

        Post post = new Post("테스트 게시글", "테스트 내용", user, category);
        entityManager.persistAndFlush(post);
        entityManager.clear();

        // when
        Optional<Post> foundPost = postRepository.findByIdWithCategoryAndAuthor(post.getId());

        // then
        assertThat(foundPost).isPresent();
        assertThat(foundPost.get().getTitle()).isEqualTo("테스트 게시글");
        assertThat(foundPost.get().getCategory().getName()).isEqualTo("Java");
        assertThat(foundPost.get().getAuthor().getName()).isEqualTo("테스트사용자");
    }
}