set WARDIR=WebContent
set WARFILE=controllers.war

del %WARFILE%

xcopy /S /Y build\classes\* %WARDIR%\WEB-INF\classes\
xcopy /S /Y ..\ControllerUtils\bin\* %WARDIR%\WEB-INF\classes\

pushd %WARDIR%
jar cvf ..\%WARFILE% *
popd

rd /S /Q %WARDIR%\WEB-INF\classes


