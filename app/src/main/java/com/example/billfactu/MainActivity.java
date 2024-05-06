package com.example.billfactu;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); // Llama al método onCreate de la superclase
        setContentView(R.layout.activity_main); // Establece la vista de contenido de esta actividad

        Intent intent = new Intent(this, LoginActivity.class); // Crea un nuevo Intent para lanzar LoginActivity
        startActivity(intent); // Inicia LoginActivity
        finish(); // Cierra MainActivity
    }
}