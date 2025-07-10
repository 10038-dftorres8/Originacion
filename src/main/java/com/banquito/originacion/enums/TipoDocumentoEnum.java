package com.banquito.originacion.enums;

public enum TipoDocumentoEnum {
    CEDULA_IDENTIDAD("Cédula de Identidad"),
    ROL_PAGOS("Rol de Pagos"),
    ESTADO_CUENTA_BANCARIA("Estado de Cuenta Bancaria");

    private final String descripcion;

    TipoDocumentoEnum(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
} 