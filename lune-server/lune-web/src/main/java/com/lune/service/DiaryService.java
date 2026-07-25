package com.lune.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lune.entity.Diary;

public interface DiaryService {
    Page<Diary> listDiaries(int page, int size);
    Diary getById(Long id);
    Diary createDiary(Diary diary);
    Diary updateDiary(Long id, Diary diary);
    void deleteDiary(Long id);
}
