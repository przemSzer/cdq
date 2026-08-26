@echo off
setlocal
set "SCRIPT=%~dp0weather-mcp.mjs"

where node >nul 2>&1
if errorlevel 1 (
  echo Node.js is not installed or not on PATH.
  echo Download the LTS installer: https://nodejs.org/
  echo After install, close this window and try again.
  pause
  exit /b 1
)

node "%SCRIPT%" %*
exit /b %ERRORLEVEL%
