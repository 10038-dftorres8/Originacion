package com.banquito.originacion.controller.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudCreditoEdicionDTO {
    
    @NotNull(message = "La cédula del solicitante es requerida")
    @NotBlank(message = "La cédula del solicitante no puede estar vacía")
    @Pattern(regexp = "^[0-9]{10}$", message = "La cédula debe tener 10 dígitos")
    private String cedulaSolicitante;
    
    @NotNull(message = "La calificación del solicitante es requerida")
    @NotBlank(message = "La calificación del solicitante no puede estar vacía")
    @Pattern(regexp = "^[A-Z][+-]?$", message = "La calificación debe ser una letra de A a Z opcionalmente seguida de + o -")
    private String calificacionSolicitante;
    
    @NotNull(message = "La capacidad de pago del solicitante es requerida")
    @DecimalMin(value = "0.01", message = "La capacidad de pago debe ser mayor a 0")
    private BigDecimal capacidadPagoSolicitante;
    
    @NotNull(message = "La placa del vehículo es requerida")
    @NotBlank(message = "La placa del vehículo no puede estar vacía")
    @Pattern(regexp = "^[A-Z]{3}[0-9]{4}$|^[A-Z]{3}-[0-9]{4}$", message = "La placa debe tener el formato AAA0000 o ABC-1234")
    private String placaVehiculo;
    
    @NotNull(message = "El RUC del concesionario es requerido")
    @NotBlank(message = "El RUC del concesionario no puede estar vacío")
    @Pattern(regexp = "^[0-9]{13}$", message = "El RUC debe tener 13 dígitos")
    private String rucConcesionario;
    
    @NotNull(message = "La cédula del vendedor es requerida")
    @NotBlank(message = "La cédula del vendedor no puede estar vacía")
    @Pattern(regexp = "^[0-9]{10}$", message = "La cédula del vendedor debe tener 10 dígitos")
    private String cedulaVendedor;
    
    @NotNull(message = "El ID del préstamo es requerido")
    @NotBlank(message = "El ID del préstamo no puede estar vacío")
    private String idPrestamo;
    
    @NotNull(message = "El valor de entrada es requerido")
    @DecimalMin(value = "0.01", message = "El valor de entrada debe ser mayor a 0")
    private BigDecimal valorEntrada;
    
    @NotNull(message = "El plazo en meses es requerido")
    @Min(value = 1, message = "El plazo debe ser al menos 1 mes")
    @Max(value = 120, message = "El plazo no puede exceder 120 meses")
    private Integer plazoMeses;
} 