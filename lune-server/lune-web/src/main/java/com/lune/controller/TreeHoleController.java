package com.lune.controller;

import com.lune.common.PageResult;
import com.lune.common.Result;
import com.lune.entity.TreeHole;
import com.lune.service.TreeHoleService;
import io.jsonwebtoken.Claims;
import org.springframework.security.core.context.SecurityContextHolder;
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

    /**
     * 发布树洞弹幕（允许匿名）。
     *
     * 服务端持有的字段必须全部无条件重置，不能只在「已登录」分支里赋值：
     * 匿名请求下若沿用请求体的 userId，任何人都能带上 userId=1 冒充管理员，
     * 服务层随后会把该用户的昵称/头像回填进响应与列表。
     * 同理 id / likeCount / createTime 也不接受客户端指定。
     */
    @PostMapping
    public Result<TreeHole> create(@RequestBody TreeHole treeHole) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = null;
        if (auth != null && auth.getPrincipal() instanceof Claims claims) {
            userId = claims.get("userId", Long.class);
        }
        treeHole.setUserId(userId);
        treeHole.setId(null);
        treeHole.setLikeCount(0L);
        treeHole.setCreateTime(null);
        return Result.success(treeHoleService.createTreeHole(treeHole));
    }
}
