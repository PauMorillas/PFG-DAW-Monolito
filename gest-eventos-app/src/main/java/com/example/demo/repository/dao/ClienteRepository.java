package com.example.demo.repository.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.repository.entity.Cliente;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
	public Optional<Cliente> findByEmail(String email);
}
