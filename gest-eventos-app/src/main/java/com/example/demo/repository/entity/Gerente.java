package com.example.demo.repository.entity;

import java.util.ArrayList;
import java.util.List;

import com.example.demo.model.Rol;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "gerente")
public class Gerente {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String nombre;

	@Column(name = "correo_elec", unique = true, nullable = false)
	private String email;

	@Column(name = "pass_hash", nullable = false)
	private String pass;

	private String telf;

	@Enumerated(EnumType.STRING)
	private Rol rol;

	// Relación 1:N con Negocio. Un Gerente puede serlo de varios negocios.
	@OneToMany(mappedBy = "gerente", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonManagedReference // Para evitar el error de referencias circulares con Negocio
	private List<Negocio> listaNegocios;

	// Constructor sin argumentos requerido por JPA
	public Gerente() {
		this.listaNegocios = new ArrayList<>();
		this.rol = Rol.GERENTE;
	}
}
