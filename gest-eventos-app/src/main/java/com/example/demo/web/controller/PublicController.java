package com.example.demo.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;

import com.example.demo.model.dto.ReservaRequestDTO;
import com.example.demo.repository.entity.Reserva;
import com.example.demo.service.ReservaService;
import com.example.demo.service.ServicioService;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;

// Se agrupan todas las rutas públicas bajo un mismo prefijo de clase /public
@Controller
@RequestMapping("/public")
@Slf4j
public class PublicController {

	@Autowired
	private ServicioService servicioService;

	@Autowired
	private ReservaService reservaService;

	// TODO: CAMBIAR AL CONTROLLER DE SERVICIOS.
	// Lógica: Mostrar la lista de servicios de un negocio para incrustar.
	@GetMapping("/servicios/{idNegocio}")
	public ModelAndView getServiciosByNegocioId(@PathVariable Long idNegocio) {
		ModelAndView mav = new ModelAndView("public/services-view");

		mav.addObject("serviciosDTO", servicioService.getServiciosByNegocio(idNegocio));
		return mav;
	}

	@GetMapping("/calendario/{idServicio}")
	public ModelAndView renderCalendarByServicio(@PathVariable Long idServicio) {
		// Lógica: Mostrar el calendario
		ModelAndView mav = new ModelAndView("public/calendar-view");
		return mav;
	}

	@PostMapping("/reservas/crear") // Endpoint: /public/reservas/crear
	public ResponseEntity<String> crearPreReserva(@RequestBody ReservaRequestDTO reservaRequestDTO) {
	    try {
			if (reservaRequestDTO != null) {
				reservaService.crearPreReserva(reservaRequestDTO);
			}
	        
	        // Devuelve 202 Accepted, indicando que la solicitud ha sido aceptada, 
	        // pero la reserva no está confirmada todavía.
	        return ResponseEntity.status(HttpStatus.ACCEPTED).body("Reserva pendiente de confirmación. Revisa tu correo.");
	        
		} catch (ResponseStatusException e) {
			// Captura las excepciones de concurrencia (409 Conflict)
			return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
		}
		catch (Exception e) {
			// Captura otros errores (ej. 500)
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error al procesar la reserva: " + e.getMessage());
	    }
	}

	@GetMapping("/reservas/confirmar")
	public ModelAndView confirmarReserva(@RequestParam String token) {
		log.info("Intento de confirmación de reserva con token: ", token);

		ModelAndView modelAndView = new ModelAndView("public/booking-confirmation");

		try {
			// Llama al servicio para ejecutar la lógica de Upsert, crear Reserva y limpiar
			// PreReserva
			Reserva reservaConfirmada = reservaService.confirmarReserva(token);

			// En caso de ÉXITO:
			modelAndView.addObject("status", "SUCCESS");
			modelAndView.addObject("mensaje", "¡Tu reserva ha sido confirmada exitosamente!");
			modelAndView.addObject("reserva", reservaConfirmada);
			log.info("Reserva confirmada exitosamente.", reservaConfirmada.getId());

		} catch (EntityNotFoundException | IllegalArgumentException e) {
			// Maneja tokens inválidos, expirados o errores de lógica
			// En caso de ERROR LÓGICO:
			modelAndView.addObject("status", "ERROR");
			modelAndView.addObject("mensaje", e.getMessage());
			log.error("Error al confirmar reserva con token:", token, e.getMessage());

		} catch (Exception e) {
			// Maneja otros errores internos
			// En caso de ERROR CRÍTICO:
			modelAndView.addObject("status", "FATAL_ERROR");
			modelAndView.addObject("mensaje", "Ocurrió un error interno del sistema. Inténtelo más tarde.");
			log.error("Error crítico al confirmar reserva con token: ", token, e);
		}

		return modelAndView;
    }

	@GetMapping("/demo")
	public ModelAndView showDemo() {
		ModelAndView mav = new ModelAndView("demo");
		return mav;
	}
}

