del zavorovo.war

xcopy /S /Y ..\Controller\bin\* war\WEB-INF\classes\
xcopy /S /Y ..\Constants\bin\* war\WEB-INF\classes\
xcopy /S /Y ..\History\bin\* war\WEB-INF\classes\
xcopy /S /Y ..\ControllerUtils\bin\* war\WEB-INF\classes\
xcopy /S /Y ..\evlang\bin\* war\WEB-INF\classes\
xcopy /S /Y ..\vm\bin\* war\WEB-INF\classes\

pushd war
jar cvf ..\zavorovo.war *
popd

rd /S /Q war\WEB-INF\classes
