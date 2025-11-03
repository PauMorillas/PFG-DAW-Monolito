package com.example.demo.model.dto;

import java.time.LocalTime;

import lombok.Data;

@Data
public class ServicioConfigDTO {
	private Long idServicio;
	private int duracionMinutos;
	private LocalTime horaApertura;
	private LocalTime horaCierre;

	public ServicioConfigDTO(Long id, int duracionMinutos, LocalTime horaApertura, LocalTime horaCierre) {
		this.idServicio = id;
		this.duracionMinutos = duracionMinutos;
		this.horaApertura = horaApertura;
		this.horaCierre = horaCierre;
	}

}
