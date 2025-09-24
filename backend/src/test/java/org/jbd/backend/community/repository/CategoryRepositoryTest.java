package org.jbd.backend.community.repository;

import org.jbd.backend.community.domain.Category;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("카테고리 레포지토리 테스트")
class CategoryRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    @DisplayName("활성 카테고리를 표시 순서대로 조회할 수 있다")
    void 활성_카테고리를_표시_순서대로_조회할_수_있다() {
        // given
        Category category1 = new Category("Java", "Java 프로그래밍 관련 게시판");
        category1.updateDisplayOrder(2);
        Category category2 = new Category("Spring", "Spring Framework 관련 게시판");
        category2.updateDisplayOrder(1);
        Category category3 = new Category("Python", "Python 프로그래밍 관련 게시판");
        category3.updateDisplayOrder(3);
        
        entityManager.persistAndFlush(category1);
        entityManager.persistAndFlush(category2);
        entityManager.persistAndFlush(category3);

        // when
        List<Category> categories = categoryRepository.findByActiveTrueOrderByDisplayOrder();

        // then
        assertThat(categories).hasSize(3);
        assertThat(categories.get(0).getName()).isEqualTo("Spring");
        assertThat(categories.get(1).getName()).isEqualTo("Java");
        assertThat(categories.get(2).getName()).isEqualTo("Python");
    }

    @Test
    @DisplayName("카테고리명으로 활성 카테고리를 조회할 수 있다")
    void 카테고리명으로_활성_카테고리를_조회할_수_있다() {
        // given
        Category category = new Category("Java", "Java 프로그래밍 관련 게시판");
        entityManager.persistAndFlush(category);

        // when
        Optional<Category> foundCategory = categoryRepository.findByNameAndActiveTrue("Java");

        // then
        assertThat(foundCategory).isPresent();
        assertThat(foundCategory.get().getName()).isEqualTo("Java");
    }

    @Test
    @DisplayName("비활성 카테고리는 조회되지 않는다")
    void 비활성_카테고리는_조회되지_않는다() {
        // given
        Category activeCategory = new Category("Java", "Java 프로그래밍 관련 게시판");
        Category inactiveCategory = new Category("Python", "Python 프로그래밍 관련 게시판");
        inactiveCategory.deactivate();
        
        entityManager.persistAndFlush(activeCategory);
        entityManager.persistAndFlush(inactiveCategory);

        // when
        List<Category> activeCategories = categoryRepository.findByActiveTrueOrderByDisplayOrder();
        Optional<Category> foundInactive = categoryRepository.findByNameAndActiveTrue("Python");

        // then
        assertThat(activeCategories).hasSize(1);
        assertThat(activeCategories.get(0).getName()).isEqualTo("Java");
        assertThat(foundInactive).isEmpty();
    }
}