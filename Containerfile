# Etapa de compilación
FROM quay-registry-quay-quay-registry.apps.ocp4-mh.cloudteco.com.ar/devops/openjdk-21:1.20 AS build

# Copia los archivos de configuración del proyecto
COPY pom.xml /usr/src/app/
COPY src /usr/src/app/src

# Establece el directorio de trabajo para la construcción
WORKDIR /usr/src/app

USER root
# Compila el proyecto y empaca el ejecutable
RUN mvn -B clean package -DskipTests

USER default

# Etapa de ejecución
FROM quay-registry-quay-quay-registry.apps.ocp4-mh.cloudteco.com.ar/devops/openjdk-21:1.20

# Copia el JAR desde la etapa de compilación
COPY --from=build /usr/src/app/target/*.jar /usr/app/app.jar

# Define el directorio de trabajo
WORKDIR /usr/app

# No es necesario exponer un puerto si la aplicación está sellada y no recibe llamadas por API

# Ejecuta la aplicación Spring Boot
ENTRYPOINT ["java","-jar","app.jar"]