package com.example.demo.repository.dao;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.repository.entity.PreReserva;

public interface PreReservaRepository extends JpaRepository<PreReserva, Long> {

	Optional<PreReserva> findByToken(String token);

	@Query("SELECT COUNT(pr) FROM PreReserva pr WHERE pr.idServicio = :idServicio "
		       + "AND pr.fechaExpiracion > :now "
		       + "AND ((pr.fechaInicio < :fechaFin AND pr.fechaFin > :fechaInicio))")
	long countOverlappingPreReserva(@Param("idServicio") Long idServicio,
			@Param("fechaInicio") LocalDateTime fechaInicio,
			@Param("fechaFin") LocalDateTime fechaFin,
			@Param("now") LocalDateTime now);
}
