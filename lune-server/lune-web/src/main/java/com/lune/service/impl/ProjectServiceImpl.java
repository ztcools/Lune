package com.lune.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lune.common.BusinessException;
import com.lune.entity.Project;
import com.lune.mapper.ProjectMapper;
import com.lune.security.SecurityUtils;
import com.lune.service.ProjectService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectServiceImpl implements ProjectService {

    private final ProjectMapper mapper;

    public ProjectServiceImpl(ProjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<Project> listAll() {
        return mapper.selectList(new LambdaQueryWrapper<Project>()
                .orderByAsc(Project::getSortOrder)
                .orderByDesc(Project::getCreateTime));
    }

    @Override
    public List<Project> listPublic() {
        return mapper.selectList(new LambdaQueryWrapper<Project>()
                .eq(Project::getStatus, 1)
                .orderByAsc(Project::getSortOrder)
                .orderByDesc(Project::getCreateTime));
    }

    @Override
    public Project create(Project item) {
        item.setId(null);
        item.setUserId(SecurityUtils.getCurrentUserId());
        if (item.getStatus() == null) item.setStatus(1);
        if (item.getSortOrder() == null) item.setSortOrder(0);
        mapper.insert(item);
        return item;
    }

    @Override
    public Project update(Long id, Project item) {
        var exist = mapper.selectById(id);
        if (exist == null) throw new BusinessException("项目不存在");
        exist.setName(item.getName());
        exist.setSummary(item.getSummary());
        exist.setDescription(item.getDescription());
        exist.setTechStack(item.getTechStack());
        exist.setRole(item.getRole());
        exist.setProjectUrl(item.getProjectUrl());
        exist.setRepoUrl(item.getRepoUrl());
        exist.setCover(item.getCover());
        exist.setMedia(item.getMedia());
        exist.setDevPeriod(item.getDevPeriod());
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
