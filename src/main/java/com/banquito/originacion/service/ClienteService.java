package com.banquito.originacion.service;

import com.banquito.originacion.controller.dto.ClienteProspectoDTO;
import com.banquito.originacion.controller.dto.ClienteResponseDTO;
import com.banquito.originacion.controller.mapper.ClienteProspectoMapper;
import com.banquito.originacion.enums.ClasificacionClienteEnum;
import com.banquito.originacion.enums.EstadoClienteEnum;
import com.banquito.originacion.exception.CreateEntityException;
import com.banquito.originacion.exception.ResourceNotFoundException;
import com.banquito.originacion.model.ClienteProspecto;
import com.banquito.originacion.repository.ClienteProspectoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ClienteService {

    private final ClienteProspectoRepository clienteProspectoRepository;
    private final ClienteProspectoMapper clienteProspectoMapper;
    private final CoreBancarioService coreBancarioService;

    /**
     * Consulta cliente por cédula en la base local o Core Bancario
     * FR-01 - Registro y Validación de Clientes
     */
    public ClienteResponseDTO consultarClientePorCedula(String cedula) {
        log.info("Consultando cliente por cédula: {}", cedula);
        
        // 1. Buscar en tabla clientes_prospectos por cédula
        Optional<ClienteProspecto> clienteLocalOpt = clienteProspectoRepository.findByCedula(cedula);
        
        if (clienteLocalOpt.isPresent()) {
            ClienteProspecto clienteLocal = clienteLocalOpt.get();
            log.info("Cliente encontrado en base local");
            ClienteResponseDTO response = clienteProspectoMapper.toResponseDTO(clienteLocal);
            response.setClasificacion(clasificarCliente(clienteLocal));
            response.setMensaje("Cliente encontrado en base local");
            return response;
        }
        
        // TODO: Consumir endpoint del microservicio Core Bancario para buscar cliente por cédula
        // Ejemplo: coreBancarioClient.consultarClientePorCedula(cedula)
        // Si existe, mapear y retornar como ClienteResponseDTO
        // Si no existe, lanzar ResourceNotFoundException
        // Cliente no encontrado
        throw new ResourceNotFoundException("Cliente con cédula " + cedula + " no encontrado");
    }

    /**
     * Registra un nuevo cliente prospecto
     * FR-01 - Registro y Validación de Clientes
     */
    public ClienteResponseDTO registrarClienteProspecto(ClienteProspectoDTO clienteDTO) {
        log.info("Registrando nuevo cliente prospecto con cédula: {}", clienteDTO.getCedula());
        
        try {
            // 1. Validar formato de cédula ecuatoriana
            validarCedulaEcuatoriana(clienteDTO.getCedula());
            
            // 2. Verificar que no exista ya en la base
            if (clienteProspectoRepository.findByCedula(clienteDTO.getCedula()).isPresent()) {
                throw new CreateEntityException("Ya existe un cliente con la cédula: " + clienteDTO.getCedula());
            }
            
            // TODO: Consumir endpoint de lista negra (microservicio externo) para validar cédula
            // Ejemplo: validacionClient.validarListaNegra(clienteDTO.getCedula())
            // Si está en lista negra, lanzar CreateEntityException
            
            // 4. Guardar en tabla clientes_prospectos
            ClienteProspecto cliente = clienteProspectoMapper.toEntity(clienteDTO);
            cliente.setEstado(EstadoClienteEnum.PROSPECTO.name());
            ClienteProspecto clienteGuardado = clienteProspectoRepository.save(cliente);
            
            // 5. Clasificar automáticamente como "Cliente nuevo"
            ClienteResponseDTO response = clienteProspectoMapper.toResponseDTO(clienteGuardado);
            response.setClasificacion(ClasificacionClienteEnum.CLIENTE_NUEVO);
            response.setMensaje("Cliente prospecto registrado exitosamente");
            
            log.info("Cliente prospecto registrado exitosamente con ID: {}", clienteGuardado.getId());
            return response;
            
        } catch (Exception e) {
            log.error("Error al registrar cliente prospecto: {}", e.getMessage());
            throw new CreateEntityException("Error al registrar cliente prospecto: " + e.getMessage());
        }
    }

    /**
     * Clasifica el cliente según su historial
     * FR-01 - Registro y Validación de Clientes
     */
    public ClasificacionClienteEnum clasificarCliente(ClienteProspecto cliente) {
        log.info("Clasificando cliente con ID: {}", cliente.getId());
        
        try {
            // TODO: Consumir endpoint de historial crediticio (microservicio externo)
            // Ejemplo: historialClient.tieneHistorialCrediticio(cliente.getIdClienteCore())
            boolean tieneHistorialCrediticio = false; // Reemplazar por llamada real
            
            // TODO: Consumir endpoint de productos activos (microservicio externo)
            // Ejemplo: productosClient.tieneProductosActivos(cliente.getIdClienteCore())
            boolean tieneProductosActivos = false; // Reemplazar por llamada real
            
            // TODO: Consumir endpoint de morosidad (microservicio externo)
            // Ejemplo: morosidadClient.tieneMorosidad(cliente.getIdClienteCore())
            boolean tieneMorosidad = false; // Reemplazar por llamada real
            
            // 4. Asignar clasificación
            if (!tieneHistorialCrediticio) {
                return ClasificacionClienteEnum.CLIENTE_NUEVO;
            } else if (tieneMorosidad) {
                return ClasificacionClienteEnum.CLIENTE_MOROSO;
            } else if (tieneProductosActivos) {
                return ClasificacionClienteEnum.CLIENTE_ACTIVO;
            } else {
                return ClasificacionClienteEnum.CLIENTE_NUEVO;
            }
            
        } catch (Exception e) {
            log.error("Error al clasificar cliente: {}", e.getMessage());
            return ClasificacionClienteEnum.CLIENTE_NUEVO; // Clasificación por defecto
        }
    }

    /**
     * Valida el formato de cédula ecuatoriana
     */
    private void validarCedulaEcuatoriana(String cedula) {
        if (cedula == null || cedula.length() != 10) {
            throw new IllegalArgumentException("La cédula debe tener exactamente 10 dígitos");
        }
        
        if (!Pattern.matches("^[0-9]{10}$", cedula)) {
            throw new IllegalArgumentException("La cédula debe contener solo números");
        }
        
        // Validación del dígito verificador (algoritmo ecuatoriano)
        if (!validarDigitoVerificador(cedula)) {
            throw new IllegalArgumentException("La cédula no es válida según el algoritmo ecuatoriano");
        }
    }

    /**
     * Valida el dígito verificador de la cédula ecuatoriana
     */
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