package com.queetto.zorionak.zorionak;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import com.queetto.zorionak.zorionak.tools.Utility;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Proyecto: Zorionak
 * Created by David Nuñez on 03/abr/18.
 */

// Actividad para mostrar la información del contacto que cumple años. Se despliega cuando el usuario da click en la burbuja.

public class BubbleActivity extends AppCompatActivity {

    @BindView(R.id.ivFotoContacto) ImageView ivFotoContacto;
    @BindView(R.id.tvFelicita) TextView tvFelicita;
    @BindView(R.id.tvFechaContacto)  TextView tvFechaContacto;

    private CompositeDisposable mDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacto);
        ButterKnife.bind(this);
        Bundle extras = getIntent().getExtras();
        String nombre="";
        String uriString="";
        int dia = 0;
        int mes = 0;
        if (extras!=null) {
            nombre = extras.getString("nombre");
            dia = extras.getInt("dia");
            mes = extras.getInt("mes");
            uriString = extras.getString("uri");
        }

        if (uriString!=null && !uriString.equals("")) {
            Uri uriImage = Uri.parse(uriString);
            ivFotoContacto.setImageURI(uriImage);
            setRotation(uriImage, ivFotoContacto);
            ivFotoContacto.setRotation(90);
        } else {
            ivFotoContacto.setImageDrawable(getResources().getDrawable(R.mipmap.ic_launcher));
        }

        String felicita = "Felicita a " + nombre;
        tvFelicita.setText(felicita);
        tvFechaContacto.setText(String.format(Locale.US, "%02d - %s", dia, Utility.mesString(mes)));
    }

    // Ajusta la rotación de la imagen que se muestra al usuario
    private void setRotation(Uri uri, ImageView view) {
        mDisposable.add(
                Single.fromCallable(() -> Utility.getExifData(this, uri))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe((rotationAngle) -> {
                            if (rotationAngle!=0) {
                                view.setRotation(rotationAngle);
                            }
                        }));
    }

    @Override
    protected void onDestroy() {
        mDisposable.dispose();
        super.onDestroy();
    }
}
