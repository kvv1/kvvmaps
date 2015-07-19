del heliostat.war

xcopy /S /Y ..\..\GwtUtils\war\WEB-INF\classes\* war\WEB-INF\classes\

pushd war
jar cvf ..\heliostat.war *
popd

rd /S /Q war\WEB-INF\classes
