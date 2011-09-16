@echo off
if "%ANDROID_HOME%" == "" (set DDMLIB=%~dp0\lib\ddmlib.jar) else (set DDMLIB=%ANDROID_HOME%\tools\lib\ddmlib.jar)
set CLASSES=%DDMLIB%;%~dp0\lib\logview.jar
start javaw -cp %CLASSES% org.bitbucket.mlopatkin.android.logviewer.Main %*