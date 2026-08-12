@REM Maven Wrapper startup batch script
@echo off
setlocal
set "MVNW_PROJECTBASEDIR=%~dp0"
set "MVNW_MULTI_MODULE_DIR=%MVNW_PROJECTBASEDIR:~0,-1%"
set "MVNW_WRAPPER_URL=https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.3.2/maven-wrapper-3.3.2.jar"
set "MVNW_WRAPPER_JAR=%MVNW_PROJECTBASEDIR%.mvn\wrapper\maven-wrapper.jar"
if not exist "%MVNW_WRAPPER_JAR%" (
  powershell -NoProfile -ExecutionPolicy Bypass -Command "Invoke-WebRequest -Uri '%MVNW_WRAPPER_URL%' -OutFile '%MVNW_WRAPPER_JAR%'"
  if errorlevel 1 exit /b 1
)
java %MAVEN_OPTS% -classpath "%MVNW_WRAPPER_JAR%" "-Dmaven.multiModuleProjectDirectory=%MVNW_MULTI_MODULE_DIR%" org.apache.maven.wrapper.MavenWrapperMain %*
if errorlevel 1 exit /b %errorlevel%
endlocal
