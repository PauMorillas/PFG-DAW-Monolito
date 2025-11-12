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
	private String email;
	private String telf;
	private String pass;
	private String rol;

	private List<ReservaDTO> listaReservasDTO;

	public ClienteDTO() {
		this.listaReservasDTO = new ArrayList<ReservaDTO>();
		this.rol = "CLIENTE";
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
		cliDTO.setEmail(cliente.getEmail());
		cliDTO.setTelf(cliente.getTelf());
		cliDTO.setPass(cliente.getPass());
		cliDTO.setPass(cliente.getPass());

		return cliDTO;
	}

	public static Cliente convertToEntity(ClienteDTO clienteDTO) {
		Cliente cliente = new Cliente();
		cliente.setId(clienteDTO.getId());
		cliente.setNombre(clienteDTO.getNombre());
		cliente.setEmail(clienteDTO.getEmail());
		cliente.setTelf(clienteDTO.getTelf());
		cliente.setPass(clienteDTO.getPass());

		return cliente;
	}

}
