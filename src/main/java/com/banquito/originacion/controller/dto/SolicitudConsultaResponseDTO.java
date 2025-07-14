package com.banquito.originacion.controller.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudConsultaResponseDTO {
    private Long idSolicitud;
    private String numeroSolicitud;
    private String estado;
    private LocalDateTime fechaSolicitud;
    private BigDecimal montoSolicitado;
    private Integer plazoMeses;
    private BigDecimal cuotaMensual;
    private String placaVehiculo;
    private String rucConcesionario;
    private String idPrestamo;
    private String cedulaCliente;
    private String nombresCliente;
} 