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
@RequestMapping("/acciones")
public class AccionWebControlador {

    private final AccionRepository accionRepository;
    private final JugadorRepository jugadorRepository;
    private final CompeticionRepository competicionRepository;

    public AccionWebControlador(AccionRepository accionRepository,
            JugadorRepository jugadorRepository,
            CompeticionRepository competicionRepository) {
        this.accionRepository = accionRepository;
        this.jugadorRepository = jugadorRepository;
        this.competicionRepository = competicionRepository;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("acciones", accionRepository.findAll());
        return "accion/listar";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        Accion accion = new Accion();
        model.addAttribute("accion", accion);
        cargarListas(model, accion);
        return "accion/formulario";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable String id, Model model, RedirectAttributes flash) {
        Accion accion = accionRepository.findById(id).orElse(null);
        if (accion == null) {
            flash.addFlashAttribute("error", "El registro solicitado no existe.");
            return "redirect:/acciones";
        }
        model.addAttribute("accion", accion);
        cargarListas(model, accion);
        return "accion/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Accion accion,
            @RequestParam(required = false) String jugadorId,
            @RequestParam(required = false) String competicionId,
            Model model,
            RedirectAttributes flash) {
        if (accion.getId() == null || accion.getId().isBlank()) {
            accion.setId(UUID.randomUUID().toString());
        }
        if (jugadorId != null && !jugadorId.isBlank()) {
            Jugador ref = new Jugador();
            ref.setId(jugadorId);
            accion.setJugador(ref);
        } else {
            accion.setJugador(null);
        }
        if (competicionId != null && !competicionId.isBlank()) {
            Competicion ref = new Competicion();
            ref.setId(competicionId);
            accion.setCompeticion(ref);
        } else {
            accion.setCompeticion(null);
        }
        String error = validar(accion);
        if (error != null) {
            model.addAttribute("error", error);
            cargarListas(model, accion);
            return "accion/formulario";
        }
        accionRepository.save(accion);
        flash.addFlashAttribute("exito", "Registro de acción guardado correctamente.");
        return "redirect:/acciones";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable String id, RedirectAttributes flash) {
        String error = dependientes(id);
        if (error != null) {
            flash.addFlashAttribute("error", error);
        } else {
            accionRepository.deleteById(id);
            flash.addFlashAttribute("exito", "Registro de acción eliminado.");
        }
        return "redirect:/acciones";
    }

    private void cargarListas(Model model, Accion accion) {
        model.addAttribute("jugadores", jugadorRepository.findAll());
        model.addAttribute("competiciones", competicionRepository.findAll());
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
