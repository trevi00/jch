package org.jbd.backend.community.service;

import org.jbd.backend.community.dto.CategoryDto;

import java.util.List;

public interface CategoryService {
    
    CategoryDto.Response createCategory(CategoryDto.Request request);
    
    CategoryDto.Response getCategoryById(Long id);
    
    List<CategoryDto.Response> getAllActiveCategories();
    
    CategoryDto.Response updateCategory(Long id, CategoryDto.Request request);
    
    void deleteCategory(Long id);
    
    CategoryDto.Response updateDisplayOrder(Long id, Integer displayOrder);
}