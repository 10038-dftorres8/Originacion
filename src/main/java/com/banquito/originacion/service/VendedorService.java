package com.banquito.originacion.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class VendedorService {

    private final RestTemplate restTemplate;
    
    @Value("${app.gestion-vehiculos.url:http://localhost:8080}")
    private String gestionVehiculosUrl;

    public VendedorService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Obtiene la cédula del vendedor por su email haciendo una llamada al servicio de gestión de vehículos
     */
    public String obtenerCedulaVendedorPorEmail(String email) {
        try {
            System.out.println("Consultando vendedor por email: " + email + " en servicio: " + gestionVehiculosUrl);
            
            // Construir la URL para consultar el vendedor
            String url = gestionVehiculosUrl + "/v1/vendedores/email/" + email;
            
            // Hacer la llamada HTTP usando ResponseEntity para obtener más información
            System.out.println("Haciendo llamada HTTP a: " + url);
            ResponseEntity<VendedorResponse> responseEntity = restTemplate.getForEntity(url, VendedorResponse.class);
            
            System.out.println("Status code: " + responseEntity.getStatusCode());
            System.out.println("Headers: " + responseEntity.getHeaders());
            
            VendedorResponse response = responseEntity.getBody();
            System.out.println("Respuesta recibida: " + response);
            
            if (response != null) {
                System.out.println("Response no es null, cedula: " + response.getCedula());
                if (response.getCedula() != null) {
                    System.out.println("Cédula encontrada para vendedor " + email + ": " + response.getCedula());
                    return response.getCedula();
                } else {
                    System.out.println("Response es null o cedula es null para el vendedor con email: " + email);
                }
            } else {
                System.out.println("Response es null para el vendedor con email: " + email);
            }
            return null;
        } catch (Exception e) {
            System.out.println("Error al consultar vendedor por email: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Clase interna para mapear la respuesta del servicio de gestión de vehículos
     */
    public static class VendedorResponse {
        private String id;
        private String nombre;
        private String telefono;
        private String email;
        private String estado;
        private Long version;
        private String cedula;

        // Getters y setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        public String getTelefono() { return telefono; }
        public void setTelefono(String telefono) { this.telefono = telefono; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getEstado() { return estado; }
        public void setEstado(String estado) { this.estado = estado; }
        public Long getVersion() { return version; }
        public void setVersion(Long version) { this.version = version; }
        public String getCedula() { return cedula; }
        public void setCedula(String cedula) { this.cedula = cedula; }
    }
}
