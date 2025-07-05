package com.banquito.originacion.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "clientes_prospectos", schema = "originacion")
@Getter
@Setter
@NoArgsConstructor
@ToString
public class ClienteProspecto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cliente_prospecto", nullable = false)
    private Long id;

    @Column(name = "id_cliente_core")
    private Long idClienteCore;

    @Column(name = "cedula", length = 10, nullable = false, unique = true)
    private String cedula;

    @Column(name = "nombres", length = 50, nullable = false)
    private String nombres;

    @Column(name = "genero", nullable = false)
    private String genero;

    @Column(name = "fecha_nacimiento", nullable = false)
    private LocalDateTime fechaNacimiento;

    @Column(name = "nivel_estudio", length = 15, nullable = false)
    private String nivelEstudio;

    @Column(name = "estado_civil", length = 15, nullable = false)
    private String estadoCivil;

    @Column(name = "ingresos", precision = 12, scale = 2, nullable = false)
    private BigDecimal ingresos;

    @Column(name = "egresos", precision = 12, scale = 2, nullable = false)
    private BigDecimal egresos;

    @Column(name = "actividad_economica", length = 120, nullable = false)
    private String actividadEconomica;

    @Column(name = "estado", nullable = false)
    private String estado;

    @Column(name = "correo_transaccional", length = 40, nullable = false)
    private String correoTransaccional;

    @Column(name = "telefono_transaccional", length = 20, nullable = false)
    private String telefonoTransaccional;

    @Column(name = "telefono_tipo", nullable = false)
    private String telefonoTipo;

    @Column(name = "telefono_numero", length = 10, nullable = false)
    private String telefonoNumero;

    @Column(name = "direccion_tipo", nullable = false)
    private String direccionTipo;

    @Column(name = "direccion_linea1", length = 150, nullable = false)
    private String direccionLinea1;

    @Column(name = "direccion_linea2", length = 150, nullable = false)
    private String direccionLinea2;

    @Column(name = "direccion_codigo_postal", length = 6, nullable = false)
    private String direccionCodigoPostal;

    @Column(name = "direccion_geo_codigo", length = 20, nullable = false)
    private String direccionGeoCodigo;

    @Version
    @Column(name = "version")
    private Long version;

    public ClienteProspecto(Long id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ClienteProspecto that = (ClienteProspecto) obj;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
} 