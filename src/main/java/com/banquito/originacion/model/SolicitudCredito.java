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

    @Column(name = "id_vehiculo", nullable = false)
    private Long idVehiculo;

    @Column(name = "id_vendedor", nullable = false)
    private Long idVendedor;

    @Column(name = "id_producto_credito", nullable = false)
    private Long idProductoCredito;

    @Column(name = "monto_solicitado", precision = 12, scale = 2, nullable = false)
    private BigDecimal montoSolicitado;

    @Column(name = "plazo_meses", nullable = false)
    private Integer plazoMeses;

    @Column(name = "valor_entrada", precision = 12, scale = 2, nullable = false)
    private BigDecimal valorEntrada;

    @Column(name = "tasa_interes_aplicada", precision = 5, scale = 2, nullable = false)
    private BigDecimal tasaInteresAplicada;

    @Column(name = "cuota_mensual_calculada", precision = 10, scale = 2, nullable = false)
    private BigDecimal cuotaMensualCalculada;

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