package com.lune.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lune.common.BusinessException;
import com.lune.common.PageResult;
import com.lune.entity.Essay;
import com.lune.mapper.EssayMapper;
import com.lune.mapper.UserMapper;
import com.lune.security.SecurityUtils;
import com.lune.service.EssayService;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class EssayServiceImpl implements EssayService {

    private final EssayMapper essayMapper;
    private final UserMapper userMapper;

    public EssayServiceImpl(EssayMapper essayMapper, UserMapper userMapper) {
        this.essayMapper = essayMapper;
        this.userMapper = userMapper;
    }

    private void populateUserInfo(java.util.List<Essay> list) {
        Set<Long> userIds = list.stream()
                .map(Essay::getUserId)
                .filter(id -> id != null && id > 0)
                .collect(Collectors.toSet());
        if (userIds.isEmpty()) return;
        var users = userMapper.selectBatchIds(userIds);
        Map<Long, com.lune.entity.User> userMap = users.stream()
                .collect(Collectors.toMap(com.lune.entity.User::getId, Function.identity()));
        for (Essay e : list) {
            var u = userMap.get(e.getUserId());
            if (u != null) {
                e.setUsername(u.getUsername());
                e.setNickname(u.getNickname());
                e.setAvatar(u.getAvatar());
            }
        }
    }

    @Override
    public PageResult<Essay> listEssays(int page, int size) {
        var wrapper = new LambdaQueryWrapper<Essay>()
                .eq(Essay::getStatus, 1)
                .orderByDesc(Essay::getCreateTime);
        var result = essayMapper.selectPage(new Page<>(page, size), wrapper);
        populateUserInfo(result.getRecords());
        return PageResult.of(result.getRecords(), result.getTotal(), page, size);
    }

    @Override
    public Essay getEssayById(Long id) {
        var essay = essayMapper.selectById(id);
        if (essay == null) throw new BusinessException("随笔不存在");
        var user = userMapper.selectById(essay.getUserId());
        if (user != null) {
            essay.setUsername(user.getUsername());
            essay.setNickname(user.getNickname());
            essay.setAvatar(user.getAvatar());
        }
        return essay;
    }

    @Override
    public Essay createEssay(Essay essay) {
        essay.setUserId(SecurityUtils.getCurrentUserId());
        essay.setStatus(1);
        essayMapper.insert(essay);
        var user = userMapper.selectById(1L);
        if (user != null) {
            essay.setUsername(user.getUsername());
            essay.setNickname(user.getNickname());
            essay.setAvatar(user.getAvatar());
        }
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
