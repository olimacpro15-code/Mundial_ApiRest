package com.mundial.app.Repositorio;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.mundial.app.Entidades.Estadio;

public interface EstadioRepository extends MongoRepository<Estadio, String> {
}
