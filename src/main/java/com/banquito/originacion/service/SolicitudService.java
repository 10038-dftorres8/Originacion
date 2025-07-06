package com.banquito.originacion.service;

import com.banquito.originacion.controller.dto.*;
import com.banquito.originacion.controller.mapper.SolicitudCreditoMapper;
import com.banquito.originacion.enums.EstadoSolicitudEnum;
import com.banquito.originacion.exception.CreateEntityException;
import com.banquito.originacion.exception.ResourceNotFoundException;
import com.banquito.originacion.model.SolicitudCredito;
import com.banquito.originacion.model.HistorialEstado;
import com.banquito.originacion.model.DocumentoAdjunto;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

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
    private final GestionVehiculosService gestionVehiculosService;



    public SolicitudCreditoResponseDTO crearSolicitudConValidacion(SolicitudCreditoExtendidaDTO solicitudDTO) {
        log.info("Iniciando creación de solicitud con validación de vehículo y vendedor");
        
        if (!clienteProspectoRepository.existsById(solicitudDTO.getIdClienteProspecto())) {
            throw new CreateEntityException("SolicitudCredito", "El cliente prospecto no existe");
        }
        
        boolean existeBorrador = solicitudCreditoRepository.findAll().stream()
            .anyMatch(s -> s.getIdClienteProspecto().equals(solicitudDTO.getIdClienteProspecto())
                && s.getIdProductoCredito().equals(solicitudDTO.getIdProductoCredito())
                && "BORRADOR".equals(s.getEstado()));
        if (existeBorrador) {
            throw new CreateEntityException("SolicitudCredito", "Ya existe una solicitud en estado BORRADOR para este cliente y producto");
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
        
        // Calcular monto solicitado = valor vehículo - entrada
        BigDecimal valorVehiculo = vehiculo.getValor();
        BigDecimal montoSolicitado = valorVehiculo.subtract(solicitudDTO.getValorEntrada());
        
        // Validar entrada mínima del 20%
        BigDecimal entradaMinima = valorVehiculo.multiply(new BigDecimal("0.2"));
        if (solicitudDTO.getValorEntrada().compareTo(entradaMinima) < 0) {
            throw new CreateEntityException("SolicitudCredito", "La entrada debe ser al menos el 20% del valor del vehículo. Mínimo requerido: " + entradaMinima);
        }
        
        // Validar monto máximo del 80%
        BigDecimal montoMaximo = valorVehiculo.multiply(new BigDecimal("0.8"));
        if (montoSolicitado.compareTo(montoMaximo) > 0) {
            throw new CreateEntityException("SolicitudCredito", "El monto solicitado excede el 80% del valor del vehículo. Máximo permitido: " + montoMaximo);
        }
        
        BigDecimal cuotaMensual = calcularCuotaMensual(montoSolicitado, solicitudDTO.getPlazoMeses(), solicitudDTO.getTasaInteres());
        String numeroSolicitud = generarNumeroSolicitud();
        String estadoInicial = "BORRADOR";
        
        SolicitudCredito solicitud = new SolicitudCredito();
        solicitud.setIdClienteProspecto(solicitudDTO.getIdClienteProspecto());
        solicitud.setIdProductoCredito(solicitudDTO.getIdProductoCredito());
        solicitud.setMontoSolicitado(montoSolicitado);
        solicitud.setPlazoMeses(solicitudDTO.getPlazoMeses());
        solicitud.setValorEntrada(solicitudDTO.getValorEntrada());
        solicitud.setTasaInteresAplicada(solicitudDTO.getTasaInteres());
        solicitud.setCuotaMensualCalculada(cuotaMensual);
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
        historial.setMotivo("Creación de solicitud con validación de vehículo y vendedor");
        historialEstadoRepository.save(historial);
        
        log.info("Solicitud creada exitosamente con número: {}", numeroSolicitud);
        return solicitudCreditoMapper.toResponseDTO(saved);
    }



    public List<AmortizacionDTO> simularCreditoConValidacion(String rucConcesionario, String placaVehiculo, BigDecimal montoSolicitado, Integer plazoMeses, BigDecimal tasaInteres) {
        log.info("Iniciando simulación de crédito para vehículo: {} en concesionario: {}", placaVehiculo, rucConcesionario);
        
        VehiculoResponseDTO vehiculo = gestionVehiculosService.obtenerVehiculo(rucConcesionario, placaVehiculo);
        
        if (!gestionVehiculosService.validarVehiculoDisponible(vehiculo)) {
            throw new CreateEntityException("SimulacionCredito", "El vehículo no está disponible para financiamiento");
        }
        
        // Validar monto máximo del 80%
        BigDecimal valorVehiculo = vehiculo.getValor();
        BigDecimal montoMaximo = valorVehiculo.multiply(new BigDecimal("0.8"));
        
        if (montoSolicitado.compareTo(montoMaximo) > 0) {
            throw new CreateEntityException("SimulacionCredito", "El monto solicitado excede el 80% del valor del vehículo. Máximo permitido: " + montoMaximo);
        }
        
        log.info("Simulando crédito: monto={}, plazo={}, valorVehiculo={}", 
                montoSolicitado, plazoMeses, valorVehiculo);
        
        // Generar 3 escenarios de simulación
        List<AmortizacionDTO> todosLosEscenarios = new ArrayList<>();
        
        // Escenario 1: Con entrada 20%
        BigDecimal entrada20Porcentaje = montoSolicitado.multiply(new BigDecimal("0.2"));
        BigDecimal montoConEntrada = montoSolicitado.subtract(entrada20Porcentaje);
        List<AmortizacionDTO> escenario1 = calcularTablaAmortizacion(montoConEntrada, plazoMeses, tasaInteres);
        escenario1.forEach(cuota -> cuota.setEscenario("Con entrada 20%"));
        todosLosEscenarios.addAll(escenario1);
        
        // Escenario 2: Sin entrada
        List<AmortizacionDTO> escenario2 = calcularTablaAmortizacion(montoSolicitado, plazoMeses, tasaInteres);
        escenario2.forEach(cuota -> cuota.setEscenario("Sin entrada"));
        todosLosEscenarios.addAll(escenario2);
        
        // Escenario 3: Plazo máximo para menor cuota
        int plazoMaximo = Math.min(60, plazoMeses * 2);
        List<AmortizacionDTO> escenario3 = calcularTablaAmortizacion(montoSolicitado, plazoMaximo, tasaInteres);
        escenario3.forEach(cuota -> cuota.setEscenario("Plazo máximo (" + plazoMaximo + " meses)"));
        todosLosEscenarios.addAll(escenario3);
        
        return todosLosEscenarios;
    }

    public DocumentoAdjuntoResponseDTO cargarDocumento(Long idSolicitud, MultipartFile archivo, String tipoDocumento) {
        if (archivo == null || archivo.isEmpty()) {
            throw new CreateEntityException("DocumentoAdjunto", "El archivo es obligatorio");
        }
        if (!archivo.getOriginalFilename().toLowerCase().endsWith(".pdf")) {
            throw new CreateEntityException("DocumentoAdjunto", "El archivo debe ser un PDF");
        }
        if (archivo.getSize() > 5 * 1024 * 1024) {
            throw new CreateEntityException("DocumentoAdjunto", "El archivo no debe superar los 5MB");
        }
        
        DocumentoAdjunto doc = new DocumentoAdjunto();
        doc.setIdSolicitud(idSolicitud);
        doc.setNombreArchivo(archivo.getOriginalFilename());
        doc.setRutaStorage("/fake/path/" + archivo.getOriginalFilename());
        doc.setFechaCarga(LocalDateTime.now());
        doc.setVersion(1L);
        DocumentoAdjunto saved = documentoAdjuntoRepository.save(doc);
        return documentoAdjuntoMapper.toResponseDTO(saved);
    }

    public EstadoSolicitudResponseDTO consultarEstadoSolicitud(Long idSolicitud) {
        SolicitudCredito solicitud = solicitudCreditoRepository.findById(idSolicitud)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud no encontrada"));
        List<HistorialEstado> historial = historialEstadoRepository.findByIdSolicitudOrderByFechaCambioAsc(idSolicitud);
        EstadoSolicitudResponseDTO response = new EstadoSolicitudResponseDTO();
        response.setEstadoActual(solicitud.getEstado());
        response.setHistorial(historial.stream().map(historialEstadoMapper::toDTO).toList());
        return response;
    }

    public void cambiarEstadoSolicitud(Long idSolicitud, String nuevoEstado, String motivo, String usuario) {
        SolicitudCredito solicitud = solicitudCreditoRepository.findById(idSolicitud)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud no encontrada"));
        EstadoSolicitudEnum estadoActual = EstadoSolicitudEnum.valueOf(solicitud.getEstado());
        EstadoSolicitudEnum estadoNuevo;
        try {
            estadoNuevo = EstadoSolicitudEnum.valueOf(nuevoEstado);
        } catch (IllegalArgumentException e) {
            throw new CreateEntityException("SolicitudCredito", "Estado de solicitud no válido: " + nuevoEstado);
        }
        if (!esTransicionValida(estadoActual, estadoNuevo)) {
            throw new CreateEntityException("SolicitudCredito", "Transición de estado no permitida: " + estadoActual + " -> " + estadoNuevo);
        }
        String estadoAnterior = solicitud.getEstado();
        solicitud.setEstado(nuevoEstado);
        solicitudCreditoRepository.save(solicitud);
        HistorialEstado historial = new HistorialEstado();
        historial.setIdSolicitud(idSolicitud);
        historial.setEstadoAnterior(estadoAnterior);
        historial.setEstadoNuevo(nuevoEstado);
        historial.setFechaCambio(LocalDateTime.now());
        historial.setUsuarioModificacion(0L); // TODO: usuario real
        historial.setMotivo(motivo);
        historialEstadoRepository.save(historial);
    }

    public SolicitudResumenDTO obtenerResumenSolicitud(Long idSolicitud) {
        SolicitudCredito solicitud = solicitudCreditoRepository.findById(idSolicitud)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud no encontrada"));
        
        // Obtener información del vehículo para el precio final
        VehiculoResponseDTO vehiculo = gestionVehiculosService.obtenerVehiculo(
            solicitud.getRucConcesionario(), 
            solicitud.getPlacaVehiculo()
        );
        
        return new SolicitudResumenDTO(
            solicitud.getId(),
            vehiculo.getValor(), // precio_final_vehiculo
            solicitud.getMontoSolicitado(), // monto_aprobado
            solicitud.getPlazoMeses(), // plazo_final_meses
            solicitud.getTasaInteresAplicada() // tasa_efectiva_anual
        );
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