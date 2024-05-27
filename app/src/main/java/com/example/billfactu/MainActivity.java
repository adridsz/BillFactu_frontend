package com.example.billfactu;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); // Llama al método onCreate de la superclase
        setContentView(R.layout.activity_main); // Establece la vista de contenido de esta actividad

        // Leer el token de las SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("MisPreferencias", MODE_PRIVATE);
        String token = sharedPreferences.getString("token", "");

        // Verificar si el token no está vacío
        if (!token.isEmpty()) {
            // El usuario ya ha iniciado sesión

            OkHttpClient client = new OkHttpClient();
            String url = Server.URL + "tokenvalido/";

            // Crear la solicitud
            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .addHeader("Authorization", token)
                    .build();

            // Enviar la solicitud y manejar la respuesta
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    // Manejar el error
                    e.printStackTrace();

                    // Mostrar un diálogo de alerta en el hilo de la interfaz de usuario
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            new AlertDialog.Builder(MainActivity.this)
                                    .setTitle("Error de conexión")
                                    .setMessage("No se pudo conectar al servidor. ¿Deseas reintentar?")
                                    .setPositiveButton("Reintentar", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            // Reintentar la conexión
                                        }
                                    })
                                    .setNegativeButton("Cancelar", null)
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .show();
                        }
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        Intent intent = new Intent(MainActivity.this, Inicio.class); // Crea un nuevo Intent para lanzar Inicio
                        startActivity(intent); // Inicia Inicio
                        finish(); // Cierra MainActivity
                    } else {
                        // Manejar la respuesta no exitosa
                        // El token ha caducado o es inválido, redirigir a LoginActivity
                        Intent intent = new Intent(MainActivity.this, LoginActivity.class); // Crea un nuevo Intent para lanzar LoginActivity
                        startActivity(intent); // Inicia LoginActivity
                        finish(); // Cierra MainActivity
                    }
                }
            });
        } else {
            // El usuario no ha iniciado sesión, redirigir a LoginActivity
            Intent intent = new Intent(this, LoginActivity.class); // Crea un nuevo Intent para lanzar LoginActivity
            startActivity(intent); // Inicia LoginActivity
            finish(); // Cierra MainActivity
        }
    }
}