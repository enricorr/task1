package com.queetto.zorionak.zorionak;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.queetto.zorionak.zorionak.room.Contacto;
import com.queetto.zorionak.zorionak.room.ContactoDatabase;
import com.queetto.zorionak.zorionak.tools.DetectBirthdayReceiver;
import com.queetto.zorionak.zorionak.tools.Utility;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


public class MainActivity extends AppCompatActivity implements PickFecha.Results {

    private static final int CODE_DRAW_OVER_OTHER_APP_PERMISSION = 2084;
    private static final int CODE_CAMERA_INTENT = 2085;
    private static final int CODE_GALLERY_INTENT = 2086;

    @BindView(R.id.edtNombre) EditText edtNombre;
    @BindView(R.id.tvFechaNacimiento) TextView tvFechaNacimiento;
    @BindView(R.id.btnfecha) Button btnFecha;
    @BindView(R.id.btnCamara) Button btnCamara;
    @BindView(R.id.btnGuardar) Button btnGuardar;
    @BindView(R.id.ivfoto) ImageView ivFoto;

    private int mes = 0;
    private int dia = 0;
    private String uriString="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainactivity);
        ButterKnife.bind(this);

        settingBirthdayReceiver();

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
            if (!edtNombre.getText().toString().equals("")) {
                if (mes!=0 && dia!=0) {
                    Single.fromCallable(() -> guardaInformacion(edtNombre.getText().toString(), mes, dia, uriString))
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe((result) -> {finish();});
                }
            } else {
                edtNombre.setError("Debe ingresar un nombre");
            }
        });

        btnCamara.setOnClickListener(view -> selectImage());

/*            findViewById(R.id.notify_me).setOnClickListener(view -> {
                startService(new Intent(MainActivity.this, ChatHeadService.class));
                finish();
            });*/
    }

    private boolean guardaInformacion(String nombre, int mes, int dia, String uri) {
        ContactoDatabase
                .getInstance(this)
                .getContactoDao()
                .insert(new Contacto(nombre, mes, dia, uri));
        return true;
    }

    private void selectImage() {
        final CharSequence[] items = { "Tomar Foto", "Escoger una foto",
                "Cancelar" };
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Agregar Imagen!");
        builder.setItems(items, (dialog, item) -> {
            boolean result= Utility.checkPermission(MainActivity.this);
            if (items[item].equals("Tomar Foto")) {
                //userChoosenTask="Tomar Foto";
                if(result)
                    cameraIntent();
            } else if (items[item].equals("Escoger una foto")) {
                //userChoosenTask="Escoger una foto";
                if(result)
                    galleryIntent();
            } else if (items[item].equals("Cancel")) {
                dialog.dismiss();
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
        protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {

            switch(requestCode) {
                case CODE_DRAW_OVER_OTHER_APP_PERMISSION:
                    //Check if the permission is granted or not.
                    if (resultCode == RESULT_OK) {
                        initializeView();
                    } else { //Permission is not available
                        Toast.makeText(this,
                                "No hay permiso para arrastrar sobre otras apps. Se cerrará la aplicación",
                                Toast.LENGTH_SHORT).show();

                        finish();
                    }
                    break;

                case CODE_CAMERA_INTENT:
                    if (resultCode == RESULT_OK) {
                        Bundle extras = imageReturnedIntent.getExtras();
                        Bitmap imageBitmap = (Bitmap) extras.get("data");
                        ivFoto.setImageBitmap(imageBitmap);
                    }
                    break;
                case CODE_GALLERY_INTENT:
                    if(resultCode == RESULT_OK){
                        Uri selectedImage = imageReturnedIntent.getData();
                        if (selectedImage!=null)
                            uriString = selectedImage.toString();
                        else
                            uriString = "";
                        ivFoto.setImageURI(selectedImage);
                    }

                    break;
                default:
                    super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

            }
        }

    @Override
    public void result_dia(int dia) {
        this.dia = dia;
    }

    @Override
    public void result_mes(int mes) {
        this.mes = mes;
    }

    @Override
    public void setFechaNacimiento() {
        if (dia!=0 && mes!=0) {
            tvFechaNacimiento.setText(String.format(Locale.US,"Cumpleaños: %02d/%02d",dia,mes));
        }
    }



    private void settingBirthdayReceiver() {
        AlarmManager alarmMgr = (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, DetectBirthdayReceiver.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.HOUR_OF_DAY, 6);

            /*alarmMgr.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+5000, alarmIntent);*/

            alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY, alarmIntent);

    }

}




