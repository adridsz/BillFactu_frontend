package com.example.billfactu;


import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Toast;
import android.Manifest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PreFacturasActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private FechaAdapter fechaAdapter;
    private List<String> fechas;
    private String nombreEmpresa;
    private static final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pre_facturas);

        recyclerView = findViewById(R.id.recyclerView);
        fechas = getIntent().getStringArrayListExtra("fechas");
        nombreEmpresa = getIntent().getStringExtra("nombreEmpresa");

        fechaAdapter = new FechaAdapter(fechas, new FechaAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(String fecha) {
                if (ContextCompat.checkSelfPermission(PreFacturasActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    // Permiso no concedido, solicítalo
                    ActivityCompat.requestPermissions(PreFacturasActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                } else {
                    // Permiso ya concedido, puedes proceder con la descarga
                    descargarPreFactura(nombreEmpresa, fecha);
                }
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(fechaAdapter);
    }

    private void descargarPreFactura(String nombreEmpresa, String fecha) {
        SharedPreferences sharedPreferences = getSharedPreferences("MisPreferencias", MODE_PRIVATE);
        String token = sharedPreferences.getString("token", "");

        OkHttpClient client = new OkHttpClient();
        String url = Server.URL + "descargarprefactura/";

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("empresa", nombreEmpresa);
            jsonObject.put("fecha", fecha);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody body = RequestBody.create(JSON, jsonObject.toString());

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Authorization", token)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseStr = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(responseStr);
                        String prefacturaUrl = jsonObject.getString("prefactura");

                        // Concatenar la URL base del servidor con la URL relativa de la factura
                        String fullPreFacturaUrl = Server.URL + prefacturaUrl;

                        // Ahora puedes usar fullFacturaUrl para iniciar la descarga
                        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(fullPreFacturaUrl));
                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "PreFactura.pdf");

                        DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                        downloadManager.enqueue(request);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    // Manejar la respuesta no exitosa
                }
            }
        });
    }

    private void abrirPreFactura(Uri uri) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "application/pdf");
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
    }
}