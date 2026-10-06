package com.mundial.app.Repositorio;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.mundial.app.Entidades.Club;

public interface ClubRepository extends MongoRepository<Club, String> {
}
