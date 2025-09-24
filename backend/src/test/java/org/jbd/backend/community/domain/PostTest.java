package org.jbd.backend.community.domain;

import org.jbd.backend.user.domain.User;
import org.jbd.backend.user.domain.enums.UserType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("게시글 도메인 테스트")
class PostTest {

    @Test
    @DisplayName("게시글을 생성할 수 있다")
    void 게시글을_생성할_수_있다() {
        // given
        User author = new User("test@example.com", "password", "테스트유저", UserType.GENERAL);
        Category category = new Category("자유게시판", "자유로운 소통을 위한 게시판");
        String title = "첫 번째 게시글";
        String content = "안녕하세요. 반갑습니다.";
        
        // when
        Post post = new Post(title, content, author, category);
        
        // then
        assertThat(post.getTitle()).isEqualTo(title);
        assertThat(post.getContent()).isEqualTo(content);
        assertThat(post.getAuthor()).isEqualTo(author);
        assertThat(post.getCategory()).isEqualTo(category);
        assertThat(post.getViewCount()).isEqualTo(0L);
        assertThat(post.getLikeCount()).isEqualTo(0L);
        assertThat(post.isNoticePost()).isFalse();
        assertThat(post.isPinned()).isFalse();
    }
    
    @Test
    @DisplayName("게시글을 수정할 수 있다")
    void 게시글을_수정할_수_있다() {
        // given
        User author = new User("test@example.com", "password", "테스트유저", UserType.GENERAL);
        Category category = new Category("자유게시판", "자유로운 소통을 위한 게시판");
        Post post = new Post("원본 제목", "원본 내용", author, category);
        
        String newTitle = "수정된 제목";
        String newContent = "수정된 내용";
        
        // when
        post.updatePost(newTitle, newContent);
        
        // then
        assertThat(post.getTitle()).isEqualTo(newTitle);
        assertThat(post.getContent()).isEqualTo(newContent);
    }
    
    @Test
    @DisplayName("조회수를 증가시킬 수 있다")
    void 조회수를_증가시킬_수_있다() {
        // given
        User author = new User("test@example.com", "password", "테스트유저", UserType.GENERAL);
        Category category = new Category("자유게시판", "자유로운 소통을 위한 게시판");
        Post post = new Post("제목", "내용", author, category);
        
        // when
        post.incrementViewCount();
        post.incrementViewCount();
        
        // then
        assertThat(post.getViewCount()).isEqualTo(2L);
    }
    
    @Test
    @DisplayName("좋아요 수를 증가시킬 수 있다")
    void 좋아요_수를_증가시킬_수_있다() {
        // given
        User author = new User("test@example.com", "password", "테스트유저", UserType.GENERAL);
        Category category = new Category("자유게시판", "자유로운 소통을 위한 게시판");
        Post post = new Post("제목", "내용", author, category);
        
        // when
        post.incrementLikeCount();
        
        // then
        assertThat(post.getLikeCount()).isEqualTo(1L);
    }
    
    @Test
    @DisplayName("좋아요 수를 감소시킬 수 있다")
    void 좋아요_수를_감소시킬_수_있다() {
        // given
        User author = new User("test@example.com", "password", "테스트유저", UserType.GENERAL);
        Category category = new Category("자유게시판", "자유로운 소통을 위한 게시판");
        Post post = new Post("제목", "내용", author, category);
        post.incrementLikeCount();
        post.incrementLikeCount();
        
        // when
        post.decrementLikeCount();
        
        // then
        assertThat(post.getLikeCount()).isEqualTo(1L);
    }
    
    @Test
    @DisplayName("좋아요 수가 0 미만으로 감소하지 않는다")
    void 좋아요_수가_0_미만으로_감소하지_않는다() {
        // given
        User author = new User("test@example.com", "password", "테스트유저", UserType.GENERAL);
        Category category = new Category("자유게시판", "자유로운 소통을 위한 게시판");
        Post post = new Post("제목", "내용", author, category);
        
        // when
        post.decrementLikeCount();
        
        // then
        assertThat(post.getLikeCount()).isEqualTo(0L);
    }
    
    @Test
    @DisplayName("공지사항으로 설정할 수 있다")
    void 공지사항으로_설정할_수_있다() {
        // given
        User author = new User("admin@example.com", "password", "관리자", UserType.ADMIN);
        Category category = new Category("공지사항", "공지사항 게시판");
        Post post = new Post("공지 제목", "공지 내용", author, category);
        
        // when
        post.markAsNotice();
        
        // then
        assertThat(post.isNoticePost()).isTrue();
    }
    
    @Test
    @DisplayName("상단 고정으로 설정할 수 있다")
    void 상단_고정으로_설정할_수_있다() {
        // given
        User author = new User("admin@example.com", "password", "관리자", UserType.ADMIN);
        Category category = new Category("자유게시판", "자유로운 소통을 위한 게시판");
        Post post = new Post("중요 공지", "중요한 내용", author, category);
        
        // when
        post.pin();
        
        // then
        assertThat(post.isPinned()).isTrue();
    }
    
    @Test
    @DisplayName("상단 고정을 해제할 수 있다")
    void 상단_고정을_해제할_수_있다() {
        // given
        User author = new User("admin@example.com", "password", "관리자", UserType.ADMIN);
        Category category = new Category("자유게시판", "자유로운 소통을 위한 게시판");
        Post post = new Post("중요 공지", "중요한 내용", author, category);
        post.pin();
        
        // when
        post.unpin();
        
        // then
        assertThat(post.isPinned()).isFalse();
    }
}