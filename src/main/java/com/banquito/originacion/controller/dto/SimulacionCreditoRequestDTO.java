package com.banquito.originacion.controller.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class SimulacionCreditoRequestDTO {
    @NotBlank(message = "La placa del vehículo es requerida")
    private String placaVehiculo;
    
    @NotBlank(message = "El RUC del concesionario es requerido")
    private String rucConcesionario;
    
    @NotNull(message = "El monto solicitado es requerido")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
    private BigDecimal montoSolicitado;
    
    @NotNull(message = "El plazo en meses es requerido")
    @Min(value = 1, message = "El plazo debe ser al menos 1 mes")
    @Max(value = 120, message = "El plazo no puede exceder 120 meses")
    private Integer plazoMeses;
    
    @NotNull(message = "La tasa de interés es requerida")
    @DecimalMin(value = "0.00", message = "La tasa no puede ser negativa")
    @DecimalMax(value = "100.00", message = "La tasa no puede exceder 100%")
    private BigDecimal tasaInteres;
} 