package com.lune.service;

import com.lune.common.PageResult;
import com.lune.entity.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface ResourceService {
    Resource upload(MultipartFile file);
    Resource importFromUrl(String url);
    PageResult<Resource> listResources(int page, int size);
    void deleteResource(Long id);
}
