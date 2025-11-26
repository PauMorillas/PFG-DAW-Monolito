package com.example.demo.model.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

import com.example.demo.model.Estado;
import com.example.demo.repository.entity.Reserva;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;

@Data
public class ReservaDTO implements Serializable {
	private static final long serialVersionUID = 1L;
	private Long id;
	private LocalDateTime fechaInicio;
	private LocalDateTime fechaFin;

	@Enumerated(EnumType.STRING)
	private Estado estado;
	private ClienteDTO clienteDTO;
	private ServicioDTO servicioDTO;

	public ReservaDTO() {
		this.clienteDTO = new ClienteDTO();
		this.servicioDTO = new ServicioDTO();
	}

	public static ReservaDTO convertToDTO(Reserva reserva, ClienteDTO clienteDTO, ServicioDTO servicioDTO) {
		ReservaDTO reservaDTO = new ReservaDTO();
		reservaDTO.setId(reserva.getId());
		reservaDTO.setFechaInicio(reserva.getFechaInicio());
		reservaDTO.setFechaFin(reserva.getFechaFin());
		reservaDTO.setEstado(reserva.getEstado());

		reservaDTO.setClienteDTO(clienteDTO);
		reservaDTO.setServicioDTO(servicioDTO);
		return reservaDTO;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ReservaDTO other = (ReservaDTO) obj;
		return Objects.equals(id, other.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

}
