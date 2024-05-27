package com.example.billfactu;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Inicio extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;
    private List<String> empresas = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio);

        // Inicializar BottomNavigationView
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Configurar el listener de selección de items en BottomNavigationView
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;

                // Determinar qué fragmento mostrar basado en el item seleccionado
                int itemId = item.getItemId();
                if (itemId == R.id.item1) {
                    selectedFragment = new FacturasFragment();
                } else if (itemId == R.id.item2) {
                    selectedFragment = new PreFacturasFragment();
                } else if (itemId == R.id.item3) {
                    selectedFragment = new ConfiguracionFragment();
                }

                // Reemplazar el fragmento actual con el fragmento seleccionado
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();

                return true; // Devuelve true para indicar que el evento de selección ha sido manejado
            }
        });

        // Leer el token de las SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("MisPreferencias", MODE_PRIVATE);
        String token = sharedPreferences.getString("token", "");

        // Crear el cliente HTTP
        OkHttpClient client = new OkHttpClient();

        // Crear la solicitud
        Request request = new Request.Builder()
                .url(Server.URL + "inicio/")
                .get()
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
                        if (jsonObject.has("empresa")) {
                            empresas.add(jsonObject.getString("empresa"));
                        } else if (jsonObject.has("empresas")) {
                            JSONArray empresasArray = jsonObject.getJSONArray("empresas");
                            for (int i = 0; i < empresasArray.length(); i++) {
                                empresas.add(empresasArray.getString(i));
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    // Manejar la respuesta no exitosa
                }
            }
        });

        // Mostrar FacturasFragment por defecto
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new FacturasFragment()).commit();
    }
}