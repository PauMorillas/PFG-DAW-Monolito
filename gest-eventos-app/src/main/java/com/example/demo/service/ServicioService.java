package com.example.demo.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.model.dto.ServicioConfigDTO;
import com.example.demo.model.dto.ServicioDTO;

@Service
public interface ServicioService {
	List<ServicioDTO> getServiciosByNegocio(Long idNegocio);

	ServicioDTO findServicioById(Long idServicio);

	ServicioConfigDTO getConfigReserva(Long idServicio);

	// TODO: métodos CRUD básicos (guardar, actualizar, eliminar)
}
