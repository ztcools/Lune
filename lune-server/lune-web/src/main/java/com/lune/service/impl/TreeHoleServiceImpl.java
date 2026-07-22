package com.lune.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lune.common.PageResult;
import com.lune.entity.TreeHole;
import com.lune.mapper.TreeHoleMapper;
import com.lune.service.TreeHoleService;
import org.springframework.stereotype.Service;

@Service
public class TreeHoleServiceImpl implements TreeHoleService {

    private final TreeHoleMapper treeHoleMapper;

    public TreeHoleServiceImpl(TreeHoleMapper treeHoleMapper) {
        this.treeHoleMapper = treeHoleMapper;
    }

    @Override
    public PageResult<TreeHole> listTreeHoles(int page, int size) {
        var wrapper = new LambdaQueryWrapper<TreeHole>()
                .eq(TreeHole::getStatus, 1)
                .orderByDesc(TreeHole::getCreateTime);
        var result = treeHoleMapper.selectPage(new Page<>(page, size), wrapper);
        return PageResult.of(result.getRecords(), result.getTotal(), page, size);
    }

    @Override
    public TreeHole createTreeHole(TreeHole treeHole) {
        treeHole.setStatus(1);
        treeHoleMapper.insert(treeHole);
        return treeHole;
    }

    @Override
    public void deleteTreeHole(Long id) {
        treeHoleMapper.deleteById(id);
    }
}
