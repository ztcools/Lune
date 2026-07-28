package com.lune.controller;

import com.lune.common.PageResult;
import com.lune.common.Result;
import com.lune.entity.Wish;
import com.lune.security.SecurityUtils;
import com.lune.service.WishService;
import org.springframework.web.bind.annotation.*;

/**
 * 许愿池公开接口
 */
@RestController
@RequestMapping("/api/wishes")
public class WishController {

    private final WishService wishService;

    public WishController(WishService wishService) {
        this.wishService = wishService;
    }

    @GetMapping
    public Result<PageResult<Wish>> list(@RequestParam(defaultValue = "1") int page,
                                         @RequestParam(defaultValue = "10") int size) {
        return Result.success(wishService.listWishes(page, size, SecurityUtils.getCurrentUserId()));
    }

    @PostMapping
    public Result<Wish> create(@RequestBody Wish wish) {
        return Result.success(wishService.create(wish));
    }

    @PostMapping("/{id}/like")
    public Result<Long> toggleLike(@PathVariable Long id) {
        return Result.success(wishService.toggleLike(id, SecurityUtils.getCurrentUserId()));
    }
}
