package com.example.demo.model.dto;

import java.io.Serializable;
import java.time.format.DateTimeFormatter;

import com.example.demo.repository.entity.Reserva;

import lombok.Data;

// DTO para mapear los campos que la API de FullCalendar espera
@Data
public class EventoCalendarioDTO implements Serializable {

	private static final long serialVersionUID = 1L;
	private String title; // Título del evento (ej: "Reservado" o el nombre del cliente si es privado)
	private String start; // Fecha y hora de inicio (Formato ISO 8601: YYYY-MM-DDTTHH:mm:ss)
	private String end; // Fecha y hora de fin
	private String color; // Opcional: Color para pintar el bloque
	private Long idReserva; // ID de la reserva en mi sistema para referenciarla

	public EventoCalendarioDTO(String title, String start, String end, String color) {
		this.title = title;
		this.start = start;
		this.end = end;
		this.color = color;
	}

	public EventoCalendarioDTO() {

	}

	public static EventoCalendarioDTO convertToEvento(Reserva reserva, String color) {
		// Definir el formateador ISO 8601, el estándar de FullCalendar
		// Usamos el formato que incluye zona horaria (offset) o el formato local,
		// pero asegurando que no haya nanosegundos si Reserva.fechaInicio es
		// LocalDateTime.
		DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

		EventoCalendarioDTO dto = new EventoCalendarioDTO();
		dto.setTitle(reserva.getServicio().getTitulo());
		dto.setStart(reserva.getFechaInicio().format(formatter)); // Ej: 2025-10-10T10:00:00
		dto.setEnd(reserva.getFechaFin().format(formatter)); // Ej: 2025-10-10T12:00:00
		dto.setColor(color);
		dto.setIdReserva(reserva.getId());

		return dto;
	}
}
