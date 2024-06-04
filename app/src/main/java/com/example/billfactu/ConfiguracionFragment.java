package com.example.billfactu;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
public class ConfiguracionFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_configuracion, container, false);

        // Obtén una referencia al botón de cierre de sesión
        Button cerrarSesionButton = view.findViewById(R.id.btnCerrarSesion);

        // Configura un OnClickListener para el botón de cierre de sesión
        cerrarSesionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Leer el token de las SharedPreferences
                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MisPreferencias", MODE_PRIVATE);
                String token = sharedPreferences.getString("token", "");

                // Crear el cliente HTTP
                OkHttpClient client = new OkHttpClient();

                // Crear la solicitud
                Request request = new Request.Builder()
                        .url(Server.URL + "logout/")
                        .post(new FormBody.Builder().build()) // cuerpo vacío
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
                            // Borrar las SharedPreferences
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.clear();
                            editor.apply();

                            // Navegar de vuelta a la actividad de inicio de sesión
                            Intent intent = new Intent(getActivity(), LoginActivity.class);
                            startActivity(intent);
                            getActivity().finish();
                        } else {
                            // Manejar la respuesta no exitosa
                        }
                    }
                });
            }
        });

        return view;
    }
}