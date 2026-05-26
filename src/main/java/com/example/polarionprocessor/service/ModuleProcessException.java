package com.example.polarionprocessor.service;

/**
 * Domain exception carrying stable error codes for API responses.
 */
public class ModuleProcessException extends RuntimeException {

    /** Stable machine-readable error code. */
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
