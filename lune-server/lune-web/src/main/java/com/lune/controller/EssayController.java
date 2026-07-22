package com.lune.controller;

import com.lune.common.PageResult;
import com.lune.common.Result;
import com.lune.entity.Essay;
import com.lune.service.EssayService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/essays")
public class EssayController {

    private final EssayService essayService;

    public EssayController(EssayService essayService) {
        this.essayService = essayService;
    }

    @GetMapping
    public Result<PageResult<Essay>> list(@RequestParam(defaultValue = "1") int page,
                                           @RequestParam(defaultValue = "10") int size) {
        return Result.success(essayService.listEssays(page, size));
    }

    @GetMapping("/{id}")
    public Result<Essay> getById(@PathVariable Long id) {
        return Result.success(essayService.getEssayById(id));
    }
}
