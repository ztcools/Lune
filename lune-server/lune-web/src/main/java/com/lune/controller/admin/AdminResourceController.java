package com.lune.controller.admin;

import com.lune.common.PageResult;
import com.lune.common.Result;
import com.lune.entity.Resource;
import com.lune.service.ResourceService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/resources")
@PreAuthorize("hasRole('ADMIN')")
public class AdminResourceController {

    private final ResourceService resourceService;

    public AdminResourceController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @GetMapping
    public Result<PageResult<Resource>> list(@RequestParam(defaultValue = "1") int page,
                                              @RequestParam(defaultValue = "20") int size,
                                              @RequestParam(required = false) String type) {
        return Result.success(resourceService.listResources(page, size, type));
    }

    @PostMapping("/upload")
    public Result<Resource> upload(@RequestParam("file") MultipartFile file) {
        return Result.success(resourceService.upload(file));
    }

    @PostMapping("/import-url")
    public Result<Resource> importUrl(@RequestBody Map<String, String> body) {
        String url = body.get("url");
        return Result.success(resourceService.importFromUrl(url));
    }

    /** Agent 专用：从外部 URL 下载图片 → 转 MultipartFile → 走正常 upload 管道 → 存 COS */
    @PostMapping("/upload-from-url")
    public Result<Resource> uploadFromUrl(@RequestBody Map<String, String> body) {
        String url = body.get("url");
        return Result.success(resourceService.uploadFromUrl(url));
    }

    /** 粘贴上传：base64 → MultipartFile → 走正常 upload 管道 */
    @PostMapping("/upload-base64")
    public Result<Resource> uploadBase64(@RequestBody Map<String, String> body) {
        String base64 = body.get("base64");
        String filename = body.getOrDefault("filename", "clipboard.jpg");
        return Result.success(resourceService.uploadBase64(base64, filename));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        resourceService.deleteResource(id);
        return Result.success();
    }
}
