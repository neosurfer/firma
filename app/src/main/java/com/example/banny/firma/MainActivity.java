package com.example.banny.firma;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.github.gcacace.signaturepad.views.SignaturePad;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {
    private SignaturePad signaturePad;
    private Button btn_firmar;
    private Button btn_limpiar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_firmar = (Button)findViewById(R.id.btn_firmar);
        btn_limpiar = (Button)findViewById(R.id.btn_limpiar);

        signaturePad = (SignaturePad)findViewById(R.id.signature_pad);
        signaturePad.setOnSignedListener(new SignaturePad.OnSignedListener(){
            @Override
            public void onStartSigning() {
                Log.e("Mensaje","inicio de la firma");
            }

            @Override
            public void onSigned() {

            }

            @Override
            public void onClear() {
                Log.e("Mensaje","Se limpio el pad");
            }
        });

        btn_firmar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Firmar();
            }
        });

        btn_limpiar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Limpiar();
            }
        });

    }

    private void Limpiar(){
        signaturePad.clear();
    }

    private void Firmar(){
        //signaturePad.getSignatureBitmap() con este metodo se obtiene el bitmap de la firma
        if(Guardar_En_Galeria(signaturePad.getSignatureBitmap())){
            Toast.makeText(getApplicationContext(), "Se guardo la firma", Toast.LENGTH_SHORT).show();
            Limpiar();
        }else{
            Toast.makeText(getApplicationContext(), "Ocurrio un error al guardar", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean Guardar_En_Galeria(Bitmap signature) {
        boolean result = false;
        try {
            //obtenemos la carpeta y el nombre  del file donde se almacenaran las firmas, en caso no exista se crea la
            // carpeta.
            File photo = new File(getAlbumStorageDir("Firmas"), String.format("firma_%d.jpg", System.currentTimeMillis()));
            saveBitmapToJPG(signature, photo);// almacenamos el bitmap como imagen
            scanMediaFile(photo);//hace el llamado broadcast para que el intent de la galeria se actualize
            result = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public File getAlbumStorageDir(String albumName) {
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), albumName);
        if (!file.mkdirs()) {
            Log.e("Firmas", "No se pudo crear la carpeta de firmas");
        }
        return file;
    }

    public void saveBitmapToJPG(Bitmap bitmap, File photo) throws IOException {
        //el bitmap lo pasamos a un canvas para crearla como imagen
        Bitmap newBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(newBitmap);
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(bitmap, 0, 0, null);
        OutputStream stream = new FileOutputStream(photo);
        newBitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
        stream.close();
    }

    private void scanMediaFile(File photo) {
        //si no llamamos a este metodo la imagen no se va a ver automaticamente
        //sino hasta que desmontemos el dispositivo.
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(photo);
        mediaScanIntent.setData(contentUri);
        MainActivity.this.sendBroadcast(mediaScanIntent);
    }
}
