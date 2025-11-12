package com.example.demo.repository.entity;

import java.time.LocalTime;
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
@Table(name = "negocio")
public class Negocio {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String nombre;

	@Column(name = "correo_elec")
	private String correoElec;

	@Column(name = "telf_contacto")
	private String telfContacto;

	// Usamos LocalTime para representar solo la hora (apertura/cierre)
	@Column(name = "hora_apertura", nullable = false)
	private LocalTime horaApertura;

	@Column(name = "hora_cierre", nullable = false)
	private LocalTime horaCierre;

	// Relación N:1 con Usuario (Gerente). FK: id_gerente
	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "id_gerente", nullable = false)
	private Gerente gerente;

	// Relación 1:N con Servicio. Un Negocio ofrece muchos Servicios.
	@OneToMany(mappedBy = "negocio", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Servicio> listaServicios;

	public Negocio() {
		this.gerente = new Gerente();
		this.listaServicios = new ArrayList<Servicio>();
	}
}