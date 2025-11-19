package com.example.demo.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.model.dto.GerenteDTO;
import com.example.demo.model.dto.NegocioDTO;
import com.example.demo.model.dto.ServicioDTO;
import com.example.demo.repository.dao.GerenteRepository;
import com.example.demo.repository.dao.NegocioRepository;
import com.example.demo.repository.entity.Gerente;
import com.example.demo.repository.entity.Negocio;
import com.example.demo.repository.entity.Servicio;

import jakarta.persistence.EntityNotFoundException;

@Service
public class NegocioServiceImpl implements NegocioService {

    @Autowired
    private NegocioRepository negocioRepository;

    @Autowired
    private GerenteRepository gerenteRepository;

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

        // Devolvemos el DTO final con la lista de servicios mapeada
        return NegocioDTO.convertToDTO(negocio, gerenteDTO, listaServiciosDTO);
    }

    @Override
    public void deleteById(Long id) {
        negocioRepository.deleteById(id);
    }

    @Override
    public void save(NegocioDTO negocioDTO) {
        Gerente gerente = gerenteRepository.findByEmail(negocioDTO.getCorreoGerente())
                .orElseThrow(() -> new RuntimeException("Gerente no encontrado"));

        List<Servicio> servicios = new ArrayList<>();

        Negocio negocio = NegocioDTO.convertToEntity(negocioDTO, gerente, servicios);

        negocioRepository.save(negocio);
    }

    @Override
    public void update(NegocioDTO negocioDTO) {
        Optional<Negocio> negocioOpt = negocioRepository.findById(negocioDTO.getId());
        negocioOpt.orElseThrow(() -> new RuntimeException("Negocio no encontrado"));

        Negocio negocio = NegocioDTO.convertToEntity(negocioDTO, negocioOpt.get().getGerente(), null);
        negocioRepository.save(negocio);
    }
}
