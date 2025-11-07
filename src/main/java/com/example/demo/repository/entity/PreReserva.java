package com.example.demo.repository.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;

@Entity
@Data
@AllArgsConstructor
@Table(name = "pre_reserva")
public class PreReserva {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// Token de verificación
	@Column(unique = true, nullable = false)
	private String token;

	// Fecha de expiración para la solicitud
	@Column(nullable = false)
	private LocalDateTime fechaExpiracion;

	// Datos de la reserva
	@Column(nullable = false)
	private Long idServicio;

	@Column(nullable = false)
	private LocalDateTime fechaInicio;

	@Column(nullable = false)
	private LocalDateTime fechaFin;

	// Datos del cliente
	@Column(nullable = false)
	private String nombreCliente;
	@Column(nullable = false)
	private String correoElec;
	private String telf;
	@Column(name = "pass_hash")
	private String pass;

	public PreReserva() {

	}
}
