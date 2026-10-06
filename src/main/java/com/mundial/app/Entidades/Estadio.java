package com.mundial.app.Entidades;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "estadios")
public class Estadio {

    @Id
    private String id; // UUID generado al crear el registro

    private String nombre;
    private String ciudad;
    private Integer capacidad;
    private Integer anioInauguracion;

    public Estadio() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCiudad() { return ciudad; }
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }

    public Integer getCapacidad() { return capacidad; }
    public void setCapacidad(Integer capacidad) { this.capacidad = capacidad; }

    public Integer getAnioInauguracion() { return anioInauguracion; }
    public void setAnioInauguracion(Integer anioInauguracion) { this.anioInauguracion = anioInauguracion; }

}
