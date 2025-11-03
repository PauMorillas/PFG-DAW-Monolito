package com.example.demo.exception;

// Elegimos extender de RuntimeException (Excepción No Comprobada) para adherirnos a la
// convención de Spring Boot para errores de negocio y validación.

// 1. Integración Futura: Facilita la implementación posterior de un 
// Global Exception Handler (@ControllerAdvice) para rutas de API REST, 
// centralizando el mapeo de errores a respuestas HTTP (e.g., 400 Bad Request).

// NOTA sobre el try-catch: Aunque es una RuntimeException, la usamos dentro del 
// RegistroController con un try-catch intencional para manejar el flujo específico 
// de la interfaz de usuario (devolver el formulario HTML con el mensaje de error).
public class ValidationException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	// Constructor que recibe el mensaje detallado de error
	public ValidationException(String message) {
		super(message);
	}
}
