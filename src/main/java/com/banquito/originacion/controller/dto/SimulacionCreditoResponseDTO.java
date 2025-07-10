package com.banquito.originacion.controller.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
public class SimulacionCreditoResponseDTO {
    private String placaVehiculo;
    private String rucConcesionario;
    private BigDecimal valorVehiculo;
    private BigDecimal montoSolicitado;
    private Integer plazoOriginal;
    private BigDecimal tasaInteres;
    
    private List<ResumenEscenarioDTO> resumenEscenarios;
    
    private List<AmortizacionDTO> tablaConEntrada20;
    private List<AmortizacionDTO> tablaSinEntrada;
    private List<AmortizacionDTO> tablaPlazoMaximo;
    
    @Data
    @NoArgsConstructor
    public static class ResumenEscenarioDTO {
        private String nombreEscenario;
        private BigDecimal montoFinanciado;
        private Integer plazoMeses;
        private BigDecimal cuotaMensual;
        private BigDecimal montoTotal;
        private BigDecimal totalIntereses;
        private BigDecimal entrada;
        private String descripcion;
    }
} 