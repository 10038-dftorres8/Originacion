package com.banquito.originacion.repository;

import com.banquito.originacion.model.HistorialEstado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface HistorialEstadoRepository extends JpaRepository<HistorialEstado, Long> {
    
    List<HistorialEstado> findByIdSolicitud(Long idSolicitud);
    
    List<HistorialEstado> findByIdSolicitudOrderByFechaCambioDesc(Long idSolicitud);
    
    List<HistorialEstado> findByEstadoNuevo(String estadoNuevo);
    
    List<HistorialEstado> findByUsuarioModificacion(Long usuarioModificacion);
    
    List<HistorialEstado> findByFechaCambioBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);
} 