package com.lune.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lune.common.BusinessException;
import com.lune.common.PageResult;
import com.lune.entity.Essay;
import com.lune.mapper.EssayMapper;
import com.lune.security.SecurityUtils;
import com.lune.service.EssayService;
import com.lune.service.support.UserInfoFiller;
import org.springframework.stereotype.Service;

@Service
public class EssayServiceImpl implements EssayService {

    private final EssayMapper essayMapper;
    private final UserInfoFiller userInfoFiller;

    public EssayServiceImpl(EssayMapper essayMapper, UserInfoFiller userInfoFiller) {
        this.essayMapper = essayMapper;
        this.userInfoFiller = userInfoFiller;
    }

    @Override
    public PageResult<Essay> listEssays(int page, int size) {
        var wrapper = new LambdaQueryWrapper<Essay>()
                .eq(Essay::getStatus, 1)
                .orderByDesc(Essay::getCreateTime);
        var result = essayMapper.selectPage(new Page<>(page, size), wrapper);
        userInfoFiller.fill(result.getRecords());
        return PageResult.of(result.getRecords(), result.getTotal(), page, size);
    }

    @Override
    public Essay getEssayById(Long id) {
        var essay = essayMapper.selectById(id);
        if (essay == null) throw new BusinessException("随笔不存在");
        userInfoFiller.fillOne(essay);
        return essay;
    }

    @Override
    public Essay createEssay(Essay essay) {
        essay.setUserId(SecurityUtils.getCurrentUserId());
        essay.setStatus(1);
        essayMapper.insert(essay);
        userInfoFiller.fillOne(essay);
        return essay;
    }

    @Override
    public Essay updateEssay(Long id, Essay essay) {
        var exist = essayMapper.selectById(id);
        if (exist == null) throw new BusinessException("随笔不存在");
        exist.setTitle(essay.getTitle());
        exist.setContent(essay.getContent());
        exist.setCover(essay.getCover());
        exist.setMedia(essay.getMedia());
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
