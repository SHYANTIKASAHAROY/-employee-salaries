@echo off
chcp 65001 >nul
title Employee Salary Management System
cd /d "%~dp0"
echo =======================================================
echo    Launching Employee Salary Management System
echo =======================================================
echo.
java -cp ".;*" EmployeeSalaryApp
pause
