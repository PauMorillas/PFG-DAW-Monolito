package com.example.demo.model.dto;

import java.io.Serializable;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.example.demo.repository.entity.Gerente;
import com.example.demo.repository.entity.Negocio;
import com.example.demo.repository.entity.Servicio;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import lombok.Data;

@Data
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id") // Para evitar el error de referencias circulares
public class NegocioDTO implements Serializable {

	private static final long serialVersionUID = 1L;
	private Long id;
	private String nombre;
	private String correoElec;
	private String telfContacto;
	private String horaApertura; // Formato HH:MM
	private String horaCierre; // Formato HH:MM
	private String diasApertura; // Ejemplo: "1,2,3,4,5"
	private String correoGerente; // Correo del Gerente que vendrá desde Angular
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

	public static NegocioDTO convertToDTO(Negocio negocio, GerenteDTO gerenteDTO, List<ServicioDTO> listaServiciosDTO) {
		NegocioDTO dto = new NegocioDTO();

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
		dto.setId(negocio.getId());
		dto.setNombre(negocio.getNombre());
		dto.setCorreoElec(negocio.getCorreoElec());
		dto.setTelfContacto(negocio.getTelfContacto());
		dto.setHoraApertura(negocio.getHoraApertura().format(formatter));
		dto.setHoraCierre(negocio.getHoraCierre().format(formatter));
		dto.setDiasApertura(negocio.getDiasApertura());

		// Asignación de dependencias (relaciones N:1 y 1:N)
		dto.setGerenteDTO(gerenteDTO);
		dto.setListaServiciosDTO(listaServiciosDTO);

		return dto;
	}

	// La lista de Servicios (relación 1:N) no se mapea en el convertToEntity
	// para evitar la gestión compleja de colecciones en el mapeo básico y referencias circulares.
	public static Negocio convertToEntity(NegocioDTO negocioDTO, Gerente gerente, List<Servicio> listaServicios) {
		Negocio negocio = new Negocio();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

		negocio.setId(negocioDTO.getId());
		negocio.setNombre(negocioDTO.getNombre());
		negocio.setCorreoElec(negocioDTO.getCorreoElec());
		negocio.setTelfContacto(negocioDTO.getTelfContacto());
		negocio.setHoraApertura(LocalTime.parse(negocioDTO.getHoraApertura(), formatter));
		negocio.setHoraCierre(LocalTime.parse(negocioDTO.getHoraCierre(), formatter));
		negocio.setDiasApertura(negocioDTO.getDiasApertura());

		// Asignación de la Entidad completa (FK)
		negocio.setGerente(gerente);
		negocio.setListaServicios(listaServicios);
		return negocio;
	}

}
