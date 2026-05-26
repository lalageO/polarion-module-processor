package com.example.polarionprocessor.service;

public class ModuleProcessException extends RuntimeException {

    private final String errorCode;

    public ModuleProcessException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ModuleProcessException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
