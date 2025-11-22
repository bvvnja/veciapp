package com.example.proyecto_de_integracion;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class Crear_Gasto_Comun extends AppCompatActivity {

    private Spinner spinnerMes;
    private EditText EtAnio, EtTotalGastosEdificio, EtDescripcion;
    private TextView txtResumen;
    private Button btnGenerarGastos;

    private ProgressDialog progressDialog;
    private DatabaseReference refUsuarios, refGastosMensuales, refCobranzas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_crear_gasto_comun);

        // Si tu layout raíz tiene id="main", se aplica el padding automático
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // ActionBar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Crear Gasto Común");
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        // Referencias UI
        spinnerMes = findViewById(R.id.spinnerMes);
        EtAnio = findViewById(R.id.EtAnio);
        EtTotalGastosEdificio = findViewById(R.id.EtTotalGastosEdificio);
        EtDescripcion = findViewById(R.id.EtDescripcion);
        txtResumen = findViewById(R.id.txtResumen);
        btnGenerarGastos = findViewById(R.id.btnGenerarGastos);

        // ProgressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Espere por favor");
        progressDialog.setCanceledOnTouchOutside(false);

        // Referencias Firebase
        refUsuarios = FirebaseDatabase.getInstance().getReference("Usuarios");
        refGastosMensuales = FirebaseDatabase.getInstance().getReference("GastosMensuales");
        refCobranzas = FirebaseDatabase.getInstance().getReference("Cobranzas");

        configurarSpinnerMeses();

        btnGenerarGastos.setOnClickListener(v -> validarYGenerar());
    }

    private void configurarSpinnerMeses() {
        String[] meses = {
                "Seleccionar mes",    // posición 0
                "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
                "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
        };
        ArrayAdapter<String> adapterMeses =
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, meses);
        adapterMeses.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMes.setAdapter(adapterMeses);
    }

    private void validarYGenerar() {
        String mesSeleccionado = spinnerMes.getSelectedItem().toString();
        if (mesSeleccionado.equals("Seleccionar mes")) {
            Toast.makeText(this, "Seleccione un mes", Toast.LENGTH_SHORT).show();
            return;
        }

        String mesNombre = spinnerMes.getSelectedItem().toString();

        // POSICIÓN CORRECTA: 1..12 (porque 0 = "Seleccionar mes")
        int mesIndex = spinnerMes.getSelectedItemPosition(); // <-- antes tenía +1, aquí estaba el error

        String anioStr = EtAnio.getText().toString().trim();
        String totalStr = EtTotalGastosEdificio.getText().toString().trim();
        String descripcion = EtDescripcion.getText().toString().trim();

        if (TextUtils.isEmpty(anioStr)) {
            Toast.makeText(this, "Ingrese el año", Toast.LENGTH_SHORT).show();
            return;
        }

        int anio;
        try {
            anio = Integer.parseInt(anioStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "El año debe ser numérico", Toast.LENGTH_SHORT).show();
            return;
        }

        if (anio < 2000 || anio > 2100) {
            Toast.makeText(this, "Ingrese un año válido", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(totalStr)) {
            Toast.makeText(this, "Ingrese el total de gastos del edificio", Toast.LENGTH_SHORT).show();
            return;
        }

        double totalEdificio;
        try {
            totalEdificio = Double.parseDouble(totalStr.replace(",", "."));
        } catch (NumberFormatException e) {
            Toast.makeText(this, "El total del edificio debe ser numérico", Toast.LENGTH_SHORT).show();
            return;
        }

        if (totalEdificio <= 0) {
            Toast.makeText(this, "El total del edificio debe ser mayor a 0", Toast.LENGTH_SHORT).show();
            return;
        }

        // Clave del mes: AAAA-MM (ej: 2025-04 para Abril)
        String mesFormateado = (mesIndex < 10 ? "0" + mesIndex : String.valueOf(mesIndex));
        String claveMes = anio + "-" + mesFormateado;

        generarGastosParaMes(claveMes, mesNombre, anio, totalEdificio, descripcion);
    }

    private void generarGastosParaMes(String claveMes, String mesNombre, int anio,
                                      double totalEdificio, String descripcion) {

        progressDialog.setMessage("Generando gastos del mes...");
        progressDialog.show();

        // 1. Guardar el registro general del mes en GastosMensuales
        HashMap<String, Object> datosMes = new HashMap<>();
        datosMes.put("mesClave", claveMes);
        datosMes.put("mesNombre", mesNombre);
        datosMes.put("anio", anio);
        datosMes.put("totalGastosEdificio", totalEdificio);
        datosMes.put("descripcion", descripcion);
        datosMes.put("timestampCreacion", System.currentTimeMillis());

        refGastosMensuales.child(claveMes).setValue(datosMes);

        // 2. Recorrer todos los usuarios y generar las cobranzas según coeficiente
        refUsuarios.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                int conteoCobranzas = 0;

                for (DataSnapshot ds : snapshot.getChildren()) {
                    String uid = ds.child("uid").getValue(String.class);
                    String nombres = ds.child("nombres").getValue(String.class);
                    String numerodepto = ds.child("numerodepto").getValue(String.class);
                    String coefStr = ds.child("coefcopropiedad").getValue(String.class);
                    String correo = ds.child("correo").getValue(String.class);

                    // Saltamos al admin
                    if (correo != null && correo.equalsIgnoreCase("admin@gmail.com")) {
                        continue;
                    }

                    if (uid == null || numerodepto == null || coefStr == null) {
                        continue;
                    }

                    double coef = 0;
                    try {
                        coef = Double.parseDouble(coefStr.replace(",", "."));
                    } catch (NumberFormatException e) {
                        continue;
                    }

                    if (coef <= 0 || coef > 1) {
                        continue;
                    }

                    double montoUsuario = totalEdificio * coef;

                    // Nodo: Cobranzas/uid/AAAA-MM
                    DatabaseReference refCobroUsuario = refCobranzas
                            .child(uid)
                            .child(claveMes);

                    HashMap<String, Object> datosCobro = new HashMap<>();
                    datosCobro.put("uidUsuario", uid);
                    datosCobro.put("nombres", nombres);
                    datosCobro.put("numeroDepto", numerodepto);
                    datosCobro.put("mesClave", claveMes);
                    datosCobro.put("mesNombre", mesNombre);
                    datosCobro.put("anio", anio);
                    datosCobro.put("totalGastosEdificio", totalEdificio);
                    datosCobro.put("coefcopropiedad", coef);
                    datosCobro.put("monto", montoUsuario);
                    datosCobro.put("descripcion", descripcion);
                    datosCobro.put("estadoPago", "pendiente");
                    datosCobro.put("timestampCreacion", System.currentTimeMillis());

                    refCobroUsuario.setValue(datosCobro);
                    conteoCobranzas++;
                }

                progressDialog.dismiss();
                Toast.makeText(Crear_Gasto_Comun.this,
                        "Gastos del mes generados para " + conteoCobranzas + " departamentos",
                        Toast.LENGTH_LONG).show();
                finish();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressDialog.dismiss();
                Toast.makeText(Crear_Gasto_Comun.this,
                        "Error al generar gastos: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}
