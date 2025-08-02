package com.banquito.originacion.controller.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
public class SimulacionSolicitudResponseDTO {
    private String numeroSolicitud;
    private String cedulaSolicitante;
    private String placaVehiculo;
    private String rucConcesionario;
    private String idPrestamo;
    private BigDecimal valorVehiculo;
    private BigDecimal valorEntrada;
    private BigDecimal montoSolicitado;
    private Integer plazoMeses;
    private BigDecimal tasaInteres;
    private LocalDateTime fechaSimulacion;
    
    // Resumen financiero
    private BigDecimal cuotaMensual;
    private BigDecimal montoTotal;
    private BigDecimal totalIntereses;
    private BigDecimal totalAPagar;
    
    // Tabla de amortización completa
    private List<AmortizacionDTO> tablaAmortizacion;
    
    // Información adicional
    private String nombrePrestamo;
    private String descripcionPrestamo;
    private BigDecimal capacidadPagoCliente;
    private boolean esAprobable;
    private String motivoRechazo;
} 