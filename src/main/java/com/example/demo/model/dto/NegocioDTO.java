package com.example.demo.model.dto;

import java.io.Serializable;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.example.demo.repository.entity.Gerente;
import com.example.demo.repository.entity.Negocio;
import com.example.demo.repository.entity.Servicio;

import lombok.Data;

@Data
public class NegocioDTO implements Serializable {

	private static final long serialVersionUID = 1L;
	private Long id;
	private String nombre;
	private String correoElec;
	private String telfContacto;
	private LocalTime horaApertura;
	private LocalTime horaCierre;
	private GerenteDTO gerenteDTO;
	private List<ServicioDTO> listaServiciosDTO;

	public NegocioDTO() {
		this.gerenteDTO = new GerenteDTO();
		this.listaServiciosDTO = new ArrayList<ServicioDTO>();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NegocioDTO other = (NegocioDTO) obj;
		return Objects.equals(id, other.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	// TODO: CAMBIAR EL MANEJO DE LOS SERVICIOS
	public static NegocioDTO convertToDTO(Negocio negocio, GerenteDTO gerenteDTO, List<ServicioDTO> listaServiciosDTO) {
		NegocioDTO dto = new NegocioDTO();

		dto.setId(negocio.getId());
		dto.setNombre(negocio.getNombre());
		dto.setCorreoElec(negocio.getCorreoElec());
		dto.setTelfContacto(negocio.getTelfContacto());
		dto.setHoraApertura(negocio.getHoraApertura());
		dto.setHoraCierre(negocio.getHoraCierre());

		// Asignación de dependencias (relaciones N:1 y 1:N)
		dto.setGerenteDTO(gerenteDTO);
		dto.setListaServiciosDTO(listaServiciosDTO);

		return dto;
	}

	// TODO: CAMBIAR EL MANEJO DE LOS SERVICIOS
	// La lista de Servicios (relación 1:N) no se mapea en el convertToEntity
	// para evitar la gestión compleja de colecciones en el mapeo básico.
	public static Negocio convertToEntity(NegocioDTO negocioDTO, Gerente gerente, List<Servicio> listaServicios) {
		Negocio negocio = new Negocio();

		negocio.setId(negocioDTO.getId());
		negocio.setNombre(negocioDTO.getNombre());
		negocio.setCorreoElec(negocioDTO.getCorreoElec());
		negocio.setTelfContacto(negocioDTO.getTelfContacto());
		negocio.setHoraApertura(negocioDTO.getHoraApertura());
		negocio.setHoraCierre(negocioDTO.getHoraCierre());

		// Asignación de la Entidad completa (FK)
		negocio.setGerente(gerente);
		negocio.setListaServicios(listaServicios);
		return negocio;
	}

}
