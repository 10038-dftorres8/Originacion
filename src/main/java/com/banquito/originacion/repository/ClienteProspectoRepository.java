package com.banquito.originacion.repository;

import com.banquito.originacion.model.ClienteProspecto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteProspectoRepository extends JpaRepository<ClienteProspecto, Long> {
    
    Optional<ClienteProspecto> findByCedula(String cedula);
    
    List<ClienteProspecto> findByEstado(String estado);
    
    boolean existsByCedula(String cedula);
    
    List<ClienteProspecto> findByNombresContainingIgnoreCase(String nombres);
    
    Optional<ClienteProspecto> findByIdClienteCore(Long idClienteCore);
} 