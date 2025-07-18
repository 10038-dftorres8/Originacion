package com.banquito.originacion.controller.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudResumenDTO {
    private Long idSolicitud;
    private String numeroSolicitud;
    private String estado;
    private LocalDateTime fechaSolicitud;
    private BigDecimal montoSolicitado;
    private BigDecimal valorEntrada;
    private Integer plazoMeses;
    private BigDecimal tasaInteresAplicada;
    private BigDecimal cuotaMensual;
    private BigDecimal montoTotal;
    private BigDecimal totalIntereses;
    
    private String placaVehiculo;
    private String marcaVehiculo;
    private String modeloVehiculo;
    private BigDecimal valorVehiculo;
    private String rucConcesionario;
    private String cedulaVendedor;
    private String nombreVendedor;
    
    private String idPrestamo;
    private String cedulaCliente;
    private String nombresCliente;
    private String correoCliente;
    private String telefonoCliente;
} 