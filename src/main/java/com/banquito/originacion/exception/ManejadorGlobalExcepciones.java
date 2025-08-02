package com.banquito.originacion.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ManejadorGlobalExcepciones {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, String>> manejarResourceNotFound(ResourceNotFoundException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Recurso no encontrado");
        error.put("detalle", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<Map<String, String>> manejarRestClientException(RestClientException ex) {
        Map<String, String> error = new HashMap<>();
        
        // Verificar si es un error 404
        if (ex.getMessage().contains("404")) {
            error.put("error", "Recurso no encontrado");
            error.put("detalle", "El recurso solicitado no existe en el servicio externo");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } else {
            error.put("error", "Error de comunicación");
            error.put("detalle", "Error al comunicarse con el servicio externo: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
        }
    }

    @ExceptionHandler({ CreateEntityException.class, UpdateEntityException.class, DeleteEntityException.class })
    public ResponseEntity<Map<String, String>> manejarErroresDeNegocio(RuntimeException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Error en la operación");
        error.put("detalle", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> manejarErroresGenerales(Exception ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Error interno");
        error.put("detalle", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}

