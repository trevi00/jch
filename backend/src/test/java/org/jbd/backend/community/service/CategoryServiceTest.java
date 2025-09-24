package org.jbd.backend.community.service;

import org.jbd.backend.community.domain.Category;
import org.jbd.backend.community.dto.CategoryDto;
import org.jbd.backend.community.repository.CategoryRepository;
import org.jbd.backend.community.repository.PostRepository;
import org.jbd.backend.community.service.impl.CategoryServiceImpl;
import org.jbd.backend.common.exception.ResourceNotFoundException;
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
@DisplayName("CategoryService 테스트")
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private PostRepository postRepository;

    private CategoryService categoryService;

    @BeforeEach
    void setUp() {
        categoryService = new CategoryServiceImpl(categoryRepository, postRepository);
    }

    @Test
    @DisplayName("카테고리를 성공적으로 생성한다")
    void 카테고리를_성공적으로_생성한다() {
        // given
        CategoryDto.Request request = new CategoryDto.Request(
                "Java", "Java 프로그래밍 관련", 1
        );
        
        Category savedCategory = new Category("Java", "Java 프로그래밍 관련");
        savedCategory.updateDisplayOrder(1);
        
        given(categoryRepository.save(any(Category.class))).willReturn(savedCategory);

        // when
        CategoryDto.Response response = categoryService.createCategory(request);

        // then
        assertThat(response.getName()).isEqualTo("Java");
        assertThat(response.getDescription()).isEqualTo("Java 프로그래밍 관련");
        assertThat(response.getDisplayOrder()).isEqualTo(1);
        assertThat(response.getActive()).isTrue();
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    @DisplayName("ID로 카테고리를 조회한다")
    void ID로_카테고리를_조회한다() {
        // given
        Long categoryId = 1L;
        Category category = new Category("Java", "Java 프로그래밍 관련");
        
        given(categoryRepository.findById(categoryId)).willReturn(Optional.of(category));

        // when
        CategoryDto.Response response = categoryService.getCategoryById(categoryId);

        // then
        assertThat(response.getName()).isEqualTo("Java");
        assertThat(response.getDescription()).isEqualTo("Java 프로그래밍 관련");
        verify(categoryRepository, times(1)).findById(categoryId);
    }

    @Test
    @DisplayName("존재하지 않는 ID로 카테고리 조회시 예외가 발생한다")
    void 존재하지_않는_ID로_카테고리_조회시_예외가_발생한다() {
        // given
        Long categoryId = 1L;
        given(categoryRepository.findById(categoryId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> categoryService.getCategoryById(categoryId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("활성 카테고리 목록을 표시 순서대로 조회한다")
    void 활성_카테고리_목록을_표시_순서대로_조회한다() {
        // given
        Category category1 = new Category("Java", "Java 프로그래밍");
        category1.updateDisplayOrder(2);
        Category category2 = new Category("Spring", "Spring 프레임워크");
        category2.updateDisplayOrder(1);
        
        given(categoryRepository.findByActiveTrueOrderByDisplayOrder())
                .willReturn(Arrays.asList(category2, category1));

        // when
        List<CategoryDto.Response> responses = categoryService.getAllActiveCategories();

        // then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getName()).isEqualTo("Spring");
        assertThat(responses.get(0).getDisplayOrder()).isEqualTo(1);
        assertThat(responses.get(1).getName()).isEqualTo("Java");
        assertThat(responses.get(1).getDisplayOrder()).isEqualTo(2);
        verify(categoryRepository, times(1)).findByActiveTrueOrderByDisplayOrder();
    }

    @Test
    @DisplayName("카테고리 정보를 업데이트한다")
    void 카테고리_정보를_업데이트한다() {
        // given
        Long categoryId = 1L;
        CategoryDto.Request request = new CategoryDto.Request(
                "Updated Java", "Updated description", 2
        );
        
        Category category = new Category("Java", "Java 프로그래밍 관련");
        
        given(categoryRepository.findById(categoryId)).willReturn(Optional.of(category));
        given(categoryRepository.save(any(Category.class))).willReturn(category);

        // when
        CategoryDto.Response response = categoryService.updateCategory(categoryId, request);

        // then
        assertThat(response.getName()).isEqualTo("Updated Java");
        assertThat(response.getDescription()).isEqualTo("Updated description");
        assertThat(response.getDisplayOrder()).isEqualTo(2);
        verify(categoryRepository, times(1)).findById(categoryId);
        verify(categoryRepository, times(1)).save(category);
    }

    @Test
    @DisplayName("카테고리를 비활성화한다")
    void 카테고리를_비활성화한다() {
        // given
        Long categoryId = 1L;
        Category category = new Category("Java", "Java 프로그래밍 관련");
        
        given(categoryRepository.findById(categoryId)).willReturn(Optional.of(category));

        // when
        categoryService.deleteCategory(categoryId);

        // then
        assertThat(category.isActive()).isFalse();
        verify(categoryRepository, times(1)).findById(categoryId);
        verify(categoryRepository, times(1)).save(category);
    }

    @Test
    @DisplayName("카테고리 표시 순서를 변경한다")
    void 카테고리_표시_순서를_변경한다() {
        // given
        Long categoryId = 1L;
        Integer newDisplayOrder = 5;
        Category category = new Category("Java", "Java 프로그래밍 관련");
        category.updateDisplayOrder(1);
        
        given(categoryRepository.findById(categoryId)).willReturn(Optional.of(category));
        given(categoryRepository.save(any(Category.class))).willReturn(category);

        // when
        CategoryDto.Response response = categoryService.updateDisplayOrder(categoryId, newDisplayOrder);

        // then
        assertThat(response.getDisplayOrder()).isEqualTo(5);
        verify(categoryRepository, times(1)).findById(categoryId);
        verify(categoryRepository, times(1)).save(category);
    }
}