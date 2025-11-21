package com.example.demo.service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.model.Estado;
import com.example.demo.model.Mail;
import com.example.demo.model.dto.ClienteDTO;
import com.example.demo.model.dto.EventoCalendarioDTO;
import com.example.demo.model.dto.ReservaRequestDTO;
import com.example.demo.model.dto.ServicioDTO;
import com.example.demo.repository.dao.PreReservaRepository;
import com.example.demo.repository.dao.ReservaRepository;
import com.example.demo.repository.dao.ServicioRepository;
import com.example.demo.repository.entity.Cliente;
import com.example.demo.repository.entity.PreReserva;
import com.example.demo.repository.entity.Reserva;
import com.example.demo.repository.entity.Servicio;

import jakarta.persistence.EntityNotFoundException;

@Service
public class ReservaServiceImpl implements ReservaService {
	@Autowired
	private ReservaRepository reservaRepository;
	@Autowired
	private PreReservaRepository preReservaRepository;
	@Autowired
	private ClienteService clienteService;
	@Autowired
	private ServicioService servicioService;
	@Autowired
	private ServicioRepository servicioRepository;
	@Autowired
	private MailService mailService;

	private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
	private static final long EXPIRATION_MINUTES = 30; // Token válido por 30 minutos

	// =======================================================
	// I. CREACIÓN DE PRE-RESERVA (CONCURRENCIA CONTROLADA)
	// =======================================================

	// ANOTACIÓN CLAVE: SERIALIZABLE previene que dos transacciones concurrentes
	// lean el mismo slot como disponible antes de que la primera lo bloquee.
	@Transactional(isolation = Isolation.SERIALIZABLE)
	public void crearPreReserva(ReservaRequestDTO reservaRequestDTO) throws ResponseStatusException {

		// 1. Parsear y Estandarizar Fechas para manejar el +02:00
		OffsetDateTime startOffset = OffsetDateTime.parse(reservaRequestDTO.getFechaInicio(), ISO_FORMATTER);
		OffsetDateTime endOffset = OffsetDateTime.parse(reservaRequestDTO.getFechaFin(), ISO_FORMATTER);

		// Convertir a LocalDateTime (eliminando el offset, pero manteniendo la hora
		// local del usuario)
		LocalDateTime start = startOffset.toLocalDateTime();
		LocalDateTime end = endOffset.toLocalDateTime();

		// 2. Validación de disponibilidad
		// Lanza una excepción si el slot ya está ocupado (por reserva activa o pre-reserva vigente).
		validarDisponibilidad(reservaRequestDTO.getIdServicio(), start, end);

		// 3. Crear y guardar la pre-reserva
		PreReserva preReserva = buildPreReserva(reservaRequestDTO, start, end);
		preReserva = preReservaRepository.save(preReserva);

		// 4. Envío del correo
		enviarMailConfirmacion(preReserva);
	}


	// =======================================================
	// II. LÓGICA DE VALIDACIÓN Y EXCEPCIONES
	// =======================================================

	/**
	 * Verifica si hay reservas o pre-reservas solapadas en el slot solicitado.
	 * 
	 * @throws ResponseStatusException con HttpStatus.CONFLICT si se encuentra
	 *                                 solapamiento.
	 */
	private void validarDisponibilidad(Long idServicio, LocalDateTime start, LocalDateTime end)
			throws ResponseStatusException {

		// 1. Contar solapamientos de Reservas ACTIVAS (confirmadas)
		long reservasSolapadas = reservaRepository.countActiveOverlappingReserva(idServicio, start, end, Estado.ACTIVA);

		if (reservasSolapadas > 0) {
			// Usamos ResponseStatusException con un estado HTTP 409 para el Controller
			throw new ResponseStatusException(HttpStatus.CONFLICT,
					"El turno ya ha sido reservado y no está disponible.");
		}

		// 2. Contar solapamientos de Pre-Reservas (vigentes)
		long preReservasSolapadas = preReservaRepository.countOverlappingPreReserva(idServicio, start, end,
				LocalDateTime.now());

		if (preReservasSolapadas > 0) {
			throw new ResponseStatusException(HttpStatus.CONFLICT,
					"El turno está temporalmente reservado. Inténtalo de nuevo en unos minutos.");
		}
	}

	@Transactional
	public Reserva confirmarReserva(String token) throws EntityNotFoundException {

		// 1. VERIFICACIÓN Y DATOS de la pre-Reserva

		// Usaremos una excepcion para la búsqueda del token
		PreReserva preReserva = preReservaRepository.findByToken(token)
				.orElseThrow(() -> new EntityNotFoundException("Token de confirmación inválido o ya validado."));

		// Chequear Expiración
		if (preReserva.getFechaExpiracion().isBefore(LocalDateTime.now())) {
			preReservaRepository.delete(preReserva); // Limpiamos el registro caducado
			throw new IllegalArgumentException("El link de confirmación ha expirado.");
		}

		// 2. OBTENER O CREAR CLIENTE (El Upsert)

		// TODO: Creamos un DTO temporal para pasar los datos al ClienteService
		ReservaRequestDTO tempDto = new ReservaRequestDTO();
		tempDto.setCorreoElec(preReserva.getCorreoElec());
		tempDto.setNombreCliente(preReserva.getNombreCliente());
		tempDto.setTelf(preReserva.getTelf());

		// Obtenemos la entidad Cliente persistida
		ClienteDTO clienteDTO = clienteService.findOrCreate(tempDto);
		// 3. OBTENER EL SERVICIODTO
		ServicioDTO servicioDTO = servicioService.findById(preReserva.getIdServicio());

		Cliente cliente = ClienteDTO.convertToEntity(clienteDTO);
		Servicio servicio = ServicioDTO.convertToEntity(servicioDTO, null, null);

		// 4. Construir la entidad Reserva
		Reserva reserva = new Reserva();
		// Establecer las relaciones (JPA/Hibernate usa estas para mapear)
		reserva.setCliente(cliente);
		reserva.setServicio(servicio);

		// Establecer el resto de los datos
		reserva.setFechaInicio(preReserva.getFechaInicio());
		reserva.setFechaFin(preReserva.getFechaFin());
		reserva.setEstado(Estado.ACTIVA);

		// 5. PERSISTIR y LIMPIAR
		Reserva reservaConfirmada = reservaRepository.save(reserva);
		preReservaRepository.delete(preReserva); // Eliminamos la solicitud temporal

		return reservaConfirmada;
	}

	// Devuelve las reservas de un determinado servicio
	@Override
	public List<EventoCalendarioDTO> getReservasByServicioId(Long idServicio) {
		// 1. Obtener el ID del Negocio asociado al servicio
		Long idNegocio = servicioRepository.findIdNegocioByServicioId(idServicio)
				.orElseThrow(() -> new EntityNotFoundException("Servicio no encontrado."));
		
		String colorEvento = "#3B83BD";
		
		// Una vez sabemos a que negocio pertenece el servicio, podemos obtener sus reservas
		return reservaRepository.findAllByNegocioId(idNegocio, Estado.ACTIVA).stream()
				.map(r -> EventoCalendarioDTO.convertToEvento(r, colorEvento)).toList();
	}

	@Override
    public List<EventoCalendarioDTO> getReservasByNegocioId(Long idNegocio) {
        String colorEvento = "#3B83BD"; // o cambiar según lógica

        return reservaRepository.findAllByNegocioId(idNegocio, Estado.ACTIVA)
                .stream()
                .map(reserva -> EventoCalendarioDTO.convertToEvento(reserva, colorEvento))
                .toList();
    }

	// =======================================================
	// III. MÉTODOS AUXILIARES
	// =======================================================

	private PreReserva buildPreReserva(ReservaRequestDTO dto, LocalDateTime start, LocalDateTime end) {
		PreReserva preReserva = new PreReserva();
		preReserva.setIdServicio(dto.getIdServicio());
		preReserva.setFechaInicio(start);
		preReserva.setFechaFin(end);
		preReserva.setNombreCliente(dto.getNombreCliente());
		preReserva.setCorreoElec(dto.getCorreoElec());
		preReserva.setTelf(dto.getTelf());
		preReserva.setToken(UUID.randomUUID().toString());
		preReserva.setFechaExpiracion(LocalDateTime.now().plus(EXPIRATION_MINUTES, ChronoUnit.MINUTES));

		return preReserva;
	}

	private String buildConfirmationLink(String token) {
		// TODO: **IMPORTANTE:** Reemplaza baseUrl con la URL real de producción
		String baseUrl = "http://localhost:8081";
		return baseUrl + "/public/reservas/confirmar?token=" + token;
	}

	private void enviarMailConfirmacion(PreReserva preReserva) {
		String link = buildConfirmationLink(preReserva.getToken());

		// Formateamos fechas a algo legible para humanos
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

		String mensaje = "Hola " + preReserva.getNombreCliente() + ",\n\n"
				+ "Hemos recibido tu solicitud de reserva para las siguientes fechas:\n" + "🗓 Desde: "
				+ preReserva.getFechaInicio().format(formatter) + "\n" + "🕓 Hasta: "
				+ preReserva.getFechaFin().format(formatter) + "\n\n"
				+ "Para confirmar tu reserva, por favor haz clic en el siguiente enlace:\n\n" + link + "\n\n"
				+ "⚠️ Este enlace es válido durante los próximos " + EXPIRATION_MINUTES + " minutos.\n\n"
				+ "Si no has solicitado esta reserva, simplemente ignora este mensaje.\n\n"
				+ "Gracias por confiar en nosotros,\n" + "El equipo de Gestión de Reservas";

		Mail mail = new Mail(preReserva.getCorreoElec(), "Confirma tu Reserva", mensaje);

		mailService.enviarMail(mail);
	}
}
