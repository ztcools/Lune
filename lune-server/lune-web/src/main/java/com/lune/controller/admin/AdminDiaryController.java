package com.lune.controller.admin;

import com.lune.common.Result;
import com.lune.entity.Diary;
import com.lune.service.DiaryService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/diaries")
@PreAuthorize("hasRole('ADMIN')")
public class AdminDiaryController {

    private final DiaryService diaryService;

    public AdminDiaryController(DiaryService diaryService) {
        this.diaryService = diaryService;
    }

    @GetMapping
    public Result<?> list(@RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "20") int size) {
        return Result.success(diaryService.listDiaries(page, size));
    }

    @PostMapping
    public Result<?> create(@RequestBody Diary diary) {
        return Result.success(diaryService.createDiary(diary));
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody Diary diary) {
        return Result.success(diaryService.updateDiary(id, diary));
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        diaryService.deleteDiary(id);
        return Result.success(null);
    }
}
