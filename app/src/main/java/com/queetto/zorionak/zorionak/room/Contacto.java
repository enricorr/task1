package com.queetto.zorionak.zorionak.room;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import java.util.Date;

@Entity
public class Contacto {
    public int getCid() {
        return cid;
    }

    public void setCid(int cid) {
        this.cid = cid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    @PrimaryKey(autoGenerate = true)
    private int cid;

    @ColumnInfo(name = "nombre")
    private String name;


    public int getMes() {
        return mes;
    }

    public void setMes(int mes) {
        this.mes = mes;
    }

    public int getDia() {
        return dia;
    }

    public void setDia(int dia) {
        this.dia = dia;
    }

    @ColumnInfo(name = "mes")
    private int mes;

    @ColumnInfo(name = "dia")
    private int dia;

    @ColumnInfo(name = "uriimagen")
    private String imagen;

    public Contacto(String name, int mes, int dia, String imagen) {
        this.name = name;
        this.mes = mes;
        this.dia = dia;
        this.imagen = imagen;
    }
    // Getters and setters are ignored for brevity,
    // but they're required for Room to work.
}
