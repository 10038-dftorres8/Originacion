package com.banquito.originacion.service;

import com.banquito.originacion.client.PrestamosClient;
import com.banquito.originacion.client.CoreBancarioClient;
import com.banquito.originacion.client.GestionVehiculosClient;
import com.banquito.originacion.controller.dto.*;
import com.banquito.originacion.controller.dto.external.PrestamosExternalDTO;
import com.banquito.originacion.controller.dto.external.PersonaResponseDTO;
import com.banquito.originacion.controller.dto.external.ConcesionarioResponseDTO;
import com.banquito.originacion.controller.mapper.*;
import com.banquito.originacion.enums.EstadoSolicitudEnum;
import static com.banquito.originacion.enums.EstadoSolicitudEnum.*;
import com.banquito.originacion.exception.CreateEntityException;
import com.banquito.originacion.exception.ResourceNotFoundException;
import com.banquito.originacion.model.SolicitudCredito;
import com.banquito.originacion.model.HistorialEstado;
import com.banquito.originacion.repository.SolicitudCreditoRepository;
import com.banquito.originacion.repository.HistorialEstadoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SolicitudService {
    private final SolicitudCreditoRepository solicitudCreditoRepository;
    private final HistorialEstadoRepository historialEstadoRepository;
    private final SolicitudCreditoMapper solicitudCreditoMapper;

    private final HistorialEstadoMapper historialEstadoMapper;
    private final SolicitudEstadoMapper solicitudEstadoMapper;

    private final SolicitudConsultaMapper solicitudConsultaMapper;
    private final SimulacionSolicitudMapper simulacionSolicitudMapper;
    private final SolicitudDetalladaMapper solicitudDetalladaMapper;
    private final GestionVehiculosService gestionVehiculosService;
    private final GestionVehiculosClient gestionVehiculosClient;
    private final PrestamosClient prestamosClient;

    private final CoreBancarioClient coreBancarioClient;

    // Metodos para la solicitud de credito
    public SolicitudCreditoResponseDTO crearSolicitud(SolicitudCreditoDTO solicitudDTO) {
        log.info("Iniciando creación de solicitud para cédula: {}", solicitudDTO.getCedulaSolicitante());

        
        validarClienteEnCore(solicitudDTO.getCedulaSolicitante());

        
        PrestamosExternalDTO prestamo = validarPrestamo(solicitudDTO.getIdPrestamo());

        
        VehiculoResponseDTO vehiculo = validarVehiculo(solicitudDTO.getRucConcesionario(),
                solicitudDTO.getPlacaVehiculo());

        
        validarVendedor(solicitudDTO.getRucConcesionario(), solicitudDTO.getCedulaVendedor());

        
        validarSolicitudExistente(solicitudDTO.getCedulaSolicitante(), solicitudDTO.getIdPrestamo());

        
        BigDecimal montoSolicitado = calcularMontoSolicitado(vehiculo.getValor(), solicitudDTO.getValorEntrada());

        
        validarLimitesPrestamo(montoSolicitado, solicitudDTO.getPlazoMeses(), prestamo);

        
        validarCapacidadPago(solicitudDTO.getCedulaSolicitante(), montoSolicitado, solicitudDTO.getPlazoMeses(),
                prestamo.getTasaInteres(), solicitudDTO.getCapacidadPagoSolicitante());

        
        SolicitudCredito solicitud = new SolicitudCredito();
        solicitud.setNumeroSolicitud(generarNumeroSolicitud());
        solicitud.setFechaSolicitud(LocalDateTime.now());
        solicitud.setCedulaSolicitante(solicitudDTO.getCedulaSolicitante());
        solicitud.setCalificacionSolicitante(solicitudDTO.getCalificacionSolicitante());
        solicitud.setCapacidadPagoSolicitante(solicitudDTO.getCapacidadPagoSolicitante());
        solicitud.setPlacaVehiculo(solicitudDTO.getPlacaVehiculo());
        solicitud.setRucConcesionario(solicitudDTO.getRucConcesionario());
        solicitud.setCedulaVendedor(solicitudDTO.getCedulaVendedor());
        solicitud.setIdPrestamo(solicitudDTO.getIdPrestamo());
        solicitud.setValorEntrada(solicitudDTO.getValorEntrada());
        solicitud.setMontoSolicitado(montoSolicitado);
        solicitud.setPlazoMeses(solicitudDTO.getPlazoMeses());
        solicitud.setEstado(EstadoSolicitudEnum.BORRADOR.name());

        SolicitudCredito saved = solicitudCreditoRepository.save(solicitud);
        log.info("Solicitud creada exitosamente con ID: {}", saved.getId());

        return solicitudCreditoMapper.toResponseDTO(saved);
    }

    public SimulacionSolicitudResponseDTO simularSolicitud(String numeroSolicitud) {
        log.info("Iniciando simulación de solicitud existente con número: {}", numeroSolicitud);

        SolicitudCredito solicitud = solicitudCreditoRepository.findByNumeroSolicitud(numeroSolicitud)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud no encontrada"));

        PrestamosExternalDTO prestamo = prestamosClient.consultarPrestamoPorId(solicitud.getIdPrestamo());

        VehiculoResponseDTO vehiculo = gestionVehiculosService.obtenerVehiculo(
                solicitud.getRucConcesionario(), solicitud.getPlacaVehiculo());

        BigDecimal capacidadPago = solicitud.getCapacidadPagoSolicitante();

        BigDecimal tasaInteresAnual = prestamo.getTasaInteres();
        log.info("Tasa de interés anual: {}%", tasaInteresAnual);

        BigDecimal cuotaMensual = calcularCuotaMensual(solicitud.getMontoSolicitado(), solicitud.getPlazoMeses(),
                tasaInteresAnual);

        BigDecimal montoTotal = cuotaMensual.multiply(new BigDecimal(solicitud.getPlazoMeses()));
        BigDecimal totalIntereses = montoTotal.subtract(solicitud.getMontoSolicitado());
        BigDecimal totalAPagar = montoTotal.add(solicitud.getValorEntrada());

        boolean esAprobable = cuotaMensual.compareTo(capacidadPago) <= 0;
        String motivoRechazo = esAprobable ? null
                : String.format("La cuota mensual %s excede la capacidad de pago del cliente %s", cuotaMensual,
                        capacidadPago);

        List<AmortizacionDTO> tablaAmortizacion = calcularTablaAmortizacion(solicitud.getMontoSolicitado(),
                solicitud.getPlazoMeses(), tasaInteresAnual);

        SimulacionSolicitudResponseDTO response = simulacionSolicitudMapper.toSimulacionSolicitudResponseDTO(solicitud,
                prestamo, vehiculo);

        response.setCuotaMensual(cuotaMensual);
        response.setMontoTotal(montoTotal);
        response.setTotalIntereses(totalIntereses);
        response.setTotalAPagar(totalAPagar);
        response.setTablaAmortizacion(tablaAmortizacion);
        response.setEsAprobable(esAprobable);
        response.setMotivoRechazo(motivoRechazo);

        log.info("Simulación de solicitud existente completada para número: {}. Cuota: {}, Aprobable: {}",
                numeroSolicitud, cuotaMensual, esAprobable);

        return response;
    }

    public EstadoSolicitudResponseDTO consultarEstadoSolicitud(Long idSolicitud) {
        SolicitudCredito solicitud = solicitudCreditoRepository.findById(idSolicitud)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud no encontrada"));

        List<HistorialEstado> historial = historialEstadoRepository.findByIdSolicitudOrderByFechaCambioAsc(idSolicitud);

        EstadoSolicitudResponseDTO response = solicitudEstadoMapper.toEstadoSolicitudResponseDTO(solicitud);
        response.setHistorial(historial.stream().map(historialEstadoMapper::toDTO).toList());

        return response;
    }

    public void cambiarEstadoSolicitud(Long idSolicitud, String nuevoEstado, String motivo, String usuario) {
        try {
            log.info("🔄 Iniciando cambio de estado de solicitud {} a estado: {}", idSolicitud, nuevoEstado);

            SolicitudCredito solicitud = solicitudCreditoRepository.findById(idSolicitud)
                    .orElseThrow(() -> new ResourceNotFoundException("Solicitud no encontrada"));
            log.info("✅ Solicitud encontrada - Estado actual: {}", solicitud.getEstado());

            EstadoSolicitudEnum estadoActual = EstadoSolicitudEnum.valueOf(solicitud.getEstado());
            log.info("✅ Estado actual parseado: {}", estadoActual);

            EstadoSolicitudEnum estadoNuevo;
            try {
                estadoNuevo = EstadoSolicitudEnum.valueOf(nuevoEstado);
                log.info("✅ Estado nuevo parseado: {}", estadoNuevo);
            } catch (IllegalArgumentException e) {
                log.error("❌ Estado de solicitud no válido: {}", nuevoEstado);
                throw new CreateEntityException("SolicitudCredito", "Estado de solicitud no válido: " + nuevoEstado);
            }

            log.info("🔍 Validando transición de estado: {} -> {}", estadoActual, estadoNuevo);

            if (!esTransicionValida(estadoActual, estadoNuevo)) {
                String mensajeError = String.format(
                        "Transición de estado no permitida: %s -> %s. La solicitud debe seguir la jerarquía: BORRADOR -> EN_REVISION -> APROBADA/RECHAZADA",
                        estadoActual, estadoNuevo);
                log.error("❌ Error en cambio de estado: {}", mensajeError);
                throw new CreateEntityException("SolicitudCredito", mensajeError);
            }
            log.info("✅ Transición válida");

            String estadoAnterior = solicitud.getEstado();
            solicitud.setEstado(nuevoEstado);
            solicitudCreditoRepository.save(solicitud);
            log.info("✅ Estado actualizado en base de datos");

            HistorialEstadoDTO historialDTO = new HistorialEstadoDTO(
                    idSolicitud, estadoAnterior, nuevoEstado,
                    LocalDateTime.now(), 0L, motivo);

            HistorialEstado historial = historialEstadoMapper.toEntity(historialDTO);
            historialEstadoRepository.save(historial);
            log.info("✅ Historial guardado");

            log.info("✅ Estado de solicitud {} cambiado exitosamente de {} a {} por usuario {}",
                    idSolicitud, estadoAnterior, nuevoEstado, usuario);
        } catch (Exception e) {
            log.error("❌ Error en cambiarEstadoSolicitud: {}", e.getMessage(), e);
            throw e;
        }
    }

    public SolicitudDetalladaResponseDTO obtenerSolicitudDetallada(String numeroSolicitud) {
        log.info("Obteniendo información detallada de la solicitud: {}", numeroSolicitud);

        SolicitudCredito solicitud = solicitudCreditoRepository.findByNumeroSolicitud(numeroSolicitud)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Solicitud no encontrada con número: " + numeroSolicitud));

        try {
            // Intentar obtener información de servicios externos, pero manejar errores graciosamente
            PersonaResponseDTO persona = null;
            VehiculoResponseDTO vehiculo = null;
            ConcesionarioResponseDTO concesionario = null;
            VendedorResponseDTO vendedor = null;
            PrestamosExternalDTO prestamo = null;

            try {
                // Primero intentar con ModuloCliente local
                String url = "http://localhost:83/api/clientes/v1/clientes/personas/CEDULA/" + solicitud.getCedulaSolicitante();
                System.out.println("Consultando persona en ModuloCliente: " + url);
                
                RestTemplate restTemplate = new RestTemplate();
                Object clienteResponse = restTemplate.getForObject(url, Object.class);
                
                if (clienteResponse != null) {
                    // Mapear la respuesta del ModuloCliente a PersonaResponseDTO
                    persona = new PersonaResponseDTO();
                    persona.setNumeroIdentificacion(solicitud.getCedulaSolicitante());
                    persona.setTipoIdentificacion("CEDULA");
                    
                    // Extraer nombre del response (asumiendo que es un Map)
                    if (clienteResponse instanceof Map) {
                        Map<String, Object> clienteMap = (Map<String, Object>) clienteResponse;
                        String nombre = (String) clienteMap.get("nombre");
                        String email = (String) clienteMap.get("correoElectronico");
                        
                        persona.setNombre(nombre != null ? nombre : "Cliente " + solicitud.getCedulaSolicitante());
                        persona.setCorreoElectronico(email != null ? email : "cliente@" + solicitud.getCedulaSolicitante() + ".com");
                    } else {
                        persona.setNombre("Cliente " + solicitud.getCedulaSolicitante());
                        persona.setCorreoElectronico("cliente@" + solicitud.getCedulaSolicitante() + ".com");
                    }
                    
                    persona.setEstado("ACTIVO");
                    System.out.println("Persona obtenida de ModuloCliente: " + persona.getNombre());
                } else {
                    throw new Exception("Respuesta vacía de ModuloCliente");
                }
            } catch (Exception e) {
                System.out.println("No se pudo obtener información de persona desde ModuloCliente: " + e.getMessage());
                // Fallback: intentar con CoreBancario
                try {
                    persona = coreBancarioClient.consultarPersonaPorIdentificacion("CEDULA",
                            solicitud.getCedulaSolicitante());
                } catch (Exception e2) {
                    System.out.println("No se pudo obtener información de persona desde CoreBancario: " + e2.getMessage());
                    // Crear persona mock como último recurso
                    persona = new PersonaResponseDTO();
                    persona.setNumeroIdentificacion(solicitud.getCedulaSolicitante());
                    persona.setTipoIdentificacion("CEDULA");
                    persona.setNombre("Cliente " + solicitud.getCedulaSolicitante());
                    persona.setCorreoElectronico("cliente@" + solicitud.getCedulaSolicitante() + ".com");
                    persona.setEstado("ACTIVO");
                }
            }

            try {
                System.out.println("Consultando vehículo - RUC: " + solicitud.getRucConcesionario() + ", Placa: " + solicitud.getPlacaVehiculo());
                vehiculo = gestionVehiculosClient.getVehiculoByPlaca(solicitud.getRucConcesionario(),
                        solicitud.getPlacaVehiculo());
                System.out.println("Vehículo obtenido: " + vehiculo);
            } catch (Exception e) {
                System.out.println("No se pudo obtener información de vehículo: " + e.getMessage());
                e.printStackTrace();
                // Crear vehículo mock
                vehiculo = new VehiculoResponseDTO();
                vehiculo.setPlaca(solicitud.getPlacaVehiculo());
                vehiculo.setMarca("Marca");
                vehiculo.setModelo("Modelo");
                vehiculo.setValor(solicitud.getMontoSolicitado());
            }

            try {
                concesionario = gestionVehiculosClient.getConcesionarioByRuc(solicitud.getRucConcesionario());
            } catch (Exception e) {
                System.out.println("No se pudo obtener información de concesionario: " + e.getMessage());
                // Crear concesionario mock
                concesionario = new ConcesionarioResponseDTO();
                concesionario.setRuc(solicitud.getRucConcesionario());
                concesionario.setRazonSocial("Concesionario " + solicitud.getRucConcesionario());
            }

            try {
                vendedor = gestionVehiculosClient.getVendedorByCedula(solicitud.getRucConcesionario(),
                        solicitud.getCedulaVendedor());
            } catch (Exception e) {
                System.out.println("No se pudo obtener información de vendedor: " + e.getMessage());
                // Crear vendedor mock
                vendedor = new VendedorResponseDTO();
                vendedor.setCedula(solicitud.getCedulaVendedor());
                vendedor.setNombre("Vendedor " + solicitud.getCedulaVendedor());
            }

            try {
                prestamo = prestamosClient.consultarPrestamoPorId(solicitud.getIdPrestamo());
            } catch (Exception e) {
                System.out.println("No se pudo obtener información de préstamo: " + e.getMessage());
                // Crear préstamo mock
                prestamo = new PrestamosExternalDTO();
                prestamo.setId(solicitud.getIdPrestamo());
                prestamo.setNombre("Préstamo Automotriz");
                prestamo.setTasaInteres(new BigDecimal("12.5"));
            }

            return solicitudDetalladaMapper.toSolicitudDetalladaResponseDTO(
                    solicitud, persona, vehiculo, concesionario, vendedor, prestamo);

        } catch (Exception e) {
            System.out.println("Error general al obtener información detallada: " + e.getMessage());
            throw new CreateEntityException("SolicitudCredito",
                    "Error al obtener información detallada de la solicitud: " + e.getMessage());
        }
    }

    public SolicitudCreditoResponseDTO editarSolicitud(String numeroSolicitud,
            SolicitudCreditoEdicionDTO solicitudEdicionDTO) {
        log.info("Editando solicitud: {} para cédula: {}", numeroSolicitud, solicitudEdicionDTO.getCedulaSolicitante());

        SolicitudCredito solicitudExistente = solicitudCreditoRepository.findByNumeroSolicitud(numeroSolicitud)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Solicitud no encontrada con número: " + numeroSolicitud));

        if (!EstadoSolicitudEnum.BORRADOR.name().equals(solicitudExistente.getEstado())) {
            throw new CreateEntityException("SolicitudCredito",
                    "Solo se pueden editar solicitudes en estado BORRADOR. Estado actual: "
                            + solicitudExistente.getEstado());
        }

        validarClienteEnCore(solicitudEdicionDTO.getCedulaSolicitante());

        PrestamosExternalDTO prestamo = validarPrestamo(solicitudEdicionDTO.getIdPrestamo());

        VehiculoResponseDTO vehiculo = validarVehiculo(solicitudEdicionDTO.getRucConcesionario(),
                solicitudEdicionDTO.getPlacaVehiculo());

        validarVendedor(solicitudEdicionDTO.getRucConcesionario(), solicitudEdicionDTO.getCedulaVendedor());

        validarSolicitudExistenteParaEdicion(solicitudEdicionDTO.getCedulaSolicitante(),
                solicitudEdicionDTO.getIdPrestamo(), solicitudExistente.getId());

        BigDecimal montoSolicitado = calcularMontoSolicitado(vehiculo.getValor(),
                solicitudEdicionDTO.getValorEntrada());

        validarLimitesPrestamo(montoSolicitado, solicitudEdicionDTO.getPlazoMeses(), prestamo);

        validarCapacidadPago(solicitudEdicionDTO.getCedulaSolicitante(), montoSolicitado,
                solicitudEdicionDTO.getPlazoMeses(), prestamo.getTasaInteres(),
                solicitudEdicionDTO.getCapacidadPagoSolicitante());

        solicitudExistente.setCedulaSolicitante(solicitudEdicionDTO.getCedulaSolicitante());
        solicitudExistente.setCalificacionSolicitante(solicitudEdicionDTO.getCalificacionSolicitante());
        solicitudExistente.setCapacidadPagoSolicitante(solicitudEdicionDTO.getCapacidadPagoSolicitante());
        solicitudExistente.setPlacaVehiculo(solicitudEdicionDTO.getPlacaVehiculo());
        solicitudExistente.setRucConcesionario(solicitudEdicionDTO.getRucConcesionario());
        solicitudExistente.setCedulaVendedor(solicitudEdicionDTO.getCedulaVendedor());
        solicitudExistente.setIdPrestamo(solicitudEdicionDTO.getIdPrestamo());
        solicitudExistente.setValorEntrada(solicitudEdicionDTO.getValorEntrada());
        solicitudExistente.setMontoSolicitado(montoSolicitado);
        solicitudExistente.setPlazoMeses(solicitudEdicionDTO.getPlazoMeses());
        solicitudExistente.setVersion(solicitudExistente.getVersion() + 1);

        SolicitudCredito saved = solicitudCreditoRepository.save(solicitudExistente);
        log.info("Solicitud editada exitosamente con ID: {}", saved.getId());

        return solicitudCreditoMapper.toResponseDTO(saved);
    }

    public SolicitudConsultaPaginadaResponseDTO consultarSolicitudesPorRangoFechas(LocalDateTime fechaInicio,
            LocalDateTime fechaFin, String estado, String cedulaVendedor, String rucConcesionario, Integer pagina,
            Integer tamanoPagina) {
        long diasEntreFechas = java.time.Duration.between(fechaInicio, fechaFin).toDays();
        if (diasEntreFechas > 31) {
            throw new CreateEntityException("SolicitudCredito",
                    "El rango de fechas no puede exceder 31 días. Días solicitados: " + diasEntreFechas);
        }

        if (fechaInicio.isAfter(fechaFin)) {
            throw new CreateEntityException("SolicitudCredito",
                    "La fecha de inicio debe ser anterior a la fecha de fin");
        }

        Pageable pageable = PageRequest.of(pagina, tamanoPagina);

        Page<SolicitudCredito> pageSolicitudes = obtenerSolicitudesConFiltros(fechaInicio, fechaFin, estado,
                cedulaVendedor, rucConcesionario, pageable);

        List<SolicitudConsultaResponseDTO> solicitudesDTO = pageSolicitudes.getContent().stream()
                .map(solicitudConsultaMapper::toSolicitudConsultaResponseDTO)
                .toList();

        long totalElementos = pageSolicitudes.getTotalElements();
        int totalPaginas = pageSolicitudes.getTotalPages();
        boolean tieneSiguiente = pageSolicitudes.hasNext();
        boolean tieneAnterior = pageSolicitudes.hasPrevious();

        return new SolicitudConsultaPaginadaResponseDTO(
                solicitudesDTO,
                pagina,
                tamanoPagina,
                totalElementos,
                totalPaginas,
                tieneSiguiente,
                tieneAnterior);
    }

    private Page<SolicitudCredito> obtenerSolicitudesConFiltros(LocalDateTime fechaInicio, LocalDateTime fechaFin,
            String estado, String cedulaVendedor, String rucConcesionario, Pageable pageable) {

        boolean tieneEstado = estado != null && !estado.trim().isEmpty();
        boolean tieneVendedor = cedulaVendedor != null && !cedulaVendedor.trim().isEmpty();
        boolean tieneConcesionario = rucConcesionario != null && !rucConcesionario.trim().isEmpty();

        log.debug("Filtros aplicados - Estado: {}, Vendedor: {}, Concesionario: {}",
                tieneEstado ? estado : "NO APLICADO",
                tieneVendedor ? cedulaVendedor : "NO APLICADO",
                tieneConcesionario ? rucConcesionario : "NO APLICADO");

        if (tieneEstado && tieneVendedor && tieneConcesionario) {
            return solicitudCreditoRepository
                    .findByEstadoAndCedulaVendedorAndRucConcesionarioAndFechaSolicitudBetweenOrderByFechaSolicitudDesc(
                            estado, cedulaVendedor, rucConcesionario, fechaInicio, fechaFin, pageable);
        } else if (tieneEstado && tieneVendedor) {
            return solicitudCreditoRepository
                    .findByEstadoAndCedulaVendedorAndFechaSolicitudBetweenOrderByFechaSolicitudDesc(
                            estado, cedulaVendedor, fechaInicio, fechaFin, pageable);
        } else if (tieneEstado && tieneConcesionario) {
            return solicitudCreditoRepository
                    .findByEstadoAndRucConcesionarioAndFechaSolicitudBetweenOrderByFechaSolicitudDesc(
                            estado, rucConcesionario, fechaInicio, fechaFin, pageable);
        } else if (tieneVendedor && tieneConcesionario) {
            return solicitudCreditoRepository
                    .findByCedulaVendedorAndRucConcesionarioAndFechaSolicitudBetweenOrderByFechaSolicitudDesc(
                            cedulaVendedor, rucConcesionario, fechaInicio, fechaFin, pageable);
        } else if (tieneEstado) {
            return solicitudCreditoRepository.findByEstadoAndFechaSolicitudBetweenOrderByFechaSolicitudDesc(
                    estado, fechaInicio, fechaFin, pageable);
        } else if (tieneVendedor) {
            return solicitudCreditoRepository.findByCedulaVendedorAndFechaSolicitudBetweenOrderByFechaSolicitudDesc(
                    cedulaVendedor, fechaInicio, fechaFin, pageable);
        } else if (tieneConcesionario) {
            return solicitudCreditoRepository.findByRucConcesionarioAndFechaSolicitudBetweenOrderByFechaSolicitudDesc(
                    rucConcesionario, fechaInicio, fechaFin, pageable);
        } else {
            return solicitudCreditoRepository.findByFechaSolicitudBetweenOrderByFechaSolicitudDesc(
                    fechaInicio, fechaFin, pageable);
        }
    }

    private void validarClienteEnCore(String cedula) {
        log.info("Validando existencia del cliente {} en Core Bancario", cedula);
        try {
            coreBancarioClient.consultarPersonaPorIdentificacion("CEDULA", cedula);
            log.info("Cliente {} encontrado en Core Bancario", cedula);
        } catch (Exception e) {
            log.error("Cliente {} no encontrado en Core Bancario: {}", cedula, e.getMessage());
            throw new CreateEntityException("SolicitudCredito",
                    "El cliente con cédula " + cedula + " no existe en Core Bancario");
        }
    }

    private PrestamosExternalDTO validarPrestamo(String idPrestamo) {
        log.info("Validando existencia del préstamo: {}", idPrestamo);
        try {
            PrestamosExternalDTO prestamo = prestamosClient.consultarPrestamoPorId(idPrestamo);
            log.info("Préstamo validado: {} - {}", prestamo.getId(), prestamo.getNombre());
            return prestamo;
        } catch (RestClientException e) {
            log.error("Error al consultar préstamo {}: {}", idPrestamo, e.getMessage());
            throw new CreateEntityException("SolicitudCredito", "No se pudo consultar el préstamo: " + e.getMessage());
        }
    }

    private VehiculoResponseDTO validarVehiculo(String rucConcesionario, String placaVehiculo) {
        log.info("Validando existencia del vehículo: {} en concesionario: {}", placaVehiculo, rucConcesionario);
        try {
            VehiculoResponseDTO vehiculo = gestionVehiculosService.obtenerVehiculo(rucConcesionario, placaVehiculo);
            if (!gestionVehiculosService.validarVehiculoDisponible(vehiculo)) {
                throw new CreateEntityException("SolicitudCredito",
                        "El vehículo no está disponible para financiamiento");
            }
            log.info("Vehículo validado: {} - {}", vehiculo.getPlaca(), vehiculo.getMarca());
            return vehiculo;
        } catch (Exception e) {
            log.error("Error al validar vehículo {}: {}", placaVehiculo, e.getMessage());
            throw new CreateEntityException("SolicitudCredito", "Error al validar vehículo: " + e.getMessage());
        }
    }

    private void validarVendedor(String rucConcesionario, String cedulaVendedor) {
        log.info("Validando existencia del vendedor: {} en concesionario: {}", cedulaVendedor, rucConcesionario);
        try {
            gestionVehiculosService.obtenerVendedor(rucConcesionario, cedulaVendedor);
            log.info("Vendedor validado: {} en concesionario: {}", cedulaVendedor, rucConcesionario);
        } catch (Exception e) {
            log.error("Error al validar vendedor {}: {}", cedulaVendedor, e.getMessage());
            throw new CreateEntityException("SolicitudCredito", "Error al validar vendedor: " + e.getMessage());
        }
    }

    private void validarSolicitudExistente(String cedula, String idPrestamo) {
        log.info("Validando que no exista solicitud en borrador para cliente: {} y préstamo: {}", cedula, idPrestamo);
        List<SolicitudCredito> solicitudes = solicitudCreditoRepository.findByCedulaSolicitanteAndIdPrestamo(cedula,
                idPrestamo);
        boolean existeBorrador = solicitudes.stream()
                .anyMatch(s -> "BORRADOR".equals(s.getEstado()));
        if (existeBorrador) {
            throw new CreateEntityException("SolicitudCredito",
                    "Ya existe una solicitud en estado BORRADOR para este cliente y préstamo");
        }
    }

    private void validarSolicitudExistenteParaEdicion(String cedula, String idPrestamo, Long idSolicitudActual) {
        log.info(
                "Validando que no exista otra solicitud en borrador para cliente: {} y préstamo: {} (excluyendo solicitud actual: {})",
                cedula, idPrestamo, idSolicitudActual);
        List<SolicitudCredito> solicitudes = solicitudCreditoRepository.findByCedulaSolicitanteAndIdPrestamo(cedula,
                idPrestamo);
        boolean existeOtroBorrador = solicitudes.stream()
                .anyMatch(s -> "BORRADOR".equals(s.getEstado()) && !s.getId().equals(idSolicitudActual));
        if (existeOtroBorrador) {
            throw new CreateEntityException("SolicitudCredito",
                    "Ya existe otra solicitud en estado BORRADOR para este cliente y préstamo");
        }
    }

    private void validarLimitesPrestamo(BigDecimal montoSolicitado, Integer plazoMeses, PrestamosExternalDTO prestamo) {
        log.info("Validando límites del préstamo: monto: {}, plazo: {}, límites: {} - {}, plazos: {} - {}",
                montoSolicitado, plazoMeses, prestamo.getMontoMinimo(), prestamo.getMontoMaximo(),
                prestamo.getPlazoMinimoMeses(), prestamo.getPlazoMaximoMeses());

        if (montoSolicitado.compareTo(prestamo.getMontoMinimo()) < 0
                || montoSolicitado.compareTo(prestamo.getMontoMaximo()) > 0) {
            throw new CreateEntityException("SolicitudCredito",
                    String.format("El monto solicitado %s está fuera de los límites del préstamo (mín: %s, máx: %s)",
                            montoSolicitado, prestamo.getMontoMinimo(), prestamo.getMontoMaximo()));
        }

        if (plazoMeses < prestamo.getPlazoMinimoMeses() || plazoMeses > prestamo.getPlazoMaximoMeses()) {
            throw new CreateEntityException("SolicitudCredito",
                    String.format("El plazo %d meses está fuera de los límites del préstamo (mín: %d, máx: %d)",
                            plazoMeses, prestamo.getPlazoMinimoMeses(), prestamo.getPlazoMaximoMeses()));
        }
    }

    private void validarCapacidadPago(String cedula, BigDecimal montoSolicitado, Integer plazoMeses,
            BigDecimal tasaInteres, BigDecimal capacidadPagoCliente) {
        log.info("Validando capacidad de pago para cliente: {}", cedula);

        BigDecimal cuotaMensual = calcularCuotaMensual(montoSolicitado, plazoMeses, tasaInteres);

        if (cuotaMensual.compareTo(capacidadPagoCliente) > 0) {
            throw new CreateEntityException("SolicitudCredito",
                    String.format("La cuota mensual %s excede la capacidad de pago del cliente %s. Capacidad: %s",
                            cuotaMensual, cedula, capacidadPagoCliente));
        }

        log.info("Capacidad de pago validada: cuota: {}, capacidad: {}", cuotaMensual, capacidadPagoCliente);
    }

    private BigDecimal calcularMontoSolicitado(BigDecimal valorVehiculo, BigDecimal valorEntrada) {
        log.info("Calculando monto solicitado: valor vehículo: {}, entrada: {}", valorVehiculo, valorEntrada);

        BigDecimal montoSolicitado = valorVehiculo.subtract(valorEntrada);
        log.info("Monto solicitado calculado: {}", montoSolicitado);
        return montoSolicitado;
    }

    private BigDecimal calcularCuotaMensual(BigDecimal monto, int plazo, BigDecimal tasaAnualPorcentaje) {
        BigDecimal tasaMensualPorcentaje = tasaAnualPorcentaje.divide(BigDecimal.valueOf(12), 8,
                RoundingMode.HALF_UP);
        BigDecimal tasaMensualDecimal = tasaMensualPorcentaje.divide(BigDecimal.valueOf(100), 8,
                RoundingMode.HALF_UP);
        BigDecimal cuota = monto.multiply(tasaMensualDecimal)
                .divide(BigDecimal.ONE.subtract(BigDecimal.ONE
                        .divide((BigDecimal.ONE.add(tasaMensualDecimal)).pow(plazo), 8, RoundingMode.HALF_UP)), 2,
                        RoundingMode.HALF_UP);
        return cuota;
    }

    private List<AmortizacionDTO> calcularTablaAmortizacion(BigDecimal monto, int plazo,
            BigDecimal tasaAnualPorcentaje) {
        List<AmortizacionDTO> tabla = new ArrayList<>();
        BigDecimal saldo = monto;
        BigDecimal tasaMensualPorcentaje = tasaAnualPorcentaje.divide(BigDecimal.valueOf(12), 8,
                RoundingMode.HALF_UP);
        BigDecimal tasaMensualDecimal = tasaMensualPorcentaje.divide(BigDecimal.valueOf(100), 8,
                RoundingMode.HALF_UP);
        BigDecimal cuota = calcularCuotaMensual(monto, plazo, tasaAnualPorcentaje);
        for (int i = 1; i <= plazo; i++) {
            BigDecimal interes = saldo.multiply(tasaMensualDecimal).setScale(2, RoundingMode.HALF_UP);
            BigDecimal abonoCapital = cuota.subtract(interes);
            BigDecimal saldoFinal = saldo.subtract(abonoCapital);
            tabla.add(new AmortizacionDTO(i, saldo, cuota, abonoCapital, interes, saldoFinal));
            saldo = saldoFinal;
        }
        return tabla;
    }

    private String generarNumeroSolicitud() {
        String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int secuencia = (int) (Math.random() * 9000) + 1000;
        return "SOL-" + fecha + "-" + secuencia;
    }

    
    private boolean esTransicionValida(EstadoSolicitudEnum actual, EstadoSolicitudEnum nuevo) {
        return switch (actual) {
            case BORRADOR ->
                nuevo == DOCUMENTACION_CARGADA || nuevo == CANCELADA;
            case DOCUMENTACION_CARGADA ->
                nuevo == DOCUMENTACION_VALIDADA || nuevo == DOCUMENTACION_RECHAZADA || nuevo == CANCELADA;
            case DOCUMENTACION_VALIDADA ->
                nuevo == CONTRATO_CARGADO || nuevo == CANCELADA;
            case DOCUMENTACION_RECHAZADA ->
                nuevo == CANCELADA;
            case CONTRATO_CARGADO ->
                nuevo == CONTRATO_VALIDADO || nuevo == CONTRATO_RECHAZADO || nuevo == CANCELADA;
            case CONTRATO_VALIDADO, CONTRATO_RECHAZADO ->
                nuevo == APROBADA || nuevo == RECHAZADA || nuevo == CANCELADA;
            default ->
                false;
        };
    }

}