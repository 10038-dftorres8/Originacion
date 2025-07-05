package com.banquito.originacion.repository;

import com.banquito.originacion.model.SolicitudCredito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SolicitudCreditoRepository extends JpaRepository<SolicitudCredito, Long> {
    
    Optional<SolicitudCredito> findByNumeroSolicitud(String numeroSolicitud);
    
    List<SolicitudCredito> findByEstado(String estado);
    
    Page<SolicitudCredito> findByEstado(String estado, Pageable pageable);
    
    List<SolicitudCredito> findByIdClienteProspecto(Long idClienteProspecto);
    
    List<SolicitudCredito> findByFechaSolicitudBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);
    
    List<SolicitudCredito> findByIdVendedor(Long idVendedor);
    
    List<SolicitudCredito> findByEstadoAndFechaSolicitudBetween(String estado, LocalDateTime fechaInicio, LocalDateTime fechaFin);
} 