package com.banquito.originacion.controller.dto;

import com.banquito.originacion.enums.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClienteProspectoDTO {

    private Long idClienteCore;

    @NotBlank(message = "La cédula es obligatoria")
    @Pattern(regexp = "^[0-9]{10}$", message = "La cédula debe tener exactamente 10 dígitos")
    private String cedula;

    @NotBlank(message = "Los nombres son obligatorios")
    @Size(max = 50, message = "Los nombres no pueden exceder 50 caracteres")
    private String nombres;

    @NotNull(message = "El género es obligatorio")
    private GeneroClienteEnum genero;

    @NotNull(message = "La fecha de nacimiento es obligatoria")
    @Past(message = "La fecha de nacimiento debe ser en el pasado")
    private LocalDateTime fechaNacimiento;

    @NotBlank(message = "El nivel de estudio es obligatorio")
    @Size(max = 15, message = "El nivel de estudio no puede exceder 15 caracteres")
    private String nivelEstudio;

    @NotBlank(message = "El estado civil es obligatorio")
    @Size(max = 15, message = "El estado civil no puede exceder 15 caracteres")
    private String estadoCivil;

    @NotNull(message = "Los ingresos son obligatorios")
    @DecimalMin(value = "0.0", inclusive = false, message = "Los ingresos deben ser mayores a 0")
    @Digits(integer = 10, fraction = 2, message = "Los ingresos deben tener máximo 10 dígitos enteros y 2 decimales")
    private BigDecimal ingresos;

    @NotNull(message = "Los egresos son obligatorios")
    @DecimalMin(value = "0.0", inclusive = true, message = "Los egresos deben ser mayores o iguales a 0")
    @Digits(integer = 10, fraction = 2, message = "Los egresos deben tener máximo 10 dígitos enteros y 2 decimales")
    private BigDecimal egresos;

    @NotBlank(message = "La actividad económica es obligatoria")
    @Size(max = 120, message = "La actividad económica no puede exceder 120 caracteres")
    private String actividadEconomica;

    @NotNull(message = "El estado es obligatorio")
    private EstadoClienteEnum estado;

    @NotBlank(message = "El correo transaccional es obligatorio")
    @Email(message = "El formato del correo electrónico no es válido")
    @Size(max = 40, message = "El correo transaccional no puede exceder 40 caracteres")
    private String correoTransaccional;

    @NotBlank(message = "El teléfono transaccional es obligatorio")
    @Size(max = 20, message = "El teléfono transaccional no puede exceder 20 caracteres")
    private String telefonoTransaccional;

    @NotNull(message = "El tipo de teléfono es obligatorio")
    private TipoTelefonoEnum telefonoTipo;

    @NotBlank(message = "El número de teléfono es obligatorio")
    @Size(max = 10, message = "El número de teléfono no puede exceder 10 caracteres")
    private String telefonoNumero;

    @NotNull(message = "El tipo de dirección es obligatorio")
    private TipoDireccionEnum direccionTipo;

    @NotBlank(message = "La línea 1 de dirección es obligatoria")
    @Size(max = 150, message = "La línea 1 de dirección no puede exceder 150 caracteres")
    private String direccionLinea1;

    @NotBlank(message = "La línea 2 de dirección es obligatoria")
    @Size(max = 150, message = "La línea 2 de dirección no puede exceder 150 caracteres")
    private String direccionLinea2;

    @NotBlank(message = "El código postal es obligatorio")
    @Size(max = 6, message = "El código postal no puede exceder 6 caracteres")
    private String direccionCodigoPostal;

    @NotBlank(message = "El código geográfico es obligatorio")
    @Size(max = 20, message = "El código geográfico no puede exceder 20 caracteres")
    private String direccionGeoCodigo;
} 