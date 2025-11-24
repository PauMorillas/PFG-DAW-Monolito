package com.example.demo.model.dto;

import lombok.Data;

// DTO para la configuración del calendario en JS según los datos del Negocio y Servicio
@Data
public class ServicioConfigDTO {
	private Long idServicio;
	private int duracionMinutos;
	private String horaApertura;
	private String horaCierre;
	private String diasApertura;

	public ServicioConfigDTO(Long id, int duracionMinutos, String horaApertura, String horaCierre, String diasApertura) {
		this.idServicio = id;
		this.duracionMinutos = duracionMinutos;
		this.horaApertura = horaApertura;
		this.horaCierre = horaCierre;
		this.diasApertura = diasApertura;
	}
}
