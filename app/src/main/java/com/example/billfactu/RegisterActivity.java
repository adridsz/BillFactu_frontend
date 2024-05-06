package com.example.billfactu;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
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
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RegisterActivity extends AppCompatActivity {
    private EditText usuarioEditText;
    private EditText correoEditText;
    private EditText contrasenaEditText;
    private EditText confirmarContrasenaEditText;
    private CheckBox EsJefeCheckBox;
    private Button registrarseButton;
    private Button iniciarSesionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        usuarioEditText = findViewById(R.id.username);
        correoEditText = findViewById(R.id.email);
        contrasenaEditText = findViewById(R.id.password);
        confirmarContrasenaEditText = findViewById(R.id.confirm_password);
        EsJefeCheckBox = findViewById(R.id.is_boss);
        registrarseButton = findViewById(R.id.register);
        registrarseButton.setOnClickListener(v -> registrarse());
        iniciarSesionButton = findViewById(R.id.login);
        iniciarSesionButton.setOnClickListener(v -> iniciarSesion());

    }

    private void registrarse() {
        String usuario = usuarioEditText.getText().toString();
        String correo = correoEditText.getText().toString();
        String contrasena = contrasenaEditText.getText().toString();
        String confirmarContrasena = confirmarContrasenaEditText.getText().toString();
        boolean esJefe = EsJefeCheckBox.isChecked();

        if (usuario.isEmpty() || correo.isEmpty() || contrasena.isEmpty() || confirmarContrasena.isEmpty()) {
            Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!contrasena.equals(confirmarContrasena)) {
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
            return;
        }

        OkHttpClient client = new OkHttpClient();
        String url = Server.URL + "register/";

        MediaType mediaType = MediaType.parse("application/json");
        JSONObject json = new JSONObject();
        try {
            json.put("nombre", usuario);
            json.put("correo", correo);
            json.put("contrasena", contrasena);
            json.put("jefe", esJefe);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody body = RequestBody.create(mediaType, json.toString());

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

                        // Guardar el token y el valor de "jefe" en SharedPreferences
                        SharedPreferences sharedPreferences = getSharedPreferences("MisPreferencias", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("token", token);
                        editor.putBoolean("esJefe", esJefe); // Usar el valor de la casilla de verificación directamente
                        editor.apply();

                        // Navegar a la actividad principal
                        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    // Manejar la respuesta no exitosa
                    String responseStr = response.body().string();
                    String mensajeError = "Error al registrarse";

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

    private void iniciarSesion() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}