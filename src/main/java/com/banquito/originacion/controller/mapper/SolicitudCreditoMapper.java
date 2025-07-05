package com.banquito.originacion.controller.mapper;

import com.banquito.originacion.controller.dto.SolicitudCreditoDTO;
import com.banquito.originacion.controller.dto.SolicitudCreditoResponseDTO;
import com.banquito.originacion.model.SolicitudCredito;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SolicitudCreditoMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "numeroSolicitud", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "fechaSolicitud", ignore = true)
    SolicitudCredito toEntity(SolicitudCreditoDTO dto);

    SolicitudCreditoResponseDTO toResponseDTO(SolicitudCredito entity);
} 