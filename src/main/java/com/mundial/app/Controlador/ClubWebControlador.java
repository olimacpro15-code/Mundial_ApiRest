package com.mundial.app.Controlador;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mundial.app.Entidades.*;
import com.mundial.app.Repositorio.*;

@Controller
@RequestMapping("/clubes")
public class ClubWebControlador {

    private final ClubRepository clubRepository;
    private final AsociacionRepository asociacionRepository;
    private final EstadioRepository estadioRepository;
    private final EntrenadorRepository entrenadorRepository;
    private final JugadorRepository jugadorRepository;
    private final CompeticionRepository competicionRepository;

    public ClubWebControlador(ClubRepository clubRepository,
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
    public String listar(Model model) {
        model.addAttribute("clubes", clubRepository.findAll());
        model.addAttribute("totalJugadores", jugadorRepository.findAll().stream()
                .filter(x -> x.getClub() != null)
                .collect(Collectors.groupingBy(x -> x.getClub().getId(), Collectors.counting())));
        model.addAttribute("totalCompeticiones", competicionRepository.findAll().stream()
                .filter(x -> x.getClubes() != null)
                .flatMap(x -> x.getClubes().stream())
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(Club::getId, Collectors.counting())));
        return "club/listar";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        Club club = new Club();
        model.addAttribute("club", club);
        cargarListas(model, club);
        return "club/formulario";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable String id, Model model, RedirectAttributes flash) {
        Club club = clubRepository.findById(id).orElse(null);
        if (club == null) {
            flash.addFlashAttribute("error", "El registro solicitado no existe.");
            return "redirect:/clubes";
        }
        model.addAttribute("club", club);
        cargarListas(model, club);
        return "club/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Club club,
            @RequestParam(required = false) String asociacionId,
            @RequestParam(required = false) String estadioId,
            @RequestParam(required = false) String entrenadorId,
            Model model,
            RedirectAttributes flash) {
        if (club.getId() == null || club.getId().isBlank()) {
            club.setId(UUID.randomUUID().toString());
        }
        if (asociacionId != null && !asociacionId.isBlank()) {
            Asociacion ref = new Asociacion();
            ref.setId(asociacionId);
            club.setAsociacion(ref);
        } else {
            club.setAsociacion(null);
        }
        if (estadioId != null && !estadioId.isBlank()) {
            Estadio ref = new Estadio();
            ref.setId(estadioId);
            club.setEstadio(ref);
        } else {
            club.setEstadio(null);
        }
        if (entrenadorId != null && !entrenadorId.isBlank()) {
            Entrenador ref = new Entrenador();
            ref.setId(entrenadorId);
            club.setEntrenador(ref);
        } else {
            club.setEntrenador(null);
        }
        String error = validar(club);
        if (error != null) {
            model.addAttribute("error", error);
            cargarListas(model, club);
            return "club/formulario";
        }
        clubRepository.save(club);
        flash.addFlashAttribute("exito", "Registro de club guardado correctamente.");
        return "redirect:/clubes";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable String id, RedirectAttributes flash) {
        String error = dependientes(id);
        if (error != null) {
            flash.addFlashAttribute("error", error);
        } else {
            clubRepository.deleteById(id);
            flash.addFlashAttribute("exito", "Registro de club eliminado.");
        }
        return "redirect:/clubes";
    }

    private void cargarListas(Model model, Club club) {
        model.addAttribute("asociaciones", asociacionRepository.findAll());
        Set<String> estadiosUsados = clubRepository.findAll().stream()
                .filter(x -> !x.getId().equals(club.getId()) && x.getEstadio() != null)
                .map(x -> x.getEstadio().getId())
                .collect(Collectors.toSet());
        model.addAttribute("estadios", estadioRepository.findAll().stream()
                .filter(x -> !estadiosUsados.contains(x.getId()))
                .toList());
        Set<String> entrenadoresUsados = clubRepository.findAll().stream()
                .filter(x -> !x.getId().equals(club.getId()) && x.getEntrenador() != null)
                .map(x -> x.getEntrenador().getId())
                .collect(Collectors.toSet());
        model.addAttribute("entrenadores", entrenadorRepository.findAll().stream()
                .filter(x -> !entrenadoresUsados.contains(x.getId()))
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
