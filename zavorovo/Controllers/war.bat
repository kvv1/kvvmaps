del zavorovo.war

xcopy /S /Y ..\Controller\bin\* war\WEB-INF\classes\
xcopy /S /Y ..\..\GwtUtils\war\WEB-INF\classes\* war\WEB-INF\classes\
xcopy /S /Y ..\..\SimpleUtils\bin\* war\WEB-INF\classes\
xcopy /S /Y ..\..\StdUtils\bin\* war\WEB-INF\classes\
xcopy /S /Y ..\..\ExprCalc\bin\* war\WEB-INF\classes\

pushd war
jar cvf ..\zavorovo.war *
popd

rd /S /Q war\WEB-INF\classes

