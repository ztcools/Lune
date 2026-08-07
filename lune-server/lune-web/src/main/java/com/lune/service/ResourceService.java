package com.lune.service;

import com.lune.common.PageResult;
import com.lune.entity.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface ResourceService {
    Resource upload(MultipartFile file);
    Resource importFromUrl(String url);
    /** 从外部URL下载图片后存COS（Agent专用） */
    Resource uploadFromUrl(String url);
    /** base64解码后走正常upload管道（粘贴上传） */
    Resource uploadBase64(String base64Data, String filename);
    /**
     * 资源列表，可按 {@code type} 过滤（{@code null}/空 表示不过滤）。
     *
     * <p>后台资源管理页一直有一个「资源类型」下拉和「搜索」按钮，但过滤条件从未
     * 送到后端 —— 选完点搜索只是回到第 1 页。这里把参数补上，让那个下拉真正生效。
     */
    PageResult<Resource> listResources(int page, int size, String type);
    void deleteResource(Long id);
}
