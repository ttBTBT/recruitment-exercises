@echo off
chcp 65001 >nul
cd /d "%~dp0"
call mvn -f web/pom.xml spring-boot:run
if errorlevel 1 pause
