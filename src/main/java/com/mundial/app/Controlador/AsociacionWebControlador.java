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
@RequestMapping("/asociaciones")
public class AsociacionWebControlador {

    private final AsociacionRepository asociacionRepository;
    private final ClubRepository clubRepository;
    private final CompeticionRepository competicionRepository;

    public AsociacionWebControlador(AsociacionRepository asociacionRepository,
            ClubRepository clubRepository,
            CompeticionRepository competicionRepository) {
        this.asociacionRepository = asociacionRepository;
        this.clubRepository = clubRepository;
        this.competicionRepository = competicionRepository;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("asociaciones", asociacionRepository.findAll());
        model.addAttribute("totalClubes", clubRepository.findAll().stream()
                .filter(x -> x.getAsociacion() != null)
                .collect(Collectors.groupingBy(x -> x.getAsociacion().getId(), Collectors.counting())));
        model.addAttribute("totalCompeticiones", competicionRepository.findAll().stream()
                .filter(x -> x.getOrganizador() != null)
                .collect(Collectors.groupingBy(x -> x.getOrganizador().getId(), Collectors.counting())));
        return "asociacion/listar";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        Asociacion asociacion = new Asociacion();
        model.addAttribute("asociacion", asociacion);
        return "asociacion/formulario";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable String id, Model model, RedirectAttributes flash) {
        Asociacion asociacion = asociacionRepository.findById(id).orElse(null);
        if (asociacion == null) {
            flash.addFlashAttribute("error", "El registro solicitado no existe.");
            return "redirect:/asociaciones";
        }
        model.addAttribute("asociacion", asociacion);
        return "asociacion/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Asociacion asociacion,
            Model model,
            RedirectAttributes flash) {
        if (asociacion.getId() == null || asociacion.getId().isBlank()) {
            asociacion.setId(UUID.randomUUID().toString());
        }
        String error = validar(asociacion);
        if (error != null) {
            model.addAttribute("error", error);
            return "asociacion/formulario";
        }
        asociacionRepository.save(asociacion);
        flash.addFlashAttribute("exito", "Registro de asociación guardado correctamente.");
        return "redirect:/asociaciones";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable String id, RedirectAttributes flash) {
        String error = dependientes(id);
        if (error != null) {
            flash.addFlashAttribute("error", error);
        } else {
            asociacionRepository.deleteById(id);
            flash.addFlashAttribute("exito", "Registro de asociación eliminado.");
        }
        return "redirect:/asociaciones";
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
