package com.banquito.originacion.config;

import feign.codec.ErrorDecoder;
import org.springframework.stereotype.Component;

@Component
public class CustomErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, feign.Response response) {
        System.out.println("Error en llamada Feign - Method: " + methodKey + ", Status: " + response.status() + ", Reason: " + response.reason());
        
        switch (response.status()) {
            case 400:
                return new RuntimeException("Error de validación en el servicio de gestión de vehículos");
            case 404:
                return new RuntimeException("Recurso no encontrado en el servicio de gestión de vehículos");
            case 500:
                return new RuntimeException("Error interno en el servicio de gestión de vehículos");
            default:
                return new RuntimeException("Error desconocido en el servicio de gestión de vehículos");
        }
    }
} 