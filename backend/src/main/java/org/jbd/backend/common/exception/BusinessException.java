package org.jbd.backend.common.exception;

public class BusinessException extends CustomException {
    
    public BusinessException(ErrorCode errorCode) {
        super(errorCode);
    }
    
    public BusinessException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
    
    public BusinessException(ErrorCode errorCode, String message, Object data) {
        super(errorCode, message, data);
    }
}