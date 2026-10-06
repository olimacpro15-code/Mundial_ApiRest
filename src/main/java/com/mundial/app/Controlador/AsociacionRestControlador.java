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
@RequestMapping("/api/asociaciones")
public class AsociacionRestControlador {

    private final AsociacionRepository asociacionRepository;
    private final ClubRepository clubRepository;
    private final CompeticionRepository competicionRepository;

    public AsociacionRestControlador(AsociacionRepository asociacionRepository,
            ClubRepository clubRepository,
            CompeticionRepository competicionRepository) {
        this.asociacionRepository = asociacionRepository;
        this.clubRepository = clubRepository;
        this.competicionRepository = competicionRepository;
    }

    @GetMapping
    public List<Asociacion> listar() {
        return asociacionRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Asociacion> obtener(@PathVariable String id) {
        return asociacionRepository.findById(id).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Asociacion asociacion) {
        asociacion.setId(UUID.randomUUID().toString());
        String error = validar(asociacion);
        if (error != null) {
            return ResponseEntity.badRequest().body(Map.of("error", error));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(asociacionRepository.save(asociacion));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable String id, @RequestBody Asociacion asociacion) {
        if (!asociacionRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        asociacion.setId(id);
        String error = validar(asociacion);
        if (error != null) {
            return ResponseEntity.badRequest().body(Map.of("error", error));
        }
        return ResponseEntity.ok(asociacionRepository.save(asociacion));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable String id) {
        if (!asociacionRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        String error = dependientes(id);
        if (error != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", error));
        }
        asociacionRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/clubes")
    public ResponseEntity<List<Club>> listarClubes(@PathVariable String id) {
        if (!asociacionRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(clubRepository.findAll().stream()
                .filter(x -> x.getAsociacion() != null && id.equals(x.getAsociacion().getId()))
                .toList());
    }

    @GetMapping("/{id}/competiciones")
    public ResponseEntity<List<Competicion>> listarCompeticiones(@PathVariable String id) {
        if (!asociacionRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(competicionRepository.findAll().stream()
                .filter(x -> x.getOrganizador() != null && id.equals(x.getOrganizador().getId()))
                .toList());
    }

    private String validar(Asociacion asociacion) {
        if (asociacion.getNombre() == null || asociacion.getNombre().isBlank()) {
            return "El campo «Nombre» es obligatorio.";
        }
        if (asociacion.getPais() == null || asociacion.getPais().isBlank()) {
            return "El campo «País» es obligatorio.";
        }
        if (asociacion.getConfederacion() == null || asociacion.getConfederacion().isBlank()) {
            return "El campo «Confederación» es obligatorio.";
        }
        return null;
    }

    private String dependientes(String id) {
        if (clubRepository.findAll().stream().anyMatch(x -> x.getAsociacion() != null && id.equals(x.getAsociacion().getId()))) {
            return "No se puede eliminar: hay clubes que dependen de este registro.";
        }
        if (competicionRepository.findAll().stream().anyMatch(x -> x.getOrganizador() != null && id.equals(x.getOrganizador().getId()))) {
            return "No se puede eliminar: hay competiciones que dependen de este registro.";
        }
        return null;
    }
}
