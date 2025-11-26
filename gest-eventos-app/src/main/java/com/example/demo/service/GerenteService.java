package com.example.demo.service;

import com.example.demo.model.dto.GerenteDTO;
import com.example.demo.model.dto.LoginRequestDTO;
import com.example.demo.model.dto.LoginResponseDTO;

public interface GerenteService {
	void save(GerenteDTO gerenteDTO);

	void update(GerenteDTO gerenteDTO);

	GerenteDTO findByEmail(String email);

    LoginResponseDTO login(LoginRequestDTO req);
}
