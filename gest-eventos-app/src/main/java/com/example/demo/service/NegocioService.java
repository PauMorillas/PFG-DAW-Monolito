package com.example.demo.service;

import com.example.demo.model.dto.NegocioDTO;

public interface NegocioService {

    NegocioDTO findById(Long id);

    void deleteById(Long id);

    void save(NegocioDTO negocioDTO);

    void update(NegocioDTO negocioDTO);
    
}
