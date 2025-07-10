package com.banquito.originacion.controller.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class SolicitudCreditoExtendidaDTO {
    @NotNull
    private Long idClienteProspecto;
    
    @NotNull
    @NotBlank(message = "El ID del préstamo es requerido")
    private String idPrestamo;
    
    private BigDecimal montoSolicitado;
    
    @NotNull
    @Min(1)
    private Integer plazoMeses;
    
    @DecimalMin(value = "0.00")
    private BigDecimal valorEntrada;
    

    @DecimalMin(value = "0.00")
    @DecimalMax(value = "100.00")
    private BigDecimal tasaInteres;
    
    // Información del vehículo y vendedor
    @NotNull(message = "La placa del vehículo es requerida")
    @NotBlank(message = "La placa del vehículo no puede estar vacía")
    private String placaVehiculo;
    
    @NotNull(message = "El RUC del concesionario es requerido")
    @NotBlank(message = "El RUC del concesionario no puede estar vacío")
    private String rucConcesionario;
    
    @NotBlank(message = "La cédula del vendedor es requerida")
    private String cedulaVendedor;
} 