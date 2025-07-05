package com.banquito.originacion.controller.mapper;

import com.banquito.originacion.controller.dto.ClienteProspectoDTO;
import com.banquito.originacion.controller.dto.ClienteResponseDTO;
import com.banquito.originacion.model.ClienteProspecto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface ClienteProspectoMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(source = "genero", target = "genero", qualifiedByName = "generoToString")
    @Mapping(source = "estado", target = "estado", qualifiedByName = "estadoToString")
    @Mapping(source = "telefonoTipo", target = "telefonoTipo", qualifiedByName = "telefonoTipoToString")
    @Mapping(source = "direccionTipo", target = "direccionTipo", qualifiedByName = "direccionTipoToString")
    ClienteProspecto toEntity(ClienteProspectoDTO dto);

    @Mapping(source = "genero", target = "genero")
    @Mapping(source = "estado", target = "estado")
    @Mapping(source = "telefonoTipo", target = "telefonoTipo")
    @Mapping(source = "direccionTipo", target = "direccionTipo")
    ClienteResponseDTO toResponseDTO(ClienteProspecto entity);

    @Named("generoToString")
    default String generoToString(com.banquito.originacion.enums.GeneroClienteEnum genero) {
        return genero != null ? genero.name() : null;
    }

    @Named("estadoToString")
    default String estadoToString(com.banquito.originacion.enums.EstadoClienteEnum estado) {
        return estado != null ? estado.name() : null;
    }

    @Named("telefonoTipoToString")
    default String telefonoTipoToString(com.banquito.originacion.enums.TipoTelefonoEnum tipo) {
        return tipo != null ? tipo.name() : null;
    }

    @Named("direccionTipoToString")
    default String direccionTipoToString(com.banquito.originacion.enums.TipoDireccionEnum tipo) {
        return tipo != null ? tipo.name() : null;
    }
} 