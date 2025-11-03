package com.example.demo.repository.entity;

import java.time.LocalDateTime;

import com.example.demo.model.Estado; // Asumo que Estado.java existe

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Entity
@Data
public class Reserva {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "fecha_inicio", nullable = false)
	private LocalDateTime fechaInicio;

	@Column(name = "fecha_fin", nullable = false)
	private LocalDateTime fechaFin;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Estado estado;

	// Relación N:1 con Cliente. FK: id_cliente
	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "id_cliente", nullable = false)
	private Cliente cliente;

	// Relación N:1 con Servicio. FK: id_servicio
	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "id_servicio", nullable = false)
	private Servicio servicio;

	public Reserva() {
		this.cliente = new Cliente();
		this.servicio = new Servicio();
	}
}
