package com.banquito.originacion.controller.mapper;

import com.banquito.originacion.controller.dto.DocumentoAdjuntoResponseDTO;
import com.banquito.originacion.model.DocumentoAdjunto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DocumentoAdjuntoMapper {
    DocumentoAdjuntoResponseDTO toResponseDTO(DocumentoAdjunto entity);
} 