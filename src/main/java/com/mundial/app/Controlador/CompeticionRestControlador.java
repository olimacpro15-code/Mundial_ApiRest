package com.mundial.app.Controlador;

import java.util.ArrayList;
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
@RequestMapping("/api/competiciones")
public class CompeticionRestControlador {

    private final CompeticionRepository competicionRepository;
    private final AsociacionRepository asociacionRepository;
    private final ClubRepository clubRepository;
    private final AccionRepository accionRepository;

    public CompeticionRestControlador(CompeticionRepository competicionRepository,
            AsociacionRepository asociacionRepository,
            ClubRepository clubRepository,
            AccionRepository accionRepository) {
        this.competicionRepository = competicionRepository;
        this.asociacionRepository = asociacionRepository;
        this.clubRepository = clubRepository;
        this.accionRepository = accionRepository;
    }

    @GetMapping
    public List<Competicion> listar() {
        return competicionRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Competicion> obtener(@PathVariable String id) {
        return competicionRepository.findById(id).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Competicion competicion) {
        competicion.setId(UUID.randomUUID().toString());
        String error = validar(competicion);
        if (error != null) {
            return ResponseEntity.badRequest().body(Map.of("error", error));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(competicionRepository.save(competicion));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable String id, @RequestBody Competicion competicion) {
        if (!competicionRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        competicion.setId(id);
        String error = validar(competicion);
        if (error != null) {
            return ResponseEntity.badRequest().body(Map.of("error", error));
        }
        return ResponseEntity.ok(competicionRepository.save(competicion));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable String id) {
        if (!competicionRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        String error = dependientes(id);
        if (error != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", error));
        }
        competicionRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/acciones")
    public ResponseEntity<List<Accion>> listarAcciones(@PathVariable String id) {
        if (!competicionRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(accionRepository.findAll().stream()
                .filter(x -> x.getCompeticion() != null && id.equals(x.getCompeticion().getId()))
                .toList());
    }

    private String validar(Competicion competicion) {
        if (competicion.getNombre() == null || competicion.getNombre().isBlank()) {
            return "El campo «Nombre» es obligatorio.";
        }
        if (competicion.getAnio() == null) {
            return "El campo «Año» es obligatorio.";
        }
        if (competicion.getSede() == null || competicion.getSede().isBlank()) {
            return "El campo «Sede» es obligatorio.";
        }
        Asociacion organizador = competicion.getOrganizador() == null || competicion.getOrganizador().getId() == null ? null
                : asociacionRepository.findById(competicion.getOrganizador().getId()).orElse(null);
        if (organizador == null) {
            return "La asociación organizadora no existe. Regístrala primero.";
        }
        competicion.setOrganizador(organizador);
        List<Club> clubesReales = new ArrayList<>();
        if (competicion.getClubes() != null) {
            for (Club item : competicion.getClubes()) {
                if (item == null || item.getId() == null) {
                    continue;
                }
                Club real = clubRepository.findById(item.getId()).orElse(null);
                if (real == null) {
                    return "Alguno de los clubes seleccionados no existe.";
                }
                if (clubesReales.stream().noneMatch(x -> x.getId().equals(real.getId()))) {
                    clubesReales.add(real);
                }
            }
        }
        if (clubesReales.isEmpty()) {
            return "La competición necesita al menos un club participante.";
        }
        competicion.setClubes(clubesReales);
        return null;
    }

    private String dependientes(String id) {
        if (accionRepository.findAll().stream().anyMatch(x -> x.getCompeticion() != null && id.equals(x.getCompeticion().getId()))) {
            return "No se puede eliminar: hay acciones que dependen de este registro.";
        }
        return null;
    }
}
