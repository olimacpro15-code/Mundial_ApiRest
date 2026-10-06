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
@RequestMapping("/jugadores")
public class JugadorWebControlador {

    private final JugadorRepository jugadorRepository;
    private final ClubRepository clubRepository;
    private final AccionRepository accionRepository;

    public JugadorWebControlador(JugadorRepository jugadorRepository,
            ClubRepository clubRepository,
            AccionRepository accionRepository) {
        this.jugadorRepository = jugadorRepository;
        this.clubRepository = clubRepository;
        this.accionRepository = accionRepository;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("jugadores", jugadorRepository.findAll());
        model.addAttribute("totalAcciones", accionRepository.findAll().stream()
                .filter(x -> x.getJugador() != null)
                .collect(Collectors.groupingBy(x -> x.getJugador().getId(), Collectors.counting())));
        return "jugador/listar";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        Jugador jugador = new Jugador();
        model.addAttribute("jugador", jugador);
        cargarListas(model, jugador);
        return "jugador/formulario";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable String id, Model model, RedirectAttributes flash) {
        Jugador jugador = jugadorRepository.findById(id).orElse(null);
        if (jugador == null) {
            flash.addFlashAttribute("error", "El registro solicitado no existe.");
            return "redirect:/jugadores";
        }
        model.addAttribute("jugador", jugador);
        cargarListas(model, jugador);
        return "jugador/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Jugador jugador,
            @RequestParam(required = false) String clubId,
            Model model,
            RedirectAttributes flash) {
        if (jugador.getId() == null || jugador.getId().isBlank()) {
            jugador.setId(UUID.randomUUID().toString());
        }
        if (clubId != null && !clubId.isBlank()) {
            Club ref = new Club();
            ref.setId(clubId);
            jugador.setClub(ref);
        } else {
            jugador.setClub(null);
        }
        String error = validar(jugador);
        if (error != null) {
            model.addAttribute("error", error);
            cargarListas(model, jugador);
            return "jugador/formulario";
        }
        jugadorRepository.save(jugador);
        flash.addFlashAttribute("exito", "Registro de jugador guardado correctamente.");
        return "redirect:/jugadores";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable String id, RedirectAttributes flash) {
        String error = dependientes(id);
        if (error != null) {
            flash.addFlashAttribute("error", error);
        } else {
            jugadorRepository.deleteById(id);
            flash.addFlashAttribute("exito", "Registro de jugador eliminado.");
        }
        return "redirect:/jugadores";
    }

    private void cargarListas(Model model, Jugador jugador) {
        model.addAttribute("clubes", clubRepository.findAll());
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
