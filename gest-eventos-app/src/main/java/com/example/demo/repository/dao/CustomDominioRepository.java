package com.example.demo.repository.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.repository.entity.Dominio;

public interface CustomDominioRepository  extends JpaRepository<Dominio, Long>  {

    Dominio findByDominio(String dominio);

    List<Dominio> findByNegocioId(Long id);
}
