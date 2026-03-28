package com.esports.zds.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 安全工具类
 */
@Component
public class SecurityUtils {

    private static JwtUtils jwtUtils;

    @Autowired
    public void setJwtUtils(JwtUtils jwtUtils) {
        SecurityUtils.jwtUtils = jwtUtils;
    }

    /**
     * 从请求中获取当前用户ID
     */
    public static Long getCurrentUserId() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            Claims claims = jwtUtils.parseToken(token);
            return Long.parseLong(claims.get("userId").toString());
        }
        return null;
    }

    /**
     * 从请求中获取当前用户角色
     */
    public static String getCurrentUserRole() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            Claims claims = jwtUtils.parseToken(token);
            return claims.get("role").toString();
        }
        return null;
    }
}
