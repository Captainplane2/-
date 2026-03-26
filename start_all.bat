@echo off
chcp 65001
title Esports Project Starter
echo ==========================================
echo   高校电竞约战赛事平台 - 一键启动脚本
echo ==========================================

:: 启动后端
echo [1/2] 正在启动后端服务 (Spring Boot)...
start "Backend - zds" cmd /k "cd zds && mvn spring-boot:run"

:: 等待几秒确保后端优先启动（可选）
timeout /t 5 /nobreak > nul

:: 启动前端
echo [2/2] 正在启动前端服务 (Vue 3)...
start "Frontend - esports-web" cmd /k "cd esports-web && npm run dev"

echo ==========================================
echo   服务已在独立窗口中启动！
echo   后端地址: http://localhost:8080
echo   前端地址: http://localhost:5173
echo   控制台地址: http://localhost:5173/admin
echo ==========================================
pause
