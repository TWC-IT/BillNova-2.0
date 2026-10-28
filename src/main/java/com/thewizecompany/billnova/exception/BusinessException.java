package com.thewizecompany.billnova.exception;

public class BusinessException extends RuntimeException {
    private String message;
    public BusinessException(String message) {
        super(message);
    }
}
