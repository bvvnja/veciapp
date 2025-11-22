package com.example.proyecto_de_integracion;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class PagoTarjetaActivity extends AppCompatActivity {

    // Resumen
    private TextView txtResumenMes, txtResumenDepto, txtResumenMonto;

    // Tarjeta guardada
    private LinearLayout layoutTarjetaGuardada;
    private TextView txtTarjetaGuardada;
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

    private DatabaseReference cobranzaRef;
    private DatabaseReference tarjetaRef;

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

        // Tarjeta guardada
        layoutTarjetaGuardada = findViewById(R.id.layoutTarjetaGuardada);
        txtTarjetaGuardada = findViewById(R.id.txtTarjetaGuardada);
        btnPagarTarjetaGuardada = findViewById(R.id.btnPagarTarjetaGuardada);
        btnEliminarTarjeta = findViewById(R.id.btnEliminarTarjeta);

        // Nueva tarjeta
        etNumeroTarjeta = findViewById(R.id.etNumeroTarjeta);
        etVencimiento = findViewById(R.id.etVencimiento);
        etCvc = findViewById(R.id.etCvc);
        btnGuardarTarjeta = findViewById(R.id.btnGuardarTarjeta);

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

        tarjetaRef = FirebaseDatabase.getInstance()
                .getReference("Tarjetas")
                .child(uidUsuario)
                .child("default");

        // Cargar tarjeta guardada si existe
        cargarTarjetaGuardada();

        // Guardar/actualizar tarjeta
        btnGuardarTarjeta.setOnClickListener(v -> guardarTarjeta());

        // Pagar con tarjeta guardada
        btnPagarTarjetaGuardada.setOnClickListener(v -> realizarPagoConTarjetaGuardada());

        // Eliminar tarjeta
        btnEliminarTarjeta.setOnClickListener(v -> eliminarTarjeta());
    }

    private void cargarTarjetaGuardada() {
        tarjetaRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Tarjeta tarjeta = snapshot.getValue(Tarjeta.class);
                    if (tarjeta != null && tarjeta.getLast4() != null) {
                        layoutTarjetaGuardada.setVisibility(View.VISIBLE);
                        String texto = "Tarjeta guardada: **** " + tarjeta.getLast4()
                                + " (" + (tarjeta.getVencimiento() != null ? tarjeta.getVencimiento() : "") + ")";
                        txtTarjetaGuardada.setText(texto);
                    } else {
                        layoutTarjetaGuardada.setVisibility(View.GONE);
                    }
                } else {
                    layoutTarjetaGuardada.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                layoutTarjetaGuardada.setVisibility(View.GONE);
            }
        });
    }

    private boolean validarCamposNuevaTarjeta() {
        String numero = etNumeroTarjeta.getText().toString().replace(" ", "");
        String venc = etVencimiento.getText().toString();
        String cvc = etCvc.getText().toString();

        if (TextUtils.isEmpty(numero) || numero.length() != 16 || !numero.matches("\\d{16}")) {
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

        return true;
    }

    private void guardarTarjeta() {
        if (!validarCamposNuevaTarjeta()) return;

        String numero = etNumeroTarjeta.getText().toString().replace(" ", "");
        String venc = etVencimiento.getText().toString();
        String last4 = numero.substring(12); // últimos 4 dígitos

        Tarjeta tarjeta = new Tarjeta(last4, venc, null);

        tarjetaRef.setValue(tarjeta)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(PagoTarjetaActivity.this,
                                "Tarjeta guardada correctamente", Toast.LENGTH_LONG).show();
                        cargarTarjetaGuardada();
                    } else {
                        Toast.makeText(PagoTarjetaActivity.this,
                                "Error al guardar tarjeta", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void realizarPagoConTarjetaGuardada() {
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

    private void eliminarTarjeta() {
        tarjetaRef.removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(PagoTarjetaActivity.this,
                                "Tarjeta eliminada", Toast.LENGTH_LONG).show();
                        layoutTarjetaGuardada.setVisibility(View.GONE);
                    } else {
                        Toast.makeText(PagoTarjetaActivity.this,
                                "Error al eliminar tarjeta", Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}
