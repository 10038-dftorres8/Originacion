package com.banquito.originacion.controller.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class SolicitudCreditoDTO {
    @NotNull
    private Long idClienteProspecto;
    @NotNull
    private Long idVehiculo;
    @NotNull
    private Long idVendedor;
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
} 