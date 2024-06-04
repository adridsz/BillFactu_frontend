package com.example.billfactu;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
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
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {
    private EditText usuarioEditText;
    private EditText contrasenaEditText;
    private Button iniciarSesionButton;
    private Button registrarseButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usuarioEditText = findViewById(R.id.username);
        contrasenaEditText = findViewById(R.id.password);

        iniciarSesionButton = findViewById(R.id.login);
        iniciarSesionButton.setOnClickListener(v -> iniciarSesion());

        registrarseButton = findViewById(R.id.register);
        registrarseButton.setOnClickListener(v -> registrarse());

    }

    private void iniciarSesion() {
        String usuario = usuarioEditText.getText().toString();
        String contrasena = contrasenaEditText.getText().toString();

        if (usuario.isEmpty() || contrasena.isEmpty()) {
            Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        } else {
            OkHttpClient client = new OkHttpClient();
            String url = Server.URL + "login/";

            // Crear el cuerpo de la solicitud en formato JSON
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("nombre", usuario);
                jsonObject.put("contrasena", contrasena);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            RequestBody body = RequestBody.create(JSON, jsonObject.toString());

            // Crear la solicitud
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
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
                            String token = jsonObject.getString("token");
                            boolean esJefe = jsonObject.getBoolean("jefe");

                            // Guardar el token y el valor de "jefe" en SharedPreferences
                            SharedPreferences sharedPreferences = getSharedPreferences("MisPreferencias", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("token", token);
                            editor.putBoolean("esJefe", esJefe);
                            editor.apply();

                            // Navegar a la actividad Inicio
                            Intent intent = new Intent(LoginActivity.this, Inicio.class);
                            startActivity(intent);
                            finish(); // Finalizar la actividad de inicio de sesión

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        // Manejar la respuesta no exitosa
                        String responseStr = response.body().string();
                        String mensajeError = "Error al conectarse con el servidor";

                        try {
                            // Intentar parsear el error del servidor
                            JSONObject jsonObject = new JSONObject(responseStr);
                            mensajeError = jsonObject.getString("error");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        final String finalMensajeError = mensajeError;

                        // Mostrar el error en un Snackbar
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Snackbar.make(findViewById(R.id.main), finalMensajeError, Snackbar.LENGTH_LONG).show();
                            }
                        });
                    }
                }
            });
        }
    }

    private void registrarse() {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
        finish();
    }
}