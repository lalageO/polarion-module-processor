package com.example.polarionprocessor.service.shared;

/**
 * 业务异常，携带用于 API 响应的稳定错误码。
 */
public class ModuleProcessException extends RuntimeException {

    /** 稳定的机器可读错误码。 */
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
