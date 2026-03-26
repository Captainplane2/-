package com.esports.zds.config;

import java.io.PrintWriter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.esports.zds.common.JwtUtils;
import com.esports.zds.common.Result;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * JWT 登录拦截器
 */
@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 放行 OPTIONS 请求
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }

        // 1. 从请求头获取 token
        String token = request.getHeader("Authorization");
        
        // 如果是 "Bearer token" 格式，截取后面部分
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        try {
            if (token != null) {
                // 2. 解析并验证 token
                Claims claims = jwtUtils.parseToken(token);
                // 3. 将解析出的用户信息存入 request，方便后续使用
                request.setAttribute("userId", claims.get("userId"));
                request.setAttribute("username", claims.get("username"));
                request.setAttribute("role", claims.get("role"));
                return true;
            }
        } catch (Exception e) {
            // token 无效或过期
        }

        // 4. 认证失败，返回错误信息
        response.setContentType("application/json;charset=utf-8");
        Result<Void> result = Result.error(401, "请先登录");
        String json = new ObjectMapper().writeValueAsString(result);
        PrintWriter writer = response.getWriter();
        writer.print(json);
        writer.flush();
        writer.close();
        return false;
    }
}
