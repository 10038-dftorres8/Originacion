package com.banquito.originacion.controller.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SolicitudCreditoResponseDTO {
    private Long id;
    private String numeroSolicitud;
    private Long idClienteProspecto;
    private Long idVehiculo;
    private Long idVendedor;
    private Long idProductoCredito;
    private BigDecimal montoSolicitado;
    private Integer plazoMeses;
    private BigDecimal valorEntrada;
    private BigDecimal tasaInteresAplicada;
    private BigDecimal cuotaMensualCalculada;
    private LocalDateTime fechaSolicitud;
    private String estado;
    private Long version;
} 