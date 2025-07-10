package com.banquito.originacion.controller.mapper;

import com.banquito.originacion.controller.dto.ClienteProspectoRegistroDTO;
import com.banquito.originacion.model.ClienteProspecto;
import org.springframework.stereotype.Component;

@Component
public class ClienteProspectoRegistroMapper {

    public ClienteProspecto toEntity(ClienteProspectoRegistroDTO dto) {
        if (dto == null) {
            return null;
        }

        ClienteProspecto clienteProspecto = new ClienteProspecto();
        
        clienteProspecto.setCedula(dto.getCedula());
        
        clienteProspecto.setNombres(dto.getNombres());
        clienteProspecto.setGenero(dto.getGenero() != null ? dto.getGenero().name() : null);
        clienteProspecto.setFechaNacimiento(dto.getFechaNacimiento());
        clienteProspecto.setNivelEstudio(dto.getNivelEstudio());
        clienteProspecto.setEstadoCivil(dto.getEstadoCivil());
        clienteProspecto.setIngresos(dto.getIngresos());
        clienteProspecto.setEgresos(dto.getEgresos());
        clienteProspecto.setActividadEconomica(dto.getActividadEconomica());
        clienteProspecto.setCorreoTransaccional(dto.getCorreoTransaccional());
        clienteProspecto.setTelefonoTransaccional(dto.getTelefonoTransaccional());
        clienteProspecto.setTelefonoTipo(dto.getTelefonoTipo() != null ? dto.getTelefonoTipo().name() : null);
        clienteProspecto.setTelefonoNumero(dto.getTelefonoNumero());
        clienteProspecto.setDireccionTipo(dto.getDireccionTipo() != null ? dto.getDireccionTipo().name() : null);
        clienteProspecto.setDireccionLinea1(dto.getDireccionLinea1());
        clienteProspecto.setDireccionLinea2(dto.getDireccionLinea2());
        clienteProspecto.setDireccionCodigoPostal(dto.getDireccionCodigoPostal());
        clienteProspecto.setDireccionGeoCodigo(dto.getDireccionGeoCodigo());
        
        clienteProspecto.setEstado("PROSPECTO");
        clienteProspecto.setIdClienteCore(null);
        clienteProspecto.setScoreInterno(null);
        
        return clienteProspecto;
    }
} 