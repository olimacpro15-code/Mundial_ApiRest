package com.mundial.app.Controlador;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.mundial.app.Repositorio.*;

/** Página de inicio con el resumen de cada sección. */
@Controller
public class HomeControlador {

    private final AsociacionRepository asociacionRepository;
    private final EstadioRepository estadioRepository;
    private final EntrenadorRepository entrenadorRepository;
    private final ClubRepository clubRepository;
    private final JugadorRepository jugadorRepository;
    private final CompeticionRepository competicionRepository;
    private final AccionRepository accionRepository;

    public HomeControlador(AsociacionRepository asociacionRepository,
            EstadioRepository estadioRepository,
            EntrenadorRepository entrenadorRepository,
            ClubRepository clubRepository,
            JugadorRepository jugadorRepository,
            CompeticionRepository competicionRepository,
            AccionRepository accionRepository) {
        this.asociacionRepository = asociacionRepository;
        this.estadioRepository = estadioRepository;
        this.entrenadorRepository = entrenadorRepository;
        this.clubRepository = clubRepository;
        this.jugadorRepository = jugadorRepository;
        this.competicionRepository = competicionRepository;
        this.accionRepository = accionRepository;
    }

    @GetMapping({ "/", "/index" })
    public String index(Model model) {
        model.addAttribute("totalAsociaciones", asociacionRepository.count());
        model.addAttribute("totalEstadios", estadioRepository.count());
        model.addAttribute("totalEntrenadores", entrenadorRepository.count());
        model.addAttribute("totalClubes", clubRepository.count());
        model.addAttribute("totalJugadores", jugadorRepository.count());
        model.addAttribute("totalCompeticiones", competicionRepository.count());
        model.addAttribute("totalAcciones", accionRepository.count());
        return "index";
    }
}
