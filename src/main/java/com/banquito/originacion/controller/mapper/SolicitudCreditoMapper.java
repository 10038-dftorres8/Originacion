package com.banquito.originacion.controller.mapper;

import com.banquito.originacion.controller.dto.SolicitudCreditoResponseDTO;
import com.banquito.originacion.model.SolicitudCredito;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SolicitudCreditoMapper {
    SolicitudCreditoResponseDTO toResponseDTO(SolicitudCredito entity);
} 