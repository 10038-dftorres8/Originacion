package com.banquito.originacion.controller.mapper;

import com.banquito.originacion.controller.dto.PersonaCoreResponseDTO;
import com.banquito.originacion.enums.GeneroClienteEnum;
import com.banquito.originacion.enums.TipoDireccionEnum;
import com.banquito.originacion.enums.TipoTelefonoEnum;
import com.banquito.originacion.model.ClienteProspecto;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Component
public class PersonaCoreMapper {

    public ClienteProspecto toEntity(PersonaCoreResponseDTO personaCore) {
        if (personaCore == null) {
            return null;
        }

        ClienteProspecto clienteProspecto = new ClienteProspecto();
        
        clienteProspecto.setCedula(personaCore.getNumeroIdentificacion());
        clienteProspecto.setNombres(personaCore.getNombre());
        clienteProspecto.setGenero(convertirGenero(personaCore.getGenero()));
        clienteProspecto.setFechaNacimiento(convertirFecha(personaCore.getFechaNacimiento()));
        clienteProspecto.setNivelEstudio(personaCore.getNivelEstudio());
        clienteProspecto.setEstadoCivil(personaCore.getEstadoCivil());
        clienteProspecto.setCorreoTransaccional(personaCore.getCorreoElectronico());
        
        // Datos que no vienen del Core se dejan vacíos
        clienteProspecto.setIngresos(null);
        clienteProspecto.setEgresos(null);
        clienteProspecto.setActividadEconomica(null);
        clienteProspecto.setTelefonoTransaccional(null);
        clienteProspecto.setTelefonoTipo(null);
        clienteProspecto.setTelefonoNumero(null);
        clienteProspecto.setDireccionTipo(null);
        clienteProspecto.setDireccionLinea1(null);
        clienteProspecto.setDireccionLinea2(null);
        clienteProspecto.setDireccionCodigoPostal(null);
        clienteProspecto.setDireccionGeoCodigo(null);
        
        clienteProspecto.setIdClienteCore(null);
        
        return clienteProspecto;
    }

    private String convertirGenero(String generoCore) {
        if (generoCore == null) return null;
        
        switch (generoCore.toUpperCase()) {
            case "M":
            case "MASCULINO":
                return GeneroClienteEnum.MASCULINO.name();
            case "F":
            case "FEMENINO":
                return GeneroClienteEnum.FEMENINO.name();
            default:
                return null;
        }
    }

    private LocalDateTime convertirFecha(LocalDate fecha) {
        if (fecha == null) return null;
        return fecha.atStartOfDay();
    }
} 