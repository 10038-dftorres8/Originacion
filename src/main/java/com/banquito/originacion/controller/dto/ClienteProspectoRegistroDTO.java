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
public class ClienteProspectoRegistroDTO {

    @NotBlank(message = "La cédula es obligatoria")
    @Pattern(regexp = "^[0-9]{10}$", message = "La cédula debe tener exactamente 10 dígitos")
    private String cedula;

    @Size(max = 50, message = "Los nombres no pueden exceder 50 caracteres")
    private String nombres;

    private GeneroClienteEnum genero;

    @Past(message = "La fecha de nacimiento debe ser en el pasado")
    private LocalDateTime fechaNacimiento;

    @Size(max = 15, message = "El nivel de estudio no puede exceder 15 caracteres")
    private String nivelEstudio;

    @Size(max = 15, message = "El estado civil no puede exceder 15 caracteres")
    private String estadoCivil;

    @DecimalMin(value = "0.0", inclusive = false, message = "Los ingresos deben ser mayores a 0")
    @Digits(integer = 10, fraction = 2, message = "Los ingresos deben tener máximo 10 dígitos enteros y 2 decimales")
    private BigDecimal ingresos;

    @DecimalMin(value = "0.0", inclusive = true, message = "Los egresos deben ser mayores o iguales a 0")
    @Digits(integer = 10, fraction = 2, message = "Los egresos deben tener máximo 10 dígitos enteros y 2 decimales")
    private BigDecimal egresos;

    @Size(max = 120, message = "La actividad económica no puede exceder 120 caracteres")
    private String actividadEconomica;

    @Email(message = "El formato del correo electrónico no es válido")
    @Size(max = 40, message = "El correo transaccional no puede exceder 40 caracteres")
    private String correoTransaccional;

    @Size(max = 20, message = "El teléfono transaccional no puede exceder 20 caracteres")
    private String telefonoTransaccional;

    private TipoTelefonoEnum telefonoTipo;

    @Size(max = 10, message = "El número de teléfono no puede exceder 10 caracteres")
    private String telefonoNumero;

    private TipoDireccionEnum direccionTipo;

    @Size(max = 150, message = "La línea 1 de dirección no puede exceder 150 caracteres")
    private String direccionLinea1;

    @Size(max = 150, message = "La línea 2 de dirección no puede exceder 150 caracteres")
    private String direccionLinea2;

    @Size(max = 6, message = "El código postal no puede exceder 6 caracteres")
    private String direccionCodigoPostal;

    @Size(max = 20, message = "El código geográfico no puede exceder 20 caracteres")
    private String direccionGeoCodigo;
} 