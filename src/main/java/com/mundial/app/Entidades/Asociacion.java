package com.mundial.app.Entidades;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "asociaciones")
public class Asociacion {

    @Id
    private String id; // UUID generado al crear el registro

    private String nombre;
    private String pais;
    private String confederacion;
    private Integer anioFundacion;
    private String presidente;

    public Asociacion() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getPais() { return pais; }
    public void setPais(String pais) { this.pais = pais; }

    public String getConfederacion() { return confederacion; }
    public void setConfederacion(String confederacion) { this.confederacion = confederacion; }

    public Integer getAnioFundacion() { return anioFundacion; }
    public void setAnioFundacion(Integer anioFundacion) { this.anioFundacion = anioFundacion; }

    public String getPresidente() { return presidente; }
    public void setPresidente(String presidente) { this.presidente = presidente; }

}
