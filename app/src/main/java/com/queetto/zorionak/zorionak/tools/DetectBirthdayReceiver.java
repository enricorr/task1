package com.queetto.zorionak.zorionak.tools;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.queetto.zorionak.zorionak.BubbleHeadService;
import com.queetto.zorionak.zorionak.room.Contacto;
import com.queetto.zorionak.zorionak.room.ContactoDatabase;

import java.util.Calendar;
import java.util.List;

import io.reactivex.schedulers.Schedulers;

/**
 * Proyecto: Zorionak
 * Created by David Nuñez on 03/abr/18.
 */

// Clase que recibe la alarma y ejecuta el trabajo respectivo para habilitar la burbuja
// Limitado a una burbuja cada vez.

public class DetectBirthdayReceiver extends BroadcastReceiver {

    Context context;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        Schedulers.io().createWorker().schedule(this::reviewExistingBirthday);
    }

    // Revisa todos los contactos para saber si alguno cumple años en la fecha actual
    private boolean reviewExistingBirthday(){
        List<Contacto> allContactos = ContactoDatabase
                .getInstance(context)
                .getContactoDao()
                .getAllContacts();

        if (allContactos != null && allContactos.size()>0) {
            final Calendar c = Calendar.getInstance();
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);
            for(Contacto contacto : allContactos) {
                if (contacto.getDia() == day && contacto.getMes() == month && contacto.getAviso() == 0) {
                    if (!isServiceRunning(BubbleHeadService.class)) {
                        updateContacto(contacto.getCid(), 1);
                        Intent intent = new Intent(context, BubbleHeadService.class);
                        Bundle extras = new Bundle();
                        extras.putString("nombre", contacto.getName());
                        extras.putInt("dia", contacto.getDia());
                        extras.putInt("mes", contacto.getMes());
                        extras.putString("uri", contacto.getImagen());
                        intent.putExtras(extras);
                        context.startService(intent);
                        break;
                    }
                } else {
                    if (!(contacto.getDia() == day && contacto.getMes() == month)) {
                        updateContacto(contacto.getCid(), 0);
                    }
                }
            }
        }
        return true;
    }

    // Actualiza al contacto indicando que ya se mostro
    private void updateContacto(int id, int aviso) {
        ContactoDatabase.getInstance(context).getContactoDao().updateContact(id, aviso);
    }

    // Detecta si una burbuja se encuentra presente
    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        if (manager != null) {
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (serviceClass.getName().equals(service.service.getClassName())) {
                    return true;
                }
            }
        }
        return false;
    }

}
