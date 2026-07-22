package com.lune.controller;

import com.lune.common.PageResult;
import com.lune.common.Result;
import com.lune.entity.TreeHole;
import com.lune.service.TreeHoleService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/treeholes")
public class TreeHoleController {

    private final TreeHoleService treeHoleService;

    public TreeHoleController(TreeHoleService treeHoleService) {
        this.treeHoleService = treeHoleService;
    }

    @GetMapping
    public Result<PageResult<TreeHole>> list(@RequestParam(defaultValue = "1") int page,
                                              @RequestParam(defaultValue = "20") int size) {
        return Result.success(treeHoleService.listTreeHoles(page, size));
    }

    @PostMapping
    public Result<TreeHole> create(@RequestBody TreeHole treeHole) {
        return Result.success(treeHoleService.createTreeHole(treeHole));
    }
}
