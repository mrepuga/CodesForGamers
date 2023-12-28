@echo off
docker-compose up -d

cd "gamecatalog"
call mvn clean install -DskipTests

cd "target"
start java -jar gamecatalog-0.0.1-SNAPSHOT.jar

cd ..
cd ..

cd "user"
call mvn clean install -DskipTests

cd ..
rem Copy the contents of "classes" directory to "target/classes"
xcopy /s /y classes\ user\target\classes\


cd "user\target"
start java -jar user-0.0.1-SNAPSHOT.jar

cd ..
cd ..

cd "notificationApp"
call mvn clean install -DskipTests

cd "target"
start java -jar notificationApp-0.0.1-SNAPSHOT.jar

echo Proceso completo.
pause