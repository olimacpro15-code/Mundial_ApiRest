package com.mundial.app.Entidades;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

@Document(collection = "jugadores")
public class Jugador {

    @Id
    private String id; // UUID generado al crear el registro

    private String nombre;
    private String posicion;
    private Integer dorsal;
    private Integer edad;
    private String nacionalidad;

    /** Muchos a uno: muchos jugadores pertenecen a un club. El jugador no existe sin su club. */
    @DocumentReference
    private Club club;

    public Jugador() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getPosicion() { return posicion; }
    public void setPosicion(String posicion) { this.posicion = posicion; }

    public Integer getDorsal() { return dorsal; }
    public void setDorsal(Integer dorsal) { this.dorsal = dorsal; }

    public Integer getEdad() { return edad; }
    public void setEdad(Integer edad) { this.edad = edad; }

    public String getNacionalidad() { return nacionalidad; }
    public void setNacionalidad(String nacionalidad) { this.nacionalidad = nacionalidad; }

    public Club getClub() { return club; }
    public void setClub(Club club) { this.club = club; }

}
