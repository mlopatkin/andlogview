@echo off
if "%ANDROID_HOME%" == "" (set DDMLIB=lib/ddmlib.jar) else (set DDMLIB=%ANDROID_HOME%\tools\lib\ddmlib.jar)
set CLASSES=%DDMLIB%;lib/logview.jar
start javaw -cp %CLASSES% org.bitbucket.mlopatkin.android.logviewer.Main %*