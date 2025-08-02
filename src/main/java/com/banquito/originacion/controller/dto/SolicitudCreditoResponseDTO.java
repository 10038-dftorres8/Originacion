package com.banquito.originacion.controller.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SolicitudCreditoResponseDTO {
    private Long id;
    private String numeroSolicitud;
    private LocalDateTime fechaSolicitud;
    private String cedulaSolicitante;
    private String calificacionSolicitante;
    private BigDecimal capacidadPagoSolicitante;
    private String placaVehiculo;
    private String rucConcesionario;
    private String cedulaVendedor;
    private String idPrestamo;
    private BigDecimal valorEntrada;
    private BigDecimal montoSolicitado;
    private Integer plazoMeses;
    private String estado;
    private Long version;
} 