package com.queetto.zorionak.zorionak;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.queetto.zorionak.zorionak.room.Contacto;
import com.queetto.zorionak.zorionak.room.ContactoDatabase;
import com.queetto.zorionak.zorionak.tools.DetectBirthdayReceiver;
import com.queetto.zorionak.zorionak.tools.Utility;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static android.os.Environment.getExternalStoragePublicDirectory;


public class MainActivity extends AppCompatActivity implements PickFecha.Results {

    private static final int CODE_DRAW_OVER_OTHER_APP_PERMISSION = 2084;
    private static final int CODE_CAMERA_INTENT = 2085;
    private static final int CODE_GALLERY_INTENT = 2086;
    public final static String settingAlarm = "settingAlarm";

    @BindView(R.id.edtNombre) EditText edtNombre;
    @BindView(R.id.tvFechaNacimiento) TextView tvFechaNacimiento;
    @BindView(R.id.btnfecha) Button btnFecha;
    @BindView(R.id.btnCamara) Button btnCamara;
    @BindView(R.id.btnGuardar) Button btnGuardar;
    @BindView(R.id.tvCountContacts) TextView tvCountContactos;
    @BindView(R.id.ivfoto) ImageView ivFoto;

    private int mes = 0;
    private int dia = 0;
    private String uriString="";
    private String mCurrentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainactivity);
        ButterKnife.bind(this);
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
            settingBirthdayReceiver();
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
                            .subscribe((result) -> {
                                Toast.makeText(this,
                                        "Contacto Agregado. Zorionak le avisará de su cumpleñaos",
                                        Toast.LENGTH_SHORT).show();
                                finish();});
                }
            } else {
                edtNombre.setError("Debe ingresar un nombre");
            }
        });

        btnCamara.setOnClickListener(view -> selectImage());

        Single.fromCallable(()->countContactosBirthday())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((result) -> {
                    String total = "Total Contactos: " + String.format(Locale.US,"%02d", result);
                    tvCountContactos.setText(total);
            });
    }

    private boolean guardaInformacion(String nombre, int mes, int dia, String uri) {
        ContactoDatabase
                .getInstance(this)
                .getContactoDao()
                .insert(new Contacto(nombre, mes, dia, uri));
        return true;
    }

    private boolean borrarInformacion(String nombre, int mes, int dia, String uri) {
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
                    dispatchTakePictureIntent();
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

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(this)) {
                        initializeView();
                    } else {
                        if (resultCode == RESULT_OK) {
                            initializeView();
                        } else { //Permission is not available
                            Toast.makeText(this,
                                    "No hay permiso para arrastrar sobre otras apps. Se cerrará la aplicación",
                                    Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                    break;

                case CODE_CAMERA_INTENT:
                    if (resultCode == RESULT_OK) {
                        Bundle extras = imageReturnedIntent.getExtras();
                        if (extras!=null) {
                            Bitmap imageBitmap = (Bitmap) extras.get("data");
                            ivFoto.setImageBitmap(imageBitmap);
                            galleryAddPic();
                        }
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
            tvFechaNacimiento.setText(String.format(Locale.US,"Cumpleaños: %02d - %s",dia,Utility.mesString(mes)));
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                return;
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.queetto.zorionak.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent,  CODE_CAMERA_INTENT);
            }
        }
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    private int countContactosBirthday() {
        List<Contacto> allContactos = ContactoDatabase
                .getInstance(this)
                .getContactoDao()
                .getAllContacts();
        return allContactos.size();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mainmenu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void settingBirthdayReceiver() {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        if (!sharedPreferences.getBoolean(settingAlarm, false)) {
            AlarmManager alarmMgr = (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(this, DetectBirthdayReceiver.class);
            PendingIntent alarmIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.HOUR_OF_DAY, 6);

            /*alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, SystemClock.elapsedRealtime(), 10000, alarmIntent);*/

           alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, alarmIntent);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(settingAlarm, true);
            editor.apply();
        }
    }


}




