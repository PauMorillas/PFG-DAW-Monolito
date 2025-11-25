package com.example.demo.model.dto;

import java.io.Serializable;
import java.time.ZoneId;
import java.time.ZonedDateTime;
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
	private Long id; // ID de la reserva en mi sistema para referenciarla

	public EventoCalendarioDTO(String title, String start, String end, String color) {
		this.title = title;
		this.start = start;
		this.end = end;
		this.color = color;
	}

	public EventoCalendarioDTO() {

	}

	public static EventoCalendarioDTO convertToEvento(Reserva reserva, String color) {
		// 1. Define la zona horaria del negocio a España
		ZoneId businessZone = ZoneId.of("Europe/Madrid");

		// 2. Convierte el LocalDateTime (que es ambiguo) a un ZonedDateTime
		ZonedDateTime startInMadrid = reserva.getFechaInicio().atZone(businessZone);
		ZonedDateTime endInMadrid = reserva.getFechaFin().atZone(businessZone);

		// 3. Usa un formateador que incluya el offset para mayor claridad en el JSON
		// Ej: 2025-10-10T10:00:00+02:00
		DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

		EventoCalendarioDTO dto = new EventoCalendarioDTO();
		dto.setTitle(reserva.getServicio().getTitulo());
		dto.setStart(startInMadrid.format(formatter)); // Ej: 2025-10-10T10:00:00+02:00
		dto.setEnd(endInMadrid.format(formatter)); // Ej: 2025-10-10T12:00:00+02:00
		dto.setColor(color);
		dto.setId(reserva.getId());

		return dto;
	}
}
