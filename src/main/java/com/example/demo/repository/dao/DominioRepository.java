package com.example.demo.repository.dao;

import java.util.List;

public interface DominioRepository {

	List<String> findAllDomains();
	// TODO: extends JpaRepository<Dominio, Long>
}
