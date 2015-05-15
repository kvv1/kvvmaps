set WARFILE=heliostat.war

call war.bat

copy %WARFILE% "d:\Tomcat 7.0\webapps\"

pause