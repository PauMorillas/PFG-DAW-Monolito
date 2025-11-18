package com.example.demo.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.model.dto.NegocioDTO;
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
        // TODO: De momento no se manejan servicios, por implementar
        return NegocioDTO.convertToDTO(
                negocioRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Negocio no encontrado")),
                null,
                null);
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
}
