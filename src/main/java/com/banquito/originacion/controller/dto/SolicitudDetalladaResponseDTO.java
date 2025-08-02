package com.banquito.originacion.controller.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudDetalladaResponseDTO {
    // Información de la solicitud
    private Long idSolicitud;
    private String numeroSolicitud;
    private String estado;
    private LocalDateTime fechaSolicitud;
    
    // Información del solicitante
    private String cedulaSolicitante;
    private String nombresSolicitante;
    private String calificacionSolicitante;
    private BigDecimal capacidadPagoSolicitante;
    
    // Información del vehículo
    private String placaVehiculo;
    private String marcaVehiculo;
    private String modeloVehiculo;
    private Integer anioVehiculo;
    private BigDecimal valorVehiculo;
    
    // Información del concesionario
    private String rucConcesionario;
    private String razonSocialConcesionario;
    private String direccionConcesionario;
    
    // Información del vendedor
    private String cedulaVendedor;
    private String nombreVendedor;
    private String telefonoVendedor;
    private String emailVendedor;
    
    // Información del préstamo
    private String idPrestamo;
    private String nombrePrestamo;
    private String descripcionPrestamo;
    
    // Información financiera
    private BigDecimal valorEntrada;
    private BigDecimal montoSolicitado;
    private Integer plazoMeses;
} 