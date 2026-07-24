@ECHO OFF
SETLOCAL

SET "PROJECT_DIR=%~dp0"
SET "WRAPPER_JAR=%PROJECT_DIR%.mvn\wrapper\maven-wrapper.jar"

IF NOT EXIST "%WRAPPER_JAR%" (
  ECHO Maven Wrapper JAR not found: %WRAPPER_JAR%
  EXIT /B 1
)

java %MAVEN_OPTS% -classpath "%WRAPPER_JAR%" "-Dmaven.multiModuleProjectDirectory=%PROJECT_DIR%" org.apache.maven.wrapper.MavenWrapperMain %*
ENDLOCAL

