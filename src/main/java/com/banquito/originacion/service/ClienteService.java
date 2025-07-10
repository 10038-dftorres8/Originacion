package com.banquito.originacion.service;

import com.banquito.originacion.client.CoreBancarioClient;
import com.banquito.originacion.controller.dto.ClienteCoreResponseDTO;
import com.banquito.originacion.controller.dto.ClienteProspectoRegistroDTO;
import com.banquito.originacion.controller.dto.ClienteResponseDTO;
import com.banquito.originacion.controller.dto.PersonaCoreResponseDTO;
import com.banquito.originacion.controller.mapper.ClienteCoreMapper;
import com.banquito.originacion.controller.mapper.ClienteProspectoMapper;
import com.banquito.originacion.controller.mapper.ClienteProspectoRegistroMapper;
import com.banquito.originacion.controller.mapper.PersonaCoreMapper;
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

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ClienteService {

    private final ClienteProspectoRepository clienteProspectoRepository;
    private final ClienteProspectoMapper clienteProspectoMapper;
    private final ClienteProspectoRegistroMapper clienteProspectoRegistroMapper;
    private final ClienteCoreMapper clienteCoreMapper;
    private final PersonaCoreMapper personaCoreMapper;
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
            
            PersonaCoreResponseDTO personaCore = coreBancarioClient.consultarPersonaPorIdentificacion("CEDULA", cedula);
            log.info("Persona encontrada en Core Bancario");
            
            try {
                List<ClienteCoreResponseDTO> clientesCore = coreBancarioClient.consultarClientePorIdentificacion("CEDULA", cedula);
                if (!clientesCore.isEmpty()) {
                    ClienteCoreResponseDTO clienteCore = clientesCore.get(0);
            log.info("Cliente encontrado en Core Bancario con ID: {}", clienteCore.getId());
            ClienteResponseDTO response = clienteCoreMapper.toResponseDTO(clienteCore);
            response.setMensaje("Cliente encontrado en Core Bancario");
            return response;
                } else {
                    log.info("La persona existe en Core Bancario pero no es cliente");
                    ClienteResponseDTO response = new ClienteResponseDTO();
                    response.setMensaje("Persona encontrada en Core Bancario pero no es cliente");
                    response.setCedula(personaCore.getNumeroIdentificacion());
                    response.setNombres(personaCore.getNombre());
                    response.setExisteEnCore(true);
                    response.setEsCliente(false);
                    return response;
                }
            } catch (RestClientException e) {
                log.info("La persona existe en Core Bancario pero no es cliente");
                ClienteResponseDTO response = new ClienteResponseDTO();
                response.setMensaje("Persona encontrada en Core Bancario pero no es cliente");
                response.setCedula(personaCore.getNumeroIdentificacion());
                response.setNombres(personaCore.getNombre());
                response.setExisteEnCore(true);
                response.setEsCliente(false);
                return response;
            }
        } catch (RestClientException e) {
            log.warn("Persona no encontrada en Core Bancario: {}", e.getMessage());
            throw new ResourceNotFoundException("Persona con cédula " + cedula + " no encontrada");
        }
    }

    public ClienteResponseDTO registrarClienteProspecto(ClienteProspectoRegistroDTO clienteDTO) {
        log.info("Registrando cliente prospecto con cédula: {}", clienteDTO.getCedula());
        
        try {
            if (clienteProspectoRepository.findByCedula(clienteDTO.getCedula()).isPresent()) {
                throw new CreateEntityException("ClienteProspecto", "Ya existe un cliente con la cédula: " + clienteDTO.getCedula());
            }
            

            PersonaCoreResponseDTO personaCore = null;
            boolean personaExisteEnCore = false;
            
            try {
                personaCore = coreBancarioClient.consultarPersonaPorIdentificacion("CEDULA", clienteDTO.getCedula());
                personaExisteEnCore = true;
                log.info("Persona encontrada en Core Bancario");
            } catch (RestClientException e) {
                log.info("Persona no existe en Core Bancario, se registrará como nueva");
                personaExisteEnCore = false;
            }
            
            ClienteProspecto cliente;
            
            if (personaExisteEnCore) {
                try {
                    List<ClienteCoreResponseDTO> clientesCore = coreBancarioClient.consultarClientePorIdentificacion("CEDULA", clienteDTO.getCedula());
                    if (!clientesCore.isEmpty()) {
                        ClienteCoreResponseDTO clienteCore = clientesCore.get(0);
                        log.info("Cliente encontrado en Core Bancario, mapeando datos del Core");
                
                        cliente = clienteCoreMapper.toEntity(clienteCore);
                        cliente.setEstado(EstadoClienteEnum.PROSPECTO.name());
                
                        if (clienteDTO.getNombres() != null && !clienteDTO.getNombres().trim().isEmpty()) {
                            cliente.setNombres(clienteDTO.getNombres());
                        }
                        if (clienteDTO.getGenero() != null) {
                            cliente.setGenero(clienteDTO.getGenero().name());
                        }
                        if (clienteDTO.getFechaNacimiento() != null) {
                            cliente.setFechaNacimiento(clienteDTO.getFechaNacimiento());
                        }
                        if (clienteDTO.getNivelEstudio() != null && !clienteDTO.getNivelEstudio().trim().isEmpty()) {
                            cliente.setNivelEstudio(clienteDTO.getNivelEstudio());
                        }
                        if (clienteDTO.getEstadoCivil() != null && !clienteDTO.getEstadoCivil().trim().isEmpty()) {
                            cliente.setEstadoCivil(clienteDTO.getEstadoCivil());
                        }
                        if (clienteDTO.getIngresos() != null) {
                            cliente.setIngresos(clienteDTO.getIngresos());
                        }
                        if (clienteDTO.getEgresos() != null) {
                            cliente.setEgresos(clienteDTO.getEgresos());
                        }
                        if (clienteDTO.getActividadEconomica() != null && !clienteDTO.getActividadEconomica().trim().isEmpty()) {
                            cliente.setActividadEconomica(clienteDTO.getActividadEconomica());
                        }
                        if (clienteDTO.getCorreoTransaccional() != null && !clienteDTO.getCorreoTransaccional().trim().isEmpty()) {
                            cliente.setCorreoTransaccional(clienteDTO.getCorreoTransaccional());
                        }
                        if (clienteDTO.getTelefonoTransaccional() != null && !clienteDTO.getTelefonoTransaccional().trim().isEmpty()) {
                            cliente.setTelefonoTransaccional(clienteDTO.getTelefonoTransaccional());
                        }
                        if (clienteDTO.getTelefonoTipo() != null) {
                            cliente.setTelefonoTipo(clienteDTO.getTelefonoTipo().name());
                        }
                        if (clienteDTO.getTelefonoNumero() != null && !clienteDTO.getTelefonoNumero().trim().isEmpty()) {
                            cliente.setTelefonoNumero(clienteDTO.getTelefonoNumero());
                        }
                        if (clienteDTO.getDireccionTipo() != null) {
                            cliente.setDireccionTipo(clienteDTO.getDireccionTipo().name());
                        }
                        if (clienteDTO.getDireccionLinea1() != null && !clienteDTO.getDireccionLinea1().trim().isEmpty()) {
                            cliente.setDireccionLinea1(clienteDTO.getDireccionLinea1());
                        }
                        if (clienteDTO.getDireccionLinea2() != null && !clienteDTO.getDireccionLinea2().trim().isEmpty()) {
                            cliente.setDireccionLinea2(clienteDTO.getDireccionLinea2());
                        }
                        if (clienteDTO.getDireccionCodigoPostal() != null && !clienteDTO.getDireccionCodigoPostal().trim().isEmpty()) {
                            cliente.setDireccionCodigoPostal(clienteDTO.getDireccionCodigoPostal());
                        }
                        if (clienteDTO.getDireccionGeoCodigo() != null && !clienteDTO.getDireccionGeoCodigo().trim().isEmpty()) {
                            cliente.setDireccionGeoCodigo(clienteDTO.getDireccionGeoCodigo());
                        }
                        
                    } else {
                        log.info("La persona existe en Core Bancario pero no es cliente, mapeando datos de persona");
                        
                        cliente = personaCoreMapper.toEntity(personaCore);
                        cliente.setEstado(EstadoClienteEnum.PROSPECTO.name());
                        
                        completarDatosDelDTO(cliente, clienteDTO);
                    }
                    
                } catch (RestClientException e) {
                    log.info("La persona existe en Core Bancario pero no es cliente, mapeando datos de persona");
                    
                    cliente = personaCoreMapper.toEntity(personaCore);
                    cliente.setEstado(EstadoClienteEnum.PROSPECTO.name());
                    
                    completarDatosDelDTO(cliente, clienteDTO);
                }
            } else {
                validarDatosCompletosParaNuevaPersona(clienteDTO);
                
                log.info("Creando nuevo prospecto sin datos del Core Bancario");
                cliente = clienteProspectoRegistroMapper.toEntity(clienteDTO);
                cliente.setEstado(EstadoClienteEnum.PROSPECTO.name());
            }
            
                ClienteProspecto clienteGuardado = clienteProspectoRepository.save(cliente);
                
                ClienteResponseDTO response = clienteProspectoMapper.toResponseDTO(clienteGuardado);
            if (personaExisteEnCore) {
                response.setMensaje("Cliente prospecto registrado exitosamente (persona existente en Core Bancario)");
            } else {
                response.setMensaje("Cliente prospecto registrado exitosamente (nueva persona)");
            }
                
                log.info("Cliente prospecto registrado exitosamente con ID: {}", clienteGuardado.getId());
                return response;
            
        } catch (Exception e) {
            log.error("Error al registrar cliente prospecto: {}", e.getMessage());
            throw new CreateEntityException("ClienteProspecto", "Error al registrar cliente prospecto: " + e.getMessage());
        }
    }

    private void completarDatosDelDTO(ClienteProspecto cliente, ClienteProspectoRegistroDTO clienteDTO) {
        if (clienteDTO.getNombres() != null && !clienteDTO.getNombres().trim().isEmpty()) {
            cliente.setNombres(clienteDTO.getNombres());
        }
        if (clienteDTO.getGenero() != null) {
            cliente.setGenero(clienteDTO.getGenero().name());
        }
        if (clienteDTO.getFechaNacimiento() != null) {
            cliente.setFechaNacimiento(clienteDTO.getFechaNacimiento());
        }
        if (clienteDTO.getNivelEstudio() != null && !clienteDTO.getNivelEstudio().trim().isEmpty()) {
            cliente.setNivelEstudio(clienteDTO.getNivelEstudio());
        }
        if (clienteDTO.getEstadoCivil() != null && !clienteDTO.getEstadoCivil().trim().isEmpty()) {
            cliente.setEstadoCivil(clienteDTO.getEstadoCivil());
        }
        if (clienteDTO.getIngresos() != null) {
            cliente.setIngresos(clienteDTO.getIngresos());
        }
        if (clienteDTO.getEgresos() != null) {
            cliente.setEgresos(clienteDTO.getEgresos());
        }
        if (clienteDTO.getActividadEconomica() != null && !clienteDTO.getActividadEconomica().trim().isEmpty()) {
            cliente.setActividadEconomica(clienteDTO.getActividadEconomica());
        }
        if (clienteDTO.getCorreoTransaccional() != null && !clienteDTO.getCorreoTransaccional().trim().isEmpty()) {
            cliente.setCorreoTransaccional(clienteDTO.getCorreoTransaccional());
        }
        if (clienteDTO.getTelefonoTransaccional() != null && !clienteDTO.getTelefonoTransaccional().trim().isEmpty()) {
            cliente.setTelefonoTransaccional(clienteDTO.getTelefonoTransaccional());
        }
        if (clienteDTO.getTelefonoTipo() != null) {
            cliente.setTelefonoTipo(clienteDTO.getTelefonoTipo().name());
        }
        if (clienteDTO.getTelefonoNumero() != null && !clienteDTO.getTelefonoNumero().trim().isEmpty()) {
            cliente.setTelefonoNumero(clienteDTO.getTelefonoNumero());
        }
        if (clienteDTO.getDireccionTipo() != null) {
            cliente.setDireccionTipo(clienteDTO.getDireccionTipo().name());
        }
        if (clienteDTO.getDireccionLinea1() != null && !clienteDTO.getDireccionLinea1().trim().isEmpty()) {
            cliente.setDireccionLinea1(clienteDTO.getDireccionLinea1());
        }
        if (clienteDTO.getDireccionLinea2() != null && !clienteDTO.getDireccionLinea2().trim().isEmpty()) {
            cliente.setDireccionLinea2(clienteDTO.getDireccionLinea2());
        }
        if (clienteDTO.getDireccionCodigoPostal() != null && !clienteDTO.getDireccionCodigoPostal().trim().isEmpty()) {
            cliente.setDireccionCodigoPostal(clienteDTO.getDireccionCodigoPostal());
        }
        if (clienteDTO.getDireccionGeoCodigo() != null && !clienteDTO.getDireccionGeoCodigo().trim().isEmpty()) {
            cliente.setDireccionGeoCodigo(clienteDTO.getDireccionGeoCodigo());
        }
    }

    private void validarDatosCompletosParaNuevaPersona(ClienteProspectoRegistroDTO clienteDTO) {
        if (clienteDTO.getNombres() == null || clienteDTO.getNombres().trim().isEmpty()) {
            throw new CreateEntityException("ClienteProspecto", "Los nombres son obligatorios para registrar una nueva persona");
        }
        if (clienteDTO.getGenero() == null) {
            throw new CreateEntityException("ClienteProspecto", "El género es obligatorio para registrar una nueva persona");
        }
        if (clienteDTO.getFechaNacimiento() == null) {
            throw new CreateEntityException("ClienteProspecto", "La fecha de nacimiento es obligatoria para registrar una nueva persona");
        }
        if (clienteDTO.getNivelEstudio() == null || clienteDTO.getNivelEstudio().trim().isEmpty()) {
            throw new CreateEntityException("ClienteProspecto", "El nivel de estudio es obligatorio para registrar una nueva persona");
        }
        if (clienteDTO.getEstadoCivil() == null || clienteDTO.getEstadoCivil().trim().isEmpty()) {
            throw new CreateEntityException("ClienteProspecto", "El estado civil es obligatorio para registrar una nueva persona");
        }
        if (clienteDTO.getIngresos() == null) {
            throw new CreateEntityException("ClienteProspecto", "Los ingresos son obligatorios para registrar una nueva persona");
        }
        if (clienteDTO.getEgresos() == null) {
            throw new CreateEntityException("ClienteProspecto", "Los egresos son obligatorios para registrar una nueva persona");
        }
        if (clienteDTO.getActividadEconomica() == null || clienteDTO.getActividadEconomica().trim().isEmpty()) {
            throw new CreateEntityException("ClienteProspecto", "La actividad económica es obligatoria para registrar una nueva persona");
            }
        if (clienteDTO.getCorreoTransaccional() == null || clienteDTO.getCorreoTransaccional().trim().isEmpty()) {
            throw new CreateEntityException("ClienteProspecto", "El correo transaccional es obligatorio para registrar una nueva persona");
        }
        if (clienteDTO.getTelefonoTransaccional() == null || clienteDTO.getTelefonoTransaccional().trim().isEmpty()) {
            throw new CreateEntityException("ClienteProspecto", "El teléfono transaccional es obligatorio para registrar una nueva persona");
        }
        if (clienteDTO.getTelefonoTipo() == null) {
            throw new CreateEntityException("ClienteProspecto", "El tipo de teléfono es obligatorio para registrar una nueva persona");
        }
        if (clienteDTO.getTelefonoNumero() == null || clienteDTO.getTelefonoNumero().trim().isEmpty()) {
            throw new CreateEntityException("ClienteProspecto", "El número de teléfono es obligatorio para registrar una nueva persona");
        }
        if (clienteDTO.getDireccionTipo() == null) {
            throw new CreateEntityException("ClienteProspecto", "El tipo de dirección es obligatorio para registrar una nueva persona");
        }
        if (clienteDTO.getDireccionLinea1() == null || clienteDTO.getDireccionLinea1().trim().isEmpty()) {
            throw new CreateEntityException("ClienteProspecto", "La línea 1 de dirección es obligatoria para registrar una nueva persona");
        }
        if (clienteDTO.getDireccionCodigoPostal() == null || clienteDTO.getDireccionCodigoPostal().trim().isEmpty()) {
            throw new CreateEntityException("ClienteProspecto", "El código postal es obligatorio para registrar una nueva persona");
        }
        if (clienteDTO.getDireccionGeoCodigo() == null || clienteDTO.getDireccionGeoCodigo().trim().isEmpty()) {
            throw new CreateEntityException("ClienteProspecto", "El código geográfico es obligatorio para registrar una nueva persona");
        }
    }
} 