package com.mundial.app.Repositorio;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.mundial.app.Entidades.Competicion;

public interface CompeticionRepository extends MongoRepository<Competicion, String> {
}
