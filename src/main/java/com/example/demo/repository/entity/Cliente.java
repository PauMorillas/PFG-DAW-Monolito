package com.example.demo.repository.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "cliente")
public class Cliente {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String nombre;

	@Column(name = "correo_elec", unique = true, nullable = false)
	private String correoElec;

	@Column(unique = true, nullable = false)
	private String telf;

	@Column(name= "pass_hash", nullable = false)
	private String pass;

	// Relación 1:N con Reserva. Un Cliente puede hacer muchas Reservas.
	@OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Reserva> listaReservas;

	public Cliente() {
		this.listaReservas = new ArrayList<Reserva>();
	}
}