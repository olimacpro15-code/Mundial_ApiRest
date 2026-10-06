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
@RequestMapping("/estadios")
public class EstadioWebControlador {

    private final EstadioRepository estadioRepository;
    private final ClubRepository clubRepository;

    public EstadioWebControlador(EstadioRepository estadioRepository,
            ClubRepository clubRepository) {
        this.estadioRepository = estadioRepository;
        this.clubRepository = clubRepository;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("estadios", estadioRepository.findAll());
        model.addAttribute("clubAsignado", clubRepository.findAll().stream()
                .filter(x -> x.getEstadio() != null)
                .collect(Collectors.toMap(x -> x.getEstadio().getId(), Club::getNombre, (a, b) -> a)));
        return "estadio/listar";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        Estadio estadio = new Estadio();
        model.addAttribute("estadio", estadio);
        return "estadio/formulario";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable String id, Model model, RedirectAttributes flash) {
        Estadio estadio = estadioRepository.findById(id).orElse(null);
        if (estadio == null) {
            flash.addFlashAttribute("error", "El registro solicitado no existe.");
            return "redirect:/estadios";
        }
        model.addAttribute("estadio", estadio);
        return "estadio/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Estadio estadio,
            Model model,
            RedirectAttributes flash) {
        if (estadio.getId() == null || estadio.getId().isBlank()) {
            estadio.setId(UUID.randomUUID().toString());
        }
        String error = validar(estadio);
        if (error != null) {
            model.addAttribute("error", error);
            return "estadio/formulario";
        }
        estadioRepository.save(estadio);
        flash.addFlashAttribute("exito", "Registro de estadio guardado correctamente.");
        return "redirect:/estadios";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable String id, RedirectAttributes flash) {
        String error = dependientes(id);
        if (error != null) {
            flash.addFlashAttribute("error", error);
        } else {
            estadioRepository.deleteById(id);
            flash.addFlashAttribute("exito", "Registro de estadio eliminado.");
        }
        return "redirect:/estadios";
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
