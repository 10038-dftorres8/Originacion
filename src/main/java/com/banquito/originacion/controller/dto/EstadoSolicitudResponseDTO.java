package com.banquito.originacion.controller.dto;

import lombok.Data;
import java.util.List;

@Data
public class EstadoSolicitudResponseDTO {
    private String estadoActual;
    private List<HistorialEstadoDTO> historial;
} 