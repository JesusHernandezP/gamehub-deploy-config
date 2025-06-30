# --- ETAPA DE CONSTRUCCIÓN (BUILD STAGE) ---
# Usa una imagen base con JDK y Maven para construir el proyecto (optimizada para x64)
FROM maven:3.9-eclipse-temurin-17 as builder

# Establece el directorio de trabajo dentro del contenedor
WORKDIR /app

# Copia el archivo pom.xml para descargar las dependencias de Maven.
# Esto aprovecha el caché de Docker: si el pom.xml no cambia, no se vuelven a descargar las dependencias.
COPY pom.xml .

# Descarga las dependencias de Maven (sin construir el JAR final)
RUN mvn dependency:go-offline -B

# Copia el resto del código fuente
COPY src ./src

# Construye la aplicación Spring Boot en un JAR ejecutable
RUN mvn clean install -DskipTests

# --- ETAPA DE EJECUCIÓN (RUNTIME STAGE) ---
# Usa una imagen base más ligera con solo el JRE para la ejecución
FROM eclipse-temurin:17-jre-jammy

# Establece un argumento para el nombre del archivo JAR (útil si tu JAR cambia de nombre)
ARG JAR_FILE=target/*.jar

# Copia el JAR ejecutable desde la etapa de construcción a la etapa de ejecución
COPY --from=builder /app/${JAR_FILE} app.jar

# Expone el puerto en el que la aplicación Spring Boot escuchará (por defecto 8080)
EXPOSE 8080

# Comando para ejecutar la aplicación cuando se inicie el contenedor
ENTRYPOINT ["java","-jar","/app.jar"]