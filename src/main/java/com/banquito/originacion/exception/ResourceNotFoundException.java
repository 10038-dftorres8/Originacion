package com.banquito.originacion.exception;

public class ResourceNotFoundException extends RuntimeException {

    private final Integer errorCode;

    public ResourceNotFoundException(String message) {
        super(message);
        this.errorCode = 404;
    }

    @Override
    public String getMessage() {
        return "Error code: " + this.errorCode + ", message: " + super.getMessage();
    }

    public Integer getErrorCode() {
        return errorCode;
    }
}