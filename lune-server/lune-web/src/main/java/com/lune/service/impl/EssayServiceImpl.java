package com.lune.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lune.common.BusinessException;
import com.lune.common.PageResult;
import com.lune.entity.Essay;
import com.lune.mapper.EssayMapper;
import com.lune.service.EssayService;
import org.springframework.stereotype.Service;

@Service
public class EssayServiceImpl implements EssayService {

    private final EssayMapper essayMapper;

    public EssayServiceImpl(EssayMapper essayMapper) {
        this.essayMapper = essayMapper;
    }

    @Override
    public PageResult<Essay> listEssays(int page, int size) {
        var wrapper = new LambdaQueryWrapper<Essay>()
                .eq(Essay::getStatus, 1)
                .orderByDesc(Essay::getCreateTime);
        var result = essayMapper.selectPage(new Page<>(page, size), wrapper);
        return PageResult.of(result.getRecords(), result.getTotal(), page, size);
    }

    @Override
    public Essay getEssayById(Long id) {
        var essay = essayMapper.selectById(id);
        if (essay == null) throw new BusinessException("随笔不存在");
        return essay;
    }

    @Override
    public Essay createEssay(Essay essay) {
        essay.setUserId(1L);
        essay.setStatus(1);
        essayMapper.insert(essay);
        return essay;
    }

    @Override
    public Essay updateEssay(Long id, Essay essay) {
        var exist = essayMapper.selectById(id);
        if (exist == null) throw new BusinessException("随笔不存在");
        exist.setTitle(essay.getTitle());
        exist.setContent(essay.getContent());
        exist.setCover(essay.getCover());
        exist.setWeather(essay.getWeather());
        exist.setMood(essay.getMood());
        exist.setLocation(essay.getLocation());
        essayMapper.updateById(exist);
        return exist;
    }

    @Override
    public void deleteEssay(Long id) {
        essayMapper.deleteById(id);
    }
}
