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

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
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

    @ColumnInfo(name = "fecha")
    private Date fecha;

    @ColumnInfo(name = "uriimagen")
    private String imagen;

    public Contacto(String name, Date fecha, String imagen) {
        this.name = name;
        this.fecha = fecha;
        this.imagen = imagen;
    }
    // Getters and setters are ignored for brevity,
    // but they're required for Room to work.
}
