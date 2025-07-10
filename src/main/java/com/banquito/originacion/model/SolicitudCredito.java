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
@Table(name = "solicitudes_credito", schema = "originacion")
@Getter
@Setter
@NoArgsConstructor
@ToString
public class SolicitudCredito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_solicitud", nullable = false)
    private Long id;

    @Column(name = "numero_solicitud", length = 50, nullable = false, unique = true)
    private String numeroSolicitud;

    @Column(name = "id_cliente_prospecto", nullable = false)
    private Long idClienteProspecto;

    @Column(name = "id_vehiculo", length = 50, nullable = false)
    private String idVehiculo;

    @Column(name = "id_vendedor", length = 50, nullable = false)
    private String idVendedor;

    @Column(name = "id_prestamo", length = 50, nullable = false)
    private String idPrestamo;

    // Campos para integración con servicio de gestión de vehículos
    @Column(name = "ruc_concesionario", length = 13)
    private String rucConcesionario;

    @Column(name = "placa_vehiculo", length = 10)
    private String placaVehiculo;

    @Column(name = "cedula_vendedor", length = 10)
    private String cedulaVendedor;

    @Column(name = "monto_solicitado", precision = 12, scale = 2, nullable = false)
    private BigDecimal montoSolicitado;

    @Column(name = "plazo_meses", nullable = false)
    private Integer plazoMeses;

    @Column(name = "valor_entrada", precision = 12, scale = 2, nullable = false)
    private BigDecimal valorEntrada;

    @Column(name = "tasa_interes_aplicada", precision = 5, scale = 4, nullable = false)
    private BigDecimal tasaInteresAplicada;

    @Column(name = "tasa_interes_base", precision = 5, scale = 4)
    private BigDecimal tasaInteresBase;

    @Column(name = "cuota_mensual_calculada", precision = 10, scale = 2, nullable = false)
    private BigDecimal cuotaMensualCalculada;

    @Column(name = "monto_total_calculado", precision = 12, scale = 2)
    private BigDecimal montoTotalCalculado;

    @Column(name = "total_intereses_calculado", precision = 12, scale = 2)
    private BigDecimal totalInteresesCalculado;

    @Column(name = "fecha_solicitud", nullable = false)
    private LocalDateTime fechaSolicitud;

    @Column(name = "estado", nullable = false)
    private String estado;

    @Version
    @Column(name = "version")
    private Long version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente_prospecto", referencedColumnName = "id_cliente_prospecto", insertable = false, updatable = false)
    private ClienteProspecto clienteProspecto;

    public SolicitudCredito(Long id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        SolicitudCredito that = (SolicitudCredito) obj;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
} 