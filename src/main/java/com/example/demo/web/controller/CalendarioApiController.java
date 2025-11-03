package com.example.demo.web.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.dto.EventoCalendarioDTO;
import com.example.demo.model.dto.ServicioConfigDTO;
import com.example.demo.service.ReservaService;
import com.example.demo.service.ServicioService;

@RestController
@RequestMapping("/public/api/calendario")
public class CalendarioApiController {
	@Autowired
	private ReservaService reservaService;

	@Autowired
	private ServicioService servicioService;

	/**
	 * Endpoint llamado por el JS para obtener todos los eventos de un determinado
	 * negocio a traves de un id de servicio en el formato que espera FullCalendar
	 * 
	 */
	@GetMapping("/eventos/{idServicio}")
	public List<EventoCalendarioDTO> getEventos(@PathVariable Long idServicio) {
		return reservaService.getReservasByServicioId(idServicio);
	}

	/**
	 * Endpoint llamado por el JS para obtener toda la configuración del calendario.
	 * 
	 * @param idServicio El ID del servicio solicitado.
	 * @return ServicioConfigDTO con horarios, duración, etc.
	 */
	@GetMapping("/config/{idServicio}")
	public ServicioConfigDTO getCalendarConfig(@PathVariable Long idServicio) {

		// El Service valida que el ID existe, está activo,
		// y pertenece a un negocio válido. Si no, lanzará una excepción.
		ServicioConfigDTO config = servicioService.getConfigReserva(idServicio);

		if (config == null) {
			// Spring Boot mapeará una excepción lanzada aquí (ej:
			// ResourceNotFoundException)
			// a un error HTTP 404/400.
			throw new IllegalArgumentException("Servicio no encontrado o inactivo.");
		}

		return config;
	}
}
