package com.esports.zds.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * 文件上传配置类
 */
@Configuration
public class UploadConfig {

    @Value("${file.upload.path}")
    private String uploadPath;

    @Value("${file.upload.url-prefix}")
    private String urlPrefix;

    public String getUploadPath() {
        return uploadPath;
    }

    public String getUrlPrefix() {
        return urlPrefix;
    }

    /**
     * 获取静态资源访问路径（用于Spring资源处理器）
     * 自动处理Windows和Linux路径格式差异
     */
    public String getResourceLocation() {
        if (uploadPath == null || uploadPath.isEmpty()) {
            return "file:./upload/";
        }
        
        String normalizedPath = uploadPath.replace("\\", "/");
        
        if (normalizedPath.startsWith("/")) {
            return "file:" + normalizedPath + "/";
        } else {
            return "file:///" + normalizedPath + "/";
        }
    }
}
