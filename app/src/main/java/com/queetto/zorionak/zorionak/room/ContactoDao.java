package com.queetto.zorionak.zorionak.room;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

/**
 * Proyecto: Zorionak
 * Created by David Nuñez on 03/abr/18.
 */

// Metodos para accesar la base de datos
@Dao
public interface ContactoDao {
    @Query("SELECT * FROM contacto")
    List<Contacto> getAllContacts();

    @Insert
    void insert(Contacto... repos);

    @Query("UPDATE contacto SET aviso = :avisod  WHERE id = :tid")
    int updateContact(int tid, int avisod);
}
