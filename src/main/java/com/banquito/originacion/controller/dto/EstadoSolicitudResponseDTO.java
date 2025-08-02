package com.banquito.originacion.controller.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class EstadoSolicitudResponseDTO {
    private Long idSolicitud;
    private String numeroSolicitud;
    private String estadoActual;
    private LocalDateTime fechaSolicitud;
    private BigDecimal montoSolicitado;
    private Integer plazoMeses;
    private BigDecimal cuotaMensual;
    private String placaVehiculo;
    private String rucConcesionario;
    private String cedulaVendedor;
    private String idPrestamo;
    private String cedulaSolicitante;
    private List<HistorialEstadoDTO> historial;
} 