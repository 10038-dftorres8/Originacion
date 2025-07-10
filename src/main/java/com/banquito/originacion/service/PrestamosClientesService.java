package com.banquito.originacion.service;

import com.banquito.originacion.client.TransaccionesClient;
import com.banquito.originacion.controller.dto.external.PrestamoClienteExternalDTO;
import com.banquito.originacion.exception.CreateEntityException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class PrestamosClientesService {

    private final TransaccionesClient transaccionesClient;

    public PrestamoClienteExternalDTO crearPrestamoCliente(String idCliente, String idPrestamo, 
                                                          BigDecimal montoSolicitado, Integer plazoMeses, 
                                                          BigDecimal tasaInteresAplicada) {
        log.info("Creando préstamo cliente para cliente: {} con préstamo: {}", idCliente, idPrestamo);
        log.info("Tasa de interés original: {} (decimal)", tasaInteresAplicada);
        
        BigDecimal tasaEnPorcentaje = tasaInteresAplicada.multiply(BigDecimal.valueOf(100));
        log.info("Tasa de interés convertida: {} (porcentaje)", tasaEnPorcentaje);
        
        PrestamoClienteExternalDTO prestamoClienteDTO = PrestamoClienteExternalDTO.builder()
                .idCliente(idCliente)
                .idPrestamo(idPrestamo)
                .montoSolicitado(montoSolicitado)
                .plazoMeses(plazoMeses)
                .tasaInteresAplicada(tasaEnPorcentaje)
                .estado("ACTIVO")
                .build();
        
        try {
            log.info("=== DATOS PARA CREAR PRÉSTAMO CLIENTE ===");
            log.info("ID Cliente: {}", prestamoClienteDTO.getIdCliente());
            log.info("ID Préstamo: {}", prestamoClienteDTO.getIdPrestamo());
            log.info("Monto Solicitado: {}", prestamoClienteDTO.getMontoSolicitado());
            log.info("Plazo Meses: {}", prestamoClienteDTO.getPlazoMeses());
            log.info("Tasa Interés: {}%", prestamoClienteDTO.getTasaInteresAplicada());
            log.info("Estado: {}", prestamoClienteDTO.getEstado());
            log.info("==========================================");
            
            PrestamoClienteExternalDTO prestamoCreado = transaccionesClient.crearPrestamoCliente(prestamoClienteDTO);
            log.info("Préstamo cliente creado exitosamente: ID={} para cliente {}", prestamoCreado.getId(), idCliente);
            return prestamoCreado;
        } catch (RestClientException e) {
            log.error("Error RestClient al crear préstamo cliente para cliente {}: {}", idCliente, e.getMessage());
            throw new CreateEntityException("PrestamoCliente", "Error al crear préstamo cliente: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error inesperado al crear préstamo cliente para cliente {}: {}", idCliente, e.getMessage());
            log.error("Stack trace completo:", e);
            throw new CreateEntityException("PrestamoCliente", "Error inesperado al crear préstamo cliente: " + e.getMessage());
        }
    }
} 