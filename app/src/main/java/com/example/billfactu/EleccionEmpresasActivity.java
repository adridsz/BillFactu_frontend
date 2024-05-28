package com.example.billfactu;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
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

public class EleccionEmpresasActivity extends AppCompatActivity {
    private EditText searchEditText;
    private Button searchButton;
    private RecyclerView recyclerView;
    private EmpresaAdapter empresaAdapter;
    private List<String> empresas = new ArrayList<>();
    private EditText nombreEmpresaEditText;
    private Button crearEmpresaButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean esJefe = getIntent().getBooleanExtra("esJefe", false);

        if (esJefe) {
            setContentView(R.layout.activity_eleccion_empresas_jefe);

            nombreEmpresaEditText = findViewById(R.id.nombreEmpresaEditText);
            crearEmpresaButton = findViewById(R.id.crearEmpresaButton);

            crearEmpresaButton.setOnClickListener(v -> crearEmpresa());
        } else {
            setContentView(R.layout.activity_eleccion_empresas_no_jefe);

            searchEditText = findViewById(R.id.searchEditText);
            searchButton = findViewById(R.id.searchButton);
            recyclerView = findViewById(R.id.recyclerView);

            empresaAdapter = new EmpresaAdapter(empresas, new EmpresaAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(String empresa) {
                    unirseAEmpresa(empresa);
                }
            });
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(empresaAdapter);

            searchButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    buscarEmpresas();
                }
            });

            // Realizar una búsqueda inicial con una cadena vacía para mostrar todas las empresas
            buscarEmpresas();
        }
    }

    private void crearEmpresa() {
        String nombreEmpresa = nombreEmpresaEditText.getText().toString();

        // Obtener el token del usuario de SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("MisPreferencias", MODE_PRIVATE);
        String token = sharedPreferences.getString("token", "");

        OkHttpClient client = new OkHttpClient();
        String url = Server.URL + "crearempresas/";

        // Crear el cuerpo de la solicitud en formato JSON
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("nombre", nombreEmpresa);
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
                    // Si la creación de la empresa fue exitosa, navegar a la pantalla de inicio
                    Intent intent = new Intent(EleccionEmpresasActivity.this, Inicio.class);
                    startActivity(intent);
                    finish();
                } else {
                    // Manejar la respuesta no exitosa
                }
            }
        });
    }

    private void unirseAEmpresa(String nombreEmpresa) {
        // Obtener el token del usuario de SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("MisPreferencias", MODE_PRIVATE);
        String token = sharedPreferences.getString("token", "");

        OkHttpClient client = new OkHttpClient();
        String url = Server.URL + "unirempresa/";

        // Crear el cuerpo de la solicitud en formato JSON
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("nombre", nombreEmpresa);
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
                    // Si la unión a la empresa fue exitosa, navegar a la pantalla de inicio
                    Intent intent = new Intent(EleccionEmpresasActivity.this, Inicio.class);
                    startActivity(intent);
                    finish();
                } else {
                    // Manejar la respuesta no exitosa
                }
            }
        });
    }

    private void buscarEmpresas() {
        String empresaQuery = searchEditText.getText().toString();

        // Obtener el token del usuario de SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("MisPreferencias", MODE_PRIVATE);
        String token = sharedPreferences.getString("token", "");

        OkHttpClient client = new OkHttpClient();
        String url = Server.URL + "empresas";
        if (!empresaQuery.isEmpty()) {
            url += "?empresa=" + empresaQuery;
        }

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("Authorization", token) // Usar el token del usuario para la autorización
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
                        JSONArray jsonArray = new JSONArray(responseStr);
                        empresas.clear();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            empresas.add(jsonArray.getString(i));
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                empresaAdapter.notifyDataSetChanged();
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}