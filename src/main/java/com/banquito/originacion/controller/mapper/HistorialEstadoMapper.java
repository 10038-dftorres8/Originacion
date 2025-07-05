package com.banquito.originacion.controller.mapper;

import com.banquito.originacion.controller.dto.HistorialEstadoDTO;
import com.banquito.originacion.model.HistorialEstado;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface HistorialEstadoMapper {
    HistorialEstadoDTO toDTO(HistorialEstado entity);
} 