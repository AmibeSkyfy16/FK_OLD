title Starting server
@echo off

Taskkill /FI "WINDOWTITLE eq [FK]"

timeout /t 3 > nul

cd /d .\0_13_3_1.18.2
cmd /C start classicLauncher.bat

exit