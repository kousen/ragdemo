@echo off
setlocal EnableExtensions EnableDelayedExpansion

set "SCRIPT_DIR=%~dp0"
set "ENV_FILE=%SCRIPT_DIR%.env"

if not exist "%ENV_FILE%" (
  set "ENV_FILE=%SCRIPT_DIR%..\.env"
)

if exist "%ENV_FILE%" (
  for /f "usebackq tokens=* delims=" %%L in ("%ENV_FILE%") do (
    set "line=%%L"
    if not "!line!"=="" if not "!line:~0,1!"=="#" (
      for /f "tokens=1* delims==" %%A in ("!line!") do (
        if not "%%A"=="" set "%%A=%%B"
      )
    )
  )
)

call "%SCRIPT_DIR%gradlew.bat" bootRun %*
endlocal
