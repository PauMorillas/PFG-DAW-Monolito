package com.example.demo.service;

import com.example.demo.model.dto.ClienteDTO;
import com.example.demo.model.dto.LoginResponseDTO;
import com.example.demo.model.dto.ReservaRequestDTO;


public interface ClienteService {
	ClienteDTO findOrCreate(ReservaRequestDTO request);

	ClienteDTO findByEmailAndPass(String email, String pass);

	void guardarCliente(ClienteDTO clienteDTO);
}
