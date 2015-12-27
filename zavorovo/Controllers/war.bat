del zavorovo.war

xcopy /S /Y ..\Controller\bin\* war\WEB-INF\classes\
xcopy /S /Y ..\..\GwtUtils\war\WEB-INF\classes\* war\WEB-INF\classes\
xcopy /S /Y ..\..\SimpleUtils\bin\* war\WEB-INF\classes\
xcopy /S /Y ..\..\StdUtils\bin\* war\WEB-INF\classes\
xcopy /S /Y ..\..\ExprCalc\bin\* war\WEB-INF\classes\

pushd war
"D:\Program Files\Java\jdk1.8.0_66\bin\jar" cvf ..\zavorovo.war *
popd

rd /S /Q war\WEB-INF\classes

