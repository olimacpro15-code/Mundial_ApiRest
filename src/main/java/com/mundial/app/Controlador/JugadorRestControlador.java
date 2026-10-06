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
@RequestMapping("/api/jugadores")
public class JugadorRestControlador {

    private final JugadorRepository jugadorRepository;
    private final ClubRepository clubRepository;
    private final AccionRepository accionRepository;

    public JugadorRestControlador(JugadorRepository jugadorRepository,
            ClubRepository clubRepository,
            AccionRepository accionRepository) {
        this.jugadorRepository = jugadorRepository;
        this.clubRepository = clubRepository;
        this.accionRepository = accionRepository;
    }

    @GetMapping
    public List<Jugador> listar() {
        return jugadorRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Jugador> obtener(@PathVariable String id) {
        return jugadorRepository.findById(id).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Jugador jugador) {
        jugador.setId(UUID.randomUUID().toString());
        String error = validar(jugador);
        if (error != null) {
            return ResponseEntity.badRequest().body(Map.of("error", error));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(jugadorRepository.save(jugador));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable String id, @RequestBody Jugador jugador) {
        if (!jugadorRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        jugador.setId(id);
        String error = validar(jugador);
        if (error != null) {
            return ResponseEntity.badRequest().body(Map.of("error", error));
        }
        return ResponseEntity.ok(jugadorRepository.save(jugador));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable String id) {
        if (!jugadorRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        String error = dependientes(id);
        if (error != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", error));
        }
        jugadorRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/acciones")
    public ResponseEntity<List<Accion>> listarAcciones(@PathVariable String id) {
        if (!jugadorRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(accionRepository.findAll().stream()
                .filter(x -> x.getJugador() != null && id.equals(x.getJugador().getId()))
                .toList());
    }

    private String validar(Jugador jugador) {
        if (jugador.getNombre() == null || jugador.getNombre().isBlank()) {
            return "El campo «Nombre» es obligatorio.";
        }
        if (jugador.getPosicion() == null || jugador.getPosicion().isBlank()) {
            return "El campo «Posición» es obligatorio.";
        }
        if (jugador.getDorsal() == null) {
            return "El campo «Dorsal» es obligatorio.";
        }
        Club club = jugador.getClub() == null || jugador.getClub().getId() == null ? null
                : clubRepository.findById(jugador.getClub().getId()).orElse(null);
        if (club == null) {
            return "El club seleccionado no existe. Regístralo primero.";
        }
        jugador.setClub(club);
        return null;
    }

    private String dependientes(String id) {
        if (accionRepository.findAll().stream().anyMatch(x -> x.getJugador() != null && id.equals(x.getJugador().getId()))) {
            return "No se puede eliminar: hay acciones que dependen de este registro.";
        }
        return null;
    }
}
