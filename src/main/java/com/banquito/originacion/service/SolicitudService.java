package com.banquito.originacion.service;

import com.banquito.originacion.controller.dto.*;
import com.banquito.originacion.controller.mapper.SolicitudCreditoMapper;
import com.banquito.originacion.enums.ClasificacionClienteEnum;
import com.banquito.originacion.enums.EstadoClienteEnum;
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

    // 1. Crear Solicitud
    public SolicitudCreditoResponseDTO crearSolicitud(SolicitudCreditoDTO solicitudDTO) {
        // Validar existencia de cliente prospecto
        if (!clienteProspectoRepository.existsById(solicitudDTO.getIdClienteProspecto())) {
            throw new CreateEntityException("El cliente prospecto no existe");
        }
        // Validar que no haya otra solicitud en BORRADOR para el mismo cliente y producto
        boolean existeBorrador = solicitudCreditoRepository.findAll().stream()
            .anyMatch(s -> s.getIdClienteProspecto().equals(solicitudDTO.getIdClienteProspecto())
                && s.getIdProductoCredito().equals(solicitudDTO.getIdProductoCredito())
                && "BORRADOR".equals(s.getEstado()));
        if (existeBorrador) {
            throw new CreateEntityException("Ya existe una solicitud en estado BORRADOR para este cliente y producto");
        }
        // TODO: Validar existencia y estado de vehículo, producto, vendedor (otros microservicios)
        // TODO: Obtener condiciones del producto (plazo, monto, etc.)
        // 1. Validar campos obligatorios (ya validados por DTO)
        // 2. Generar número de solicitud único
        String numeroSolicitud = generarNumeroSolicitud();
        // 3. Establecer estado inicial
        String estadoInicial = "BORRADOR";
        // 4. Guardar en tabla solicitudes_credito
        SolicitudCredito solicitud = solicitudCreditoMapper.toEntity(solicitudDTO);
        solicitud.setNumeroSolicitud(numeroSolicitud);
        solicitud.setEstado(estadoInicial);
        solicitud.setFechaSolicitud(LocalDateTime.now());
        SolicitudCredito saved = solicitudCreditoRepository.save(solicitud);
        // 5. Registrar en historial_estados
        HistorialEstado historial = new HistorialEstado();
        historial.setIdSolicitud(saved.getId());
        historial.setEstadoNuevo(estadoInicial);
        historial.setFechaCambio(LocalDateTime.now());
        historial.setUsuarioModificacion(0L); // TODO: usuario real
        historial.setMotivo("Creación de solicitud");
        historialEstadoRepository.save(historial);
        return solicitudCreditoMapper.toResponseDTO(saved);
    }

    private String generarNumeroSolicitud() {
        String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int secuencia = (int) (Math.random() * 9000) + 1000;
        return "SOL-" + fecha + "-" + secuencia;
    }

    // 2. Simular Crédito
    public List<AmortizacionDTO> simularCredito(SimulacionDTO simulacionDTO) {
        // TODO: Obtener valor del vehículo y condiciones del producto (otros microservicios)
        // TODO: Validar monto vs valor vehículo (máximo 80%)
        // TODO: Validar cuota vs ingresos (máximo 40%)
        // TODO: Generar escenarios (entrada 20%, sin entrada, plazo máximo)
        // Por ahora, solo un escenario simple:
        BigDecimal monto = simulacionDTO.getMontoSolicitado();
        int plazo = simulacionDTO.getPlazoMeses();
        BigDecimal tasa = new BigDecimal("0.15"); // TODO: traer tasa real
        return calcularTablaAmortizacion(monto, plazo, tasa);
    }

    private List<AmortizacionDTO> calcularTablaAmortizacion(BigDecimal monto, int plazo, BigDecimal tasaAnual) {
        List<AmortizacionDTO> tabla = new ArrayList<>();
        BigDecimal saldo = monto;
        BigDecimal tasaMensual = tasaAnual.divide(BigDecimal.valueOf(12), 8, BigDecimal.ROUND_HALF_UP);
        BigDecimal cuota = monto.multiply(tasaMensual).divide(BigDecimal.ONE.subtract(BigDecimal.ONE.divide((BigDecimal.ONE.add(tasaMensual)).pow(plazo), 8, BigDecimal.ROUND_HALF_UP)), 2, BigDecimal.ROUND_HALF_UP);
        for (int i = 1; i <= plazo; i++) {
            BigDecimal interes = saldo.multiply(tasaMensual).setScale(2, BigDecimal.ROUND_HALF_UP);
            BigDecimal abonoCapital = cuota.subtract(interes);
            BigDecimal saldoFinal = saldo.subtract(abonoCapital);
            tabla.add(new AmortizacionDTO(i, saldo, cuota, abonoCapital, interes, saldoFinal));
            saldo = saldoFinal;
        }
        return tabla;
    }

    // 3. Cargar Documento
    public DocumentoAdjuntoResponseDTO cargarDocumento(Long idSolicitud, MultipartFile archivo, String tipoDocumento) {
        // Validar tipo y tamaño de archivo
        if (archivo == null || archivo.isEmpty()) {
            throw new CreateEntityException("El archivo es obligatorio");
        }
        if (!archivo.getOriginalFilename().toLowerCase().endsWith(".pdf")) {
            throw new CreateEntityException("El archivo debe ser un PDF");
        }
        if (archivo.getSize() > 5 * 1024 * 1024) {
            throw new CreateEntityException("El archivo no debe superar los 5MB");
        }
        // TODO: Generar nombre único y guardar en sistema de archivos
        // TODO: Obtener lista de documentos requeridos del producto (microservicio productos)
        // TODO: Registrar en tabla documentos_adjuntos
        // TODO: Actualizar historial si es el último documento obligatorio
        // Simulación de registro:
        DocumentoAdjunto doc = new DocumentoAdjunto();
        doc.setIdSolicitud(idSolicitud);
        doc.setNombreArchivo(archivo.getOriginalFilename());
        doc.setRutaStorage("/fake/path/" + archivo.getOriginalFilename());
        doc.setFechaCarga(LocalDateTime.now());
        doc.setVersion(1L);
        DocumentoAdjunto saved = documentoAdjuntoRepository.save(doc);
        return documentoAdjuntoMapper.toResponseDTO(saved);
    }

    // 4. Consultar Estado Solicitud
    public EstadoSolicitudResponseDTO consultarEstadoSolicitud(Long idSolicitud) {
        SolicitudCredito solicitud = solicitudCreditoRepository.findById(idSolicitud)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud no encontrada"));
        List<HistorialEstado> historial = historialEstadoRepository.findByIdSolicitudOrderByFechaCambioAsc(idSolicitud);
        EstadoSolicitudResponseDTO response = new EstadoSolicitudResponseDTO();
        response.setEstadoActual(solicitud.getEstado());
        response.setHistorial(historial.stream().map(historialEstadoMapper::toDTO).toList());
        return response;
    }

    // 5. Cambiar Estado Solicitud
    public void cambiarEstadoSolicitud(Long idSolicitud, String nuevoEstado, String motivo, String usuario) {
        SolicitudCredito solicitud = solicitudCreditoRepository.findById(idSolicitud)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud no encontrada"));
        EstadoSolicitudEnum estadoActual = EstadoSolicitudEnum.valueOf(solicitud.getEstado());
        EstadoSolicitudEnum estadoNuevo;
        try {
            estadoNuevo = EstadoSolicitudEnum.valueOf(nuevoEstado);
        } catch (IllegalArgumentException e) {
            throw new CreateEntityException("Estado de solicitud no válido: " + nuevoEstado);
        }
        if (!esTransicionValida(estadoActual, estadoNuevo)) {
            throw new CreateEntityException("Transición de estado no permitida: " + estadoActual + " -> " + estadoNuevo);
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
        // TODO: Enviar notificación (futuro)
    }

    private boolean esTransicionValida(EstadoSolicitudEnum actual, EstadoSolicitudEnum nuevo) {
        return switch (actual) {
            case BORRADOR -> nuevo == EstadoSolicitudEnum.EN_REVISION || nuevo == EstadoSolicitudEnum.CANCELADA;
            case EN_REVISION -> nuevo == EstadoSolicitudEnum.APROBADA || nuevo == EstadoSolicitudEnum.RECHAZADA || nuevo == EstadoSolicitudEnum.CANCELADA;
            case APROBADA, RECHAZADA, CANCELADA -> false;
        };
    }

    // 6. Validar Documentos Completos
    public List<String> validarDocumentosCompletos(Long idSolicitud) {
        // TODO: Obtener lista de documentos requeridos del producto (microservicio productos)
        // TODO: Verificar documentos cargados para la solicitud
        // TODO: Comparar y retornar lista de faltantes
        throw new UnsupportedOperationException("No implementado aún");
    }
} 