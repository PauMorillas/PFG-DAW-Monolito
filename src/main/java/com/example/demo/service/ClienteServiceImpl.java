package com.example.demo.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.model.dto.ClienteDTO;
import com.example.demo.model.dto.ReservaRequestDTO;
import com.example.demo.repository.dao.ClienteRepository;
import com.example.demo.repository.entity.Cliente;

@Service
public class ClienteServiceImpl implements ClienteService {
	@Autowired
	private ClienteRepository clienteRepository;

	/**
	 * Busca un cliente existente por email. Si no existe, lo crea y lo guarda. Se
	 * implementa así para simplificar la lógica de Reserva.
	 * 
	 * @param request DTO de solicitud de reserva que contiene los datos del
	 *                cliente.
	 * @return ClienteDTO persistido (existente o nuevo).
	 */
	public ClienteDTO findOrCreate(ReservaRequestDTO request) {

		// 1. Intentar buscar por correo electrónico
		Optional<Cliente> cliExistente = clienteRepository.findByCorreoElec(request.getCorreoElec());

		if (cliExistente.isPresent()) {
			// Cliente encontrado, lo devolvemos para usarlo en la Reserva.
			return ClienteDTO.convertToDTO(cliExistente.get());
		}

		// 2. Cliente no encontrado, creamos uno nuevo
		Cliente cliNuevo = new Cliente();
		cliNuevo.setNombre(request.getNombreCliente());
		cliNuevo.setCorreoElec(request.getCorreoElec());
		cliNuevo.setTelf(request.getTelf());

		// 3. Guardamos el nuevo cliente y lo devolvemos
		return ClienteDTO.convertToDTO(clienteRepository.save(cliNuevo));
	}
}
