@echo off
cd src
javac -encoding UTF-8 com/thegame/web/SimpleWebServer.java
echo Starting Web Server...
java com.thegame.web.SimpleWebServer
pause
