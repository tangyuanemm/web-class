@echo off
title QQ Web Chat

echo ========================================
echo   仿QQ Web即时聊天系统
echo ========================================
echo.

:: JDK 17
set JAVA_HOME=%USERPROFILE%\.jdks\ms-17.0.17
set PATH=%JAVA_HOME%\bin;%PATH%

:: 切换到脚本所在目录
cd /d "%~dp0"

:: ── 后端 ──
echo [1/2] 启动后端...
start "QQWeb-Backend" cmd /c "cd /d %~dp0后端 && %JAVA_HOME%\bin\java -jar target\qqweb-backend-1.0.0.jar"
echo         后端: http://localhost:8080

:: 等后端启动
echo         等待后端启动中...
timeout /t 8 /nobreak >nul

:: ── 前端 ──
echo [2/2] 启动前端...
start "QQWeb-Frontend" cmd /c "cd /d %~dp0frontend && npx vite --host --port 5173"
echo         前端: http://localhost:5173

timeout /t 3 /nobreak >nul

echo.
echo ========================================
echo   启动完成！
echo   管理员: admin / admin123
echo   测试号: zhangsan / 123
echo           lisi / 123
echo ========================================
echo.
echo 正在打开浏览器...
start http://localhost:5173
pause
