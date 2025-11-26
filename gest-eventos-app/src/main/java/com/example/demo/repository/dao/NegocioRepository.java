package com.example.demo.repository.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.repository.entity.Negocio;

public interface NegocioRepository extends JpaRepository<Negocio, Long> {
	// Solo se usan las CRUD de JpaRepository.
}
