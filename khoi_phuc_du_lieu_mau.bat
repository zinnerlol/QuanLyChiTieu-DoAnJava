@echo off
chcp 65001 >nul
cd /d "%~dp0"
echo CANH BAO: lenh nay se XOA toan bo du lieu hien tai va tao lai du lieu mau ban dau.
echo Nhan phim bat ky de tiep tuc, hoac dong cua so de huy.
pause >nul
del /q data\quanlychitieu.mv.db 2>nul
java -Dfile.encoding=UTF-8 -cp "lib/h2-2.2.220.jar" org.h2.tools.RunScript -url "jdbc:h2:file:./data/quanlychitieu" -user sa -script sql/data_h2.sql
echo.
echo Xong. Da tao lai database o thu muc data.
pause
