package com.banquito.originacion.controller.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SolicitudConsultaPaginadaResponseDTO {
    private List<SolicitudConsultaResponseDTO> solicitudes;
    private Integer paginaActual;
    private Integer tamanoPagina;
    private Long totalElementos;
    private Integer totalPaginas;
    private Boolean tieneSiguiente;
    private Boolean tieneAnterior;
} 