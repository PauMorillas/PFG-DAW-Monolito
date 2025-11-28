package com.example.demo.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.example.demo.exception.EmailAlreadyInUseException;
import com.example.demo.model.dto.DominioDTO;
import com.example.demo.model.dto.GerenteDTO;
import com.example.demo.model.dto.NegocioDTO;
import com.example.demo.model.dto.ServicioDTO;
import com.example.demo.repository.dao.GerenteRepository;
import com.example.demo.repository.dao.NegocioRepository;
import com.example.demo.repository.entity.Gerente;
import com.example.demo.repository.entity.Negocio;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class NegocioServiceImpl implements NegocioService {

    @Autowired
    private NegocioRepository negocioRepository;

    @Autowired
    private GerenteRepository gerenteRepository;

    @Autowired
    private DominioService dominioService;

    @Override
    public NegocioDTO findById(Long id) {
        Negocio negocio = negocioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Negocio no encontrado"));

        // Convertimos el gerente a DTO (solo datos básicos, sin lista de negocios)
        GerenteDTO gerenteDTO = GerenteDTO.convertToDTO(negocio.getGerente());

        // Mapear la lista de servicios del negocio
        List<ServicioDTO> listaServiciosDTO = new ArrayList<>();
        listaServiciosDTO = negocio.getListaServicios().stream()
                .map(servicio -> ServicioDTO.convertToDTO(servicio, null, null))
                .collect(Collectors.toList());

        // Mapear la lista de dominios
        List<DominioDTO> listaDominiosDTO = negocio.getListaDominios()
                .stream()
                .map(d -> DominioDTO.convertToDTO(d, null))
                .collect(Collectors.toList());

        // Devolvemos el DTO final con la lista de servicios mapeada
        return NegocioDTO.convertToDTO(negocio, gerenteDTO, listaServiciosDTO, listaDominiosDTO);
        // TODO: MAPEAR LA LISTA DE DOMINIOS
    }

    @Override
    public void deleteById(Long id) {
        negocioRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void save(NegocioDTO negocioDTO) {
        // 1. Obtener gerente
        Gerente gerente = gerenteRepository.findByEmail(negocioDTO.getCorreoGerente())
                .orElseThrow(() -> new RuntimeException("Gerente no encontrado"));

        try {
            // 2. Convertir el NegocioDTO en entidad SIN LISTA DE SERVICIOS NI DOMINIOS
            // TODAVÍA
            Negocio negocio = NegocioDTO.convertToEntity(negocioDTO, gerente, null, null);

            if (negocioRepository.existsByCorreoElec(negocioDTO.getCorreoElec())) {
                throw new EmailAlreadyInUseException("El correo ya está registrado.");
            }

            // 4. Guardar el negocio (para generar el ID)
            negocio = negocioRepository.save(negocio);

            // 5. Guardar los dominios y asignarles el negocio
            dominioService.saveAll(negocioDTO.getListaDominiosDTO(), negocio);

        } catch (DataIntegrityViolationException e) {

            // Esta excepción llega cuando MySQL lanza "Duplicate entry..."
            if (e.getCause() instanceof org.hibernate.exception.ConstraintViolationException) {
                throw new EmailAlreadyInUseException("El correo ya está en uso por otro negocio");
            }

            throw e; // otras violaciones de integridad
        }
    }

    @Override
    @Transactional
    public void update(NegocioDTO negocioDTO) {
        Optional<Negocio> negocioOpt = negocioRepository.findById(negocioDTO.getId());
        negocioOpt.orElseThrow(() -> new RuntimeException("Negocio no encontrado"));
        try {
            Negocio negocioExistente = negocioOpt.get();

            Negocio negocioActualizado = NegocioDTO.convertToEntity(
                    negocioDTO,
                    negocioExistente.getGerente(),
                    negocioExistente.getListaServicios(),
                    negocioExistente.getListaDominios());

            // PRIMERO Actualizamos los dominios usando DominioService, si hay errores se
            // parará el flujo, de lo contrario guardará en la BD y fallará silenciosamente
            dominioService.updateAll(negocioDTO.getListaDominiosDTO(), negocioActualizado);
            negocioRepository.save(negocioActualizado);
        } catch (DataIntegrityViolationException e) {

            // Esta excepción llega cuando MySQL lanza "Duplicate entry..."
            if (e.getCause() instanceof org.hibernate.exception.ConstraintViolationException) {
                throw new EmailAlreadyInUseException("El correo ya está en uso por otro negocio");
            }

            throw e; // otras violaciones de integridad
        }
    }
}
