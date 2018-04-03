package com.queetto.zorionak.zorionak.tools;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

/**
 * Proyecto: Zorionak
 * Created by David Nuñez on 03/abr/18.
 */


// Esta clase coloca la alarma para detectar las burbujas una vez que el telefono se a booteado.
public class BootReceiver extends BroadcastReceiver {

    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;
    private Context context;


    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            setAlarm();
        }
    }

    // Coloca la Alarma que hará que surja la burbuja  - Configurada todos los dias a las 6am
    // Solo se coloca una vez.
    private void setAlarm() {
        alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, DetectBirthdayReceiver.class);
        alarmIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        // Set the alarm to start at approximately 2:00 p.m.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 6);

        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                        AlarmManager.INTERVAL_DAY, alarmIntent);
    }

}
