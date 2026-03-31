package com.esports.zds.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 静态资源配置
 */
@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    @Autowired
    private UploadConfig uploadConfig;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadConfig.getResourceLocation())
                .setCachePeriod(0);
        
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/");
    }
}
