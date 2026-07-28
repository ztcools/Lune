package com.lune.controller.admin;

import com.lune.common.PageResult;
import com.lune.common.Result;
import com.lune.entity.Wish;
import com.lune.service.WishService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/wishes")
@PreAuthorize("hasRole('ADMIN')")
public class AdminWishController {

    private final WishService wishService;

    public AdminWishController(WishService wishService) {
        this.wishService = wishService;
    }

    @GetMapping
    public Result<PageResult<Wish>> list(@RequestParam(defaultValue = "1") int page,
                                         @RequestParam(defaultValue = "20") int size) {
        return Result.success(wishService.listWishes(page, size, null));
    }

    @PutMapping("/{id}")
    public Result<Wish> update(@PathVariable Long id, @RequestBody Wish wish) {
        return Result.success(wishService.update(id, wish));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        wishService.delete(id);
        return Result.success();
    }
}
