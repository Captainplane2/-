package com.esports.zds.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web 配置类 (跨域与拦截器)
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private LoginInterceptor loginInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 添加登录拦截器，指定拦截路径和排除路径
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/api/**") // 拦截所有接口
                .excludePathPatterns(
                    "/api/user/login",      // 登录接口放行
                    "/api/user/register",   // 注册接口放行
                    "/api/notice/list",     // 公告列表放行
                    "/api/notice/*",        // 公告详情放行
                    "/api/team/list",       // 战队列表放行
                    "/api/match/list"       // 约赛列表放行
                );
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 设置允许跨域的路径
        registry.addMapping("/**")
                // 设置允许跨域请求的域名
                .allowedOriginPatterns("*")
                // 是否允许证书
                .allowCredentials(true)
                // 设置允许的方法
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                // 设置允许的 header
                .allowedHeaders("*")
                // 跨域允许时间
                .maxAge(3600);
    }
}
