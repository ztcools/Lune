package com.lune.service;

import com.lune.entity.Project;
import java.util.List;

public interface ProjectService {
    List<Project> listAll();
    List<Project> listPublic();
    Project create(Project item);
    Project update(Long id, Project item);
    void delete(Long id);
}
