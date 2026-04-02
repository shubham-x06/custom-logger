@echo off
set JAR_PATH=%~dp0target\logger-cli.jar

if not exist "%JAR_PATH%" (
    echo ERROR: JAR not found at %JAR_PATH%. Please run 'mvn clean package' first.
    exit /b 1
)

java -jar "%JAR_PATH%" %*
