package com.banquito.originacion.repository;

import com.banquito.originacion.model.SolicitudCredito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SolicitudCreditoRepository extends JpaRepository<SolicitudCredito, Long> {
    
    Optional<SolicitudCredito> findByNumeroSolicitud(String numeroSolicitud);
    
    List<SolicitudCredito> findByEstado(String estado);
    
    Page<SolicitudCredito> findByEstado(String estado, Pageable pageable);
    
    List<SolicitudCredito> findByCedulaSolicitante(String cedulaSolicitante);
    
    List<SolicitudCredito> findByCedulaSolicitanteAndIdPrestamo(String cedulaSolicitante, String idPrestamo);
    
    List<SolicitudCredito> findByFechaSolicitudBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);
    
    List<SolicitudCredito> findByEstadoAndFechaSolicitudBetween(String estado, LocalDateTime fechaInicio, LocalDateTime fechaFin);
    
    // Métodos con paginación usando Pageable
    Page<SolicitudCredito> findByFechaSolicitudBetweenOrderByFechaSolicitudDesc(LocalDateTime fechaInicio, LocalDateTime fechaFin, Pageable pageable);
    
    Page<SolicitudCredito> findByEstadoAndFechaSolicitudBetweenOrderByFechaSolicitudDesc(String estado, LocalDateTime fechaInicio, LocalDateTime fechaFin, Pageable pageable);
    
    // Métodos con filtros adicionales por vendedor y concesionario
    Page<SolicitudCredito> findByCedulaVendedorAndFechaSolicitudBetweenOrderByFechaSolicitudDesc(String cedulaVendedor, LocalDateTime fechaInicio, LocalDateTime fechaFin, Pageable pageable);
    
    Page<SolicitudCredito> findByEstadoAndCedulaVendedorAndFechaSolicitudBetweenOrderByFechaSolicitudDesc(String estado, String cedulaVendedor, LocalDateTime fechaInicio, LocalDateTime fechaFin, Pageable pageable);
    
    Page<SolicitudCredito> findByRucConcesionarioAndFechaSolicitudBetweenOrderByFechaSolicitudDesc(String rucConcesionario, LocalDateTime fechaInicio, LocalDateTime fechaFin, Pageable pageable);
    
    Page<SolicitudCredito> findByEstadoAndRucConcesionarioAndFechaSolicitudBetweenOrderByFechaSolicitudDesc(String estado, String rucConcesionario, LocalDateTime fechaInicio, LocalDateTime fechaFin, Pageable pageable);
    
    Page<SolicitudCredito> findByCedulaVendedorAndRucConcesionarioAndFechaSolicitudBetweenOrderByFechaSolicitudDesc(String cedulaVendedor, String rucConcesionario, LocalDateTime fechaInicio, LocalDateTime fechaFin, Pageable pageable);
    
    Page<SolicitudCredito> findByEstadoAndCedulaVendedorAndRucConcesionarioAndFechaSolicitudBetweenOrderByFechaSolicitudDesc(String estado, String cedulaVendedor, String rucConcesionario, LocalDateTime fechaInicio, LocalDateTime fechaFin, Pageable pageable);
    
    List<SolicitudCredito> findByPlacaVehiculo(String placaVehiculo);
    
    List<SolicitudCredito> findByRucConcesionario(String rucConcesionario);
    
    List<SolicitudCredito> findByMontoSolicitadoBetween(BigDecimal montoMinimo, BigDecimal montoMaximo);
    
    List<SolicitudCredito> findByPlazoMeses(Integer plazoMeses);
    
    boolean existsByNumeroSolicitud(String numeroSolicitud);
    
    boolean existsByCedulaSolicitante(String cedulaSolicitante);
} 