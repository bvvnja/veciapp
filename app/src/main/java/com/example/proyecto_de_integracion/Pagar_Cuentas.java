package com.example.proyecto_de_integracion;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Pagar_Cuentas extends AppCompatActivity {

    private RecyclerView recyclerCobranzas;
    private CobranzaAdapter cobranzaAdapter;
    private List<Cobranza> cobranzaList;
    private DatabaseReference cobranzasUsuarioRef;
    private String uidUsuario;

    private Button btnGestionarTarjeta; // botón que te llevará a la pantalla de tarjeta

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pagar_cuentas);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Pagar Cuenta");
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        // RecyclerView
        recyclerCobranzas = findViewById(R.id.recyclerCobranzas);
        recyclerCobranzas.setLayoutManager(new LinearLayoutManager(this));
        cobranzaList = new ArrayList<>();

        // Botón para ir a la interfaz de la tarjeta
        btnGestionarTarjeta = findViewById(R.id.btnGestionarTarjeta);
        btnGestionarTarjeta.setOnClickListener(v -> {
            if (cobranzaList.isEmpty()) {
                Toast.makeText(Pagar_Cuentas.this,
                        "No hay cobranzas para pagar",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            // Por simplicidad, tomo la primera cobranza de la lista.
            // Si después quieres seleccionar una específica, lo movemos al adapter.
            Cobranza c = cobranzaList.get(0);

            Intent intent = new Intent(Pagar_Cuentas.this, PagoTarjetaActivity.class);
            intent.putExtra("uidUsuario", uidUsuario);
            intent.putExtra("mesClave", c.getMesClave());
            intent.putExtra("mesNombre", c.getMesNombre());
            intent.putExtra("anio", c.getAnio());
            intent.putExtra("monto", c.getMonto());
            intent.putExtra("descripcion", c.getDescripcion());
            startActivity(intent);
        });

        // UID del usuario autenticado
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            uidUsuario = currentUser.getUid();
            Log.d("Pagar_Cuentas", "UID del usuario: " + uidUsuario);
        } else {
            Log.e("Pagar_Cuentas", "Usuario no autenticado");
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Ruta: /Cobranzas/<uidUsuario>
        cobranzasUsuarioRef = FirebaseDatabase.getInstance()
                .getReference("Cobranzas")
                .child(uidUsuario);

        Log.d("Pagar_Cuentas", "Leyendo desde ruta: " + cobranzasUsuarioRef.getPath().toString());

        // IMPORTANTE: pasar uidUsuario al adapter
        cobranzaAdapter = new CobranzaAdapter(this, cobranzaList, uidUsuario);
        recyclerCobranzas.setAdapter(cobranzaAdapter);

        cargarCobranzas();
    }

    private void cargarCobranzas() {
        cobranzasUsuarioRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("Pagar_Cuentas", "Datos obtenidos de Firebase. children=" + snapshot.getChildrenCount());
                cobranzaList.clear();

                if (!snapshot.exists()) {
                    Log.d("Pagar_Cuentas", "El usuario no tiene cobranzas");
                    Toast.makeText(Pagar_Cuentas.this,
                            "No hay cobranzas para este usuario",
                            Toast.LENGTH_SHORT).show();
                    cobranzaAdapter.notifyDataSetChanged();
                    return;
                }

                // Hijos: 2025-04, 2025-09, etc.
                for (DataSnapshot mesSnapshot : snapshot.getChildren()) {
                    Cobranza c = mesSnapshot.getValue(Cobranza.class);
                    if (c != null) {
                        // Guardamos la clave del nodo (ej: "2025-09") para poder actualizar luego
                        c.setMesClave(mesSnapshot.getKey());
                        Log.d("Pagar_Cuentas",
                                "Cobranza añadida. mes=" + mesSnapshot.getKey()
                                        + " nombres=" + c.getNombres()
                                        + " monto=" + c.getMonto());
                        cobranzaList.add(c);
                    } else {
                        Log.w("Pagar_Cuentas",
                                "Cobranza nula en mes " + mesSnapshot.getKey());
                    }
                }

                cobranzaAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Pagar_Cuentas", "Error al obtener datos: " + error.getMessage());
                Toast.makeText(Pagar_Cuentas.this,
                        "Error al cargar las cobranzas",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Al volver desde PagoTarjetaActivity, recarga para ver el estado actualizado
        cargarCobranzas();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}
