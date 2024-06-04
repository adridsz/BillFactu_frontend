package com.example.billfactu;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PreFacturasFragment extends Fragment {
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private EmpresaAdapter empresaAdapter;
    private List<String> empresas = new ArrayList<>();
    private boolean esJefe;
    private static final int PICK_FILE_REQUEST_CODE = 1;
    private Uri selectedFileUri;
    private int selectedYear, selectedMonth, selectedDay;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Obtenemos el valor de "jefe" de SharedPreferences
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MisPreferencias", getActivity().MODE_PRIVATE);
        esJefe = sharedPreferences.getBoolean("esJefe", false);
        if (esJefe) {
            View view = inflater.inflate(R.layout.fragment_pre_facturas_jefe, container, false);

            Button selectFileButton = view.findViewById(R.id.selectFileButton);
            selectFileButton.setOnClickListener(v -> selectFile());

            Button selectDateButton = view.findViewById(R.id.selectDateButton);
            selectDateButton.setOnClickListener(v -> selectDate());

            Button uploadButton = view.findViewById(R.id.uploadButton);
            uploadButton.setOnClickListener(v -> uploadPreFactura());

            return view;
        } else {
            View view = inflater.inflate(R.layout.fragment_pre_facturas_no_jefe, container, false);

            recyclerView = view.findViewById(R.id.recyclerView);
            progressBar = view.findViewById(R.id.progressBar);

            empresaAdapter = new EmpresaAdapter(empresas, new EmpresaAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(String empresa) {
                    obtenerPreFacturas(empresa);
                }
            });
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerView.setAdapter(empresaAdapter);

            obtenerEmpresas();

            return view;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_FILE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                // Mostrar un mensaje de error
                Toast.makeText(getContext(), "No se seleccionó ningún archivo", Toast.LENGTH_SHORT).show();
                return;
            }
            // Obtener la URI del archivo seleccionado
            selectedFileUri = data.getData();
        }
    }

    private void selectFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        startActivityForResult(intent, PICK_FILE_REQUEST_CODE);
    }

    private void uploadPreFactura() {
        if (selectedFileUri == null) {
            Toast.makeText(getContext(), "Por favor, selecciona un archivo", Toast.LENGTH_SHORT).show();
            return;
        }

        // Obtener el token del usuario de SharedPreferences
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MisPreferencias", getActivity().MODE_PRIVATE);
        String token = sharedPreferences.getString("token", "");

        // Crear el cliente HTTP
        OkHttpClient client = new OkHttpClient();

        // Usar la fecha seleccionada
        String fecha = selectedYear + "-" + String.format("%02d", selectedMonth + 1) + "-" + String.format("%02d", selectedDay);

        // Crear el cuerpo de la solicitud
        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("fecha", fecha);

        // Intentar abrir un InputStream del archivo seleccionado
        try {
            InputStream inputStream = getContext().getContentResolver().openInputStream(selectedFileUri);
            byte[] fileBytes = getBytes(inputStream);
            RequestBody requestBody = RequestBody.create(MediaType.parse(getContext().getContentResolver().getType(selectedFileUri)), fileBytes);
            builder.addFormDataPart("prefactura", "prefactura", requestBody);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        MultipartBody multipartBody = builder.build();

        // Crear la solicitud
        Request request = new Request.Builder()
                .url(Server.URL + "subirprefactura/")
                .post(multipartBody)
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
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getContext(), "Prefactura subida correctamente", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    private void obtenerEmpresas() {
        // Mostrar ProgressBar
        progressBar.setVisibility(View.VISIBLE);

        // Obtener el token del usuario de SharedPreferences
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MisPreferencias", getActivity().MODE_PRIVATE);
        String token = sharedPreferences.getString("token", "");

        OkHttpClient client = new OkHttpClient();
        String url = Server.URL + "verempresas/";

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
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    // Manejar la respuesta exitosa
                    String responseStr = response.body().string();

                    // Parsear la respuesta a JSON
                    try {
                        JSONArray jsonArray = new JSONArray(responseStr);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            empresas.add(jsonArray.getString(i));
                        }

                        // Actualizar el RecyclerView en el hilo principal
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                empresaAdapter.notifyDataSetChanged();
                                recyclerView.setVisibility(View.VISIBLE);
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    // Manejar la respuesta no exitosa
                }

                // Ocultar ProgressBar en el hilo principal
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                    }
                });
            }
        });
    }

    private void obtenerPreFacturas(String nombreEmpresa) {
        // Mostrar ProgressBar
        progressBar.setVisibility(View.VISIBLE);

        // Obtener el token del usuario de SharedPreferences
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MisPreferencias", getActivity().MODE_PRIVATE);
        String token = sharedPreferences.getString("token", "");

        OkHttpClient client = new OkHttpClient();
        String url = Server.URL + "prefacturas/";

        // Crear el cuerpo de la solicitud en formato JSON
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("empresa", nombreEmpresa);
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
                    // Manejar la respuesta exitosa
                    String responseStr = response.body().string();

                    // Parsear la respuesta a JSON
                    try {
                        JSONObject jsonObject = new JSONObject(responseStr);
                        JSONArray fechasArray = jsonObject.getJSONArray("fechas");
                        List<String> fechas = new ArrayList<>();
                        for (int i = 0; i < fechasArray.length(); i++) {
                            fechas.add(fechasArray.getString(i));
                        }

                        // Navegar a la nueva Activity con las prefacturas
                        Intent intent = new Intent(getActivity(), PreFacturasActivity.class);
                        intent.putStringArrayListExtra("fechas", new ArrayList<>(fechas));
                        intent.putExtra("nombreEmpresa", nombreEmpresa); // Agregar el nombre de la empresa como un extra
                        startActivity(intent);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    // Manejar la respuesta no exitosa
                }

                // Ocultar ProgressBar
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                    }
                });
            }
        });
    }

    private void selectDate() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                (view, year1, monthOfYear, dayOfMonth) -> {
                    selectedYear = year1;
                    selectedMonth = monthOfYear;
                    selectedDay = dayOfMonth;
                },
                year, month, day);
        datePickerDialog.show();
    }
}