package com.example.demo.service;

import com.example.demo.model.dto.ClienteDTO;
import com.example.demo.model.dto.ReservaRequestDTO;

public interface ClienteService {
	ClienteDTO findOrCreate(ReservaRequestDTO request);

	boolean guardarCliente(ClienteDTO clienteDTO);
}
