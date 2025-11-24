package com.example.demo.repository.dao;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.model.Estado;
import com.example.demo.repository.entity.Reserva;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {

	// Nota: Para referenciar un valor de Enum en HQL, se debe usar la ruta completa
	// de la clase y el nombre del valor.
	@Query("SELECT DISTINCT r FROM Reserva r JOIN r.servicio s WHERE s.negocio.id = :idNegocio"
			+ " AND r.estado = :estadoParam")
	List<Reserva> findAllByNegocioId(Long idNegocio, @Param("estadoParam") Estado estado);

	@Query("SELECT r FROM Reserva r WHERE r.servicio.id = :idServicio AND r.estado = :estado")
	List<Reserva> findAllByServicioIdAndEstado(Long idServicio, Estado estado);

	@Query("SELECT COUNT(r) FROM Reserva r WHERE r.servicio.id = :idServicio "
			+ " AND r.estado = :estadoParam"
			+ " AND ((r.fechaInicio < :fechaFin AND r.fechaFin > :fechaInicio))")
	long countActiveOverlappingReserva(@Param("idServicio") Long idServicio,
			@Param("fechaInicio") LocalDateTime fechaInicio,
			@Param("fechaFin") LocalDateTime fechaFin,
			@Param("estadoParam") Estado estado);
}
