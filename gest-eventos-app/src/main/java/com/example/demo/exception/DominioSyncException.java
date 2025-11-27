package com.example.demo.exception;

public class DominioSyncException extends RuntimeException{
    private static final long serialVersionUID = 1L;
    private String dominio;

    public DominioSyncException(String message, String dominio) {
        super(message);
        this.dominio = dominio;
    }

    public String getDominio() {
        return dominio;
    }
}
