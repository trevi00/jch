package org.jbd.backend.community.repository;

import org.jbd.backend.community.domain.Category;
import org.jbd.backend.community.domain.Comment;
import org.jbd.backend.community.domain.Post;
import org.jbd.backend.user.domain.User;
import org.jbd.backend.user.domain.enums.UserType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Comment Repository 테스트")
class CommentRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CommentRepository commentRepository;

    @Test
    @DisplayName("게시글별 댓글을 작성일 오름차순으로 조회할 수 있다")
    void 게시글별_댓글을_작성일_오름차순으로_조회할_수_있다() {
        // given
        Category category = new Category("Java", "Java 프로그래밍 관련 게시판");
        entityManager.persistAndFlush(category);

        User user = new User("test@example.com", "password", "테스트사용자", UserType.GENERAL);
        entityManager.persistAndFlush(user);

        Post post = new Post("테스트 게시글", "테스트 내용", user, category);
        entityManager.persistAndFlush(post);

        Comment comment1 = new Comment("첫 번째 댓글", user, post);
        Comment comment2 = new Comment("두 번째 댓글", user, post);
        Comment comment3 = new Comment("세 번째 댓글", user, post);

        entityManager.persistAndFlush(comment1);
        entityManager.persistAndFlush(comment2);
        entityManager.persistAndFlush(comment3);

        // when
        List<Comment> comments = commentRepository.findByPostOrderByCreatedAtAsc(post);

        // then
        assertThat(comments).hasSize(3);
        assertThat(comments.get(0).getContent()).isEqualTo("첫 번째 댓글");
        assertThat(comments.get(1).getContent()).isEqualTo("두 번째 댓글");
        assertThat(comments.get(2).getContent()).isEqualTo("세 번째 댓글");
    }

    @Test
    @DisplayName("사용자별 댓글을 작성일 내림차순으로 조회할 수 있다")
    void 사용자별_댓글을_작성일_내림차순으로_조회할_수_있다() {
        // given
        Category category = new Category("Java", "Java 프로그래밍 관련 게시판");
        entityManager.persistAndFlush(category);

        User user1 = new User("user1@example.com", "password", "사용자1", UserType.GENERAL);
        User user2 = new User("user2@example.com", "password", "사용자2", UserType.GENERAL);
        entityManager.persistAndFlush(user1);
        entityManager.persistAndFlush(user2);

        Post post = new Post("테스트 게시글", "테스트 내용", user1, category);
        entityManager.persistAndFlush(post);

        Comment comment1 = new Comment("사용자1의 댓글1", user1, post);
        Comment comment2 = new Comment("사용자1의 댓글2", user1, post);
        Comment comment3 = new Comment("사용자2의 댓글", user2, post);

        entityManager.persistAndFlush(comment1);
        entityManager.persistAndFlush(comment2);
        entityManager.persistAndFlush(comment3);

        // when
        List<Comment> comments = commentRepository.findByAuthorOrderByCreatedAtDesc(user1);

        // then
        assertThat(comments).hasSize(2);
        assertThat(comments).allMatch(comment -> comment.getAuthor().equals(user1));
        assertThat(comments.get(0).getContent()).isEqualTo("사용자1의 댓글2");
        assertThat(comments.get(1).getContent()).isEqualTo("사용자1의 댓글1");
    }

    @Test
    @DisplayName("대댓글을 포함한 댓글 계층 구조를 조회할 수 있다")
    void 대댓글을_포함한_댓글_계층_구조를_조회할_수_있다() {
        // given
        Category category = new Category("Java", "Java 프로그래밍 관련 게시판");
        entityManager.persistAndFlush(category);

        User user = new User("test@example.com", "password", "테스트사용자", UserType.GENERAL);
        entityManager.persistAndFlush(user);

        Post post = new Post("테스트 게시글", "테스트 내용", user, category);
        entityManager.persistAndFlush(post);

        // 부모 댓글
        Comment parentComment = new Comment("부모 댓글", user, post);
        entityManager.persistAndFlush(parentComment);

        // 대댓글들
        Comment childComment1 = new Comment("첫 번째 대댓글", user, post, parentComment);
        Comment childComment2 = new Comment("두 번째 대댓글", user, post, parentComment);
        entityManager.persistAndFlush(childComment1);
        entityManager.persistAndFlush(childComment2);

        // when
        List<Comment> parentComments = commentRepository.findByPostAndParentCommentIsNullOrderByCreatedAtAsc(post);
        List<Comment> childComments = commentRepository.findByParentCommentOrderByCreatedAtAsc(parentComment);

        // then
        assertThat(parentComments).hasSize(1);
        assertThat(parentComments.get(0).getContent()).isEqualTo("부모 댓글");
        
        assertThat(childComments).hasSize(2);
        assertThat(childComments.get(0).getContent()).isEqualTo("첫 번째 대댓글");
        assertThat(childComments.get(1).getContent()).isEqualTo("두 번째 대댓글");
    }

    @Test
    @DisplayName("게시글의 전체 댓글 수를 조회할 수 있다")
    void 게시글의_전체_댓글_수를_조회할_수_있다() {
        // given
        Category category = new Category("Java", "Java 프로그래밍 관련 게시판");
        entityManager.persistAndFlush(category);

        User user = new User("test@example.com", "password", "테스트사용자", UserType.GENERAL);
        entityManager.persistAndFlush(user);

        Post post1 = new Post("게시글1", "내용1", user, category);
        Post post2 = new Post("게시글2", "내용2", user, category);
        entityManager.persistAndFlush(post1);
        entityManager.persistAndFlush(post2);

        // post1에 댓글 3개
        Comment comment1 = new Comment("댓글1", user, post1);
        Comment comment2 = new Comment("댓글2", user, post1);
        Comment comment3 = new Comment("댓글3", user, post1);

        // post2에 댓글 1개
        Comment comment4 = new Comment("댓글4", user, post2);

        entityManager.persistAndFlush(comment1);
        entityManager.persistAndFlush(comment2);
        entityManager.persistAndFlush(comment3);
        entityManager.persistAndFlush(comment4);

        // when
        long post1CommentCount = commentRepository.countByPost(post1);
        long post2CommentCount = commentRepository.countByPost(post2);

        // then
        assertThat(post1CommentCount).isEqualTo(3);
        assertThat(post2CommentCount).isEqualTo(1);
    }

    @Test
    @DisplayName("특정 댓글의 대댓글 수를 조회할 수 있다")
    void 특정_댓글의_대댓글_수를_조회할_수_있다() {
        // given
        Category category = new Category("Java", "Java 프로그래밍 관련 게시판");
        entityManager.persistAndFlush(category);

        User user = new User("test@example.com", "password", "테스트사용자", UserType.GENERAL);
        entityManager.persistAndFlush(user);

        Post post = new Post("테스트 게시글", "테스트 내용", user, category);
        entityManager.persistAndFlush(post);

        Comment parentComment1 = new Comment("부모 댓글1", user, post);
        Comment parentComment2 = new Comment("부모 댓글2", user, post);
        entityManager.persistAndFlush(parentComment1);
        entityManager.persistAndFlush(parentComment2);

        // parentComment1에 대댓글 2개
        Comment childComment1 = new Comment("대댓글1", user, post, parentComment1);
        Comment childComment2 = new Comment("대댓글2", user, post, parentComment1);

        // parentComment2에 대댓글 1개
        Comment childComment3 = new Comment("대댓글3", user, post, parentComment2);

        entityManager.persistAndFlush(childComment1);
        entityManager.persistAndFlush(childComment2);
        entityManager.persistAndFlush(childComment3);

        // when
        long parent1ChildCount = commentRepository.countByParentComment(parentComment1);
        long parent2ChildCount = commentRepository.countByParentComment(parentComment2);

        // then
        assertThat(parent1ChildCount).isEqualTo(2);
        assertThat(parent2ChildCount).isEqualTo(1);
    }
}