package com.lune.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lune.common.PageResult;
import com.lune.entity.TreeHole;
import com.lune.mapper.TreeHoleMapper;
import com.lune.service.TreeHoleService;
import com.lune.service.support.UserInfoFiller;
import org.springframework.stereotype.Service;

@Service
public class TreeHoleServiceImpl implements TreeHoleService {

    private final TreeHoleMapper treeHoleMapper;
    private final UserInfoFiller userInfoFiller;

    public TreeHoleServiceImpl(TreeHoleMapper treeHoleMapper, UserInfoFiller userInfoFiller) {
        this.treeHoleMapper = treeHoleMapper;
        this.userInfoFiller = userInfoFiller;
    }

    @Override
    public PageResult<TreeHole> listTreeHoles(int page, int size) {
        var wrapper = new LambdaQueryWrapper<TreeHole>()
                .eq(TreeHole::getStatus, 1)
                .orderByDesc(TreeHole::getCreateTime);
        var result = treeHoleMapper.selectPage(new Page<>(page, size), wrapper);
        userInfoFiller.fill(result.getRecords());
        return PageResult.of(result.getRecords(), result.getTotal(), page, size);
    }

    @Override
    public TreeHole createTreeHole(TreeHole treeHole) {
        treeHole.setStatus(1);
        // XSS 清洗：树洞内容为纯文本弹幕
        treeHole.setContent(com.lune.security.XssSanitizer.clean(treeHole.getContent(), 200));
        treeHoleMapper.insert(treeHole);
        userInfoFiller.fillOne(treeHole);
        return treeHole;
    }

    @Override
    public void deleteTreeHole(Long id) {
        treeHoleMapper.deleteById(id);
    }
}
