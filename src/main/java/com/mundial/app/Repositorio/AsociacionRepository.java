package com.mundial.app.Repositorio;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.mundial.app.Entidades.Asociacion;

public interface AsociacionRepository extends MongoRepository<Asociacion, String> {
}
