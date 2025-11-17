package com.example.demo.model.dto;

import lombok.Data;

@Data
public class ServicioConfigDTO {
	private Long idServicio;
	private int duracionMinutos;
	private String horaApertura;
	private String horaCierre;

	public ServicioConfigDTO(Long id, int duracionMinutos, String horaApertura, String horaCierre) {
		this.idServicio = id;
		this.duracionMinutos = duracionMinutos;
		this.horaApertura = horaApertura;
		this.horaCierre = horaCierre;
	}
}
