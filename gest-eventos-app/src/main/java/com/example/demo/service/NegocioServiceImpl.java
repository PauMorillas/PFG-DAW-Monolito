package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.model.dto.NegocioDTO;
import com.example.demo.repository.dao.NegocioRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class NegocioServiceImpl implements NegocioService {

    @Autowired
    private NegocioRepository negocioRepository;
    
    @Override
    public NegocioDTO findById(Long id) {
        // TODO: De momento no se manejan servicios, por implementar
        return NegocioDTO.convertToDTO(negocioRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Negocio no encontrado")),
         null,
         null);
    }

    @Override
    public void deleteById(Long id) {
        negocioRepository.deleteById(id);
    }
    
}
