package com.banquito.originacion.controller.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class SolicitudCreditoExtendidaDTO {
    @NotNull
    private Long idClienteProspecto;
    
    @NotNull
    private Long idProductoCredito;
    
    // El monto solicitado se calcula automáticamente como: valor del vehículo - entrada
    private BigDecimal montoSolicitado;
    
    @NotNull
    @Min(1)
    private Integer plazoMeses;
    
    @DecimalMin(value = "0.00")
    private BigDecimal valorEntrada;
    
    @NotNull
    @DecimalMin(value = "0.01")
    @DecimalMax(value = "1.00")
    private BigDecimal tasaInteres;
    
    // Información del concesionario y vehículo
    @NotBlank(message = "El RUC del concesionario es requerido")
    private String rucConcesionario;
    
    @NotBlank(message = "La placa del vehículo es requerida")
    private String placaVehiculo;
    
    @NotBlank(message = "La cédula del vendedor es requerida")
    private String cedulaVendedor;
} 