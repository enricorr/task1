package com.queetto.zorionak.zorionak.room;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;

@Database(entities = { Contacto.class }, version = 1)
public abstract class ContactoDatabase extends RoomDatabase {
    private static final String DB_NAME = "contactoDatabase.db";
    private static volatile ContactoDatabase instance;

    static synchronized ContactoDatabase getInstance(Context context) {
        if (instance == null) {
            instance = create(context);
        }
        return instance;
    }

    private static ContactoDatabase create(final Context context) {
        return Room.databaseBuilder(
                context,
                ContactoDatabase.class,
                DB_NAME).build();
    }

    public abstract ContactoDao getContactoDao();
}
