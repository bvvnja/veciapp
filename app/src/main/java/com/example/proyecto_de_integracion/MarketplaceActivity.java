package com.example.proyecto_de_integracion;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MarketplaceActivity extends AppCompatActivity {

    private RecyclerView recyclerPublicaciones;
    private Button btnMisPublicaciones, btnAplicarFiltros;
    private EditText etBuscarNombre, etPrecioMin, etPrecioMax;

    private ArrayList<Publicacion> listaPublicacionesOriginal = new ArrayList<>();
    private ArrayList<Publicacion> listaPublicacionesFiltrada = new ArrayList<>();
    private PublicacionAdapter adapter;
    private DatabaseReference refPublicaciones;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marketplace);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Marketplace");
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        recyclerPublicaciones = findViewById(R.id.recyclerPublicaciones);
        btnMisPublicaciones = findViewById(R.id.btnMisPublicaciones);
        btnAplicarFiltros = findViewById(R.id.btnAplicarFiltros);
        etBuscarNombre = findViewById(R.id.etBuscarNombre);
        etPrecioMin = findViewById(R.id.etPrecioMin);
        etPrecioMax = findViewById(R.id.etPrecioMax);

        recyclerPublicaciones.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PublicacionAdapter(this, listaPublicacionesFiltrada);
        recyclerPublicaciones.setAdapter(adapter);

        refPublicaciones = FirebaseDatabase.getInstance().getReference("Publicaciones");

        cargarPublicaciones();

        btnMisPublicaciones.setOnClickListener(v ->
                startActivity(new Intent(MarketplaceActivity.this, MisPublicacionesActivity.class))
        );

        btnAplicarFiltros.setOnClickListener(v -> aplicarFiltros());
    }

    private void cargarPublicaciones() {
        refPublicaciones.orderByChild("timestamp")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        listaPublicacionesOriginal.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            Publicacion p = ds.getValue(Publicacion.class);
                            if (p != null && p.isActivo()) {
                                listaPublicacionesOriginal.add(0, p);
                            }
                        }
                        aplicarFiltros();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }

    private void aplicarFiltros() {
        String nombreFiltro = etBuscarNombre.getText().toString().trim().toLowerCase();
        String precioMinStr = etPrecioMin.getText().toString().trim();
        String precioMaxStr = etPrecioMax.getText().toString().trim();

        Double precioMin = null;
        Double precioMax = null;

        if (!TextUtils.isEmpty(precioMinStr)) {
            try {
                precioMin = Double.parseDouble(
                        precioMinStr.replace(".", "").replace(",", "."));
            } catch (NumberFormatException ignore) {
            }
        }

        if (!TextUtils.isEmpty(precioMaxStr)) {
            try {
                precioMax = Double.parseDouble(
                        precioMaxStr.replace(".", "").replace(",", "."));
            } catch (NumberFormatException ignore) {
            }
        }

        listaPublicacionesFiltrada.clear();

        for (Publicacion p : listaPublicacionesOriginal) {
            boolean coincide = true;

            if (!nombreFiltro.isEmpty()) {
                if (p.getTitulo() == null ||
                        !p.getTitulo().toLowerCase().contains(nombreFiltro)) {
                    coincide = false;
                }
            }

            double precio = p.getPrecio();

            if (precioMin != null && precio < precioMin) coincide = false;
            if (precioMax != null && precio > precioMax) coincide = false;

            if (coincide) listaPublicacionesFiltrada.add(p);
        }

        adapter.notifyDataSetChanged();
    }
}
