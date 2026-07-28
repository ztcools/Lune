package com.lune.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lune.common.BusinessException;
import com.lune.entity.WorkExperience;
import com.lune.mapper.WorkExperienceMapper;
import com.lune.security.SecurityUtils;
import com.lune.service.WorkExperienceService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WorkExperienceServiceImpl implements WorkExperienceService {

    private final WorkExperienceMapper mapper;

    public WorkExperienceServiceImpl(WorkExperienceMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<WorkExperience> listAll() {
        return mapper.selectList(new LambdaQueryWrapper<WorkExperience>()
                .orderByAsc(WorkExperience::getSortOrder)
                .orderByDesc(WorkExperience::getStartDate));
    }

    @Override
    public List<WorkExperience> listPublic() {
        return mapper.selectList(new LambdaQueryWrapper<WorkExperience>()
                .eq(WorkExperience::getStatus, 1)
                .orderByAsc(WorkExperience::getSortOrder)
                .orderByDesc(WorkExperience::getStartDate));
    }

    @Override
    public WorkExperience create(WorkExperience item) {
        item.setId(null);
        item.setUserId(SecurityUtils.getCurrentUserId());
        if (item.getStatus() == null) item.setStatus(1);
        if (item.getSortOrder() == null) item.setSortOrder(0);
        if (Boolean.TRUE.equals(item.getIsCurrent())) item.setEndDate(null);
        mapper.insert(item);
        return item;
    }

    @Override
    public WorkExperience update(Long id, WorkExperience item) {
        var exist = mapper.selectById(id);
        if (exist == null) throw new BusinessException("工作经历不存在");
        exist.setCompany(item.getCompany());
        exist.setPosition(item.getPosition());
        exist.setLocation(item.getLocation());
        exist.setStartDate(item.getStartDate());
        exist.setIsCurrent(item.getIsCurrent());
        exist.setEndDate(Boolean.TRUE.equals(item.getIsCurrent()) ? null : item.getEndDate());
        exist.setDescription(item.getDescription());
        exist.setResponsibilities(item.getResponsibilities());
        exist.setMedia(item.getMedia());
        if (item.getSortOrder() != null) exist.setSortOrder(item.getSortOrder());
        if (item.getStatus() != null) exist.setStatus(item.getStatus());
        mapper.updateById(exist);
        return exist;
    }

    @Override
    public void delete(Long id) {
        mapper.deleteById(id);
    }
}
