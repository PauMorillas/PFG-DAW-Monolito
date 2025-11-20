package com.example.demo.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.model.dto.ServicioConfigDTO;
import com.example.demo.model.dto.ServicioDTO;

@Service
public interface ServicioService {
	List<ServicioDTO> getServiciosByNegocio(Long idNegocio);

	ServicioDTO findById(Long idServicio);

	ServicioConfigDTO getConfigReserva(Long idServicio);

	void update(ServicioDTO servicioDTO);

	void delete(Long idServicio);

	void save(ServicioDTO servicioDTO);
}
