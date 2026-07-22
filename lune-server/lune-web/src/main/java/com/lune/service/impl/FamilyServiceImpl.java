package com.lune.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lune.common.BusinessException;
import com.lune.entity.Family;
import com.lune.mapper.FamilyMapper;
import com.lune.service.FamilyService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FamilyServiceImpl implements FamilyService {

    private final FamilyMapper familyMapper;

    public FamilyServiceImpl(FamilyMapper familyMapper) {
        this.familyMapper = familyMapper;
    }

    @Override
    public List<Family> listFamilies() {
        return familyMapper.selectList(new LambdaQueryWrapper<Family>()
                .eq(Family::getStatus, 1)
                .orderByDesc(Family::getCreateTime));
    }

    @Override
    public Family createFamily(Family family) {
        family.setUserId(1L);
        family.setStatus(1);
        familyMapper.insert(family);
        return family;
    }

    @Override
    public Family updateFamily(Long id, Family family) {
        var exist = familyMapper.selectById(id);
        if (exist == null) throw new BusinessException("不存在");
        exist.setTitle(family.getTitle());
        exist.setContent(family.getContent());
        exist.setCover(family.getCover());
        exist.setBgCover(family.getBgCover());
        exist.setTiming(family.getTiming());
        exist.setCountdownTitle(family.getCountdownTitle());
        exist.setCountdownTime(family.getCountdownTime());
        exist.setStatus(family.getStatus());
        familyMapper.updateById(exist);
        return exist;
    }

    @Override
    public void deleteFamily(Long id) {
        familyMapper.deleteById(id);
    }
}
