package com.queetto.zorionak.zorionak;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.FileProvider;
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
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;


/**
 * Proyecto: Zorionak
 * Created by David Nuñez on 03/abr/18.
 */

public class MainActivity extends AppCompatActivity implements PickFecha.Results {

    private static final int CODE_DRAW_OVER_OTHER_APP_PERMISSION = 2084;
    private static final int CODE_CAMERA_INTENT = 2085;
    private static final int CODE_GALLERY_INTENT = 2086;
    private CompositeDisposable mDisposable = new CompositeDisposable();

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
    private Uri contentUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainactivity);
        ButterKnife.bind(this);

        // Revisa Permisos para colocar burbuja sobre otras apps.
        // Solicita los permisos en tiempo de ejecución al usuario.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, CODE_DRAW_OVER_OTHER_APP_PERMISSION);
        } else {
            settingBirthdayReceiver();
            initializeView();
        }
    }


    // Inicializa las vistas con sus respectivos Listeners (Buttones y Campos de Edición)
    private void initializeView() {

        // Inicia Dialogo para obtener fecha de cumpleaños.
        btnFecha.setOnClickListener(view -> {
            DialogFragment newFragment = new PickFecha();
            newFragment.show(getSupportFragmentManager(), "datePicker");
        });

        // Acción para guardar los datos en la BD Local
        btnGuardar.setOnClickListener(view -> {
            if (!edtNombre.getText().toString().equals("")) {
                if (mes!=0 && dia!=0) {
                    mDisposable.add(
                    Single.fromCallable(() -> guardaInformacion(edtNombre.getText().toString(), mes, dia, uriString))
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe((result) -> {
                                Toast.makeText(this,
                                        "Contacto Agregado. Zorionak le avisará de su cumpleaños",
                                        Toast.LENGTH_LONG).show();
                                finish();}));
                } else {
                    tvFechaNacimiento.setError("Falta fecha de nacimiento");
                }
            } else {
                edtNombre.setError("Falta nombre");
            }
        });

        // Acción cundo se quiere foto
        btnCamara.setOnClickListener(view -> selectImage());

        // Muestra la cuenta de contactos en la app
        mDisposable.add(
        Single.fromCallable(this::countContactosBirthday)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((result) -> {
                    String total = "Total Contactos: " + String.format(Locale.US,"%02d", result);
                    tvCountContactos.setText(total);
            }));
    }

    // Guarda el Contacto
    private boolean guardaInformacion(String nombre, int mes, int dia, String uri) {
        ContactoDatabase
                .getInstance(this)
                .getContactoDao()
                .insert(new Contacto(nombre, mes, dia, uri));
        return true;
    }

    // Dialogo para seleccionar si quiere una foto o escoge una ya existe.
    private void selectImage() {
        final CharSequence[] items = { "Tomar Foto", "Escoger una foto",
                "Cancelar" };
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Agregar Imagen!");
        builder.setItems(items, (dialog, item) -> {
            boolean result= Utility.checkPermission(MainActivity.this);
            if (items[item].equals("Tomar Foto")) {
                if(result)
                    dispatchTakePictureIntent();
            } else if (items[item].equals("Escoger una foto")) {
                if(result)
                    galleryIntent();
            } else if (items[item].equals("Cancel")) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    // Inicia el programa para seleccionar una foto
    private void galleryIntent() {
        Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhoto , CODE_GALLERY_INTENT);
    }

    // Resultado de escoger o tomar una foto y otorgar permisos para poner burbujas.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {

        switch(requestCode) {
            // Comprobación de si la app tiene permiso para usar las burbujas
            case CODE_DRAW_OVER_OTHER_APP_PERMISSION:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(this)) {
                    settingBirthdayReceiver();
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
            // Resultado de tomar una foto
            case CODE_CAMERA_INTENT:
                if (resultCode == RESULT_OK) {
                    galleryAddPic();
                    if (contentUri!=null) {
                        ivFoto.setImageURI(contentUri);
                        setRotation(contentUri, ivFoto);
                        uriString = contentUri.toString();
                    } else {
                        uriString = "";
                    }
                }
                break;
            // Resultado de escoger una foto
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

    // Almacena dia escogido por usuario
    @Override
    public void result_dia(int dia) {
        this.dia = dia;
    }

    // Almacena mes escogido por usuario
    @Override
    public void result_mes(int mes) {
        this.mes = mes;
    }

    // Coloca fecha de cumpleaños seleccionada por el usuario.
    @Override
    public void setFechaNacimiento() {
        if (dia!=0 && mes!=0) {
            tvFechaNacimiento.setError(null);
            tvFechaNacimiento.setText(String.format(Locale.US,"Cumpleaños: %02d - %s",dia,Utility.mesString(mes)));
        }
    }

    // Crea un archivo de imagen en el disco del celular para almacenar la foto tomada por el usuario
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        //File storageDir = getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    // Habilita la camara solo en el caso de que se pueda crear el archivo para almacenar la foto
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
                        "com.queetto.zorionak.zorionak.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent,  CODE_CAMERA_INTENT);
            }
        }
    }

    // Una vez que se toma la foto esta se almacena en la galeria privada del app.
    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    // Cuenta los contactos de la base de datos local del telefono
    private int countContactosBirthday() {
        List<Contacto> allContactos = ContactoDatabase
                .getInstance(this)
                .getContactoDao()
                .getAllContacts();
        return allContactos.size();
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

    // Coloca la Alarma que hará que surja la burbuja  - Configurada todos los dias a las 6am
    // Solo se coloca una vez.
    private void settingBirthdayReceiver() {
        AlarmManager alarmMgr = (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, DetectBirthdayReceiver.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 6);

        if (alarmMgr!=null) {
            //alarmMgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), 60000, alarmIntent);

            alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                        AlarmManager.INTERVAL_DAY, alarmIntent);

        }
    }

    @Override
    protected void onDestroy() {
        mDisposable.dispose();
        super.onDestroy();
    }
}




