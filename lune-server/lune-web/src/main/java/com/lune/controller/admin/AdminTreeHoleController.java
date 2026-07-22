package com.lune.controller.admin;

import com.lune.common.PageResult;
import com.lune.common.Result;
import com.lune.entity.TreeHole;
import com.lune.service.TreeHoleService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/treeholes")
@PreAuthorize("hasRole('ADMIN')")
public class AdminTreeHoleController {

    private final TreeHoleService treeHoleService;

    public AdminTreeHoleController(TreeHoleService treeHoleService) {
        this.treeHoleService = treeHoleService;
    }

    @GetMapping
    public Result<PageResult<TreeHole>> list(@RequestParam(defaultValue = "1") int page,
                                              @RequestParam(defaultValue = "20") int size) {
        return Result.success(treeHoleService.listTreeHoles(page, size));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        treeHoleService.deleteTreeHole(id);
        return Result.success();
    }
}
