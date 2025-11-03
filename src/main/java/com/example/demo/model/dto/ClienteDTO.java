package com.example.demo.model.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.example.demo.repository.entity.Cliente;

import lombok.Data;

@Data
public class ClienteDTO implements Serializable {

	private static final long serialVersionUID = 1L;
	private Long id;
	private String nombre;
	private String correoElec;
	private String telf;
	private List<ReservaDTO> listaReservasDTO;

	public ClienteDTO() {
		this.listaReservasDTO = new ArrayList<ReservaDTO>();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ClienteDTO other = (ClienteDTO) obj;
		return Objects.equals(id, other.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	public static ClienteDTO convertToDTO(Cliente cliente) {
		ClienteDTO cliDTO = new ClienteDTO();
		cliDTO.setId(cliente.getId());
		cliDTO.setNombre(cliente.getNombre());
		cliDTO.setCorreoElec(cliente.getCorreoElec());
		cliDTO.setTelf(cliente.getTelf());

		return cliDTO;
	}

	public static Cliente convertToEntity(ClienteDTO clienteDTO) {
		Cliente cliente = new Cliente();
		cliente.setId(clienteDTO.getId());
		cliente.setNombre(clienteDTO.getNombre());
		cliente.setCorreoElec(clienteDTO.getCorreoElec());
		cliente.setTelf(clienteDTO.getTelf());

		return cliente;
	}

}
