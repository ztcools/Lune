package com.lune.controller.admin;

import com.lune.common.PageResult;
import com.lune.common.Result;
import com.lune.entity.Essay;
import com.lune.service.EssayService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/essays")
@PreAuthorize("hasRole('ADMIN')")
public class AdminEssayController {

    private final EssayService essayService;

    public AdminEssayController(EssayService essayService) {
        this.essayService = essayService;
    }

    @GetMapping
    public Result<PageResult<Essay>> list(@RequestParam(defaultValue = "1") int page,
                                           @RequestParam(defaultValue = "10") int size) {
        return Result.success(essayService.listEssays(page, size));
    }

    @PostMapping
    public Result<Essay> create(@RequestBody Essay essay) {
        return Result.success(essayService.createEssay(essay));
    }

    @PutMapping("/{id}")
    public Result<Essay> update(@PathVariable Long id, @RequestBody Essay essay) {
        return Result.success(essayService.updateEssay(id, essay));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        essayService.deleteEssay(id);
        return Result.success();
    }
}
