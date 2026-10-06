package com.mundial.app.Repositorio;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.mundial.app.Entidades.Accion;

public interface AccionRepository extends MongoRepository<Accion, String> {
}
