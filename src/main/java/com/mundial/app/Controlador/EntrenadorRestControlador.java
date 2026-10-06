package com.mundial.app.Controlador;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mundial.app.Entidades.*;
import com.mundial.app.Repositorio.*;

@RestController
@RequestMapping("/api/entrenadores")
public class EntrenadorRestControlador {

    private final EntrenadorRepository entrenadorRepository;
    private final ClubRepository clubRepository;

    public EntrenadorRestControlador(EntrenadorRepository entrenadorRepository,
            ClubRepository clubRepository) {
        this.entrenadorRepository = entrenadorRepository;
        this.clubRepository = clubRepository;
    }

    @GetMapping
    public List<Entrenador> listar() {
        return entrenadorRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Entrenador> obtener(@PathVariable String id) {
        return entrenadorRepository.findById(id).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Entrenador entrenador) {
        entrenador.setId(UUID.randomUUID().toString());
        String error = validar(entrenador);
        if (error != null) {
            return ResponseEntity.badRequest().body(Map.of("error", error));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(entrenadorRepository.save(entrenador));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable String id, @RequestBody Entrenador entrenador) {
        if (!entrenadorRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        entrenador.setId(id);
        String error = validar(entrenador);
        if (error != null) {
            return ResponseEntity.badRequest().body(Map.of("error", error));
        }
        return ResponseEntity.ok(entrenadorRepository.save(entrenador));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable String id) {
        if (!entrenadorRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        String error = dependientes(id);
        if (error != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", error));
        }
        entrenadorRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/club")
    public ResponseEntity<Club> obtenerClub(@PathVariable String id) {
        return clubRepository.findAll().stream()
                .filter(x -> x.getEntrenador() != null && id.equals(x.getEntrenador().getId()))
                .findFirst().map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    private String validar(Entrenador entrenador) {
        if (entrenador.getNombre() == null || entrenador.getNombre().isBlank()) {
            return "El campo «Nombre» es obligatorio.";
        }
        if (entrenador.getNacionalidad() == null || entrenador.getNacionalidad().isBlank()) {
            return "El campo «Nacionalidad» es obligatorio.";
        }
        return null;
    }

    private String dependientes(String id) {
        if (clubRepository.findAll().stream().anyMatch(x -> x.getEntrenador() != null && id.equals(x.getEntrenador().getId()))) {
            return "No se puede eliminar: hay clubes que dependen de este registro.";
        }
        return null;
    }
}
