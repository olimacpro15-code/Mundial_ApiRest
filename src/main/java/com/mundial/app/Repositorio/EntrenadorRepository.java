package com.mundial.app.Repositorio;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.mundial.app.Entidades.Entrenador;

public interface EntrenadorRepository extends MongoRepository<Entrenador, String> {
}
