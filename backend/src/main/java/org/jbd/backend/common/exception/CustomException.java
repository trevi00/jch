package org.jbd.backend.common.exception;

public class CustomException extends RuntimeException {
    
    private final ErrorCode errorCode;
    private final Object data;
    
    public CustomException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.data = null;
    }
    
    public CustomException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.data = null;
    }
    
    public CustomException(ErrorCode errorCode, String message, Object data) {
        super(message);
        this.errorCode = errorCode;
        this.data = data;
    }
    
    public CustomException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.data = null;
    }
    
    public CustomException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
        this.data = null;
    }
    
    public ErrorCode getErrorCode() {
        return errorCode;
    }
    
    public Object getData() {
        return data;
    }
    
    public int getStatus() {
        return errorCode.getStatus();
    }
    
    public String getCode() {
        return errorCode.getCode();
    }
}