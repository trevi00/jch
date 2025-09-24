package org.jbd.backend.community.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("카테고리 도메인 테스트")
class CategoryTest {

    @Test
    @DisplayName("카테고리를 생성할 수 있다")
    void 카테고리를_생성할_수_있다() {
        // given
        String name = "취업정보";
        String description = "취업 관련 정보를 공유하는 카테고리";
        
        // when
        Category category = new Category(name, description);
        
        // then
        assertThat(category.getName()).isEqualTo(name);
        assertThat(category.getDescription()).isEqualTo(description);
        assertThat(category.isActive()).isTrue();
        assertThat(category.getDisplayOrder()).isEqualTo(0);
    }
    
    @Test
    @DisplayName("카테고리를 비활성화할 수 있다")
    void 카테고리를_비활성화할_수_있다() {
        // given
        Category category = new Category("테스트", "설명");
        
        // when
        category.deactivate();
        
        // then
        assertThat(category.isActive()).isFalse();
    }
    
    @Test
    @DisplayName("카테고리 표시 순서를 변경할 수 있다")
    void 카테고리_표시_순서를_변경할_수_있다() {
        // given
        Category category = new Category("테스트", "설명");
        int newOrder = 5;
        
        // when
        category.updateDisplayOrder(newOrder);
        
        // then
        assertThat(category.getDisplayOrder()).isEqualTo(newOrder);
    }
}