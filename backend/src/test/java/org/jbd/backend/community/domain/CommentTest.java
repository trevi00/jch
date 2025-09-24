package org.jbd.backend.community.domain;

import org.jbd.backend.user.domain.User;
import org.jbd.backend.user.domain.enums.UserType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("댓글 도메인 테스트")
class CommentTest {

    @Test
    @DisplayName("댓글을 생성할 수 있다")
    void 댓글을_생성할_수_있다() {
        // given
        User author = new User("test@example.com", "password", "테스트유저", UserType.GENERAL);
        Category category = new Category("자유게시판", "자유로운 소통을 위한 게시판");
        Post post = new Post("게시글 제목", "게시글 내용", author, category);
        String content = "좋은 게시글이네요!";
        
        // when
        Comment comment = new Comment(content, author, post);
        
        // then
        assertThat(comment.getContent()).isEqualTo(content);
        assertThat(comment.getAuthor()).isEqualTo(author);
        assertThat(comment.getPost()).isEqualTo(post);
        assertThat(comment.getParentComment()).isNull();
        assertThat(comment.getLikeCount()).isEqualTo(0L);
        assertThat(comment.isDeleted()).isFalse();
    }
    
    @Test
    @DisplayName("대댓글을 생성할 수 있다")
    void 대댓글을_생성할_수_있다() {
        // given
        User author = new User("test@example.com", "password", "테스트유저", UserType.GENERAL);
        User replyAuthor = new User("reply@example.com", "password", "답글유저", UserType.GENERAL);
        Category category = new Category("자유게시판", "자유로운 소통을 위한 게시판");
        Post post = new Post("게시글 제목", "게시글 내용", author, category);
        
        Comment parentComment = new Comment("원댓글 내용", author, post);
        String replyContent = "답글 내용입니다.";
        
        // when
        Comment reply = new Comment(replyContent, replyAuthor, post, parentComment);
        
        // then
        assertThat(reply.getContent()).isEqualTo(replyContent);
        assertThat(reply.getAuthor()).isEqualTo(replyAuthor);
        assertThat(reply.getPost()).isEqualTo(post);
        assertThat(reply.getParentComment()).isEqualTo(parentComment);
    }
    
    @Test
    @DisplayName("댓글을 수정할 수 있다")
    void 댓글을_수정할_수_있다() {
        // given
        User author = new User("test@example.com", "password", "테스트유저", UserType.GENERAL);
        Category category = new Category("자유게시판", "자유로운 소통을 위한 게시판");
        Post post = new Post("게시글 제목", "게시글 내용", author, category);
        Comment comment = new Comment("원래 댓글 내용", author, post);
        
        String newContent = "수정된 댓글 내용";
        
        // when
        comment.updateContent(newContent);
        
        // then
        assertThat(comment.getContent()).isEqualTo(newContent);
    }
    
    @Test
    @DisplayName("댓글을 삭제할 수 있다")
    void 댓글을_삭제할_수_있다() {
        // given
        User author = new User("test@example.com", "password", "테스트유저", UserType.GENERAL);
        Category category = new Category("자유게시판", "자유로운 소통을 위한 게시판");
        Post post = new Post("게시글 제목", "게시글 내용", author, category);
        Comment comment = new Comment("댓글 내용", author, post);
        
        // when
        comment.delete();
        
        // then
        assertThat(comment.isDeleted()).isTrue();
        assertThat(comment.getContent()).isEqualTo("삭제된 댓글입니다.");
    }
    
    @Test
    @DisplayName("좋아요 수를 증가시킬 수 있다")
    void 좋아요_수를_증가시킬_수_있다() {
        // given
        User author = new User("test@example.com", "password", "테스트유저", UserType.GENERAL);
        Category category = new Category("자유게시판", "자유로운 소통을 위한 게시판");
        Post post = new Post("게시글 제목", "게시글 내용", author, category);
        Comment comment = new Comment("댓글 내용", author, post);
        
        // when
        comment.incrementLikeCount();
        
        // then
        assertThat(comment.getLikeCount()).isEqualTo(1L);
    }
    
    @Test
    @DisplayName("좋아요 수를 감소시킬 수 있다")
    void 좋아요_수를_감소시킬_수_있다() {
        // given
        User author = new User("test@example.com", "password", "테스트유저", UserType.GENERAL);
        Category category = new Category("자유게시판", "자유로운 소통을 위한 게시판");
        Post post = new Post("게시글 제목", "게시글 내용", author, category);
        Comment comment = new Comment("댓글 내용", author, post);
        comment.incrementLikeCount();
        comment.incrementLikeCount();
        
        // when
        comment.decrementLikeCount();
        
        // then
        assertThat(comment.getLikeCount()).isEqualTo(1L);
    }
    
    @Test
    @DisplayName("좋아요 수가 0 미만으로 감소하지 않는다")
    void 좋아요_수가_0_미만으로_감소하지_않는다() {
        // given
        User author = new User("test@example.com", "password", "테스트유저", UserType.GENERAL);
        Category category = new Category("자유게시판", "자유로운 소통을 위한 게시판");
        Post post = new Post("게시글 제목", "게시글 내용", author, category);
        Comment comment = new Comment("댓글 내용", author, post);
        
        // when
        comment.decrementLikeCount();
        
        // then
        assertThat(comment.getLikeCount()).isEqualTo(0L);
    }
}