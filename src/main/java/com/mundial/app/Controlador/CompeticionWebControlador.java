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
@RequestMapping("/competiciones")
public class CompeticionWebControlador {

    private final CompeticionRepository competicionRepository;
    private final AsociacionRepository asociacionRepository;
    private final ClubRepository clubRepository;
    private final AccionRepository accionRepository;

    public CompeticionWebControlador(CompeticionRepository competicionRepository,
            AsociacionRepository asociacionRepository,
            ClubRepository clubRepository,
            AccionRepository accionRepository) {
        this.competicionRepository = competicionRepository;
        this.asociacionRepository = asociacionRepository;
        this.clubRepository = clubRepository;
        this.accionRepository = accionRepository;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("competiciones", competicionRepository.findAll());
        model.addAttribute("totalAcciones", accionRepository.findAll().stream()
                .filter(x -> x.getCompeticion() != null)
                .collect(Collectors.groupingBy(x -> x.getCompeticion().getId(), Collectors.counting())));
        return "competicion/listar";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        Competicion competicion = new Competicion();
        model.addAttribute("competicion", competicion);
        cargarListas(model, competicion);
        return "competicion/formulario";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable String id, Model model, RedirectAttributes flash) {
        Competicion competicion = competicionRepository.findById(id).orElse(null);
        if (competicion == null) {
            flash.addFlashAttribute("error", "El registro solicitado no existe.");
            return "redirect:/competiciones";
        }
        model.addAttribute("competicion", competicion);
        cargarListas(model, competicion);
        return "competicion/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Competicion competicion,
            @RequestParam(required = false) String organizadorId,
            @RequestParam(required = false) List<String> clubesIds,
            Model model,
            RedirectAttributes flash) {
        if (competicion.getId() == null || competicion.getId().isBlank()) {
            competicion.setId(UUID.randomUUID().toString());
        }
        if (organizadorId != null && !organizadorId.isBlank()) {
            Asociacion ref = new Asociacion();
            ref.setId(organizadorId);
            competicion.setOrganizador(ref);
        } else {
            competicion.setOrganizador(null);
        }
        List<Club> clubesSeleccion = new ArrayList<>();
        if (clubesIds != null) {
            for (String itemId : clubesIds) {
                Club ref = new Club();
                ref.setId(itemId);
                clubesSeleccion.add(ref);
            }
        }
        competicion.setClubes(clubesSeleccion);
        String error = validar(competicion);
        if (error != null) {
            model.addAttribute("error", error);
            cargarListas(model, competicion);
            return "competicion/formulario";
        }
        competicionRepository.save(competicion);
        flash.addFlashAttribute("exito", "Registro de competición guardado correctamente.");
        return "redirect:/competiciones";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable String id, RedirectAttributes flash) {
        String error = dependientes(id);
        if (error != null) {
            flash.addFlashAttribute("error", error);
        } else {
            competicionRepository.deleteById(id);
            flash.addFlashAttribute("exito", "Registro de competición eliminado.");
        }
        return "redirect:/competiciones";
    }

    private void cargarListas(Model model, Competicion competicion) {
        model.addAttribute("asociaciones", asociacionRepository.findAll());
        model.addAttribute("clubes", clubRepository.findAll());
        Set<String> clubesIds = competicion.getClubes() == null ? new HashSet<>() : competicion.getClubes().stream()
                .filter(Objects::nonNull).map(Club::getId).collect(Collectors.toSet());
        model.addAttribute("clubesIds", clubesIds);
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
