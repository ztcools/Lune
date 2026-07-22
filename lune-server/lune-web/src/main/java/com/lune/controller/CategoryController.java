package com.lune.controller;

import com.lune.common.Result;
import com.lune.entity.Category;
import com.lune.service.CategoryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public Result<List<Category>> list(@RequestParam(required = false, defaultValue = "article") String type) {
        return Result.success(categoryService.listByType(type));
    }
}
