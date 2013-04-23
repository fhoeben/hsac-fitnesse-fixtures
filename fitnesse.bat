@echo off
echo After the message that Fitnesse is started point your browser to:
echo    http://localhost/
echo.
echo You can stop Fitnesse by pressing Ctrl+C, or by closing this window.
echo.
cd wiki
java -jar fitnesse-standalone.jar
