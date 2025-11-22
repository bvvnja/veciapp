package com.example.proyecto_de_integracion;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class PagoTarjetaActivity extends AppCompatActivity {

    // Resumen
    private TextView txtResumenMes, txtResumenDepto, txtResumenMonto;

    // Tarjetas guardadas
    private LinearLayout layoutTarjetaGuardada;
    private RadioGroup rgTarjetas;
    private Button btnPagarTarjetaGuardada;
    private Button btnEliminarTarjeta;

    // Nueva tarjeta
    private EditText etNumeroTarjeta, etVencimiento, etCvc;
    private Button btnGuardarTarjeta;

    // Datos de la cobranza
    private String uidUsuario;
    private String mesClave;
    private String mesNombre;
    private int anio;
    private double monto;
    private String descripcion;
    private String numeroDepto;

    // Firebase
    private DatabaseReference cobranzaRef;
    private DatabaseReference tarjetasUsuarioRef;

    // Lista de tarjetas del usuario (máx 3)
    private final List<Tarjeta> tarjetasGuardadas = new ArrayList<>();
    private final List<String> tarjetasKeys = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pago_tarjeta);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Pago con tarjeta");
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        // Resumen
        txtResumenMes = findViewById(R.id.txtResumenMes);
        txtResumenDepto = findViewById(R.id.txtResumenDepto);
        txtResumenMonto = findViewById(R.id.txtResumenMonto);

        // Tarjetas guardadas
        layoutTarjetaGuardada = findViewById(R.id.layoutTarjetaGuardada);
        rgTarjetas = findViewById(R.id.rgTarjetas);
        btnPagarTarjetaGuardada = findViewById(R.id.btnPagarTarjetaGuardada);
        btnEliminarTarjeta = findViewById(R.id.btnEliminarTarjeta);

        // Nueva tarjeta
        etNumeroTarjeta = findViewById(R.id.etNumeroTarjeta);
        etVencimiento = findViewById(R.id.etVencimiento);
        etCvc = findViewById(R.id.etCvc);
        btnGuardarTarjeta = findViewById(R.id.btnGuardarTarjeta);

        // Formato número: 1234 5678 9012 3456
        configurarTextWatcherNumeroTarjeta();

        // Formato vencimiento: MM/AA con "/" automático
        configurarTextWatcherVencimiento();

        // Datos recibidos desde CobranzaAdapter
        uidUsuario = getIntent().getStringExtra("uidUsuario");
        mesClave = getIntent().getStringExtra("mesClave");
        mesNombre = getIntent().getStringExtra("mesNombre");
        anio = getIntent().getIntExtra("anio", 0);
        monto = getIntent().getDoubleExtra("monto", 0);
        descripcion = getIntent().getStringExtra("descripcion");
        numeroDepto = getIntent().getStringExtra("numeroDepto");

        // Resumen de la cobranza
        txtResumenMes.setText("Mes: " + mesNombre + " " + anio);
        txtResumenDepto.setText("Depto: " + (numeroDepto != null ? numeroDepto : "-"));
        txtResumenMonto.setText("Monto: $" + String.format("%,.0f", monto));

        cobranzaRef = FirebaseDatabase.getInstance()
                .getReference("Cobranzas")
                .child(uidUsuario)
                .child(mesClave);

        tarjetasUsuarioRef = FirebaseDatabase.getInstance()
                .getReference("Tarjetas")
                .child(uidUsuario);

        // Cargar tarjetas guardadas
        cargarTarjetasGuardadas();

        // Guardar/actualizar
        btnGuardarTarjeta.setOnClickListener(v -> guardarTarjeta());

        // Pagar con tarjeta seleccionada
        btnPagarTarjetaGuardada.setOnClickListener(v -> realizarPagoConTarjetaSeleccionada());

        // Eliminar tarjeta seleccionada
        btnEliminarTarjeta.setOnClickListener(v -> eliminarTarjetaSeleccionada());
    }

    // --------------------- TEXT WATCHERS -----------------------------

    private void configurarTextWatcherNumeroTarjeta() {
        etNumeroTarjeta.addTextChangedListener(new TextWatcher() {
            private boolean isFormatting;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                if (isFormatting) return;
                isFormatting = true;

                String digits = s.toString().replace(" ", "");
                if (digits.length() > 16) {
                    digits = digits.substring(0, 16);
                }

                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < digits.length(); i++) {
                    sb.append(digits.charAt(i));
                    if ((i + 1) % 4 == 0 && (i + 1) < digits.length()) {
                        sb.append(" ");
                    }
                }

                etNumeroTarjeta.removeTextChangedListener(this);
                etNumeroTarjeta.setText(sb.toString());
                etNumeroTarjeta.setSelection(etNumeroTarjeta.getText().length());
                etNumeroTarjeta.addTextChangedListener(this);

                isFormatting = false;

                // Actualizar texto del botón Guardar/Actualizar
                actualizarTextoBotonGuardar(digits);
            }
        });
    }

    private void configurarTextWatcherVencimiento() {
        etVencimiento.addTextChangedListener(new TextWatcher() {
            private boolean isDeleting = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                isDeleting = count > after;
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                if (!isDeleting && s.length() == 2) {
                    if (!s.toString().contains("/")) {
                        s.append("/");
                    }
                }
            }
        });
    }

    // --------------------- CARGA TARJETAS -----------------------------

    private void cargarTarjetasGuardadas() {
        tarjetasUsuarioRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                tarjetasGuardadas.clear();
                tarjetasKeys.clear();
                rgTarjetas.removeAllViews();

                if (snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        Tarjeta t = ds.getValue(Tarjeta.class);
                        if (t != null && t.getNumeroCompleto() != null) {
                            tarjetasGuardadas.add(t);
                            tarjetasKeys.add(ds.getKey());
                        }
                    }
                }

                if (!tarjetasGuardadas.isEmpty()) {
                    layoutTarjetaGuardada.setVisibility(View.VISIBLE);

                    for (int i = 0; i < tarjetasGuardadas.size(); i++) {
                        Tarjeta t = tarjetasGuardadas.get(i);
                        String last4 = t.getLast4() != null ? t.getLast4() : "****";
                        String venc = t.getVencimiento() != null ? t.getVencimiento() : "";

                        RadioButton rb = new RadioButton(PagoTarjetaActivity.this);
                        rb.setText("**** " + last4 + " (" + venc + ")");
                        rb.setTag(i); // guardamos el índice
                        rgTarjetas.addView(rb);

                        if (i == 0) {
                            rgTarjetas.check(rb.getId());
                        }
                    }
                } else {
                    layoutTarjetaGuardada.setVisibility(View.GONE);
                }

                // Ajustar texto de botón Guardar/Actualizar según lo que haya escrito
                String numeroPlano = etNumeroTarjeta.getText().toString().replace(" ", "");
                if (numeroPlano.length() == 16) {
                    actualizarTextoBotonGuardar(numeroPlano);
                } else {
                    btnGuardarTarjeta.setText("Guardar tarjeta");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                layoutTarjetaGuardada.setVisibility(View.GONE);
            }
        });
    }

    // --------------------- VALIDACIONES -----------------------------

    private boolean validarCamposNuevaTarjeta() {
        String numeroConEspacios = etNumeroTarjeta.getText().toString();
        String numeroPlano = numeroConEspacios.replace(" ", "");
        String venc = etVencimiento.getText().toString();
        String cvc = etCvc.getText().toString();

        if (TextUtils.isEmpty(numeroPlano) ||
                numeroPlano.length() != 16 ||
                !numeroPlano.matches("\\d{16}")) {
            etNumeroTarjeta.setError("Debe tener 16 dígitos");
            return false;
        }

        if (TextUtils.isEmpty(venc) || !venc.matches("(0[1-9]|1[0-2])/[0-9]{2}")) {
            etVencimiento.setError("Formato MM/AA");
            return false;
        }

        if (TextUtils.isEmpty(cvc) || !cvc.matches("\\d{3}")) {
            etCvc.setError("CVC de 3 dígitos");
            return false;
        }

        // No vencida: mes/año tarjeta >= mes/año actual
        String[] parts = venc.split("/");
        int mes = Integer.parseInt(parts[0]);
        int anioTarjeta = 2000 + Integer.parseInt(parts[1]);

        Calendar cal = Calendar.getInstance();
        int currentYear = cal.get(Calendar.YEAR);
        int currentMonth = cal.get(Calendar.MONTH) + 1;

        if (anioTarjeta < currentYear ||
                (anioTarjeta == currentYear && mes < currentMonth)) {
            etVencimiento.setError("Tarjeta vencida");
            return false;
        }

        return true;
    }

    private void actualizarTextoBotonGuardar(String numeroPlano) {
        if (numeroPlano == null || numeroPlano.length() != 16) {
            btnGuardarTarjeta.setText("Guardar tarjeta");
            return;
        }
        if (existeTarjeta(numeroPlano)) {
            btnGuardarTarjeta.setText("Actualizar tarjeta");
        } else {
            btnGuardarTarjeta.setText("Guardar tarjeta");
        }
    }

    private boolean existeTarjeta(String numeroPlano) {
        for (Tarjeta t : tarjetasGuardadas) {
            if (numeroPlano.equals(t.getNumeroCompleto())) {
                return true;
            }
        }
        return false;
    }

    private String obtenerKeyTarjetaPorNumero(String numeroPlano) {
        for (int i = 0; i < tarjetasGuardadas.size(); i++) {
            Tarjeta t = tarjetasGuardadas.get(i);
            if (numeroPlano.equals(t.getNumeroCompleto())) {
                return tarjetasKeys.get(i);
            }
        }
        return null;
    }

    // --------------------- GUARDAR / ACTUALIZAR -----------------------------

    private void guardarTarjeta() {
        if (!validarCamposNuevaTarjeta()) return;

        String numeroPlano = etNumeroTarjeta.getText().toString().replace(" ", "");
        String venc = etVencimiento.getText().toString();
        String last4 = numeroPlano.substring(12);

        String keyExistente = obtenerKeyTarjetaPorNumero(numeroPlano);

        if (keyExistente != null) {
            // Actualizar tarjeta existente
            Tarjeta tarjetaActualizada = new Tarjeta(numeroPlano, last4, venc, null);
            tarjetasUsuarioRef.child(keyExistente).setValue(tarjetaActualizada)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(PagoTarjetaActivity.this,
                                    "Tarjeta actualizada", Toast.LENGTH_LONG).show();
                            cargarTarjetasGuardadas();
                        } else {
                            Toast.makeText(PagoTarjetaActivity.this,
                                    "Error al actualizar tarjeta", Toast.LENGTH_LONG).show();
                        }
                    });
        } else {
            // Nueva tarjeta
            if (tarjetasGuardadas.size() >= 3) {
                Toast.makeText(this,
                        "Ya tienes 3 tarjetas guardadas. Elimina una para agregar otra.",
                        Toast.LENGTH_LONG).show();
                return;
            }

            Tarjeta nuevaTarjeta = new Tarjeta(numeroPlano, last4, venc, null);
            tarjetasUsuarioRef.push().setValue(nuevaTarjeta)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(PagoTarjetaActivity.this,
                                    "Tarjeta guardada correctamente", Toast.LENGTH_LONG).show();
                            cargarTarjetasGuardadas();
                        } else {
                            Toast.makeText(PagoTarjetaActivity.this,
                                    "Error al guardar tarjeta", Toast.LENGTH_LONG).show();
                        }
                    });
        }
    }

    // --------------------- PAGAR / ELIMINAR SEGÚN SELECCIÓN -----------------------------

    private int obtenerIndiceTarjetaSeleccionada() {
        int checkedId = rgTarjetas.getCheckedRadioButtonId();
        if (checkedId == -1) {
            return -1;
        }
        RadioButton rb = findViewById(checkedId);
        if (rb == null || rb.getTag() == null) {
            return -1;
        }
        return (int) rb.getTag();
    }

    private void realizarPagoConTarjetaSeleccionada() {
        if (tarjetasGuardadas.isEmpty()) {
            Toast.makeText(this, "No hay tarjetas guardadas", Toast.LENGTH_LONG).show();
            return;
        }

        int index = obtenerIndiceTarjetaSeleccionada();
        if (index < 0 || index >= tarjetasGuardadas.size()) {
            Toast.makeText(this, "Seleccione una tarjeta para pagar", Toast.LENGTH_LONG).show();
            return;
        }

        // Aquí podrías usar tarjetasGuardadas.get(index) si quieres guardar last4 en el comprobante
        cobranzaRef.child("estadoPago").setValue("Pagado")
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(PagoTarjetaActivity.this,
                                "Pago realizado correctamente", Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        Toast.makeText(PagoTarjetaActivity.this,
                                "Error al registrar el pago", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void eliminarTarjetaSeleccionada() {
        if (tarjetasGuardadas.isEmpty()) {
            Toast.makeText(this, "No hay tarjetas guardadas para eliminar", Toast.LENGTH_LONG).show();
            return;
        }

        int index = obtenerIndiceTarjetaSeleccionada();
        if (index < 0 || index >= tarjetasGuardadas.size()) {
            Toast.makeText(this, "Seleccione una tarjeta para eliminar", Toast.LENGTH_LONG).show();
            return;
        }

        String key = tarjetasKeys.get(index);

        tarjetasUsuarioRef.child(key).removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(PagoTarjetaActivity.this,
                                "Tarjeta eliminada", Toast.LENGTH_LONG).show();
                        cargarTarjetasGuardadas();
                    } else {
                        Toast.makeText(PagoTarjetaActivity.this,
                                "Error al eliminar tarjeta", Toast.LENGTH_LONG).show();
                    }
                });
    }

    // --------------------- NAV ATRÁS -----------------------------

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}
