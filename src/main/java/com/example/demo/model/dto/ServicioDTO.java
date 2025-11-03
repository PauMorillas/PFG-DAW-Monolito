package com.example.demo.model.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.example.demo.repository.entity.Negocio;
import com.example.demo.repository.entity.Reserva;
import com.example.demo.repository.entity.Servicio;

import lombok.Data;

@Data
public class ServicioDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long id;
	private String titulo;
	private String descripcion;
	private String ubicacion;
	private LocalDateTime fechaCreacion;
	private int duracionMinutos;
	private NegocioDTO negocioDTO;
	private List<ReservaDTO> listaReservasDTO;

	public ServicioDTO() {
		this.negocioDTO = new NegocioDTO();
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
		ServicioDTO other = (ServicioDTO) obj;
		return Objects.equals(id, other.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	// TODO: segurmanete haya que buscar otra estrategia con la conversion de las
	// reservas a ese servicio
	public static ServicioDTO convertToDTO(Servicio servicio, NegocioDTO negocioDTO,
			List<ReservaDTO> listaReservasDTO) {
		ServicioDTO servicioDTO = new ServicioDTO();

		servicioDTO.setId(servicio.getId());
		servicioDTO.setTitulo(servicio.getTitulo());
		servicioDTO.setDescripcion(servicio.getDescripcion());
		servicioDTO.setUbicacion(servicio.getUbicacion());
		servicioDTO.setFechaCreacion(servicio.getFechaCreacion());
		servicioDTO.setDuracionMinutos(servicio.getDuracionMinutos());
		
		// NOTA: La entidad Negocio (FK) debe ser establecida en el Service Layer, pasando por parámetro el negocio convertido
		servicioDTO.setNegocioDTO(negocioDTO);

		servicioDTO.setListaReservasDTO(listaReservasDTO);

		return servicioDTO;
	}

	// TODO: segurmanete haya que buscar otra estrategia con la conversion de las
	// reservas a ese servicio
	public static Servicio convertToEntity(ServicioDTO servicioDTO, Negocio negocio, List<Reserva> listaReservas) {
        Servicio servicio = new Servicio();
        
        servicio.setId(servicioDTO.getId());
        servicio.setTitulo(servicioDTO.getTitulo());
        servicio.setDescripcion(servicioDTO.getDescripcion());
        servicio.setUbicacion(servicioDTO.getUbicacion());
        servicio.setFechaCreacion(servicioDTO.getFechaCreacion());
        servicio.setDuracionMinutos(servicioDTO.getDuracionMinutos());
        
        // NOTA: La entidad Negocio (FK) debe ser establecida en el Service Layer:
        servicio.setNegocio(negocio);
        servicio.setListaReservas(listaReservas);

        return servicio;
	}

}
