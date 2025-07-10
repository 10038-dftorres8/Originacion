package com.banquito.originacion.controller.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SolicitudCreditoResponseDTO {
    private Long id;
    private String numeroSolicitud;
    private Long idClienteProspecto;
    private String idVehiculo;
    private String idVendedor;
    private String idPrestamo;
    private BigDecimal montoSolicitado;
    private Integer plazoMeses;
    private BigDecimal valorEntrada;
    private BigDecimal tasaInteresAplicada;
    private BigDecimal tasaInteresBase;
    private BigDecimal cuotaMensualCalculada;
    private BigDecimal montoTotalCalculado;
    private BigDecimal totalInteresesCalculado;
    private LocalDateTime fechaSolicitud;
    private String estado;
    private Long version;
    
    private String rucConcesionario;
    private String placaVehiculo;
    private String cedulaVendedor;
} 