package com.example.demo.repository.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "servicio")
public class Servicio {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String titulo;

	private String descripcion;
	private String ubicacion;

	@Column(name = "fecha_creacion")
	private LocalDateTime fechaCreacion;

	@Column(name = "duracion_min", nullable = false)
	private int duracionMinutos;

	@Column(name = "coste", nullable = false, precision = 10, scale = 2)
    private BigDecimal coste = BigDecimal.ZERO;
	
	// Relación N:1 con Negocio. FK: id_negocio
	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "id_negocio", nullable = false)
	private Negocio negocio;

	// Relación 1:N con Reserva. Un Servicio genera muchas Reservas.
	@OneToMany(mappedBy = "servicio", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Reserva> listaReservas;

	public Servicio() {
		this.negocio = new Negocio();
		this.listaReservas = new ArrayList<Reserva>();
	}
}