package com.mundial.app.Entidades;

import java.util.ArrayList;
import java.util.List;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

@Document(collection = "competiciones")
public class Competicion {

    @Id
    private String id; // UUID generado al crear el registro

    private String nombre;
    private Integer anio;
    private String sede;

    /** Muchos a uno: una asociación puede organizar muchas competiciones. */
    @DocumentReference
    private Asociacion organizador;

    /** Muchos a muchos: una competición reúne varios clubes y cada club puede jugar varias competiciones. */
    @DocumentReference
    private List<Club> clubes = new ArrayList<>();

    public Competicion() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public Integer getAnio() { return anio; }
    public void setAnio(Integer anio) { this.anio = anio; }

    public String getSede() { return sede; }
    public void setSede(String sede) { this.sede = sede; }

    public Asociacion getOrganizador() { return organizador; }
    public void setOrganizador(Asociacion organizador) { this.organizador = organizador; }

    public List<Club> getClubes() { return clubes; }
    public void setClubes(List<Club> clubes) { this.clubes = clubes; }

}
