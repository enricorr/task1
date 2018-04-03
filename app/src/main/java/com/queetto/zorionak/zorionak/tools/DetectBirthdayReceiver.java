package com.queetto.zorionak.zorionak.tools;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.queetto.zorionak.zorionak.ChatHeadService;
import com.queetto.zorionak.zorionak.room.Contacto;
import com.queetto.zorionak.zorionak.room.ContactoDatabase;

import java.util.Calendar;
import java.util.List;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Proyecto: zorionak
 * Created by enricorr on 02/04/18.
 */
public class DetectBirthdayReceiver extends BroadcastReceiver {

    Context context;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        Single.fromCallable(() -> reviewExistingBirthday())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((result) -> {});
    }

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
                if (contacto.getDia() == day && contacto.getMes() == month) {
                    context.startService(new Intent(context, ChatHeadService.class));
                }
            }
        }
        return true;

    }
}
