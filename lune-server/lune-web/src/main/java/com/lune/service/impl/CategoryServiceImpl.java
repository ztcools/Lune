package com.lune.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lune.common.BusinessException;
import com.lune.entity.Article;
import com.lune.entity.Category;
import com.lune.mapper.ArticleMapper;
import com.lune.mapper.CategoryMapper;
import com.lune.service.CategoryService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;
    private final ArticleMapper articleMapper;

    public CategoryServiceImpl(CategoryMapper categoryMapper, ArticleMapper articleMapper) {
        this.categoryMapper = categoryMapper;
        this.articleMapper = articleMapper;
    }

    @Override
    @Cacheable(value = "categories", key = "#type")
    public List<Category> listByType(String type) {
        return categoryMapper.selectList(new LambdaQueryWrapper<Category>()
                .eq(Category::getType, type)
                .eq(Category::getStatus, 1)
                .orderByAsc(Category::getSortOrder));
    }

    @Override
    public List<Category> listAll() {
        return categoryMapper.selectList(new LambdaQueryWrapper<Category>()
                .orderByAsc(Category::getSortOrder));
    }

    @Override
    @CacheEvict(value = "categories", allEntries = true)
    public Category createCategory(Category category) {
        categoryMapper.insert(category);
        return category;
    }

    @Override
    @CacheEvict(value = "categories", allEntries = true)
    public Category updateCategory(Long id, Category category) {
        var exist = categoryMapper.selectById(id);
        if (exist == null) throw new BusinessException("分类不存在");
        exist.setName(category.getName());
        exist.setDescription(category.getDescription());
        exist.setType(category.getType());
        exist.setSortOrder(category.getSortOrder());
        exist.setStatus(category.getStatus());
        categoryMapper.updateById(exist);
        return exist;
    }

    @Override
    @CacheEvict(value = "categories", allEntries = true)
    public void deleteCategory(Long id) {
        long count = articleMapper.selectCount(new LambdaQueryWrapper<Article>()
                .eq(Article::getCategoryId, id));
        if (count > 0) throw new BusinessException("该分类下有文章，无法删除");
        categoryMapper.deleteById(id);
    }
}
