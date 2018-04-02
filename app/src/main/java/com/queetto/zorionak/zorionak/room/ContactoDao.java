package com.queetto.zorionak.zorionak.room;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface ContactoDao {
    @Query("SELECT * FROM contacto")
    List<Contacto> getAllRepos();

    @Insert
    void insert(Contacto... repos);

    @Update
    void update(Contacto... repos);
}
