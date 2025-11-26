package com.example.demo.service;

import java.util.List;

import com.example.demo.model.dto.DominioDTO;
import com.example.demo.repository.entity.Dominio;
import com.example.demo.repository.entity.Negocio;

public interface DominioService {
    List<String> findAll();

    void guardarDominioEnLaravel(Dominio dominio);

    void saveAll(List<DominioDTO> dominiosDTO, Negocio negocio);

    void updateAll(List<DominioDTO> dominio, Negocio negocio);
}
