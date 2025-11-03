package com.example.demo.repository.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.repository.entity.Servicio;

public interface ServicioRepository extends JpaRepository<Servicio, Long> {
	// Busca todos los servicios ofrecidos por un Negocio.
	// Se usará para listar los servicios en la vista del cliente.
	List<Servicio> findAllByNegocio_Id(Long idNegocio);

	// Consulta para obtener solo el ID del negocio a partir del ID del servicio
	@Query("SELECT s.negocio.id FROM Servicio s WHERE s.id = :idServicio")
	Optional<Long> findIdNegocioByServicioId(@Param("idServicio") Long idServicio);
}
