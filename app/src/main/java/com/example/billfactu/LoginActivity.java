package com.example.billfactu;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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
            return;
        }
    }

    private void registrarse() {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }
}