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
@RequestMapping("/api/estadios")
public class EstadioRestControlador {

    private final EstadioRepository estadioRepository;
    private final ClubRepository clubRepository;

    public EstadioRestControlador(EstadioRepository estadioRepository,
            ClubRepository clubRepository) {
        this.estadioRepository = estadioRepository;
        this.clubRepository = clubRepository;
    }

    @GetMapping
    public List<Estadio> listar() {
        return estadioRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Estadio> obtener(@PathVariable String id) {
        return estadioRepository.findById(id).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Estadio estadio) {
        estadio.setId(UUID.randomUUID().toString());
        String error = validar(estadio);
        if (error != null) {
            return ResponseEntity.badRequest().body(Map.of("error", error));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(estadioRepository.save(estadio));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable String id, @RequestBody Estadio estadio) {
        if (!estadioRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        estadio.setId(id);
        String error = validar(estadio);
        if (error != null) {
            return ResponseEntity.badRequest().body(Map.of("error", error));
        }
        return ResponseEntity.ok(estadioRepository.save(estadio));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable String id) {
        if (!estadioRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        String error = dependientes(id);
        if (error != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", error));
        }
        estadioRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/club")
    public ResponseEntity<Club> obtenerClub(@PathVariable String id) {
        return clubRepository.findAll().stream()
                .filter(x -> x.getEstadio() != null && id.equals(x.getEstadio().getId()))
                .findFirst().map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    private String validar(Estadio estadio) {
        if (estadio.getNombre() == null || estadio.getNombre().isBlank()) {
            return "El campo «Nombre» es obligatorio.";
        }
        if (estadio.getCiudad() == null || estadio.getCiudad().isBlank()) {
            return "El campo «Ciudad» es obligatorio.";
        }
        if (estadio.getCapacidad() == null) {
            return "El campo «Capacidad» es obligatorio.";
        }
        return null;
    }

    private String dependientes(String id) {
        if (clubRepository.findAll().stream().anyMatch(x -> x.getEstadio() != null && id.equals(x.getEstadio().getId()))) {
            return "No se puede eliminar: hay clubes que dependen de este registro.";
        }
        return null;
    }
}
