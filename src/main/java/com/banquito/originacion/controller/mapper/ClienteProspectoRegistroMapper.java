package com.banquito.originacion.controller.mapper;

import com.banquito.originacion.controller.dto.ClienteProspectoRegistroDTO;
import com.banquito.originacion.model.ClienteProspecto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface ClienteProspectoRegistroMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "idClienteCore", ignore = true)
    @Mapping(source = "genero", target = "genero", qualifiedByName = "generoToString")
    @Mapping(source = "telefonoTipo", target = "telefonoTipo", qualifiedByName = "telefonoTipoToString")
    @Mapping(source = "direccionTipo", target = "direccionTipo", qualifiedByName = "direccionTipoToString")
    @Mapping(target = "estado", constant = "PROSPECTO")
    @Mapping(target = "scoreInterno", constant = "0.00")
    ClienteProspecto toEntity(ClienteProspectoRegistroDTO dto);

    @Named("generoToString")
    default String generoToString(com.banquito.originacion.enums.GeneroClienteEnum genero) {
        return genero != null ? genero.name() : null;
    }

    @Named("telefonoTipoToString")
    default String telefonoTipoToString(com.banquito.originacion.enums.TipoTelefonoEnum telefonoTipo) {
        return telefonoTipo != null ? telefonoTipo.name() : null;
    }

    @Named("direccionTipoToString")
    default String direccionTipoToString(com.banquito.originacion.enums.TipoDireccionEnum direccionTipo) {
        return direccionTipo != null ? direccionTipo.name() : null;
    }
} 