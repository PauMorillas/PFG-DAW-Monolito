package com.example.demo.service;

import com.example.demo.model.dto.GerenteDTO;

public interface GerenteService {
	void save(GerenteDTO gerenteDTO);

	GerenteDTO findByEmail(String email);
}
