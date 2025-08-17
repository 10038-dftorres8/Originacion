package com.banquito.originacion.controller.dto;

import java.util.List;

public class SolicitudConsultaPaginadaResponseDTO {
    private List<SolicitudConsultaResponseDTO> solicitudes;
    private Integer paginaActual;
    private Integer tamanoPagina;
    private Long totalElementos;
    private Integer totalPaginas;
    private Boolean tieneSiguiente;
    private Boolean tieneAnterior;

    // Constructors
    public SolicitudConsultaPaginadaResponseDTO() {}

    public SolicitudConsultaPaginadaResponseDTO(List<SolicitudConsultaResponseDTO> solicitudes, Integer paginaActual, 
                                               Integer tamanoPagina, Long totalElementos, Integer totalPaginas, 
                                               Boolean tieneSiguiente, Boolean tieneAnterior) {
        this.solicitudes = solicitudes;
        this.paginaActual = paginaActual;
        this.tamanoPagina = tamanoPagina;
        this.totalElementos = totalElementos;
        this.totalPaginas = totalPaginas;
        this.tieneSiguiente = tieneSiguiente;
        this.tieneAnterior = tieneAnterior;
    }

    // Getters
    public List<SolicitudConsultaResponseDTO> getSolicitudes() { return solicitudes; }
    public Integer getPaginaActual() { return paginaActual; }
    public Integer getTamanoPagina() { return tamanoPagina; }
    public Long getTotalElementos() { return totalElementos; }
    public Integer getTotalPaginas() { return totalPaginas; }
    public Boolean getTieneSiguiente() { return tieneSiguiente; }
    public Boolean getTieneAnterior() { return tieneAnterior; }

    // Setters
    public void setSolicitudes(List<SolicitudConsultaResponseDTO> solicitudes) { this.solicitudes = solicitudes; }
    public void setPaginaActual(Integer paginaActual) { this.paginaActual = paginaActual; }
    public void setTamanoPagina(Integer tamanoPagina) { this.tamanoPagina = tamanoPagina; }
    public void setTotalElementos(Long totalElementos) { this.totalElementos = totalElementos; }
    public void setTotalPaginas(Integer totalPaginas) { this.totalPaginas = totalPaginas; }
    public void setTieneSiguiente(Boolean tieneSiguiente) { this.tieneSiguiente = tieneSiguiente; }
    public void setTieneAnterior(Boolean tieneAnterior) { this.tieneAnterior = tieneAnterior; }
} 