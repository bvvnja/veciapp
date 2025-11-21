package com.example.proyecto_de_integracion;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class EditarGastoComun extends AppCompatActivity {

    private Spinner spinnerMes;
    private EditText etAnio, etTotalEdificio, etDescripcion;
    private Button btnGuardar;
    private TextView txtTitulo, txtResumen;

    private DatabaseReference cobranzaRef;
    private String uidUsuario;
    private String mesClaveActual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // OJO: puedes usar el mismo layout que Crear_Gasto_Comun
        setContentView(R.layout.activity_crear_gasto_comun);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Editar gasto común");
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        // Extras
        uidUsuario     = getIntent().getStringExtra("uidUsuario");
        mesClaveActual = getIntent().getStringExtra("mesClave");

        if (uidUsuario == null || mesClaveActual == null) {
            Toast.makeText(this,
                    "No se pudo identificar el gasto a editar",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        cobranzaRef = FirebaseDatabase.getInstance()
                .getReference("Cobranzas")
                .child(uidUsuario)
                .child(mesClaveActual);

        // Referencias UI (mismos IDs del layout de crear)
        spinnerMes      = findViewById(R.id.spinnerMes);
        etAnio          = findViewById(R.id.EtAnio);
        etTotalEdificio = findViewById(R.id.EtTotalGastosEdificio);
        etDescripcion   = findViewById(R.id.EtDescripcion);
        btnGuardar      = findViewById(R.id.btnGenerarGastos);

        txtTitulo  = findViewById(R.id.txtTituloGenerar);
        txtResumen = findViewById(R.id.txtResumen);

        // Cambiamos textos para que se note que es edición
        if (txtTitulo != null) {
            txtTitulo.setText("Editar gasto común");
        }
        if (txtResumen != null) {
            txtResumen.setText("Modifica los datos del gasto común seleccionado y guarda los cambios.");
        }
        btnGuardar.setText("Guardar cambios");

        // Configurar meses en el Spinner
        String[] meses = new String[]{
                "Enero","Febrero","Marzo","Abril","Mayo","Junio",
                "Julio","Agosto","Septiembre","Octubre","Noviembre","Diciembre"
        };
        ArrayAdapter<String> adapterMeses = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                meses
        );
        spinnerMes.setAdapter(adapterMeses);

        // Cargar datos de la cobranza
        cargarDatosGasto();

        // Guardar cambios
        btnGuardar.setOnClickListener(v -> guardarCambios());
    }

    private void cargarDatosGasto() {
        cobranzaRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Cobranza c = snapshot.getValue(Cobranza.class);
                if (c == null) {
                    Toast.makeText(EditarGastoComun.this,
                            "No se encontraron datos del gasto",
                            Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                // Mes
                if (c.getMesNombre() != null) {
                    int idx = obtenerIndexMes(c.getMesNombre());
                    if (idx >= 0) {
                        spinnerMes.setSelection(idx);
                    }
                }

                // Año
                etAnio.setText(String.valueOf(c.getAnio()));

                // Total edificio: si no está, puedes usar monto
                if (c.getTotalGastosEdificio() > 0) {
                    etTotalEdificio.setText(String.valueOf(c.getTotalGastosEdificio()));
                } else {
                    etTotalEdificio.setText(String.valueOf(c.getMonto()));
                }

                // Descripción
                if (c.getDescripcion() != null) {
                    etDescripcion.setText(c.getDescripcion());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EditarGastoComun.this,
                        "Error al cargar datos: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private int obtenerIndexMes(String mesNombre) {
        String[] meses = new String[]{
                "Enero","Febrero","Marzo","Abril","Mayo","Junio",
                "Julio","Agosto","Septiembre","Octubre","Noviembre","Diciembre"
        };
        for (int i = 0; i < meses.length; i++) {
            if (meses[i].equalsIgnoreCase(mesNombre)) {
                return i;
            }
        }
        return -1;
    }

    private void guardarCambios() {
        String mesNombre = spinnerMes.getSelectedItem().toString();
        String anioStr   = etAnio.getText().toString().trim();
        String totalStr  = etTotalEdificio.getText().toString().trim();
        String desc      = etDescripcion.getText().toString().trim();

        if (TextUtils.isEmpty(anioStr) ||
                TextUtils.isEmpty(totalStr) ||
                TextUtils.isEmpty(desc)) {
            Toast.makeText(this,
                    "Completa todos los campos",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        int anio;
        long totalEdificio;
        try {
            anio = Integer.parseInt(anioStr);
            totalEdificio = Long.parseLong(totalStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this,
                    "Año o monto inválidos",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("mesNombre", mesNombre);
        updates.put("anio", anio);
        updates.put("totalGastosEdificio", totalEdificio);
        updates.put("descripcion", desc);

        cobranzaRef.updateChildren(updates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(EditarGastoComun.this,
                                "Gasto común actualizado correctamente",
                                Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(EditarGastoComun.this,
                                "Error al guardar cambios",
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
