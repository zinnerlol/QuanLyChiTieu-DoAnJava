@echo off
chcp 65001 >nul
cd /d "%~dp0"
echo Dang bien dich...
javac -encoding UTF-8 -cp "lib/*" -sourcepath src -d bin src\LoginFrame.java
if errorlevel 1 (
    echo.
    echo BIEN DICH LOI. Kiem tra da cai JDK ^(ban 17 tro len^) chua: go lenh  javac -version
    pause
    exit /b 1
)
echo Dang chay chuong trinh...
java -cp "bin;lib/*" LoginFrame
