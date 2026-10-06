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
@RequestMapping("/api/acciones")
public class AccionRestControlador {

    private final AccionRepository accionRepository;
    private final JugadorRepository jugadorRepository;
    private final CompeticionRepository competicionRepository;

    public AccionRestControlador(AccionRepository accionRepository,
            JugadorRepository jugadorRepository,
            CompeticionRepository competicionRepository) {
        this.accionRepository = accionRepository;
        this.jugadorRepository = jugadorRepository;
        this.competicionRepository = competicionRepository;
    }

    @GetMapping
    public List<Accion> listar() {
        return accionRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Accion> obtener(@PathVariable String id) {
        return accionRepository.findById(id).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Accion accion) {
        accion.setId(UUID.randomUUID().toString());
        String error = validar(accion);
        if (error != null) {
            return ResponseEntity.badRequest().body(Map.of("error", error));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(accionRepository.save(accion));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable String id, @RequestBody Accion accion) {
        if (!accionRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        accion.setId(id);
        String error = validar(accion);
        if (error != null) {
            return ResponseEntity.badRequest().body(Map.of("error", error));
        }
        return ResponseEntity.ok(accionRepository.save(accion));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable String id) {
        if (!accionRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        String error = dependientes(id);
        if (error != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", error));
        }
        accionRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private String validar(Accion accion) {
        if (accion.getTipo() == null || accion.getTipo().isBlank()) {
            return "El campo «Tipo» es obligatorio.";
        }
        if (accion.getMinuto() == null) {
            return "El campo «Minuto» es obligatorio.";
        }
        Jugador jugador = accion.getJugador() == null || accion.getJugador().getId() == null ? null
                : jugadorRepository.findById(accion.getJugador().getId()).orElse(null);
        if (jugador == null) {
            return "El jugador seleccionado no existe. Regístralo primero.";
        }
        accion.setJugador(jugador);
        Competicion competicion = accion.getCompeticion() == null || accion.getCompeticion().getId() == null ? null
                : competicionRepository.findById(accion.getCompeticion().getId()).orElse(null);
        if (competicion == null) {
            return "La competición seleccionada no existe. Regístrala primero.";
        }
        accion.setCompeticion(competicion);
        return null;
    }

    private String dependientes(String id) {
        return null;
    }
}
