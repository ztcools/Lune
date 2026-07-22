package com.lune.service;

import com.lune.entity.Category;
import java.util.List;

public interface CategoryService {
    List<Category> listByType(String type);
    List<Category> listAll();
    Category createCategory(Category category);
    Category updateCategory(Long id, Category category);
    void deleteCategory(Long id);
}
