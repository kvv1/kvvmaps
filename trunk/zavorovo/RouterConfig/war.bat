set WARDIR=WebContent
set WARFILE=rconfig.war

del %WARFILE%

xcopy /S /Y build\classes\* %WARDIR%\WEB-INF\classes\

pushd %WARDIR%
jar cvf ..\%WARFILE% *
popd

rd /S /Q %WARDIR%\WEB-INF\classes


