package com.mundial.app.Entidades;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

@Document(collection = "clubes")
public class Club {

    @Id
    private String id; // UUID generado al crear el registro

    private String nombre;
    private String ciudad;
    private Integer anioFundacion;

    /** Muchos a uno: muchos clubes pertenecen a una misma asociación. El club no se guarda si la asociación no existe. */
    @DocumentReference
    private Asociacion asociacion;

    /** Uno a uno: cada club tiene un único estadio y cada estadio pertenece a un solo club. */
    @DocumentReference
    private Estadio estadio;

    /** Uno a uno: cada club tiene un único entrenador y cada entrenador dirige a un solo club. */
    @DocumentReference
    private Entrenador entrenador;

    public Club() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCiudad() { return ciudad; }
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }

    public Integer getAnioFundacion() { return anioFundacion; }
    public void setAnioFundacion(Integer anioFundacion) { this.anioFundacion = anioFundacion; }

    public Asociacion getAsociacion() { return asociacion; }
    public void setAsociacion(Asociacion asociacion) { this.asociacion = asociacion; }

    public Estadio getEstadio() { return estadio; }
    public void setEstadio(Estadio estadio) { this.estadio = estadio; }

    public Entrenador getEntrenador() { return entrenador; }
    public void setEntrenador(Entrenador entrenador) { this.entrenador = entrenador; }

}
