package com.lune.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lune.common.BusinessException;
import com.lune.entity.Diary;
import com.lune.mapper.DiaryMapper;
import com.lune.security.SecurityUtils;
import com.lune.service.DiaryService;
import org.springframework.stereotype.Service;

@Service
public class DiaryServiceImpl implements DiaryService {

    private final DiaryMapper diaryMapper;

    public DiaryServiceImpl(DiaryMapper diaryMapper) {
        this.diaryMapper = diaryMapper;
    }

    @Override
    public Page<Diary> listDiaries(int page, int size) {
        var wrapper = new LambdaQueryWrapper<Diary>()
                .eq(Diary::getStatus, 1)
                .orderByAsc(Diary::getPageOrder);
        return diaryMapper.selectPage(new Page<>(page, size), wrapper);
    }

    @Override
    public Diary getById(Long id) {
        var diary = diaryMapper.selectById(id);
        if (diary == null || diary.getStatus() != 1) {
            throw new BusinessException("日记不存在");
        }
        return diary;
    }

    @Override
    public Diary createDiary(Diary diary) {
        diary.setUserId(SecurityUtils.getCurrentUserId());
        diary.setStatus(1);
        if (diary.getPageOrder() == null) diary.setPageOrder(0);
        diaryMapper.insert(diary);
        return diary;
    }

    @Override
    public Diary updateDiary(Long id, Diary diary) {
        var existing = diaryMapper.selectById(id);
        if (existing == null) throw new BusinessException("日记不存在");
        existing.setTitle(diary.getTitle());
        existing.setContent(diary.getContent());
        existing.setImages(diary.getImages());
        existing.setRecordTime(diary.getRecordTime());
        existing.setPageOrder(diary.getPageOrder());
        existing.setStatus(diary.getStatus());
        diaryMapper.updateById(existing);
        return existing;
    }

    @Override
    public void deleteDiary(Long id) {
        diaryMapper.deleteById(id);
    }
}
