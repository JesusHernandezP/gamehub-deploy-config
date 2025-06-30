package com.grupo5.gamehub.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "GameHub API",
                version = "1.0",
                description = "Documentación completa de la API para GameHub, una plataforma dedicada a la gestión de torneos y comunicación entre jugadores.",
                termsOfService = "http://swagger.io/terms/",
                contact = @Contact(
                        name = "GameHub Team",
                        email = "contact@gamehub.com",
                        url = "https://www.gamehub.com"
                ),
                license = @License(
                        name = "Apache 2.0",
                        url = "http://www.apache.org/licenses/LICENSE-2.0.html"
                )
        ),
        servers = {
                @Server(url = "http://localhost:8080", description = "Servidor de desarrollo local")
        }
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer",
        description = "Token de autenticación JWT. Introduce 'Bearer ' seguido de tu token. Ejemplo: 'Bearer eyJhbGciOiJIUzI1Ni...'"
)
public class OpenApiConfig {
}