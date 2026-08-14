package com.lune.agent.pipeline.tools;

import cn.hutool.json.JSONObject;
import com.lune.agent.client.LuneApiClient;
import com.lune.agent.pipeline.ToolHandler;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static com.lune.agent.pipeline.ToolSupport.*;

/** 图片资源工具处理器。 */
@Component
public class ResourceHandler implements ToolHandler {

    private final LuneApiClient api;

    public ResourceHandler(LuneApiClient api) { this.api = api; }

    @Override
    public List<String> names() {
        return List.of("upload_image");
    }

    @Override
    public Map<String, Object> execute(String name, Map<String, Object> a, String token) {
        String url = (String) a.get("url");
        if (url == null || url.isBlank()) return m("success", false, "message", "请提供图片URL");
        String uploadPath = extractUploadPath(url);
        if (uploadPath != null) {
            var p = new JSONObject();
            p.set("path", uploadPath);
            return m("success", true, "message", "图片已就绪", "url", uploadPath, "preview", p);
        }
        if (!isSafeImageUrl(url)) return m("success", false, "message", "图片URL不合法（仅支持公网 http/https，禁止内网地址）");
        var opt = api.uploadFromUrl(url, token);
        if (opt.isPresent()) return m("success", true, "message", "图片已上传", "url", opt.get().get("path"), "preview", preview(opt.get()));
        return m("success", false, "message", "上传失败");
    }

    /** SSRF 防护：仅允许公网 http/https，拒绝 file://、内网/环回/链路本地/云元数据地址。 */
    private static boolean isSafeImageUrl(String url) {
        try {
            var u = java.net.URI.create(url);
            var scheme = u.getScheme();
            if (scheme == null || !(scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))) return false;
            var host = u.getHost();
            if (host == null || host.isBlank()) return false;
            host = host.toLowerCase();
            if (host.equals("localhost") || host.endsWith(".localhost")
                    || host.equals("169.254.169.254") || host.equals("metadata.google.internal")) return false;
            if (host.matches("\\d{1,3}(\\.\\d{1,3}){3}")) {
                var ip = java.net.InetAddress.getByName(host);
                if (ip.isLoopbackAddress() || ip.isLinkLocalAddress() || ip.isSiteLocalAddress()
                        || ip.isAnyLocalAddress() || ip.isMulticastAddress()) return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static String extractUploadPath(String url) {
        int idx = url.indexOf("/upload/");
        if (idx >= 0) {
            String path = url.substring(idx);
            int end = path.length();
            for (int i = 0; i < path.length(); i++) {
                char c = path.charAt(i);
                if (c == ' ' || c == '\n' || c == '\r' || c == ',' || c == ']' || c == '}') { end = i; break; }
            }
            return path.substring(0, end);
        }
        return null;
    }
}
