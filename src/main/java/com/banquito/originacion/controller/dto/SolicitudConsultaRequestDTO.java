package com.banquito.originacion.controller.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SolicitudConsultaRequestDTO {
    @NotNull(message = "La fecha de inicio es requerida")
    private LocalDateTime fechaInicio;
    
    @NotNull(message = "La fecha de fin es requerida")
    private LocalDateTime fechaFin;
    
    private String estado;
    
    @Pattern(regexp = "^[0-9]{10}$", message = "La cédula del vendedor debe tener 10 dígitos")
    private String cedulaVendedor; // Opcional: si es null, no se aplica filtro
    
    @Pattern(regexp = "^[0-9]{13}$", message = "El RUC del concesionario debe tener 13 dígitos")
    private String rucConcesionario; // Opcional: si es null, no se aplica filtro
    
    @Min(value = 0, message = "El número de página debe ser mayor o igual a 0")
    private Integer pagina = 0;
    
    @Min(value = 1, message = "El tamaño de página debe ser al menos 1")
    @Max(value = 100, message = "El tamaño de página no puede exceder 100")
    private Integer tamanoPagina = 20;
} 