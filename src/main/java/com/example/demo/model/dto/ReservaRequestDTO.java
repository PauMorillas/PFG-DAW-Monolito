package com.example.demo.model.dto;

import java.io.Serializable;

import lombok.Data;

/**
 * @Class ReservaRequestDTO: DTO para la transferencia de los datos de la
 *        reserva
 */
@Data
public class ReservaRequestDTO implements Serializable {
	private static final long serialVersionUID = 1L;
	// Datos del Slot (String, se convierte a LocalDateTime en el Service)
	private String fechaInicio; // Ejemplo: "2025-10-05T10:00:00"
	private String fechaFin; // Ejemplo: "2025-10-05T11:00:00"

	// Clave de la Reserva (Usamos solo el ID)
	private Long idServicio;

	// Datos del Cliente (se usan para el Upsert)
	private String nombreCliente;
	private String correoElec;
	private String telf;
	private String pass;
}
