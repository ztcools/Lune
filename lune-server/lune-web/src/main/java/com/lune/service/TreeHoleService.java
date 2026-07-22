package com.lune.service;

import com.lune.common.PageResult;
import com.lune.entity.TreeHole;

public interface TreeHoleService {
    PageResult<TreeHole> listTreeHoles(int page, int size);
    TreeHole createTreeHole(TreeHole treeHole);
    void deleteTreeHole(Long id);
}
