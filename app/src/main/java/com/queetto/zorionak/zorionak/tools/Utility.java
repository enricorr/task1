package com.queetto.zorionak.zorionak.tools;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.media.ExifInterface;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

/**
 * Proyecto: Zorionak
 * Created by David Nuñez on 03/abr/18.
 */

public class Utility {
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;


    // Se solicita paermiso para escritura de fotos (en caso necesario)
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static boolean checkPermission(final Context context)
    {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if(currentAPIVersion>=android.os.Build.VERSION_CODES.M)
        {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
                    alertBuilder.setCancelable(true);
                    alertBuilder.setTitle("Permiso necesario");
                    alertBuilder.setMessage("Permiso de Almacenamiento Externo es necesario");
                    alertBuilder.setPositiveButton(android.R.string.yes, (dialog, which) -> ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE));
                    AlertDialog alert = alertBuilder.create();
                    alert.show();
                } else {
                    ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                }
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    // Conversión de mes (numerico a letras). mes entero de 0 a 11
    public static String mesString(int mes) {
        List<String> meses = Arrays.asList("Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic");
        if (mes >=0 && mes <=11) {
            return meses.get(mes);
        } else {
            return "";
        }
    }

    // Obtiene la información de orientación de la foto y devuelve la rotación de ajuste para que se vea bien.
    public static int getExifData(Context context, Uri uri) {
        try (InputStream inputStream = context.getContentResolver().openInputStream(uri)) {
            if (inputStream!=null) {
                ExifInterface exif = new ExifInterface(inputStream);
                String orientString = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
                int orientation = orientString != null ? Integer.parseInt(orientString) :  ExifInterface.ORIENTATION_NORMAL;
                int rotationAngle = 0;
                if (orientation == ExifInterface.ORIENTATION_ROTATE_90) rotationAngle = 90;
                if (orientation == ExifInterface.ORIENTATION_ROTATE_180) rotationAngle = 180;
                if (orientation == ExifInterface.ORIENTATION_ROTATE_270) rotationAngle = 270;
                return rotationAngle;
            } else {
                return 0;
            }
        } catch (IOException e) {
            return 0;
        }
    }

}
