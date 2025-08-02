package com.banquito.originacion.controller.mapper;

import com.banquito.originacion.controller.dto.HistorialEstadoDTO;
import com.banquito.originacion.model.HistorialEstado;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface HistorialEstadoMapper {
    HistorialEstadoDTO toDTO(HistorialEstado entity);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "solicitudCredito", ignore = true)
    HistorialEstado toEntity(HistorialEstadoDTO dto);
} 