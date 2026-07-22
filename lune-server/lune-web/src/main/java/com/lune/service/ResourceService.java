package com.lune.service;

import com.lune.entity.Resource;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface ResourceService {
    Resource upload(MultipartFile file);
    List<Resource> listResources(int page, int size);
    void deleteResource(Long id);
}
