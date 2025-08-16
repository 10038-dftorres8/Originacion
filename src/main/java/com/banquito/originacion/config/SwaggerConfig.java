package com.banquito.originacion.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API de Originación - Banco Banquito")
                        .description("Microservicio para la gestión de solicitudes de crédito y originación de préstamos")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Equipo de Desarrollo Banquito")
                                .email("desarrollo@banquito.com")
                                .url("https://www.banquito.com"))
                        .license(new License()
                                .name("Licencia Privada")
                                .url("https://www.banquito.com/licencia")))
                .servers(List.of(
                        new Server()
                                .url("http://banquito-alb-1166574131.us-east-2.elb.amazonaws.com/api/originacion")
                                .description("Servidor de Producción"),
                        new Server()
                                .url("http://localhost:80/api/originacion")
                                .description("Servidor Local")
                ));
    }
}
