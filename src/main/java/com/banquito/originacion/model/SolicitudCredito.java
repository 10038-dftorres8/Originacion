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

    @Column(name = "fecha_solicitud", nullable = false)
    private LocalDateTime fechaSolicitud;

    @Column(name = "cedula_solicitante", length = 10, nullable = false)
    private String cedulaSolicitante;

    @Column(name = "calificacion_solicitante", length = 5, nullable = false)
    private String calificacionSolicitante;

    @Column(name = "capacidad_pago_solicitante", precision = 12, scale = 2)
    private BigDecimal capacidadPagoSolicitante;

    @Column(name = "placa_vehiculo", length = 10, nullable = false)
    private String placaVehiculo;

    @Column(name = "ruc_concesionario", length = 13, nullable = false)
    private String rucConcesionario;

    @Column(name = "cedula_vendedor", length = 10, nullable = false)
    private String cedulaVendedor;

    @Column(name = "id_prestamo", length = 50, nullable = false)
    private String idPrestamo;

    @Column(name = "valor_entrada", precision = 12, scale = 2, nullable = false)
    private BigDecimal valorEntrada;

    @Column(name = "monto_solicitado", precision = 12, scale = 2, nullable = false)
    private BigDecimal montoSolicitado;

    @Column(name = "plazo_meses", nullable = false)
    private Integer plazoMeses;

    @Column(name = "estado", nullable = false)
    private String estado;

    @Version
    @Column(name = "version")
    private Long version;

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