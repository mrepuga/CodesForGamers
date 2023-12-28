@echo off
docker-compose up -d

cd "gamecatalog/target"
start java -jar gamecatalog-0.0.1-SNAPSHOT.jar

cd ..
cd ..

cd "user/target"
start java -jar user-0.0.1-SNAPSHOT.jar

cd ..
cd ..

cd "notificationApp/target"
start java -jar notificationApp-0.0.1-SNAPSHOT.jar

echo Proceso completo.
pause