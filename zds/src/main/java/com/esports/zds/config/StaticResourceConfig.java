package com.esports.zds.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 静态资源配置
 */
@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置上传文件的访问路径
        String uploadPath = System.getProperty("user.dir") + "/upload/";
        registry.addResourceHandler("/upload/**")
                .addResourceLocations("file:" + uploadPath)
                .setCachePeriod(0); // 禁用缓存，方便测试
        
        // 配置其他静态资源
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/");
    }
}
