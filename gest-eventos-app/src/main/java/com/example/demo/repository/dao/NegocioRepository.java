package com.example.demo.repository.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.repository.entity.Negocio;

public interface NegocioRepository extends JpaRepository<Negocio, Long> {
	// TODO: Eliminar si no se va a usar
	List<Negocio> findAllByGerente_Id(Long idGerente);
}
