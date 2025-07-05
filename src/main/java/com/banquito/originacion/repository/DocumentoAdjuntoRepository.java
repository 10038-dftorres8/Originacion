package com.banquito.originacion.repository;

import com.banquito.originacion.model.DocumentoAdjunto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentoAdjuntoRepository extends JpaRepository<DocumentoAdjunto, Long> {
    
    List<DocumentoAdjunto> findByIdSolicitud(Long idSolicitud);
    
    List<DocumentoAdjunto> findByIdSolicitudAndIdDocumentoRequerido(Long idSolicitud, Long idDocumentoRequerido);
    
    boolean existsByIdSolicitudAndIdDocumentoRequerido(Long idSolicitud, Long idDocumentoRequerido);
    
    void deleteByIdSolicitud(Long idSolicitud);
} 