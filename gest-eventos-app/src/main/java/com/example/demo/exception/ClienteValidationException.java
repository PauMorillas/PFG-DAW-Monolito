package com.example.demo.exception;

public class ClienteValidationException extends ValidationException {
    private static final long serialVersionUID = 1L;

    public ClienteValidationException(String message) {
        super(message);
    }
}