@echo off
if "%ANDROID_HOME%" == "" (set DDMLIB=lib/ddmlib.jar) else (set DDMLIB=%ANDROID_HOME%\tools\lib\ddmlib.jar)
set CLASSPATH=%DDMLIB%;lib/logview.jar
start javaw org.bitbucket.mlopatkin.android.logviewer.Main %*