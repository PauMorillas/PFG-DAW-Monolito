package com.example.demo.service;

import java.util.List;

import com.example.demo.model.dto.EventoCalendarioDTO;
import com.example.demo.model.dto.ReservaRequestDTO;
import com.example.demo.repository.entity.Reserva;

public interface ReservaService {

	void crearPreReserva(ReservaRequestDTO dto);

	Reserva confirmarReserva(String token);

	List<EventoCalendarioDTO> getReservasByServicioId(Long idServicio);

    List<EventoCalendarioDTO> getReservasByNegocioId(Long idNegocio);
}
