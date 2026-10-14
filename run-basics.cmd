@echo off
chcp 65001 >nul
if "%~1"=="" (echo Usage: run-basics.cmd HelloWorld & exit /b 1)
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0basicsun.ps1" -Main "%~1"
