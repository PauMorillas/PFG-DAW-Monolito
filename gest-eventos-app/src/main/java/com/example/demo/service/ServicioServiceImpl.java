package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.model.dto.GerenteDTO;
import com.example.demo.model.dto.NegocioDTO;
import com.example.demo.model.dto.ReservaDTO;
import com.example.demo.model.dto.ServicioConfigDTO;
import com.example.demo.model.dto.ServicioDTO;
import com.example.demo.repository.dao.NegocioRepository;
import com.example.demo.repository.dao.ServicioRepository;
import com.example.demo.repository.entity.Negocio;
import com.example.demo.repository.entity.Servicio;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ServicioServiceImpl implements ServicioService {
	@Autowired
	private ServicioRepository servicioRepository;

	@Autowired
	private NegocioRepository negocioRepository;

	@Override
	public List<ServicioDTO> getServiciosByNegocio(Long idNegocio) {

		// 1. Obtener la Entidad Negocio
		Negocio negocio = negocioRepository.findById(idNegocio)
				// Lanza la excepción inmediatamente si no se encuentra
				.orElseThrow(() -> new RuntimeException("No se pudo encontrar el negocio con id: " + idNegocio));

		// 2. Obtener y mapear el gerente
		GerenteDTO gerenteDTO = GerenteDTO.convertToDTO(negocio.getGerente());

		// 3. Mapear el Negocio una sola vez para inyectarlo en cada ServicioDTO
		// Pasamos null como lista de servicios para evitar recursión y confusiones
		// innecesarias.
		NegocioDTO negocioDTO = NegocioDTO.convertToDTO(negocio, gerenteDTO, null);

		// 4. Obtener los Servicios de ese Negocio
		List<Servicio> servicios = servicioRepository.findAllByNegocio_Id(idNegocio);

		// TODO: Hacer la lista de reservas?
		List<ReservaDTO> listaReservasDTO = new ArrayList<>();

		// 6. Mapear los Servicios, inyectando las dependencias
		List<ServicioDTO> serviciosDTO = servicios.stream()
				// Inyectamos el NegocioDTO (que ahora no tiene Servicios anidados)
				.map(s -> ServicioDTO.convertToDTO(s, negocioDTO, listaReservasDTO)).collect(Collectors.toList());

		// 7. Retornamos la lista de servicios. El NegocioDTO ya está contenido en cada
		// uno.
		return serviciosDTO;

	}

	@Override
	public ServicioDTO findById(Long idServicio) {

		// 1. Obtener la entidad Servicio o lanzar una excepción de no encontrado
		Servicio servicio = servicioRepository.findById(idServicio)
				.orElseThrow(() -> new EntityNotFoundException("Servicio no encontrado con ID: " + idServicio));

		// 2. Obtener el Negocio asociado
		// Ya que la entidad Servicio tiene el Negocio como campo podemos hacer
		NegocioDTO negocioDTO = NegocioDTO.convertToDTO(servicio.getNegocio(), null, null);

		// 3. Mapear la entidad a DTO, incluyendo el Negocio
		ServicioDTO servicioDTO = ServicioDTO.convertToDTO(servicio, negocioDTO, null);

		return servicioDTO;
	}

	@Override
	public ServicioConfigDTO getConfigReserva(Long idServicio) {
		// 1. Obtener la información completa del servicio (incluyendo el Negocio
		// asociado)
		ServicioDTO servicioDTO = this.findById(idServicio);

		// TODO: 2. Comprobar la activación (Validación de Negocio)
		// Es CRUCIAL que el servicio esté activo para permitir reservas.
		// if (!servicioDTO.isActivo()) {
		// throw new ValidationException("El servicio no está activo para reservas.");
		// }

		// 3. Extraer la configuración necesaria del DTO
		NegocioDTO negocioDTO = servicioDTO.getNegocioDTO();

		// Si el NegocioDTO es nulo (por error en getServicioById), lanzamos error
		if (negocioDTO == null) {
			throw new RuntimeException("Error interno: Los datos del negocio son requeridos.");
		}

		// 4. Mapear a ServicioConfigDTO 
		ServicioConfigDTO configDTO = new ServicioConfigDTO(servicioDTO.getId(), servicioDTO.getDuracionMinutos(),
				negocioDTO.getHoraApertura(), negocioDTO.getHoraCierre(), negocioDTO.getDiasApertura());

		// 5. Devolver el objeto de configuración (JSON)
		return configDTO;
	}

	@Override
	public void update(ServicioDTO servicioDTO) {

		Servicio servicio = servicioRepository.findById(servicioDTO.getId())
				.orElseThrow(() -> new EntityNotFoundException(
						"No se pudo completar la actualización. Servicio no encontrado con ID: "
								+ servicioDTO.getId()));

		// Obtener el negocio real desde la BD (solo ID es suficiente)
		Negocio negocio = negocioRepository.findById(servicioDTO.getNegocioDTO().getId())
				.orElseThrow(() -> new EntityNotFoundException("Negocio no encontrado con ID: "
						+ servicioDTO.getNegocioDTO().getId()));

		servicioDTO.setFechaCreacion(LocalDateTime.now());

		servicioRepository.save(ServicioDTO.convertToEntity(servicioDTO, negocio, servicio.getListaReservas()));
	}

	@Override
	public void delete(Long idServicio) {
		servicioRepository.deleteById(idServicio);
	}

	@Override
	public void save(ServicioDTO servicioDTO) {
		Optional<Negocio> negocio = negocioRepository.findById(servicioDTO.getNegocioDTO().getId());
		if (negocio.isPresent()) {
			// TODO: Validar info del servicio en el backend antes de guardar
			servicioRepository.save(ServicioDTO.convertToEntity(servicioDTO, negocio.get(), new ArrayList<>()));
		} else {
			throw new EntityNotFoundException("Negocio no encontrado con ID: " + servicioDTO.getNegocioDTO().getId());
		}
	}

}
