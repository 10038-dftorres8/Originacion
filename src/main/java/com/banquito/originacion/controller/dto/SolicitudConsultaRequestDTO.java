package com.banquito.originacion.controller.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDateTime;

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

    // Getters
    public LocalDateTime getFechaInicio() { return fechaInicio; }
    public LocalDateTime getFechaFin() { return fechaFin; }
    public String getEstado() { return estado; }
    public String getCedulaVendedor() { return cedulaVendedor; }
    public String getRucConcesionario() { return rucConcesionario; }
    public Integer getPagina() { return pagina; }
    public Integer getTamanoPagina() { return tamanoPagina; }

    // Setters
    public void setFechaInicio(LocalDateTime fechaInicio) { this.fechaInicio = fechaInicio; }
    public void setFechaFin(LocalDateTime fechaFin) { this.fechaFin = fechaFin; }
    public void setEstado(String estado) { this.estado = estado; }
    public void setCedulaVendedor(String cedulaVendedor) { this.cedulaVendedor = cedulaVendedor; }
    public void setRucConcesionario(String rucConcesionario) { this.rucConcesionario = rucConcesionario; }
    public void setPagina(Integer pagina) { this.pagina = pagina; }
    public void setTamanoPagina(Integer tamanoPagina) { this.tamanoPagina = tamanoPagina; }
} 