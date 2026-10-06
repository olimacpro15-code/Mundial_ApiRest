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
@RequestMapping("/entrenadores")
public class EntrenadorWebControlador {

    private final EntrenadorRepository entrenadorRepository;
    private final ClubRepository clubRepository;

    public EntrenadorWebControlador(EntrenadorRepository entrenadorRepository,
            ClubRepository clubRepository) {
        this.entrenadorRepository = entrenadorRepository;
        this.clubRepository = clubRepository;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("entrenadores", entrenadorRepository.findAll());
        model.addAttribute("clubAsignado", clubRepository.findAll().stream()
                .filter(x -> x.getEntrenador() != null)
                .collect(Collectors.toMap(x -> x.getEntrenador().getId(), Club::getNombre, (a, b) -> a)));
        return "entrenador/listar";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        Entrenador entrenador = new Entrenador();
        model.addAttribute("entrenador", entrenador);
        return "entrenador/formulario";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable String id, Model model, RedirectAttributes flash) {
        Entrenador entrenador = entrenadorRepository.findById(id).orElse(null);
        if (entrenador == null) {
            flash.addFlashAttribute("error", "El registro solicitado no existe.");
            return "redirect:/entrenadores";
        }
        model.addAttribute("entrenador", entrenador);
        return "entrenador/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Entrenador entrenador,
            Model model,
            RedirectAttributes flash) {
        if (entrenador.getId() == null || entrenador.getId().isBlank()) {
            entrenador.setId(UUID.randomUUID().toString());
        }
        String error = validar(entrenador);
        if (error != null) {
            model.addAttribute("error", error);
            return "entrenador/formulario";
        }
        entrenadorRepository.save(entrenador);
        flash.addFlashAttribute("exito", "Registro de entrenador guardado correctamente.");
        return "redirect:/entrenadores";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable String id, RedirectAttributes flash) {
        String error = dependientes(id);
        if (error != null) {
            flash.addFlashAttribute("error", error);
        } else {
            entrenadorRepository.deleteById(id);
            flash.addFlashAttribute("exito", "Registro de entrenador eliminado.");
        }
        return "redirect:/entrenadores";
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
