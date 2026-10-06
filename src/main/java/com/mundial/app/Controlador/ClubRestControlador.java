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
@RequestMapping("/api/clubes")
public class ClubRestControlador {

    private final ClubRepository clubRepository;
    private final AsociacionRepository asociacionRepository;
    private final EstadioRepository estadioRepository;
    private final EntrenadorRepository entrenadorRepository;
    private final JugadorRepository jugadorRepository;
    private final CompeticionRepository competicionRepository;

    public ClubRestControlador(ClubRepository clubRepository,
            AsociacionRepository asociacionRepository,
            EstadioRepository estadioRepository,
            EntrenadorRepository entrenadorRepository,
            JugadorRepository jugadorRepository,
            CompeticionRepository competicionRepository) {
        this.clubRepository = clubRepository;
        this.asociacionRepository = asociacionRepository;
        this.estadioRepository = estadioRepository;
        this.entrenadorRepository = entrenadorRepository;
        this.jugadorRepository = jugadorRepository;
        this.competicionRepository = competicionRepository;
    }

    @GetMapping
    public List<Club> listar() {
        return clubRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Club> obtener(@PathVariable String id) {
        return clubRepository.findById(id).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Club club) {
        club.setId(UUID.randomUUID().toString());
        String error = validar(club);
        if (error != null) {
            return ResponseEntity.badRequest().body(Map.of("error", error));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(clubRepository.save(club));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable String id, @RequestBody Club club) {
        if (!clubRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        club.setId(id);
        String error = validar(club);
        if (error != null) {
            return ResponseEntity.badRequest().body(Map.of("error", error));
        }
        return ResponseEntity.ok(clubRepository.save(club));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable String id) {
        if (!clubRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        String error = dependientes(id);
        if (error != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", error));
        }
        clubRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/jugadores")
    public ResponseEntity<List<Jugador>> listarJugadores(@PathVariable String id) {
        if (!clubRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(jugadorRepository.findAll().stream()
                .filter(x -> x.getClub() != null && id.equals(x.getClub().getId()))
                .toList());
    }

    @GetMapping("/{id}/competiciones")
    public ResponseEntity<List<Competicion>> listarCompeticiones(@PathVariable String id) {
        if (!clubRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(competicionRepository.findAll().stream()
                .filter(x -> x.getClubes() != null && x.getClubes().stream().anyMatch(i -> i != null && id.equals(i.getId())))
                .toList());
    }

    private String validar(Club club) {
        if (club.getNombre() == null || club.getNombre().isBlank()) {
            return "El campo «Nombre» es obligatorio.";
        }
        if (club.getCiudad() == null || club.getCiudad().isBlank()) {
            return "El campo «Ciudad» es obligatorio.";
        }
        Asociacion asociacion = club.getAsociacion() == null || club.getAsociacion().getId() == null ? null
                : asociacionRepository.findById(club.getAsociacion().getId()).orElse(null);
        if (asociacion == null) {
            return "La asociación seleccionada no existe. Regístrala primero.";
        }
        club.setAsociacion(asociacion);
        Estadio estadio = club.getEstadio() == null || club.getEstadio().getId() == null ? null
                : estadioRepository.findById(club.getEstadio().getId()).orElse(null);
        if (estadio == null) {
            return "El estadio seleccionado no existe. Regístralo primero.";
        }
        boolean estadioOcupado = clubRepository.findAll().stream().anyMatch(x -> !x.getId().equals(club.getId())
                && x.getEstadio() != null && estadio.getId().equals(x.getEstadio().getId()));
        if (estadioOcupado) {
            return "El estadio ya está asignado a otro club (relación uno a uno).";
        }
        club.setEstadio(estadio);
        Entrenador entrenador = club.getEntrenador() == null || club.getEntrenador().getId() == null ? null
                : entrenadorRepository.findById(club.getEntrenador().getId()).orElse(null);
        if (entrenador == null) {
            return "El entrenador seleccionado no existe. Regístralo primero.";
        }
        boolean entrenadorOcupado = clubRepository.findAll().stream().anyMatch(x -> !x.getId().equals(club.getId())
                && x.getEntrenador() != null && entrenador.getId().equals(x.getEntrenador().getId()));
        if (entrenadorOcupado) {
            return "El entrenador ya dirige a otro club (relación uno a uno).";
        }
        club.setEntrenador(entrenador);
        return null;
    }

    private String dependientes(String id) {
        if (jugadorRepository.findAll().stream().anyMatch(x -> x.getClub() != null && id.equals(x.getClub().getId()))) {
            return "No se puede eliminar: hay jugadores que dependen de este registro.";
        }
        if (competicionRepository.findAll().stream().anyMatch(x -> x.getClubes() != null && x.getClubes().stream().anyMatch(i -> i != null && id.equals(i.getId())))) {
            return "No se puede eliminar: hay competiciones que dependen de este registro.";
        }
        return null;
    }
}
