package com.example.billfactu;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class Inicio extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;

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

        // Mostrar FacturasFragment por defecto
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new FacturasFragment()).commit();
    }
}