package com.mundial.app.Entidades;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

@Document(collection = "acciones")
public class Accion {

    @Id
    private String id; // UUID generado al crear el registro

    private String tipo;
    private Integer minuto;
    private String descripcion;

    /** Muchos a uno: un jugador puede tener muchas acciones. La acción no existe sin el jugador. */
    @DocumentReference
    private Jugador jugador;

    /** Muchos a uno: una competición registra muchas acciones. */
    @DocumentReference
    private Competicion competicion;

    public Accion() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public Integer getMinuto() { return minuto; }
    public void setMinuto(Integer minuto) { this.minuto = minuto; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Jugador getJugador() { return jugador; }
    public void setJugador(Jugador jugador) { this.jugador = jugador; }

    public Competicion getCompeticion() { return competicion; }
    public void setCompeticion(Competicion competicion) { this.competicion = competicion; }

}
