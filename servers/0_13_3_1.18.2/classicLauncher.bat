@echo off
title [FK]

:loop

rem java -Xms8G -Xmx8G -jar fabric-server-launch.jar

"C:\Program Files\BellSoft\LibericaJDK-17-Full\bin\java.exe" -Xms4G -Xmx4G -jar fabric-server-launch.jar nogui

if "%1"=="stop" goto end

timeout /t 20 /nobreak

goto loop
:end
exit
