package com.queetto.zorionak.zorionak;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.queetto.zorionak.zorionak.tools.Utility;

import butterknife.BindView;


public class MainActivity extends AppCompatActivity {

        private static final int CODE_DRAW_OVER_OTHER_APP_PERMISSION = 2084;
    private static final int CODE_CAMERA_INTENT = 2085;
    private static final int CODE_GALLERY_INTENT = 2086;

        @BindView(R.id.edtNombre) EditText edtNombre;
        @BindView(R.id.tvFechaNacimiento) TextView tvFechaNacimiento;
        @BindView(R.id.btnfecha) Button btnFecha;
        @BindView(R.id.btnCamara) Button btnCamara;
        @BindView(R.id.btnGuardar) Button btnGuardar;

        int mes = 0;
        int dia = 0;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            //Check if the application has draw over other apps permission or not?
            //This permission is by default available for API<23. But for API > 23
            //you have to ask for the permission in runtime.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {

                //If the draw over permission is not available open the settings screen
                //to grant the permission.
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, CODE_DRAW_OVER_OTHER_APP_PERMISSION);
            } else {
                initializeView();
            }
        }

        /**
         * Set and initialize the view elements.
         */
        private void initializeView() {

            btnFecha.setOnClickListener(view -> {
                DialogFragment newFragment = new PickFecha();
                newFragment.show(getSupportFragmentManager(), "datePicker");
            });

            btnGuardar.setOnClickListener(view -> {
                if (!edtNombre.getText().equals("")) {
                    if (mes!=0 && dia!=0) {
                        guardaInformacion();
                    }
                } else {
                    edtNombre.setError("Debe ingresar un nombre");
                }
            });

            findViewById(R.id.notify_me).setOnClickListener(view -> {
                startService(new Intent(MainActivity.this, ChatHeadService.class));
                finish();
            });
        }

        private void guardaInformacion() {

        }

    private void selectImage() {
        final CharSequence[] items = { "Tomar Foto", "Escoger una foto",
                "Cancelar" };
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Agregar Imagen!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                boolean result= Utility.checkPermission(MainActivity.this);
                if (items[item].equals("Tomar Foto")) {
                    userChoosenTask="Tomar Foto";
                    if(result)
                        cameraIntent();
                } else if (items[item].equals("Escoger una foto")) {
                    userChoosenTask="Escoger una foto";
                    if(result)
                        galleryIntent();
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    private void cameraIntent() {
        Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(takePicture, CODE_CAMERA_INTENT);
    }

    private void galleryIntent() {
        Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhoto , CODE_GALLERY_INTENT);
    }

        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            if (requestCode == CODE_DRAW_OVER_OTHER_APP_PERMISSION) {

                //Check if the permission is granted or not.
                if (resultCode == RESULT_OK) {
                    initializeView();
                } else { //Permission is not available
                    Toast.makeText(this,
                            "No hay permiso para arrastrar sobre otras apps. Se cerrará la aplicación",
                            Toast.LENGTH_SHORT).show();

                    finish();
                }
            } else {
                super.onActivityResult(requestCode, resultCode, data);
            }
        }
}
