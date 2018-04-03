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

public class BubbleActivity extends AppCompatActivity {

    @BindView(R.id.ivFotoContacto) ImageView ivFotoContacto;
    @BindView(R.id.tvFelicita) TextView tvFelicita;
    @BindView(R.id.tvFechaContacto)  TextView tvFechaContacto;

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
        } else {
            ivFotoContacto.setImageDrawable(getResources().getDrawable(R.mipmap.ic_launcher));
        }

        String felicita = "Felicita a " + nombre;
        tvFelicita.setText(felicita);
        tvFechaContacto.setText(String.format(Locale.US, "%02d - %s", dia, Utility.mesString(mes)));
    }

}
