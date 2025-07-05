package com.banquito.originacion.controller.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class SimulacionDTO {
    @NotNull
    private Long idVehiculo;
    @NotNull
    private Long idProductoCredito;
    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal montoSolicitado;
    @NotNull
    @Min(1)
    private Integer plazoMeses;
    @DecimalMin(value = "0.00")
    private BigDecimal valorEntrada;
    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal ingresosCliente;
} 