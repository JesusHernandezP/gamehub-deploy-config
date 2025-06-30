# 🕹️ GameHub API - Backend para Organización de Torneos de Videojuegos

## 📝 Descripción del Proyecto

Este proyecto es el backend de una plataforma para la creación, gestión y seguimiento de torneos de videojuegos online.
Ofrece una API RESTful completa.

Las funcionalidades clave incluyen:

* **Gestión Integral de Usuarios:** Registro, inicio de sesión (JWT) y perfiles de usuario.
* **Creación y Administración de Torneos:** Funcionalidad para organizar y gestionar diversos tipos de torneos.
* **Sistema de Emparejamiento Automatizado:** Generación de emparejamientos de partidos de forma eficiente.
* **Gestión de Partidos y Resultados:** Reporte, actualización y seguimiento de resultados de cada encuentro.
* **Sistema de Clasificación Dinámico:** Cálculo y actualización de rankings en tiempo real para torneos.
* **Comunicación en Tiempo Real:** Un chat básico integrado para la interacción entre jugadores y participantes del
  torneo (sin WebSockets, basado en solicitudes HTTP).
* **Seguridad Robustez:** Implementación de Spring Security y tokens JWT para autenticación y autorización segura.
* **Preparado para Despliegue:** Configuraciones que facilitan el despliegue en entornos de producción.

## 🚀 Tecnologías Utilizadas

* **Lenguaje:** Java 17
* **Framework:** Spring Boot 3.5.0 (Spring Web MVC)
* **Control de Dependencias:** Apache Maven 3.9
* **Seguridad:** Spring Security con JSON Web Tokens (JWT) (com.auth0:java-jwt 4.4.0)
* **Base de Datos:** PostgreSQL (org.postgresql:postgresql)
* **Persistencia:** Spring Data JPA / Hibernate
* **Validación:** Spring Boot Starter Validation
* **Utilidades:** Lombok (lombok:1.18.32)
* **Documentación de APIs:** SpringDoc OpenAPI con Swagger UI (org.springdoc:springdoc-openapi-starter-webmvc-ui 2.8.9)
* **Contenedorización:** Docker y Docker Compose

## 🏁 Cómo Empezar (Getting Started)

Estas instrucciones te guiarán para poner el proyecto en funcionamiento en tu máquina local para desarrollo y pruebas.

### Prerrequisitos

Asegúrate de tener instalado lo siguiente:

* **Git:** Para clonar el repositorio.
* **Java Development Kit (JDK) 17:**
* **Apache Maven 3.8+:**
* **Docker Desktop:** Incluye Docker Engine y Docker Compose (recomendado para una configuración rápida de la base de
  datos y la aplicación).
* **PostgreSQL (Opcional, si no usas Docker para la DB):** PostgreSQL 16 o compatible.

### Clonar el Repositorio

Abre tu terminal y ejecuta:

### Clonar el Repositorio

Abre tu terminal y ejecuta:

```bash
  git clone git@github.com:msxd26/GameHub.git
  cd GameHub
````

## Configuración de la Base de Datos

Puedes configurar la base de datos de dos maneras:

### Opción 1: Usando Docker Compose (Recomendado y más Sencillo)

La forma más rápida de levantar la base de datos es usando Docker Compose, ya que la configuración está predefinida en
los archivos del proyecto.

Asegúrate de que Docker Desktop esté en ejecución.

Desde el directorio raíz del proyecto (GameHub), ejecuta:

```bash
    docker-compose up -d db
  ```

Este comando levantará solo el contenedor de PostgreSQL (gamehub_db_container) y lo mantendrá en segundo plano. La base
de datos GameHub se creará automáticamente con el usuario postgres y contraseña 1234.

### Opción 2: Configuración Local de PostgreSQL (sin Docker para la DB)

Si prefieres usar una instalación local de PostgreSQL:

1. Abre un cliente de PostgreSQL (ej. `psql`, pgAdmin) y crea una nueva base de datos.

```sql
    CREATE DATABASE "GameHub";
```

2. Asegúrate de que el usuario `postgres` con la contraseña `1234` tenga acceso a esta base de datos. Si tu
   configuración es diferente, deberás modificar el archivo `src/main/resources/application.properties`:

```properties
    spring.datasource.url=jdbc:postgresql://localhost:5432/GameHub
    spring.datasource.username=tu_usuario_pg
    spring.datasource.password=tu_contraseña_pg
```

Nota sobre tablas: Spring Data JPA, con spring.jpa.hibernate.ddl-auto=update, creará y actualizará automáticamente el
esquema de la base de datos (tablas, relaciones) basándose en las entidades de Java cuando la aplicación se inicie por
primera vez.

## Ejecutar la Aplicación

Tienes dos opciones principales para ejecutar el backend:

### Opción 1: Con Docker Compose (Aplicación y DB)

Esta es la forma recomendada, ya que levanta tanto la base de datos (si no está ya corriendo con docker-compose up -d
db) como la aplicación en contenedores aislados.

1. Asegúrate de que Docker Desktop esté en ejecución.
2. Desde el directorio raíz del proyecto (`GameHub`), ejecuta:

```bash
    docker-compose up --build
  ```

Este comando construirá la imagen de Docker de tu aplicación y levantará ambos contenedores (gamehub_db_container y
gamehub_app_container). La aplicación estará disponible en http://localhost:8080.

### Opción 2: Directamente con Maven (Requiere DB local o DB Docker separada)

Si ya tienes la base de datos ejecutándose (ya sea localmente o con docker-compose up -d db):

1. Asegúrate de que tu `application.properties` esté configurado para conectar a tu instancia de PostgreSQL.
2. Desde el directorio raíz del proyecto (`GameHub`), ejecuta:

```bash
    mvn spring-boot:run
````

Esto compilará y ejecutará la aplicación Spring Boot. Estará disponible en http://localhost:8080.

### ⚙️ Uso de la API

### Usuarios por Defecto para Prueba (Solo Desarrollo Local)

Para facilitar las pruebas de la API y el acceso a funcionalidades de administrador, la aplicación crea un usuario administrador por defecto al iniciar si no existe uno. **¡ADVERTENCIA: Estas credenciales son solo para entornos de desarrollo/prueba y NO deben usarse en producción!**

* **Usuario Administrador:**
    * **Username:** `admin_gamehub`
    * **Email:** `admin@gamehub.com`
    * **Contraseña:** `adminpass`

Puedes usar estas credenciales para iniciar sesión a través del endpoint `/api/auth/login` y obtener un token JWT con el rol `ADMIN`.

La API estará disponible en http://localhost:8080/api/ una vez que la aplicación esté en funcionamiento.

Autenticación (JWT)
La mayoría de los endpoints requieren autenticación mediante un token JWT.

Registro: Envía una solicitud POST a /api/auth/register con username, email y password.

Login: Envía una solicitud POST a /api/auth/login con username y password. La respuesta incluirá el token JWT.

Uso del Token: Incluye el token JWT en el encabezado Authorization de tus solicitudes protegidas, con el formato
Bearer <tu_token_jwt>.

Documentación de la API (Swagger UI)
Una vez que la aplicación esté ejecutándose, puedes acceder a la documentación interactiva de la API a través de Swagger
UI:

URL: http://localhost:8080/swagger-ui.html

Aquí podrás ver todos los endpoints, sus descripciones, modelos de solicitud/respuesta y probarlos directamente.

Colección de Postman
Hemos proporcionado una colección de Postman para facilitar las pruebas de la API.

Abre Postman.

Ve a File > Import (o el botón "Import" en la interfaz).

Selecciona los archivos .json que se encuentran en la carpeta postman/ de este repositorio:

GameHub.postman_collection.json

GameHub - Local Development.postman_environment.json

Una vez importados, selecciona el entorno GameHub - Local Development en la esquina superior derecha de Postman para que
las URLs de las solicitudes ({{baseUrl}}) se configuren correctamente.

🧪 Ejecutar Pruebas

Para ejecutar las pruebas unitarias y de integración del proyecto, usa el siguiente comando Maven desde el directorio
raíz del proyecto:

```bash
mvn test
````

🤝 Contribución

Si deseas contribuir a este proyecto, por favor sigue estos pasos:

Haz un "fork" del repositorio.

Crea una nueva rama (git checkout -b feature/nueva-funcionalidad).

Realiza tus cambios y asegúrate de que las pruebas pasen.

Haz un commit de tus cambios (git commit -m "feat: Descripción de la nueva funcionalidad").

Sube tu rama (git push origin feature/nueva-funcionalidad).

Abre un "Pull Request".

📄 Licencia
Este proyecto está bajo la Licencia Apache 2.0. Consulta el archivo LICENSE para más detalles.