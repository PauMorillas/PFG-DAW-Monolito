package com.example.demo.repository.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.repository.entity.Gerente;

@Repository
public interface GerenteRepository extends JpaRepository<Gerente, Long> {
	Optional<Gerente> findByEmail(String correoElec);
}
