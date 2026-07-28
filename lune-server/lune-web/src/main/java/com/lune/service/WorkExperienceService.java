package com.lune.service;

import com.lune.entity.WorkExperience;
import java.util.List;

public interface WorkExperienceService {
    List<WorkExperience> listAll();
    List<WorkExperience> listPublic();
    WorkExperience create(WorkExperience item);
    WorkExperience update(Long id, WorkExperience item);
    void delete(Long id);
}
