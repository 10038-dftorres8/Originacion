package com.banquito.originacion.service;

import com.banquito.originacion.client.CoreBancarioClient;
import com.banquito.originacion.controller.dto.ClienteCoreResponseDTO;
import com.banquito.originacion.controller.dto.ClienteProspectoDTO;
import com.banquito.originacion.controller.dto.ClienteResponseDTO;
import com.banquito.originacion.controller.mapper.ClienteCoreMapper;
import com.banquito.originacion.controller.mapper.ClienteProspectoMapper;
import com.banquito.originacion.enums.EstadoClienteEnum;
import com.banquito.originacion.exception.CreateEntityException;
import com.banquito.originacion.exception.ResourceNotFoundException;
import com.banquito.originacion.model.ClienteProspecto;
import com.banquito.originacion.repository.ClienteProspectoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;

import java.util.Optional;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ClienteService {

    private final ClienteProspectoRepository clienteProspectoRepository;
    private final ClienteProspectoMapper clienteProspectoMapper;
    private final ClienteCoreMapper clienteCoreMapper;
    private final CoreBancarioClient coreBancarioClient;

    public ClienteResponseDTO consultarClientePorCedula(String cedula) {
        log.info("Consultando cliente por cédula: {}", cedula);
        
        Optional<ClienteProspecto> clienteLocalOpt = clienteProspectoRepository.findByCedula(cedula);
        
        if (clienteLocalOpt.isPresent()) {
            ClienteProspecto clienteLocal = clienteLocalOpt.get();
            log.info("Cliente encontrado en base local");
            ClienteResponseDTO response = clienteProspectoMapper.toResponseDTO(clienteLocal);
            response.setMensaje("Cliente encontrado en base local");
            return response;
        }
        
        try {
            ClienteCoreResponseDTO clienteCore = coreBancarioClient.consultarClientePorIdentificacion("CEDULA", cedula);
            log.info("Cliente encontrado en Core Bancario con ID: {}", clienteCore.getId());
            ClienteResponseDTO response = clienteCoreMapper.toResponseDTO(clienteCore);
            response.setMensaje("Cliente encontrado en Core Bancario");
            return response;
        } catch (RestClientException e) {
            log.warn("Cliente no encontrado en Core Bancario: {}", e.getMessage());
            throw new ResourceNotFoundException("Cliente con cédula " + cedula + " no encontrado");
        }
    }

    public ClienteResponseDTO registrarClienteProspecto(ClienteProspectoDTO clienteDTO) {
        log.info("Registrando nuevo cliente prospecto con cédula: {}", clienteDTO.getCedula());
        
        try {
            validarCedulaEcuatoriana(clienteDTO.getCedula());
            
            if (clienteProspectoRepository.findByCedula(clienteDTO.getCedula()).isPresent()) {
                throw new CreateEntityException("ClienteProspecto", "Ya existe un cliente con la cédula: " + clienteDTO.getCedula());
            }
            
            ClienteProspecto cliente = clienteProspectoMapper.toEntity(clienteDTO);
            cliente.setEstado(EstadoClienteEnum.PROSPECTO.name());
            
            try {
                ClienteCoreResponseDTO clienteCore = coreBancarioClient.consultarClientePorIdentificacion("CEDULA", clienteDTO.getCedula());
                log.info("Cliente encontrado en Core Bancario, guardando ID: {}", clienteCore.getId());
                
                // Mapear datos del Core Bancario a la entidad local
                ClienteProspecto clienteFromCore = clienteCoreMapper.toEntity(clienteCore);
                clienteFromCore.setEstado(EstadoClienteEnum.PROSPECTO.name());
                
                // Sobrescribir datos que vienen del DTO de registro si están presentes
                if (clienteDTO.getNombres() != null) clienteFromCore.setNombres(clienteDTO.getNombres());
                if (clienteDTO.getGenero() != null) clienteFromCore.setGenero(clienteDTO.getGenero().name());
                if (clienteDTO.getFechaNacimiento() != null) clienteFromCore.setFechaNacimiento(clienteDTO.getFechaNacimiento());
                if (clienteDTO.getNivelEstudio() != null) clienteFromCore.setNivelEstudio(clienteDTO.getNivelEstudio());
                if (clienteDTO.getEstadoCivil() != null) clienteFromCore.setEstadoCivil(clienteDTO.getEstadoCivil());
                if (clienteDTO.getIngresos() != null) clienteFromCore.setIngresos(clienteDTO.getIngresos());
                if (clienteDTO.getEgresos() != null) clienteFromCore.setEgresos(clienteDTO.getEgresos());
                if (clienteDTO.getActividadEconomica() != null) clienteFromCore.setActividadEconomica(clienteDTO.getActividadEconomica());
                if (clienteDTO.getCorreoTransaccional() != null) clienteFromCore.setCorreoTransaccional(clienteDTO.getCorreoTransaccional());
                if (clienteDTO.getTelefonoTransaccional() != null) clienteFromCore.setTelefonoTransaccional(clienteDTO.getTelefonoTransaccional());
                if (clienteDTO.getTelefonoTipo() != null) clienteFromCore.setTelefonoTipo(clienteDTO.getTelefonoTipo().name());
                if (clienteDTO.getTelefonoNumero() != null) clienteFromCore.setTelefonoNumero(clienteDTO.getTelefonoNumero());
                if (clienteDTO.getDireccionTipo() != null) clienteFromCore.setDireccionTipo(clienteDTO.getDireccionTipo().name());
                if (clienteDTO.getDireccionLinea1() != null) clienteFromCore.setDireccionLinea1(clienteDTO.getDireccionLinea1());
                if (clienteDTO.getDireccionLinea2() != null) clienteFromCore.setDireccionLinea2(clienteDTO.getDireccionLinea2());
                if (clienteDTO.getDireccionCodigoPostal() != null) clienteFromCore.setDireccionCodigoPostal(clienteDTO.getDireccionCodigoPostal());
                if (clienteDTO.getDireccionGeoCodigo() != null) clienteFromCore.setDireccionGeoCodigo(clienteDTO.getDireccionGeoCodigo());
                
                ClienteProspecto clienteGuardado = clienteProspectoRepository.save(clienteFromCore);
                
                ClienteResponseDTO response = clienteProspectoMapper.toResponseDTO(clienteGuardado);
                response.setMensaje("Cliente prospecto registrado exitosamente (existente en Core Bancario)");
                
                log.info("Cliente prospecto registrado exitosamente con ID Core: {}", clienteGuardado.getIdClienteCore());
                return response;
            } catch (RestClientException e) {
                log.info("Cliente no existe en Core Bancario, registrando como nuevo prospecto");
                ClienteProspecto clienteGuardado = clienteProspectoRepository.save(cliente);
                
                ClienteResponseDTO response = clienteProspectoMapper.toResponseDTO(clienteGuardado);
                response.setMensaje("Cliente prospecto registrado exitosamente");
                
                log.info("Cliente prospecto registrado exitosamente con ID: {}", clienteGuardado.getId());
                return response;
            }
            
        } catch (Exception e) {
            log.error("Error al registrar cliente prospecto: {}", e.getMessage());
            throw new CreateEntityException("ClienteProspecto", "Error al registrar cliente prospecto: " + e.getMessage());
        }
    }



    private void validarCedulaEcuatoriana(String cedula) {
        if (cedula == null || cedula.length() != 10) {
            throw new IllegalArgumentException("La cédula debe tener exactamente 10 dígitos");
        }
        
        if (!Pattern.matches("^[0-9]{10}$", cedula)) {
            throw new IllegalArgumentException("La cédula debe contener solo números");
        }
        
        if (!validarDigitoVerificador(cedula)) {
            throw new IllegalArgumentException("La cédula no es válida según el algoritmo ecuatoriano");
        }
    }

    private boolean validarDigitoVerificador(String cedula) {
        try {
            int[] coeficientes = {2, 1, 2, 1, 2, 1, 2, 1, 2};
            int suma = 0;
            
            for (int i = 0; i < 9; i++) {
                int digito = Integer.parseInt(cedula.substring(i, i + 1));
                int producto = digito * coeficientes[i];
                suma += (producto / 10) + (producto % 10);
            }
            
            int residuo = suma % 10;
            int digitoVerificador = residuo == 0 ? 0 : 10 - residuo;
            int digitoCalculado = Integer.parseInt(cedula.substring(9, 10));
            
            return digitoVerificador == digitoCalculado;
        } catch (Exception e) {
            return false;
        }
    }




} 