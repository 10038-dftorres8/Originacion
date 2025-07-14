package com.banquito.originacion.service;

import com.banquito.originacion.client.PrestamosClient;
import com.banquito.originacion.client.CoreBancarioClient;
import com.banquito.originacion.controller.dto.*;
import com.banquito.originacion.controller.dto.external.PrestamosExternalDTO;
import com.banquito.originacion.enums.TipoDocumentoEnum;
import org.springframework.core.io.Resource;
import com.banquito.originacion.controller.mapper.SolicitudCreditoMapper;
import com.banquito.originacion.enums.EstadoSolicitudEnum;
import com.banquito.originacion.exception.CreateEntityException;
import com.banquito.originacion.exception.ResourceNotFoundException;
import com.banquito.originacion.model.SolicitudCredito;
import com.banquito.originacion.model.HistorialEstado;
import com.banquito.originacion.model.DocumentoAdjunto;
import com.banquito.originacion.model.ClienteProspecto;
import com.banquito.originacion.repository.SolicitudCreditoRepository;
import com.banquito.originacion.repository.HistorialEstadoRepository;
import com.banquito.originacion.repository.DocumentoAdjuntoRepository;
import com.banquito.originacion.repository.ClienteProspectoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.banquito.originacion.controller.dto.SolicitudCreditoResponseDTO;
import com.banquito.originacion.controller.dto.DocumentoAdjuntoResponseDTO;
import com.banquito.originacion.controller.mapper.DocumentoAdjuntoMapper;
import com.banquito.originacion.controller.dto.EstadoSolicitudResponseDTO;
import com.banquito.originacion.controller.dto.HistorialEstadoDTO;
import com.banquito.originacion.controller.mapper.HistorialEstadoMapper;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import com.banquito.originacion.controller.dto.SolicitudConsultaRequestDTO;
import com.banquito.originacion.controller.dto.SolicitudConsultaResponseDTO;
import com.banquito.originacion.controller.mapper.SolicitudEstadoMapper;
import com.banquito.originacion.controller.mapper.SolicitudResumenMapper;
import com.banquito.originacion.controller.mapper.SolicitudConsultaMapper;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SolicitudService {
    private final SolicitudCreditoRepository solicitudCreditoRepository;
    private final HistorialEstadoRepository historialEstadoRepository;
    private final DocumentoAdjuntoRepository documentoAdjuntoRepository;
    private final SolicitudCreditoMapper solicitudCreditoMapper;
    private final ClienteProspectoRepository clienteProspectoRepository;
    private final DocumentoAdjuntoMapper documentoAdjuntoMapper;
    private final HistorialEstadoMapper historialEstadoMapper;
    private final SolicitudEstadoMapper solicitudEstadoMapper;
    private final SolicitudResumenMapper solicitudResumenMapper;
    private final SolicitudConsultaMapper solicitudConsultaMapper;
    private final GestionVehiculosService gestionVehiculosService;
    private final PrestamosClient prestamosClient;
    private final CalculoFinancieroService calculoFinancieroService;
    private final FileStorageService fileStorageService;
    private final CoreBancarioClient coreBancarioClient;
    private final PrestamosClientesService prestamosClientesService;

    public SolicitudCreditoResponseDTO crearSolicitudConValidacion(SolicitudCreditoExtendidaDTO solicitudDTO) {
        log.info("Iniciando creación de solicitud con validación de vehículo, vendedor y préstamo");
        
        if (!clienteProspectoRepository.existsById(solicitudDTO.getIdClienteProspecto())) {
            throw new CreateEntityException("SolicitudCredito", "El cliente prospecto no existe");
        }
        
        PrestamosExternalDTO prestamo;
        try {
            prestamo = prestamosClient.consultarPrestamoPorId(solicitudDTO.getIdPrestamo());
            log.info("Préstamo consultado: {} - {}", prestamo.getId(), prestamo.getNombre());
        } catch (RestClientException e) {
            log.error("Error al consultar préstamo {}: {}", solicitudDTO.getIdPrestamo(), e.getMessage());
            throw new CreateEntityException("SolicitudCredito", "No se pudo consultar el préstamo: " + e.getMessage());
        }
        
        boolean existeBorrador = solicitudCreditoRepository.findAll().stream()
            .anyMatch(s -> s.getIdClienteProspecto().equals(solicitudDTO.getIdClienteProspecto())
                && s.getIdPrestamo().equals(solicitudDTO.getIdPrestamo())
                && "BORRADOR".equals(s.getEstado()));
        if (existeBorrador) {
            throw new CreateEntityException("SolicitudCredito", "Ya existe una solicitud en estado BORRADOR para este cliente y préstamo");
        }
        
        VehiculoResponseDTO vehiculo = gestionVehiculosService.obtenerVehiculo(
            solicitudDTO.getRucConcesionario(), 
            solicitudDTO.getPlacaVehiculo()
        );
        
        if (!gestionVehiculosService.validarVehiculoDisponible(vehiculo)) {
            throw new CreateEntityException("SolicitudCredito", "El vehículo no está disponible para financiamiento");
        }
        
        VendedorResponseDTO vendedor = gestionVehiculosService.obtenerVendedor(
            solicitudDTO.getRucConcesionario(), 
            solicitudDTO.getCedulaVendedor()
        );
        
        if (!gestionVehiculosService.validarVendedorActivo(vendedor)) {
            throw new CreateEntityException("SolicitudCredito", "El vendedor no está activo");
        }
        
        
        log.info("Vendedor {} validado exitosamente en concesionario {}", 
                solicitudDTO.getCedulaVendedor(), solicitudDTO.getRucConcesionario());
        
        BigDecimal valorVehiculo = vehiculo.getValor();
        BigDecimal montoSolicitado;
        
        log.info("Valor del vehículo: {}, Entrada: {}", valorVehiculo, solicitudDTO.getValorEntrada());
        
        if (solicitudDTO.getMontoSolicitado() != null) {
            montoSolicitado = solicitudDTO.getMontoSolicitado();
            log.info("Monto solicitado proporcionado: {}", montoSolicitado);
        } else {
            montoSolicitado = calculoFinancieroService.calcularMontoSolicitado(valorVehiculo, solicitudDTO.getValorEntrada());
            log.info("Monto solicitado calculado automáticamente: {} (valor vehículo: {} - entrada: {})", 
                    montoSolicitado, valorVehiculo, solicitudDTO.getValorEntrada());
        }
        
        log.info("Validando límites del préstamo: monto solicitado: {}, límites del préstamo: {} - {}", 
                montoSolicitado, prestamo.getMontoMinimo(), prestamo.getMontoMaximo());
        
        if (!calculoFinancieroService.validarMontoSolicitado(montoSolicitado, prestamo.getMontoMinimo(), prestamo.getMontoMaximo())) {
            throw new CreateEntityException("SolicitudCredito", 
                String.format("El monto solicitado %s está fuera de los límites del préstamo (mín: %s, máx: %s). Valor del vehículo: %s, Entrada: %s", 
                    montoSolicitado, prestamo.getMontoMinimo(), prestamo.getMontoMaximo(), valorVehiculo, solicitudDTO.getValorEntrada()));
        }
        
        if (!calculoFinancieroService.validarPlazo(solicitudDTO.getPlazoMeses(), prestamo.getPlazoMinimoMeses(), prestamo.getPlazoMaximoMeses())) {
            throw new CreateEntityException("SolicitudCredito", 
                String.format("El plazo %d meses está fuera de los límites del préstamo (mín: %d, máx: %d)", 
                    solicitudDTO.getPlazoMeses(), prestamo.getPlazoMinimoMeses(), prestamo.getPlazoMaximoMeses()));
        }
        
        BigDecimal entradaMinima = valorVehiculo.multiply(new BigDecimal("0.2"));
        BigDecimal entradaMaxima = valorVehiculo.multiply(new BigDecimal("0.8"));
        
        log.info("Validando entrada: {} (mínima: {}, máxima: {}, valor vehículo: {})", 
                solicitudDTO.getValorEntrada(), entradaMinima, entradaMaxima, valorVehiculo);
        
        if (solicitudDTO.getValorEntrada().compareTo(entradaMinima) < 0) {
            throw new CreateEntityException("SolicitudCredito", 
                String.format("La entrada debe ser al menos el 20%% del valor del vehículo. Entrada: %s, Mínimo requerido: %s, Valor vehículo: %s", 
                    solicitudDTO.getValorEntrada(), entradaMinima, valorVehiculo));
        }
        
        if (solicitudDTO.getValorEntrada().compareTo(entradaMaxima) > 0) {
            throw new CreateEntityException("SolicitudCredito", 
                String.format("La entrada no puede ser mayor al 80%% del valor del vehículo. Entrada: %s, Máximo permitido: %s, Valor vehículo: %s", 
                    solicitudDTO.getValorEntrada(), entradaMaxima, valorVehiculo));
        }
        
        BigDecimal tasaInteresAplicada;
        if (solicitudDTO.getTasaInteres() != null && solicitudDTO.getTasaInteres().compareTo(BigDecimal.ZERO) > 0) {
            
            BigDecimal tasaInteresConvertida;
            if (solicitudDTO.getTasaInteres().compareTo(new BigDecimal("1")) > 0) {
                
                tasaInteresConvertida = solicitudDTO.getTasaInteres().divide(new BigDecimal("100"), 4, BigDecimal.ROUND_HALF_UP);
                log.info("Tasa convertida de porcentaje {}% a decimal {}", solicitudDTO.getTasaInteres(), tasaInteresConvertida);
            } else {
                
                tasaInteresConvertida = solicitudDTO.getTasaInteres();
            }
            
            
            BigDecimal tasaBaseConvertida;
            if (prestamo.getTasaInteres().compareTo(new BigDecimal("1")) > 0) {
                
                tasaBaseConvertida = prestamo.getTasaInteres().divide(new BigDecimal("100"), 4, BigDecimal.ROUND_HALF_UP);
                log.info("Tasa base convertida de porcentaje {}% a decimal {}", prestamo.getTasaInteres(), tasaBaseConvertida);
            } else {
                
                tasaBaseConvertida = prestamo.getTasaInteres();
            }
            
            BigDecimal margenPermitido = new BigDecimal("0.05"); // 5%
            BigDecimal tasaMinima = tasaBaseConvertida.subtract(margenPermitido);
            BigDecimal tasaMaxima = tasaBaseConvertida.add(margenPermitido);
            
            if (!calculoFinancieroService.validarTasaInteres(tasaInteresConvertida, tasaBaseConvertida, margenPermitido)) {
                throw new CreateEntityException("SolicitudCredito", 
                    String.format("La tasa de interés %s está fuera del margen permitido. Tasa base: %s, Rango permitido: %s - %s", 
                        tasaInteresConvertida, tasaBaseConvertida, tasaMinima, tasaMaxima));
            }
            tasaInteresAplicada = tasaInteresConvertida;
            log.info("Tasa de interés modificada: {} (base: {})", tasaInteresAplicada, tasaBaseConvertida);
        } else {
            
            if (prestamo.getTasaInteres().compareTo(new BigDecimal("1")) > 0) {
                
                tasaInteresAplicada = prestamo.getTasaInteres().divide(new BigDecimal("100"), 4, BigDecimal.ROUND_HALF_UP);
                log.info("Tasa base convertida de porcentaje {}% a decimal {}", prestamo.getTasaInteres(), tasaInteresAplicada);
            } else {
                
                tasaInteresAplicada = prestamo.getTasaInteres();
                log.info("Usando tasa de interés base del préstamo: {}", tasaInteresAplicada);
            }
        }
        
        
        BigDecimal cuotaMensual = calculoFinancieroService.calcularCuotaMensual(montoSolicitado, tasaInteresAplicada, solicitudDTO.getPlazoMeses());
        
        
        BigDecimal montoTotal = calculoFinancieroService.calcularMontoTotal(cuotaMensual, solicitudDTO.getPlazoMeses());
        BigDecimal totalIntereses = calculoFinancieroService.calcularTotalIntereses(montoTotal, montoSolicitado);
        
        String numeroSolicitud = generarNumeroSolicitud();
        String estadoInicial = "BORRADOR";
        
        
        SolicitudCredito solicitud = new SolicitudCredito();
        solicitud.setIdClienteProspecto(solicitudDTO.getIdClienteProspecto());
        solicitud.setIdPrestamo(solicitudDTO.getIdPrestamo());
        solicitud.setIdVehiculo(vehiculo.getId()); 
        solicitud.setIdVendedor(vendedor.getId());
        solicitud.setMontoSolicitado(montoSolicitado);
        solicitud.setPlazoMeses(solicitudDTO.getPlazoMeses());
        solicitud.setValorEntrada(solicitudDTO.getValorEntrada());
        solicitud.setTasaInteresAplicada(tasaInteresAplicada);
        
        BigDecimal tasaBaseParaGuardar;
        if (prestamo.getTasaInteres().compareTo(new BigDecimal("1")) > 0) {
            tasaBaseParaGuardar = prestamo.getTasaInteres().divide(new BigDecimal("100"), 4, BigDecimal.ROUND_HALF_UP);
        } else {
            tasaBaseParaGuardar = prestamo.getTasaInteres();
        }
        solicitud.setTasaInteresBase(tasaBaseParaGuardar);
        solicitud.setCuotaMensualCalculada(cuotaMensual);
        solicitud.setMontoTotalCalculado(montoTotal);
        solicitud.setTotalInteresesCalculado(totalIntereses);
        solicitud.setNumeroSolicitud(numeroSolicitud);
        solicitud.setEstado(estadoInicial);
        solicitud.setFechaSolicitud(LocalDateTime.now());
        solicitud.setRucConcesionario(solicitudDTO.getRucConcesionario());
        solicitud.setPlacaVehiculo(solicitudDTO.getPlacaVehiculo());
        solicitud.setCedulaVendedor(solicitudDTO.getCedulaVendedor());
        
        SolicitudCredito saved = solicitudCreditoRepository.save(solicitud);
        
        
        HistorialEstado historial = new HistorialEstado();
        historial.setIdSolicitud(saved.getId());
        historial.setEstadoNuevo(estadoInicial);
        historial.setFechaCambio(LocalDateTime.now());
        historial.setUsuarioModificacion(0L); // TODO: usuario real
        historial.setMotivo("Creación de solicitud con validación de vehículo, vendedor y préstamo");
        historialEstadoRepository.save(historial);
        
        log.info("Solicitud creada exitosamente con número: {} - Cuota mensual: {} - Total a pagar: {}", 
                numeroSolicitud, cuotaMensual, montoTotal);
        
        return solicitudCreditoMapper.toResponseDTO(saved);
    }



    public SimulacionCreditoResponseDTO simularCreditoConValidacion(String rucConcesionario, String placaVehiculo, BigDecimal montoSolicitado, Integer plazoMeses, BigDecimal tasaInteres) {
        log.info("Iniciando simulación de crédito para vehículo: {} en concesionario: {}", placaVehiculo, rucConcesionario);
        
        
        VehiculoResponseDTO vehiculo = gestionVehiculosService.obtenerVehiculo(rucConcesionario, placaVehiculo);
        
        if (!gestionVehiculosService.validarVehiculoDisponible(vehiculo)) {
            throw new CreateEntityException("SimulacionCredito", "El vehículo no está disponible para financiamiento");
        }
        
        
        BigDecimal tasaInteresConvertida;
        if (tasaInteres.compareTo(new BigDecimal("1")) > 0) {
            tasaInteresConvertida = tasaInteres.divide(new BigDecimal("100"), 4, BigDecimal.ROUND_HALF_UP);
            log.info("Tasa convertida de porcentaje {}% a decimal {}", tasaInteres, tasaInteresConvertida);
        } else {
            tasaInteresConvertida = tasaInteres;
        }
        
        
        BigDecimal valorVehiculo = vehiculo.getValor();
        BigDecimal montoMaximo = valorVehiculo.multiply(new BigDecimal("0.8"));
        
        if (montoSolicitado.compareTo(montoMaximo) > 0) {
            throw new CreateEntityException("SimulacionCredito", 
                String.format("El monto solicitado %s excede el 80%% del valor del vehículo. Máximo permitido: %s, Valor vehículo: %s", 
                    montoSolicitado, montoMaximo, valorVehiculo));
        }
        
        log.info("Simulando crédito: monto={}, plazo={}, valorVehiculo={}, tasa={}", 
                montoSolicitado, plazoMeses, valorVehiculo, tasaInteresConvertida);
        
        
        SimulacionCreditoResponseDTO response = new SimulacionCreditoResponseDTO();
        response.setPlacaVehiculo(placaVehiculo);
        response.setRucConcesionario(rucConcesionario);
        response.setValorVehiculo(valorVehiculo);
        response.setMontoSolicitado(montoSolicitado);
        response.setPlazoOriginal(plazoMeses);
        response.setTasaInteres(tasaInteresConvertida);
        
        
        List<SimulacionCreditoResponseDTO.ResumenEscenarioDTO> resumenEscenarios = new ArrayList<>();
        
        // Escenario 1: Con entrada 20%
        BigDecimal entrada20Porcentaje = montoSolicitado.multiply(new BigDecimal("0.2"));
        BigDecimal montoConEntrada = montoSolicitado.subtract(entrada20Porcentaje);
        BigDecimal cuotaConEntrada = calculoFinancieroService.calcularCuotaMensual(montoConEntrada, tasaInteresConvertida, plazoMeses);
        BigDecimal totalConEntrada = calculoFinancieroService.calcularMontoTotal(cuotaConEntrada, plazoMeses);
        BigDecimal interesesConEntrada = calculoFinancieroService.calcularTotalIntereses(totalConEntrada, montoConEntrada);
        
        SimulacionCreditoResponseDTO.ResumenEscenarioDTO escenario1 = new SimulacionCreditoResponseDTO.ResumenEscenarioDTO();
        escenario1.setNombreEscenario("Con entrada 20%");
        escenario1.setMontoFinanciado(montoConEntrada);
        escenario1.setPlazoMeses(plazoMeses);
        escenario1.setCuotaMensual(cuotaConEntrada);
        escenario1.setMontoTotal(totalConEntrada);
        escenario1.setTotalIntereses(interesesConEntrada);
        escenario1.setEntrada(entrada20Porcentaje);
        escenario1.setDescripcion("Financiamiento con entrada del 20% del monto solicitado");
        resumenEscenarios.add(escenario1);
        
        // Escenario 2: Sin entrada
        BigDecimal cuotaSinEntrada = calculoFinancieroService.calcularCuotaMensual(montoSolicitado, tasaInteresConvertida, plazoMeses);
        BigDecimal totalSinEntrada = calculoFinancieroService.calcularMontoTotal(cuotaSinEntrada, plazoMeses);
        BigDecimal interesesSinEntrada = calculoFinancieroService.calcularTotalIntereses(totalSinEntrada, montoSolicitado);
        
        SimulacionCreditoResponseDTO.ResumenEscenarioDTO escenario2 = new SimulacionCreditoResponseDTO.ResumenEscenarioDTO();
        escenario2.setNombreEscenario("Sin entrada");
        escenario2.setMontoFinanciado(montoSolicitado);
        escenario2.setPlazoMeses(plazoMeses);
        escenario2.setCuotaMensual(cuotaSinEntrada);
        escenario2.setMontoTotal(totalSinEntrada);
        escenario2.setTotalIntereses(interesesSinEntrada);
        escenario2.setEntrada(BigDecimal.ZERO);
        escenario2.setDescripcion("Financiamiento del 100% del monto solicitado");
        resumenEscenarios.add(escenario2);
        
        // Escenario 3: Plazo máximo para menor cuota
        int plazoMaximo = Math.min(120, Math.max(plazoMeses + 12, 48)); // Mínimo 48 meses, máximo 120
        BigDecimal cuotaPlazoMaximo = calculoFinancieroService.calcularCuotaMensual(montoSolicitado, tasaInteresConvertida, plazoMaximo);
        BigDecimal totalPlazoMaximo = calculoFinancieroService.calcularMontoTotal(cuotaPlazoMaximo, plazoMaximo);
        BigDecimal interesesPlazoMaximo = calculoFinancieroService.calcularTotalIntereses(totalPlazoMaximo, montoSolicitado);
        
        SimulacionCreditoResponseDTO.ResumenEscenarioDTO escenario3 = new SimulacionCreditoResponseDTO.ResumenEscenarioDTO();
        escenario3.setNombreEscenario("Plazo máximo para menor cuota");
        escenario3.setMontoFinanciado(montoSolicitado);
        escenario3.setPlazoMeses(plazoMaximo);
        escenario3.setCuotaMensual(cuotaPlazoMaximo);
        escenario3.setMontoTotal(totalPlazoMaximo);
        escenario3.setTotalIntereses(interesesPlazoMaximo);
        escenario3.setEntrada(BigDecimal.ZERO);
        escenario3.setDescripcion("Financiamiento con plazo extendido para reducir la cuota mensual");
        resumenEscenarios.add(escenario3);
        
        response.setResumenEscenarios(resumenEscenarios);
        
        
        response.setTablaConEntrada20(calcularTablaAmortizacion(montoConEntrada, plazoMeses.intValue(), tasaInteresConvertida));
        response.setTablaSinEntrada(calcularTablaAmortizacion(montoSolicitado, plazoMeses.intValue(), tasaInteresConvertida));
        response.setTablaPlazoMaximo(calcularTablaAmortizacion(montoSolicitado, plazoMaximo, tasaInteresConvertida));
        
        log.info("Simulación completada exitosamente con 3 escenarios");
        return response;
    }

    public DocumentoAdjuntoResponseDTO cargarDocumento(Long idSolicitud, MultipartFile archivo, String tipoDocumento) {
        // Validar que la solicitud existe
        SolicitudCredito solicitud = solicitudCreditoRepository.findById(idSolicitud)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud no encontrada"));

        // Validar que el tipo de documento es válido
        try {
            TipoDocumentoEnum.valueOf(tipoDocumento);
        } catch (IllegalArgumentException e) {
            throw new CreateEntityException("DocumentoAdjunto", "Tipo de documento no válido: " + tipoDocumento);
        }

        // Validar que no existe ya un documento de este tipo para esta solicitud
        if (documentoAdjuntoRepository.existsByIdSolicitudAndTipoDocumento(idSolicitud, tipoDocumento)) {
            throw new CreateEntityException("DocumentoAdjunto", "Ya existe un documento de tipo " + tipoDocumento + " para esta solicitud");
        }

        String rutaArchivo = fileStorageService.storeFile(archivo, idSolicitud, tipoDocumento);
        
        DocumentoAdjunto doc = new DocumentoAdjunto();
        doc.setIdSolicitud(idSolicitud);
        doc.setTipoDocumento(tipoDocumento);
        doc.setNombreArchivo(archivo.getOriginalFilename());
        doc.setRutaStorage(rutaArchivo);
        doc.setFechaCarga(LocalDateTime.now());
        doc.setVersion(1L);
        
        DocumentoAdjunto saved = documentoAdjuntoRepository.save(doc);
        log.info("Documento cargado exitosamente: {} para solicitud {}", tipoDocumento, idSolicitud);
        return documentoAdjuntoMapper.toResponseDTO(saved);
    }

    public EstadoSolicitudResponseDTO consultarEstadoSolicitud(Long idSolicitud) {
        SolicitudCredito solicitud = solicitudCreditoRepository.findById(idSolicitud)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud no encontrada"));
        
        ClienteProspecto clienteProspecto = clienteProspectoRepository.findById(solicitud.getIdClienteProspecto())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente prospecto no encontrado"));
        
        List<HistorialEstado> historial = historialEstadoRepository.findByIdSolicitudOrderByFechaCambioAsc(idSolicitud);
        
        EstadoSolicitudResponseDTO response = solicitudEstadoMapper.toEstadoSolicitudResponseDTO(solicitud, clienteProspecto);
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
            String mensajeError = String.format("Transición de estado no permitida: %s -> %s. La solicitud debe seguir la jerarquía: BORRADOR -> EN_REVISION -> APROBADA/RECHAZADA", 
                estadoActual, estadoNuevo);
            log.error("Error en cambio de estado: {}", mensajeError);
            throw new CreateEntityException("SolicitudCredito", mensajeError);
        }
        
        // SEGUNDO: Si se va a aprobar, validar todo ANTES de cambiar el estado
        if (estadoNuevo == EstadoSolicitudEnum.APROBADA) {
            log.info("Solicitud {} será aprobada. Validando requisitos...", idSolicitud);
            
            try {
                log.info("Validando documentos obligatorios...");
                validarDocumentosObligatorios(idSolicitud);
                
                log.info("Validando y creando cliente en Core Bancario...");
                validarYCrearClienteEnCore(idSolicitud);
                
                log.info("Todas las validaciones exitosas para aprobación de solicitud {}", idSolicitud);
                
            } catch (Exception e) {
                log.error("Error durante validaciones para aprobación de solicitud {}: {}", idSolicitud, e.getMessage());
                throw new CreateEntityException("SolicitudCredito", 
                    "No se puede aprobar la solicitud. Error en validaciones: " + e.getMessage());
            }
        }
        
        // TERCERO: Cambiar el estado de la solicitud
        String estadoAnterior = solicitud.getEstado();
        solicitud.setEstado(nuevoEstado);
        solicitudCreditoRepository.save(solicitud);
        
        // CUARTO: Crear el historial del cambio
        HistorialEstado historial = new HistorialEstado();
        historial.setIdSolicitud(idSolicitud);
        historial.setEstadoAnterior(estadoAnterior);
        historial.setEstadoNuevo(nuevoEstado);
        historial.setFechaCambio(LocalDateTime.now());
        historial.setUsuarioModificacion(0L); 
        historial.setMotivo(motivo);
        historialEstadoRepository.save(historial);
        
        log.info("Estado de solicitud {} cambiado exitosamente de {} a {} por usuario {}", 
                idSolicitud, estadoAnterior, nuevoEstado, usuario);
        
        // QUINTO: SOLO si se aprobó exitosamente, crear el préstamo cliente
        if (estadoNuevo == EstadoSolicitudEnum.APROBADA) {
            try {
                log.info("Creando préstamo cliente para solicitud aprobada {}", idSolicitud);
                crearPrestamoCliente(idSolicitud);
                log.info("Préstamo cliente creado exitosamente para solicitud {}", idSolicitud);
            } catch (Exception e) {
                log.error("Error al crear préstamo cliente para solicitud {}: {}", idSolicitud, e.getMessage());
                // NO revertimos el estado de la solicitud, pero registramos el error
                throw new CreateEntityException("PrestamoCliente", 
                    "La solicitud fue aprobada pero falló la creación del préstamo cliente: " + e.getMessage());
            }
        }
    }

    private void validarDocumentosObligatorios(Long idSolicitud) {
        List<String> documentosRequeridos = List.of(
            TipoDocumentoEnum.CEDULA_IDENTIDAD.name(),
            TipoDocumentoEnum.ROL_PAGOS.name(),
            TipoDocumentoEnum.ESTADO_CUENTA_BANCARIA.name()
        );
        
        List<DocumentoAdjunto> documentos = documentoAdjuntoRepository.findByIdSolicitud(idSolicitud);
        List<String> documentosCargados = documentos.stream()
                .map(DocumentoAdjunto::getTipoDocumento)
                .toList();
        
        List<String> documentosFaltantes = documentosRequeridos.stream()
            .filter(doc -> !documentosCargados.contains(doc))
            .toList();
        
        if (!documentosFaltantes.isEmpty()) {
            throw new CreateEntityException("SolicitudCredito", 
                "No se puede aprobar la solicitud. Faltan los siguientes documentos obligatorios: " + 
                String.join(", ", documentosFaltantes));
        }
        
        log.info("Validación de documentos obligatorios exitosa para solicitud {}", idSolicitud);
    }

    private void validarYCrearClienteEnCore(Long idSolicitud) {
        SolicitudCredito solicitud = solicitudCreditoRepository.findById(idSolicitud)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud no encontrada"));
        
        ClienteProspecto clienteProspecto = clienteProspectoRepository.findById(solicitud.getIdClienteProspecto())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente prospecto no encontrado"));
        
        log.info("Validando cliente prospecto {} en Core Bancario", clienteProspecto.getCedula());
        
        try {
            PersonaCoreResponseDTO personaCore = coreBancarioClient.consultarPersonaPorIdentificacion("CEDULA", clienteProspecto.getCedula());
            log.info("Persona encontrada en Core Bancario: {}", personaCore.getNombre());
            
            List<ClienteCoreResponseDTO> clientes = coreBancarioClient.consultarClientePorIdentificacion("CEDULA", clienteProspecto.getCedula());
            if (!clientes.isEmpty()) {
                log.info("Cliente ya existe en Core Bancario: {}", clientes.get(0).getNombre());
                return;
            }
            
            ClienteCoreResponseDTO clienteDTO = ClienteCoreResponseDTO.builder()
                    .tipoEntidad("PERSONA")
                    .idEntidad(personaCore.getId())
                    .nombre(personaCore.getNombre())
                    .tipoIdentificacion("CEDULA")
                    .numeroIdentificacion(clienteProspecto.getCedula())
                    .tipoCliente("NATURAL")
                    .segmento("RETAIL")
                    .canalAfiliacion("DIGITAL")
                    .estado("ACTIVO")
                    .build();
            
            ClienteCoreResponseDTO clienteCreado = coreBancarioClient.crearClienteDesdePersona("CEDULA", clienteProspecto.getCedula(), clienteDTO);
            log.info("Cliente creado exitosamente en Core Bancario: {}", clienteCreado.getId());
            
        } catch (Exception e) {
            log.warn("Persona no encontrada en Core Bancario, creando persona y cliente: {}", e.getMessage());
            
            PersonaCoreResponseDTO personaDTO = PersonaCoreResponseDTO.builder()
                    .tipoIdentificacion("CEDULA")
                    .numeroIdentificacion(clienteProspecto.getCedula())
                    .nombre(clienteProspecto.getNombres())
                    .genero(clienteProspecto.getGenero())
                    .fechaNacimiento(clienteProspecto.getFechaNacimiento().toLocalDate())
                    .estadoCivil(clienteProspecto.getEstadoCivil())
                    .nivelEstudio(clienteProspecto.getNivelEstudio())
                    .estado("ACTIVO")
                    .build();
            
            PersonaCoreResponseDTO personaCreada = coreBancarioClient.crearPersona(personaDTO);
            log.info("Persona creada exitosamente en Core Bancario: {}", personaCreada.getId());
            
            ClienteCoreResponseDTO clienteDTO = ClienteCoreResponseDTO.builder()
                    .tipoEntidad("PERSONA")
                    .idEntidad(personaCreada.getId())
                    .nombre(personaCreada.getNombre())
                    .tipoIdentificacion("CEDULA")
                    .numeroIdentificacion(clienteProspecto.getCedula())
                    .tipoCliente("NATURAL")
                    .segmento("RETAIL")
                    .canalAfiliacion("DIGITAL")
                    .estado("ACTIVO")
                    .build();
            
            ClienteCoreResponseDTO clienteCreado = coreBancarioClient.crearClienteDesdePersona("CEDULA", clienteProspecto.getCedula(), clienteDTO);
            log.info("Cliente creado exitosamente en Core Bancario: {}", clienteCreado.getId());
        }
        
        log.info("Validación y creación de cliente en Core Bancario exitosa para solicitud {}", idSolicitud);
    }



    private void crearPrestamoCliente(Long idSolicitud) {
        SolicitudCredito solicitud = solicitudCreditoRepository.findById(idSolicitud)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud no encontrada"));
        
        ClienteProspecto clienteProspecto = clienteProspectoRepository.findById(solicitud.getIdClienteProspecto())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente prospecto no encontrado"));
        
        log.info("Creando préstamo cliente para solicitud: {}", idSolicitud);
        
        try {
            List<ClienteCoreResponseDTO> clientes = coreBancarioClient.consultarClientePorIdentificacion("CEDULA", clienteProspecto.getCedula());
            if (clientes.isEmpty()) {
                throw new CreateEntityException("PrestamoCliente", "El cliente debe existir en Core Bancario antes de crear préstamo");
            }
            
            String idClienteCore = clientes.get(0).getId();
            log.info("Cliente encontrado en Core Bancario con ID: {}", idClienteCore);
            
            prestamosClientesService.crearPrestamoCliente(
                idClienteCore,
                solicitud.getIdPrestamo(),
                solicitud.getMontoSolicitado(),
                solicitud.getPlazoMeses(),
                solicitud.getTasaInteresAplicada()
            );
            
            log.info("Préstamo cliente creado exitosamente para solicitud {}", idSolicitud);
            
        } catch (Exception e) {
            log.error("Error al crear préstamo cliente para solicitud {}: {}", idSolicitud, e.getMessage());
            throw new CreateEntityException("PrestamoCliente", "Error al crear préstamo cliente: " + e.getMessage());
        }
    }

    public Resource descargarDocumento(Long idSolicitud, Long idDocumento) {
        DocumentoAdjunto documento = documentoAdjuntoRepository.findById(idDocumento)
                .orElseThrow(() -> new ResourceNotFoundException("Documento no encontrado"));
        
        if (!documento.getIdSolicitud().equals(idSolicitud)) {
            throw new CreateEntityException("DocumentoAdjunto", "El documento no pertenece a la solicitud especificada");
        }
        
        return fileStorageService.loadFileAsResource(documento.getRutaStorage());
    }

    public List<DocumentoAdjuntoResponseDTO> listarDocumentos(Long idSolicitud) {
        solicitudCreditoRepository.findById(idSolicitud)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud no encontrada"));
        
        List<DocumentoAdjunto> documentos = documentoAdjuntoRepository.findByIdSolicitud(idSolicitud);
        return documentos.stream()
                .map(documentoAdjuntoMapper::toResponseDTO)
                .toList();
    }

    public SolicitudResumenDTO obtenerResumenSolicitud(Long idSolicitud) {
        SolicitudCredito solicitud = solicitudCreditoRepository.findById(idSolicitud)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud no encontrada"));
        
        ClienteProspecto clienteProspecto = clienteProspectoRepository.findById(solicitud.getIdClienteProspecto())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente prospecto no encontrado"));
        
        VehiculoResponseDTO vehiculo = gestionVehiculosService.obtenerVehiculo(
            solicitud.getRucConcesionario(), 
            solicitud.getPlacaVehiculo()
        );
        
        VendedorResponseDTO vendedor = gestionVehiculosService.obtenerVendedor(
            solicitud.getRucConcesionario(), 
            solicitud.getCedulaVendedor()
        );
        
        return solicitudResumenMapper.toSolicitudResumenDTO(solicitud, clienteProspecto, vehiculo, vendedor);
    }

    public List<SolicitudConsultaResponseDTO> consultarSolicitudesPorRangoFechas(LocalDateTime fechaInicio, LocalDateTime fechaFin, String estado) {
        List<SolicitudCredito> solicitudes;
        
        if (estado != null && !estado.trim().isEmpty()) {
            solicitudes = solicitudCreditoRepository.findByEstadoAndFechaSolicitudBetween(estado, fechaInicio, fechaFin);
        } else {
            solicitudes = solicitudCreditoRepository.findByFechaSolicitudBetween(fechaInicio, fechaFin);
        }
        
        return solicitudes.stream().map(solicitud -> {
            ClienteProspecto clienteProspecto = clienteProspectoRepository.findById(solicitud.getIdClienteProspecto())
                    .orElse(new ClienteProspecto());
            
            return solicitudConsultaMapper.toSolicitudConsultaResponseDTO(solicitud, clienteProspecto);
        }).toList();
    }

    private String generarNumeroSolicitud() {
        String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int secuencia = (int) (Math.random() * 9000) + 1000;
        return "SOL-" + fecha + "-" + secuencia;
    }

    // Método francés para calcular cuota mensual
    private BigDecimal calcularCuotaMensual(BigDecimal monto, int plazo, BigDecimal tasaAnual) {
        BigDecimal tasaMensual = tasaAnual.divide(BigDecimal.valueOf(12), 8, BigDecimal.ROUND_HALF_UP);
        BigDecimal cuota = monto.multiply(tasaMensual)
                .divide(BigDecimal.ONE.subtract(BigDecimal.ONE.divide((BigDecimal.ONE.add(tasaMensual)).pow(plazo), 8, BigDecimal.ROUND_HALF_UP)), 2, BigDecimal.ROUND_HALF_UP);
        return cuota;
    }

    private List<AmortizacionDTO> calcularTablaAmortizacion(BigDecimal monto, int plazo, BigDecimal tasaAnual) {
        List<AmortizacionDTO> tabla = new ArrayList<>();
        BigDecimal saldo = monto;
        BigDecimal tasaMensual = tasaAnual.divide(BigDecimal.valueOf(12), 8, BigDecimal.ROUND_HALF_UP);
        BigDecimal cuota = calcularCuotaMensual(monto, plazo, tasaAnual);
        for (int i = 1; i <= plazo; i++) {
            BigDecimal interes = saldo.multiply(tasaMensual).setScale(2, BigDecimal.ROUND_HALF_UP);
            BigDecimal abonoCapital = cuota.subtract(interes);
            BigDecimal saldoFinal = saldo.subtract(abonoCapital);
            tabla.add(new AmortizacionDTO(i, saldo, cuota, abonoCapital, interes, saldoFinal));
            saldo = saldoFinal;
        }
        return tabla;
    }

    private boolean esTransicionValida(EstadoSolicitudEnum actual, EstadoSolicitudEnum nuevo) {
        return switch (actual) {
            case BORRADOR -> nuevo == EstadoSolicitudEnum.EN_REVISION || nuevo == EstadoSolicitudEnum.CANCELADA;
            case EN_REVISION -> nuevo == EstadoSolicitudEnum.APROBADA || nuevo == EstadoSolicitudEnum.RECHAZADA || nuevo == EstadoSolicitudEnum.CANCELADA;
            case APROBADA, RECHAZADA, CANCELADA -> false;
        };
    }
} 