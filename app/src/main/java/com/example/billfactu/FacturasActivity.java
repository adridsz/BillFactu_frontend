package com.example.billfactu;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FacturasActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private FechaAdapter fechaAdapter;
    private List<String> fechas = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_facturas);

        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);

        // Obtener las fechas del Intent
        fechas = getIntent().getStringArrayListExtra("fechas");

        // Agregar un registro de depuración aquí
        Log.d("FacturasActivity", "Fechas: " + fechas);

        fechaAdapter = new FechaAdapter(fechas, new FechaAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(String fecha) {
                descargarFactura(fecha);
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(fechaAdapter);

        fechaAdapter.notifyDataSetChanged();
    }

    private void descargarFactura(String fecha) {
        // Mostrar ProgressBar
        progressBar.setVisibility(View.VISIBLE);
        // Obtener el nombre de la empresa del Intent
        String nombreEmpresa = getIntent().getStringExtra("nombreEmpresa");

        // Obtener el token del usuario de SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("MisPreferencias", MODE_PRIVATE);
        String token = sharedPreferences.getString("token", "");

        OkHttpClient client = new OkHttpClient();
        String url = Server.URL + "descargarfactura/";

        // Crear el cuerpo de la solicitud en formato JSON
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("fecha", fecha);
            // Aquí debes agregar el nombre de la empresa
            jsonObject.put("empresa", nombreEmpresa);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody body = RequestBody.create(JSON, jsonObject.toString());

        // Crear la solicitud
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Authorization", token)
                .build();

        // Enviar la solicitud y manejar la respuesta
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Manejar el error
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    // Manejar la respuesta exitosa
                    String responseStr = response.body().string();

                    // Parsear la respuesta a JSON
                    try {
                        JSONObject jsonObject = new JSONObject(responseStr);
                        String urlFactura = jsonObject.getString("factura");

                        // Aquí debes implementar la lógica para abrir el archivo de la factura
                        // Por ejemplo, puedes abrir el archivo en un navegador web
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlFactura));
                        startActivity(intent);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    // Manejar la respuesta no exitosa
                }

                // Ocultar ProgressBar
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                    }
                });
            }
        });
    }
}