@echo off
chcp 65001 >nul
cd /d "%~dp0"
title Tienda - NO CIERRES ESTA VENTANA

rem La clave de la base de datos se lee de config.bat (ese archivo NO se sube a git).
rem Si no existe, copia config.ejemplo.bat como config.bat y escribe la clave.
if exist "%~dp0config.bat" call "%~dp0config.bat"

if "%DB_PASS%"=="" (
  echo Falta la clave de la base de datos.
  echo Copia config.ejemplo.bat como config.bat y escribe la clave dentro.
  pause
  exit /b 1
)

echo ============================================
echo   Iniciando la tienda, espera un momento...
echo   NO CIERRES ESTA VENTANA mientras uses el sistema.
echo   Para apagar la tienda, cierra esta ventana.
echo ============================================

start "" "%~dp0Cargando.html"
java -jar tienda-api.jar

echo.
echo La tienda se detuvo. Si fue un error, tomale foto a esta pantalla.
pause
