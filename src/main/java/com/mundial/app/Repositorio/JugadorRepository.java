package com.mundial.app.Repositorio;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.mundial.app.Entidades.Jugador;

public interface JugadorRepository extends MongoRepository<Jugador, String> {
}
