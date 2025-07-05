package com.banquito.originacion.controller.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class HistorialEstadoDTO {
    private String estadoAnterior;
    private String estadoNuevo;
    private LocalDateTime fechaCambio;
    private String motivo;
    private Long usuarioModificacion;
} 