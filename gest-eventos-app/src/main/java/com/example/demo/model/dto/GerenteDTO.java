package com.example.demo.model.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.example.demo.model.Rol;
import com.example.demo.repository.entity.Gerente;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class GerenteDTO implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long id;
	private String nombre;
	@JsonProperty("email")
	private String correoElec;
	private String pass;
	private String telf;
	private Rol rol;
	private List<NegocioDTO> listaNegociosDTO;

	public GerenteDTO() {
		this.listaNegociosDTO = new ArrayList<NegocioDTO>();
		this.rol = Rol.GERENTE;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GerenteDTO other = (GerenteDTO) obj;
		return Objects.equals(id, other.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	// No mostramos el pass por lo que no se incluye en la conversión
	public static GerenteDTO convertToDTO(Gerente gerente) {
		GerenteDTO dto = new GerenteDTO();
		dto.setId(gerente.getId());
		dto.setNombre(gerente.getNombre());
		dto.setCorreoElec(gerente.getEmail());
		dto.setTelf(gerente.getTelf());
		dto.setRol(gerente.getRol());

		return dto;
	}


	public static Gerente convertToEntity(GerenteDTO gerenteDTO) {
		Gerente gerente = new Gerente();
		gerente.setId(gerenteDTO.getId());
		gerente.setNombre(gerenteDTO.getNombre());
		gerente.setEmail(gerenteDTO.getCorreoElec());
		gerente.setPass(gerenteDTO.getPass());
		gerente.setTelf(gerenteDTO.getTelf());
		gerente.setRol(gerenteDTO.getRol());

		return gerente;
	}

}
