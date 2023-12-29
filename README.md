# CodesForGamers

CodesForGamers es una aplicación Web que hace de tienda para códigos de juegos los cuales se canjean en plataformas externas.

# Requisitos Previos

Antes de poner en marcha el proyecto de CodesForGamers se han de obtener los programas siguientes:

- [Java JDK 17](https://www.oracle.com/es/java/technologies/downloads/#jdk17-windows)
- [Maven](https://maven.apache.org/download.cgi)
- [Docker Desktop](https://www.docker.com/products/docker-desktop/)


## Credenciales APIs Externas

Una vez tenemos todos los softwares necesarios para ejecutar la aplicacion, tendremos que configurar las claves de las APIs externas siguientes:


### user API:
En el mismo [application.properties](user/src/main/resources/application.properties) se deben configurar las keys para las distintas APIs externas:

- **Youtube data Api v3:**
  - **youtube.api.key**:
    
  
Como obtener la clave de Youtbe Data Api v3 en el siguiente tutorial: https://www.youtube.com/watch?v=qWUobN0xtcE


- **Paypal:**
  - **paypal.client.id**
  - **paypal.client.secret**
  - **paypal.client.mode**

Como obtener las claves de PayPal en el siguiente tutorial:
https://www.upwork.com/resources/paypal-client-id-secret-key

### notificationApp API:

En el archivo de configuración [application.properties](notificationApp/src/main/resources/application.properties) se deben configurar las credenciales para la notificion mail:

- Gmail:
  - **spring.mail.username**
  - **spring.mail.password**

Como obtener la clave de Gmail en el siguiente tutorial:
https://www.sysinfotools.com/how-to/generate-app-password-in-gmail.html


# Despliegue

El despliegue se ha realizado en el S.O de Windows 10 y se han detallado dos formas para conseguirlo:

### Windows 10 - Scripts:

Si se dispone de este S.O, se han creado dos ficheros para el lanzamiento de la aplicacion:

- [__build_and_start__](build_and_start.bat): Se compila el código y se ejecuta la aplicacion. 
__(Primera vez que se lanza la aplicación y/o se ha cambiado el código de alguna de las APIs)__


- [__start.bat__](start.bat): Se ejecuta la aplicación. 



###  Windows 10 - Comandos:

Si se desea ejecutar las APIs de con comandos a partir de CMD/BASH, se ha de seguir el siguiente orden:

**1. Levantamiendo de los contendores** 

-  Nos dirigimos al PATH del Proyecto y lanzamos los contenedores Docker


```
cd PROYECTO
docker compose up -d
```


**2. Compilación de código** (Saltar si ya se disponen de los .jars)

- Ahora compilaremos las 3 APIs con los siguiente comandos

```
cd PROYECTO
cd gamecatalog
mvn clean install -DskipTest
cd ..

cd user
mvn clean install -DskipTest
cd ..

cd notificationApp
mvn clean install -DskipTest
cd ..
```


**3. Ejecución de las APIs** 

- Para finalizar lanzaremos las 3 APIs con los siguientes comandos.

```
cd PROYECTO
start java -jar gamecatalog/target/gamecatalog-0.0.1-SNAPSHOT.jar
start java -jar user/target/user-0.0.1-SNAPSHOT.jar
start java -jar notificationApp/target/notificationApp-SNAPSHOT.jar
```

# Funcionamiento

Una vez levantado los contenedores correctamente como asi los archivos .jar, la aplicacion se encontrará en funcionamiento.

Para acceder a la web, vaya **https://localhost/home** donde se encuentra la apliación 
en marcha.

También puede acceder a **http://localhost:18081/swagger-ui/index.html** para acceder al Swagger de la API de **gamecatalog** donde encontrará todos los métodos implementados de esta API


# Configuración

De forma predeterminada, la aplicación tiene información ya agregada:

- 20 Juegos y 10 Categorías.
  

- Un usuario registrado para las funciones de Administrador:
  - Acceda a [application.properties](user/src/main/resources/application.properties) para obtener la credenciales: 
    - **web.admin.email**
    - **web.admin.password**
