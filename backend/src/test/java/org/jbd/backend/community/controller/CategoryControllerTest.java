package org.jbd.backend.community.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jbd.backend.community.dto.CategoryDto;
import org.jbd.backend.community.service.CategoryService;
import org.jbd.backend.common.exception.GlobalExceptionHandler;
import org.jbd.backend.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryController 테스트")
class CategoryControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private CategoryService categoryService;

    @BeforeEach
    void setUp() {
        CategoryController categoryController = new CategoryController(categoryService);
        mockMvc = MockMvcBuilders.standaloneSetup(categoryController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("카테고리를 생성한다")
    void 카테고리를_생성한다() throws Exception {
        // given
        CategoryDto.Request request = new CategoryDto.Request("Java", "Java 프로그래밍", 1);
        CategoryDto.Response response = new CategoryDto.Response();
        response.setId(1L);
        response.setName("Java");
        response.setDescription("Java 프로그래밍");
        response.setDisplayOrder(1);
        response.setActive(true);

        given(categoryService.createCategory(any(CategoryDto.Request.class)))
                .willReturn(response);

        // when & then
        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Java"))
                .andExpect(jsonPath("$.description").value("Java 프로그래밍"))
                .andExpect(jsonPath("$.displayOrder").value(1))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    @DisplayName("ID로 카테고리를 조회한다")
    void ID로_카테고리를_조회한다() throws Exception {
        // given
        Long categoryId = 1L;
        CategoryDto.Response response = new CategoryDto.Response();
        response.setId(1L);
        response.setName("Java");
        response.setDescription("Java 프로그래밍");
        response.setActive(true);

        given(categoryService.getCategoryById(categoryId)).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/categories/{id}", categoryId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Java"))
                .andExpect(jsonPath("$.description").value("Java 프로그래밍"));
    }

    @Test
    @DisplayName("존재하지 않는 카테고리 조회시 404 에러가 발생한다")
    void 존재하지_않는_카테고리_조회시_404_에러가_발생한다() throws Exception {
        // given
        Long categoryId = 999L;
        given(categoryService.getCategoryById(categoryId))
                .willThrow(new ResourceNotFoundException("Category", "id", categoryId));

        // when & then
        mockMvc.perform(get("/api/categories/{id}", categoryId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("모든 활성 카테고리 목록을 조회한다")
    void 모든_활성_카테고리_목록을_조회한다() throws Exception {
        // given
        CategoryDto.Response category1 = new CategoryDto.Response();
        category1.setId(1L);
        category1.setName("Java");
        category1.setDisplayOrder(1);

        CategoryDto.Response category2 = new CategoryDto.Response();
        category2.setId(2L);
        category2.setName("Spring");
        category2.setDisplayOrder(2);

        List<CategoryDto.Response> categories = Arrays.asList(category1, category2);
        given(categoryService.getAllActiveCategories()).willReturn(categories);

        // when & then
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Java"))
                .andExpect(jsonPath("$[1].name").value("Spring"));
    }

    @Test
    @DisplayName("카테고리를 수정한다")
    void 카테고리를_수정한다() throws Exception {
        // given
        Long categoryId = 1L;
        CategoryDto.Request request = new CategoryDto.Request("Updated Java", "Updated description", 2);
        CategoryDto.Response response = new CategoryDto.Response();
        response.setId(1L);
        response.setName("Updated Java");
        response.setDescription("Updated description");
        response.setDisplayOrder(2);

        given(categoryService.updateCategory(eq(categoryId), any(CategoryDto.Request.class)))
                .willReturn(response);

        // when & then
        mockMvc.perform(put("/api/categories/{id}", categoryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("Updated Java"))
                .andExpect(jsonPath("$.description").value("Updated description"))
                .andExpect(jsonPath("$.displayOrder").value(2));
    }

    @Test
    @DisplayName("카테고리를 삭제한다")
    void 카테고리를_삭제한다() throws Exception {
        // given
        Long categoryId = 1L;
        willDoNothing().given(categoryService).deleteCategory(categoryId);

        // when & then
        mockMvc.perform(delete("/api/categories/{id}", categoryId))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("카테고리 표시 순서를 변경한다")
    void 카테고리_표시_순서를_변경한다() throws Exception {
        // given
        Long categoryId = 1L;
        Integer newDisplayOrder = 5;
        CategoryDto.Response response = new CategoryDto.Response();
        response.setId(1L);
        response.setName("Java");
        response.setDisplayOrder(5);

        given(categoryService.updateDisplayOrder(categoryId, newDisplayOrder))
                .willReturn(response);

        // when & then
        mockMvc.perform(patch("/api/categories/{id}/display-order", categoryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newDisplayOrder)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.displayOrder").value(5));
    }
}