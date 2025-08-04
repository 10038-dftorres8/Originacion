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

import java.math.BigDecimal;
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

        // 1. Validar existencia del cliente en Core Bancario
        validarClienteEnCore(solicitudDTO.getCedulaSolicitante());

        // 2. Validar existencia del préstamo
        PrestamosExternalDTO prestamo = validarPrestamo(solicitudDTO.getIdPrestamo());

        // 3. Validar existencia del vehículo
        VehiculoResponseDTO vehiculo = validarVehiculo(solicitudDTO.getRucConcesionario(),
                solicitudDTO.getPlacaVehiculo());

        // 4. Validar existencia del vendedor
        validarVendedor(solicitudDTO.getRucConcesionario(), solicitudDTO.getCedulaVendedor());

        // 5. Validar que no exista una solicitud en borrador para este cliente
        validarSolicitudExistente(solicitudDTO.getCedulaSolicitante(), solicitudDTO.getIdPrestamo());

        // 6. Calcular monto solicitado basado en el valor del vehículo y entrada
        BigDecimal montoSolicitado = calcularMontoSolicitado(vehiculo.getValor(), solicitudDTO.getValorEntrada());

        // 7. Validar límites del préstamo
        validarLimitesPrestamo(montoSolicitado, solicitudDTO.getPlazoMeses(), prestamo);

        // 8. Validar capacidad de pago
        validarCapacidadPago(solicitudDTO.getCedulaSolicitante(), montoSolicitado, solicitudDTO.getPlazoMeses(),
                prestamo.getTasaInteres(), solicitudDTO.getCapacidadPagoSolicitante());

        // 9. Crear la solicitud
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

        // 1. Obtener la solicitud existente
        SolicitudCredito solicitud = solicitudCreditoRepository.findByNumeroSolicitud(numeroSolicitud)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud no encontrada"));

        // 2. Obtener información del préstamo
        PrestamosExternalDTO prestamo = prestamosClient.consultarPrestamoPorId(solicitud.getIdPrestamo());

        // 3. Obtener información del vehículo
        VehiculoResponseDTO vehiculo = gestionVehiculosService.obtenerVehiculo(
                solicitud.getRucConcesionario(), solicitud.getPlacaVehiculo());

        // 4. Usar capacidad de pago de la solicitud
        BigDecimal capacidadPago = solicitud.getCapacidadPagoSolicitante();

        // 5. Usar tasa de interés directamente (ya viene como porcentaje)
        BigDecimal tasaInteresAnual = prestamo.getTasaInteres();
        log.info("Tasa de interés anual: {}%", tasaInteresAnual);

        // 6. Calcular cuota mensual (el método convierte porcentaje anual a mensual)
        BigDecimal cuotaMensual = calcularCuotaMensual(solicitud.getMontoSolicitado(), solicitud.getPlazoMeses(),
                tasaInteresAnual);

        // 7. Calcular montos totales
        BigDecimal montoTotal = cuotaMensual.multiply(new BigDecimal(solicitud.getPlazoMeses()));
        BigDecimal totalIntereses = montoTotal.subtract(solicitud.getMontoSolicitado());
        BigDecimal totalAPagar = montoTotal.add(solicitud.getValorEntrada());

        // 8. Validar si es aprobable
        boolean esAprobable = cuotaMensual.compareTo(capacidadPago) <= 0;
        String motivoRechazo = esAprobable ? null
                : String.format("La cuota mensual %s excede la capacidad de pago del cliente %s", cuotaMensual,
                        capacidadPago);

        // 9. Generar tabla de amortización
        List<AmortizacionDTO> tablaAmortizacion = calcularTablaAmortizacion(solicitud.getMontoSolicitado(),
                solicitud.getPlazoMeses(), tasaInteresAnual);

        // 10. Crear respuesta de simulación usando MapStruct
        SimulacionSolicitudResponseDTO response = simulacionSolicitudMapper.toSimulacionSolicitudResponseDTO(solicitud,
                prestamo, vehiculo);

        // 11. Establecer campos calculados que no se pueden mapear automáticamente
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
        log.info("Iniciando cambio de estado de solicitud {} a estado: {}", idSolicitud, nuevoEstado);

        SolicitudCredito solicitud = solicitudCreditoRepository.findById(idSolicitud)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud no encontrada"));

        EstadoSolicitudEnum estadoActual = EstadoSolicitudEnum.valueOf(solicitud.getEstado());
        EstadoSolicitudEnum estadoNuevo;
        try {
            estadoNuevo = EstadoSolicitudEnum.valueOf(nuevoEstado);
        } catch (IllegalArgumentException e) {
            throw new CreateEntityException("SolicitudCredito", "Estado de solicitud no válido: " + nuevoEstado);
        }

        log.info("Validando transición de estado: {} -> {}", estadoActual, estadoNuevo);

        // PRIMERO: Validar que la transición de estado sea válida
        if (!esTransicionValida(estadoActual, estadoNuevo)) {
            String mensajeError = String.format(
                    "Transición de estado no permitida: %s -> %s. La solicitud debe seguir la jerarquía: BORRADOR -> EN_REVISION -> APROBADA/RECHAZADA",
                    estadoActual, estadoNuevo);
            log.error("Error en cambio de estado: {}", mensajeError);
            throw new CreateEntityException("SolicitudCredito", mensajeError);
        }

        // TERCERO: Cambiar el estado de la solicitud
        String estadoAnterior = solicitud.getEstado();
        solicitud.setEstado(nuevoEstado);
        solicitudCreditoRepository.save(solicitud);

        // CUARTO: Crear el historial del cambio usando MapStruct
        HistorialEstadoDTO historialDTO = new HistorialEstadoDTO(
                idSolicitud, estadoAnterior, nuevoEstado,
                LocalDateTime.now(), 0L, motivo);

        HistorialEstado historial = historialEstadoMapper.toEntity(historialDTO);
        historialEstadoRepository.save(historial);

        log.info("Estado de solicitud {} cambiado exitosamente de {} a {} por usuario {}",
                idSolicitud, estadoAnterior, nuevoEstado, usuario);

    }

    public SolicitudDetalladaResponseDTO obtenerSolicitudDetallada(String numeroSolicitud) {
        log.info("Obteniendo información detallada de la solicitud: {}", numeroSolicitud);

        // 1. Buscar la solicitud por número
        SolicitudCredito solicitud = solicitudCreditoRepository.findByNumeroSolicitud(numeroSolicitud)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Solicitud no encontrada con número: " + numeroSolicitud));

        try {
            // 2. Obtener información del solicitante (persona)
            PersonaResponseDTO persona = coreBancarioClient.consultarPersonaPorIdentificacion("CEDULA",
                    solicitud.getCedulaSolicitante());

            // 3. Obtener información del vehículo
            VehiculoResponseDTO vehiculo = gestionVehiculosClient.getVehiculoByPlaca(solicitud.getRucConcesionario(),
                    solicitud.getPlacaVehiculo());

            // 4. Obtener información del concesionario
            ConcesionarioResponseDTO concesionario = gestionVehiculosClient
                    .getConcesionarioByRuc(solicitud.getRucConcesionario());

            // 5. Obtener información del vendedor
            VendedorResponseDTO vendedor = gestionVehiculosClient.getVendedorByCedula(solicitud.getRucConcesionario(),
                    solicitud.getCedulaVendedor());

            // 6. Obtener información del préstamo
            PrestamosExternalDTO prestamo = prestamosClient.consultarPrestamoPorId(solicitud.getIdPrestamo());

            // 7. Mapear toda la información usando MapStruct
            return solicitudDetalladaMapper.toSolicitudDetalladaResponseDTO(
                    solicitud, persona, vehiculo, concesionario, vendedor, prestamo);

        } catch (RestClientException e) {
            log.error("Error al obtener información externa para la solicitud {}: {}", numeroSolicitud, e.getMessage());
            throw new CreateEntityException("SolicitudCredito",
                    "Error al obtener información detallada de la solicitud: " + e.getMessage());
        }
    }

    public SolicitudCreditoResponseDTO editarSolicitud(String numeroSolicitud,
            SolicitudCreditoEdicionDTO solicitudEdicionDTO) {
        log.info("Editando solicitud: {} para cédula: {}", numeroSolicitud, solicitudEdicionDTO.getCedulaSolicitante());

        // 1. Buscar la solicitud existente
        SolicitudCredito solicitudExistente = solicitudCreditoRepository.findByNumeroSolicitud(numeroSolicitud)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Solicitud no encontrada con número: " + numeroSolicitud));

        // 2. Validar que la solicitud esté en estado BORRADOR
        if (!EstadoSolicitudEnum.BORRADOR.name().equals(solicitudExistente.getEstado())) {
            throw new CreateEntityException("SolicitudCredito",
                    "Solo se pueden editar solicitudes en estado BORRADOR. Estado actual: "
                            + solicitudExistente.getEstado());
        }

        // 3. Validar existencia del cliente en Core Bancario
        validarClienteEnCore(solicitudEdicionDTO.getCedulaSolicitante());

        // 4. Validar existencia del préstamo
        PrestamosExternalDTO prestamo = validarPrestamo(solicitudEdicionDTO.getIdPrestamo());

        // 5. Validar existencia del vehículo
        VehiculoResponseDTO vehiculo = validarVehiculo(solicitudEdicionDTO.getRucConcesionario(),
                solicitudEdicionDTO.getPlacaVehiculo());

        // 6. Validar existencia del vendedor
        validarVendedor(solicitudEdicionDTO.getRucConcesionario(), solicitudEdicionDTO.getCedulaVendedor());

        // 7. Validar que no exista otra solicitud en borrador para este cliente
        // (excluyendo la actual)
        validarSolicitudExistenteParaEdicion(solicitudEdicionDTO.getCedulaSolicitante(),
                solicitudEdicionDTO.getIdPrestamo(), solicitudExistente.getId());

        // 8. Calcular monto solicitado basado en el valor del vehículo y entrada
        BigDecimal montoSolicitado = calcularMontoSolicitado(vehiculo.getValor(),
                solicitudEdicionDTO.getValorEntrada());

        // 9. Validar límites del préstamo
        validarLimitesPrestamo(montoSolicitado, solicitudEdicionDTO.getPlazoMeses(), prestamo);

        // 10. Validar capacidad de pago
        validarCapacidadPago(solicitudEdicionDTO.getCedulaSolicitante(), montoSolicitado,
                solicitudEdicionDTO.getPlazoMeses(), prestamo.getTasaInteres(),
                solicitudEdicionDTO.getCapacidadPagoSolicitante());

        // 11. Actualizar la solicitud (manteniendo número y fecha original)
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
        // Validar que el rango de fechas no exceda un mes
        long diasEntreFechas = java.time.Duration.between(fechaInicio, fechaFin).toDays();
        if (diasEntreFechas > 31) {
            throw new CreateEntityException("SolicitudCredito",
                    "El rango de fechas no puede exceder 31 días. Días solicitados: " + diasEntreFechas);
        }

        // Validar que fechaInicio sea anterior a fechaFin
        if (fechaInicio.isAfter(fechaFin)) {
            throw new CreateEntityException("SolicitudCredito",
                    "La fecha de inicio debe ser anterior a la fecha de fin");
        }

        // Crear Pageable para paginación
        Pageable pageable = PageRequest.of(pagina, tamanoPagina);

        // Obtener solicitudes con paginación usando Page según los filtros aplicados
        Page<SolicitudCredito> pageSolicitudes = obtenerSolicitudesConFiltros(fechaInicio, fechaFin, estado,
                cedulaVendedor, rucConcesionario, pageable);

        // Convertir a DTOs
        List<SolicitudConsultaResponseDTO> solicitudesDTO = pageSolicitudes.getContent().stream()
                .map(solicitudConsultaMapper::toSolicitudConsultaResponseDTO)
                .toList();

        // Obtener información de paginación desde Page
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
        // Tratar valores null como si no se hubieran enviado
        boolean tieneEstado = estado != null && !estado.trim().isEmpty();
        boolean tieneVendedor = cedulaVendedor != null && !cedulaVendedor.trim().isEmpty();
        boolean tieneConcesionario = rucConcesionario != null && !rucConcesionario.trim().isEmpty();

        log.debug("Filtros aplicados - Estado: {}, Vendedor: {}, Concesionario: {}",
                tieneEstado ? estado : "NO APLICADO",
                tieneVendedor ? cedulaVendedor : "NO APLICADO",
                tieneConcesionario ? rucConcesionario : "NO APLICADO");

        // Determinar qué método del repositorio usar basado en los filtros aplicados
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

    // Metodos para la validacion de datos
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

        // Calcular cuota mensual (el método ya maneja la conversión de porcentaje)
        BigDecimal cuotaMensual = calcularCuotaMensual(montoSolicitado, plazoMeses, tasaInteres);

        if (cuotaMensual.compareTo(capacidadPagoCliente) > 0) {
            throw new CreateEntityException("SolicitudCredito",
                    String.format("La cuota mensual %s excede la capacidad de pago del cliente %s. Capacidad: %s",
                            cuotaMensual, cedula, capacidadPagoCliente));
        }

        log.info("Capacidad de pago validada: cuota: {}, capacidad: {}", cuotaMensual, capacidadPagoCliente);
    }

    // Metodos para el calculo de datos
    private BigDecimal calcularMontoSolicitado(BigDecimal valorVehiculo, BigDecimal valorEntrada) {
        log.info("Calculando monto solicitado: valor vehículo: {}, entrada: {}", valorVehiculo, valorEntrada);

        BigDecimal montoSolicitado = valorVehiculo.subtract(valorEntrada);
        log.info("Monto solicitado calculado: {}", montoSolicitado);
        return montoSolicitado;
    }

    // Método francés para calcular cuota mensual (tasa en porcentaje)
    private BigDecimal calcularCuotaMensual(BigDecimal monto, int plazo, BigDecimal tasaAnualPorcentaje) {
        BigDecimal tasaMensualPorcentaje = tasaAnualPorcentaje.divide(BigDecimal.valueOf(12), 8,
                BigDecimal.ROUND_HALF_UP);
        BigDecimal tasaMensualDecimal = tasaMensualPorcentaje.divide(BigDecimal.valueOf(100), 8,
                BigDecimal.ROUND_HALF_UP);
        BigDecimal cuota = monto.multiply(tasaMensualDecimal)
                .divide(BigDecimal.ONE.subtract(BigDecimal.ONE
                        .divide((BigDecimal.ONE.add(tasaMensualDecimal)).pow(plazo), 8, BigDecimal.ROUND_HALF_UP)), 2,
                        BigDecimal.ROUND_HALF_UP);
        return cuota;
    }

    private List<AmortizacionDTO> calcularTablaAmortizacion(BigDecimal monto, int plazo,
            BigDecimal tasaAnualPorcentaje) {
        List<AmortizacionDTO> tabla = new ArrayList<>();
        BigDecimal saldo = monto;
        BigDecimal tasaMensualPorcentaje = tasaAnualPorcentaje.divide(BigDecimal.valueOf(12), 8,
                BigDecimal.ROUND_HALF_UP);
        BigDecimal tasaMensualDecimal = tasaMensualPorcentaje.divide(BigDecimal.valueOf(100), 8,
                BigDecimal.ROUND_HALF_UP);
        BigDecimal cuota = calcularCuotaMensual(monto, plazo, tasaAnualPorcentaje);
        for (int i = 1; i <= plazo; i++) {
            BigDecimal interes = saldo.multiply(tasaMensualDecimal).setScale(2, BigDecimal.ROUND_HALF_UP);
            BigDecimal abonoCapital = cuota.subtract(interes);
            BigDecimal saldoFinal = saldo.subtract(abonoCapital);
            tabla.add(new AmortizacionDTO(i, saldo, cuota, abonoCapital, interes, saldoFinal));
            saldo = saldoFinal;
        }
        return tabla;
    }

    // Metodos para la generacion de datos
    private String generarNumeroSolicitud() {
        String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int secuencia = (int) (Math.random() * 9000) + 1000;
        return "SOL-" + fecha + "-" + secuencia;
    }

    // Metodos para la validacion de transiciones
    /*
     * private boolean esTransicionValida(EstadoSolicitudEnum actual,
     * EstadoSolicitudEnum nuevo) {
     * return switch (actual) {
     * case BORRADOR -> nuevo == EstadoSolicitudEnum.EN_REVISION || nuevo ==
     * EstadoSolicitudEnum.CANCELADA;
     * case EN_REVISION -> nuevo == EstadoSolicitudEnum.APROBADA || nuevo ==
     * EstadoSolicitudEnum.RECHAZADA || nuevo == EstadoSolicitudEnum.CANCELADA;
     * case APROBADA, RECHAZADA, CANCELADA -> false;
     * };
     * }
     */
    private boolean esTransicionValida(EstadoSolicitudEnum actual, EstadoSolicitudEnum nuevo) {
        return switch (actual) {
            case BORRADOR ->
                nuevo == EstadoSolicitudEnum.DOCUMENTACION_CARGADA || nuevo == EstadoSolicitudEnum.CANCELADA;
            case EstadoSolicitudEnum.DOCUMENTACION_CARGADA ->
                nuevo == EstadoSolicitudEnum.DOCUMENTACION_VALIDADA || nuevo == EstadoSolicitudEnum.DOCUMENTACION_RECHAZADA || nuevo == EstadoSolicitudEnum.CANCELADA;
            case EstadoSolicitudEnum.DOCUMENTACION_VALIDADA ->
                nuevo == EstadoSolicitudEnum.CONTRATO_CARGADO || nuevo == EstadoSolicitudEnum.CANCELADA;
            case EstadoSolicitudEnum.DOCUMENTACION_RECHAZADA ->
                // acá podrías permitir volver a cargar docs o cancelar
                nuevo == EstadoSolicitudEnum.CANCELADA;
            case EstadoSolicitudEnum.CONTRATO_CARGADO ->
                nuevo == EstadoSolicitudEnum.CONTRATO_VALIDADO || nuevo == EstadoSolicitudEnum.CONTRATO_RECHAZADO || nuevo == EstadoSolicitudEnum.CANCELADA;
            case EstadoSolicitudEnum.CONTRATO_VALIDADO, EstadoSolicitudEnum.CONTRATO_RECHAZADO ->
                nuevo == EstadoSolicitudEnum.APROBADA || nuevo == EstadoSolicitudEnum.RECHAZADA || nuevo == EstadoSolicitudEnum.CANCELADA;
            default ->
                false;
        };
    }

}