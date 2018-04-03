package com.queetto.zorionak.zorionak.room;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;


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

    public int getAviso() {
        return aviso;
    }

    public void setAviso(int aviso) {
        this.aviso = aviso;
    }

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private int cid;

    @ColumnInfo(name = "nombre")
    private String name;

    @ColumnInfo(name = "mes")
    private int mes;

    @ColumnInfo(name = "dia")
    private int dia;

    @ColumnInfo(name = "uriimagen")
    private String imagen;

    @ColumnInfo(name = "aviso")
    private int aviso; //0 - no se ha avisado. 1 - Ya se aviso

    public Contacto(String name, int mes, int dia, String imagen) {
        this.name = name;
        this.mes = mes;
        this.dia = dia;
        this.imagen = imagen;
        this.aviso = 0;
    }


    // Getters and setters are ignored for brevity,
    // but they're required for Room to work.
}
