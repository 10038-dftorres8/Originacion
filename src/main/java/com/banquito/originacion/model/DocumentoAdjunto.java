package com.banquito.originacion.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "documentos_adjuntos", schema = "originacion")
@Getter
@Setter
@NoArgsConstructor
@ToString
public class DocumentoAdjunto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_documento_adjunto", nullable = false)
    private Long id;

    @Column(name = "id_solicitud", nullable = false)
    private Long idSolicitud;

    @Column(name = "id_documento_requerido", nullable = false)
    private Long idDocumentoRequerido;

    @Column(name = "nombre_archivo", length = 100, nullable = false)
    private String nombreArchivo;

    @Column(name = "ruta_storage", length = 255, nullable = false)
    private String rutaStorage;

    @Column(name = "fecha_carga", nullable = false)
    private LocalDateTime fechaCarga;

    @Version
    @Column(name = "version")
    private Long version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_solicitud", referencedColumnName = "id_solicitud", insertable = false, updatable = false)
    private SolicitudCredito solicitudCredito;

    public DocumentoAdjunto(Long id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        DocumentoAdjunto that = (DocumentoAdjunto) obj;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
} 