package com.example.billfactu;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class EleccionEmpresasActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean esJefe = getIntent().getBooleanExtra("esJefe", false); // El segundo parámetro es el valor por defecto si "esJefe" no se encuentra

        if (esJefe) {
            setContentView(R.layout.activity_eleccion_empresas_jefe);
        } else {
            setContentView(R.layout.activity_eleccion_empresas_no_jefe);
        }
    }
}