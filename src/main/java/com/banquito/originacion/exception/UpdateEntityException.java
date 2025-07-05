package com.banquito.originacion.exception;

public class UpdateEntityException extends RuntimeException {

    private final Integer errorCode;

    public UpdateEntityException(String entityName, String message) {
        super(entityName + ": " + message);
        this.errorCode = 500;
    }

    @Override
    public String getMessage() {
        return "Error code: " + this.errorCode + ", message: " + super.getMessage();
    }

    public Integer getErrorCode() {
        return errorCode;
    }
}
