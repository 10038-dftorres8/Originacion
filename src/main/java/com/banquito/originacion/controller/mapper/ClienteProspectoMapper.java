package com.banquito.originacion.controller.mapper;

import com.banquito.originacion.controller.dto.ClienteResponseDTO;
import com.banquito.originacion.model.ClienteProspecto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ClienteProspectoMapper {

    @Mapping(target = "mensaje", ignore = true)
    @Mapping(target = "existeEnCore", ignore = true)
    @Mapping(target = "esCliente", ignore = true)
    ClienteResponseDTO toResponseDTO(ClienteProspecto clienteProspecto);
} 