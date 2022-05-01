rem https://serverfault.com/questions/77762/how-to-reconcile-the-following-error-jvm-cannot-use-large-page-memory-because#:~:text=In%20order%20to%20use%20it,add%20users%20and%2For%20groups
rem https://github.com/hilltty/hilltty-flags/blob/main/english-lang.md
rem https://aikar.co/2018/07/02/tuning-the-jvm-g1gc-garbage-collector-flags-for-minecraft/

@echo off
title [MTEA]->[HighPerformanceLauncher.bat]->[Fabric 0.13.3]
cd %~dp0

:loop

java -jar -server -Xms16G -Xmx16G -XX:+UseLargePages -XX:LargePageSizeInBytes=2M -XX:+UnlockExperimentalVMOptions -XX:+UseShenandoahGC -XX:ShenandoahGCMode=iu -XX:+UseNUMA -XX:+AlwaysPreTouch -XX:-UseBiasedLocking -XX:+DisableExplicitGC -Dfile.encoding=UTF-8 fabric-server-launch.jar nogui

if "%1"=="stop" goto end

timeout /t 20 /nobreak

goto loop
:end
exit
