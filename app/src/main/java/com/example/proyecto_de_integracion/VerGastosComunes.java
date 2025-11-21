package com.example.proyecto_de_integracion;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class VerGastosComunes extends AppCompatActivity
        implements GastoComunAdapter.OnGastoActionListener {

    private RecyclerView recyclerViewGastos;
    private SearchView searchViewGastos;

    private GastoComunAdapter adapter;
    private List<Cobranza> listaGastos      = new ArrayList<>();
    private List<Cobranza> listaFiltrada    = new ArrayList<>();

    private DatabaseReference cobranzasRootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // OJO: usa tu layout con SearchView + RecyclerView
        setContentView(R.layout.activity_ver_gastos_comunes);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Gastos comunes");
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        recyclerViewGastos = findViewById(R.id.recyclerViewGastos);
        searchViewGastos   = findViewById(R.id.searchViewGastos);

        recyclerViewGastos.setLayoutManager(new LinearLayoutManager(this));

        // Raíz de todas las cobranzas
        cobranzasRootRef = FirebaseDatabase.getInstance().getReference("Cobranzas");

        // Adapter usando la lista filtrada
        adapter = new GastoComunAdapter(
                listaFiltrada,
                "",  // correoUsuario (para admin lo dejamos vacío)
                this // this implementa OnGastoActionListener
        );

        recyclerViewGastos.setAdapter(adapter);

        // Cargar datos desde Firebase
        cargarGastosDesdeFirebase();

        // Buscar por nombre, mes, año o estado
        searchViewGastos.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // No necesitamos acción especial al presionar enter
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                aplicarFiltro(newText);
                return true;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Al volver de editar, recarga la lista
        cargarGastosDesdeFirebase();
    }

    /**
     * Lee /Cobranzas y arma una lista con TODAS las cobranzas de todos los usuarios.
     *
     * Estructura esperada:
     * Cobranzas
     *   └── uidUsuario
     *        └── 2025-02  (mesClave)
     *             ├── uidUsuario: ...
     *             ├── mesClave: "2025-02"
     *             ├── mesNombre: "Febrero"
     *             ├── anio: 2025
     *             ├── monto: 12345
     *             ├── estadoPago: "Pendiente"/"Pagado"
     *             └── ...
     */
    private void cargarGastosDesdeFirebase() {
        cobranzasRootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listaGastos.clear();
                listaFiltrada.clear();

                if (!snapshot.exists()) {
                    Toast.makeText(VerGastosComunes.this,
                            "No existen gastos comunes registrados",
                            Toast.LENGTH_SHORT).show();
                    adapter.notifyDataSetChanged();
                    return;
                }

                // Nivel 1: uidUsuario
                for (DataSnapshot uidSnapshot : snapshot.getChildren()) {
                    String uidUsuario = uidSnapshot.getKey();

                    // Nivel 2: mesClave (ej: 2025-02)
                    for (DataSnapshot mesSnapshot : uidSnapshot.getChildren()) {
                        String mesClave = mesSnapshot.getKey();

                        Cobranza c = mesSnapshot.getValue(Cobranza.class);
                        if (c != null) {
                            // Por si el modelo no lo trae guardado:
                            try {
                                if (c.getUidUsuario() == null || c.getUidUsuario().isEmpty()) {
                                    c.setUidUsuario(uidUsuario);
                                }
                            } catch (Exception ignored) {}

                            try {
                                if (c.getMesClave() == null || c.getMesClave().isEmpty()) {
                                    c.setMesClave(mesClave);
                                }
                            } catch (Exception ignored) {}

                            listaGastos.add(c);
                        }
                    }
                }

                listaFiltrada.addAll(listaGastos);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(VerGastosComunes.this,
                        "Error al cargar los gastos: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Filtro por:
     * - Nombre (nombres)
     * - Mes (mesNombre)
     * - Año (anio)
     * - Estado de pago (estadoPago)
     */
    private void aplicarFiltro(String query) {
        listaFiltrada.clear();

        if (TextUtils.isEmpty(query)) {
            listaFiltrada.addAll(listaGastos);
        } else {
            String q = query.toLowerCase();

            for (Cobranza c : listaGastos) {
                boolean coincide = false;

                // Nombre
                if (c.getNombres() != null &&
                        c.getNombres().toLowerCase().contains(q)) {
                    coincide = true;
                }

                // Mes
                if (!coincide && c.getMesNombre() != null &&
                        c.getMesNombre().toLowerCase().contains(q)) {
                    coincide = true;
                }

                // Año
                if (!coincide) {
                    int anio = c.getAnio();
                    if (String.valueOf(anio).contains(q)) {
                        coincide = true;
                    }
                }

                // Estado de pago
                if (!coincide && c.getEstadoPago() != null &&
                        c.getEstadoPago().toLowerCase().contains(q)) {
                    coincide = true;
                }

                if (coincide) {
                    listaFiltrada.add(c);
                }
            }
        }

        adapter.notifyDataSetChanged();
    }

    // ======================= ACCIONES ADAPTER =======================

    @Override
    public void onEditar(Cobranza c) {
        if (c == null || c.getUidUsuario() == null || c.getMesClave() == null) {
            Toast.makeText(this,
                    "No se pudo identificar el gasto a editar",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Intent i = new Intent(VerGastosComunes.this, EditarGastoComun.class);
        i.putExtra("uidUsuario", c.getUidUsuario());
        i.putExtra("mesClave", c.getMesClave());
        startActivity(i);
    }

    @Override
    public void onEliminar(Cobranza c) {
        if (c == null || c.getUidUsuario() == null || c.getMesClave() == null) {
            Toast.makeText(this,
                    "No se pudo identificar el gasto a eliminar",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // /Cobranzas/uid/mesClave
        cobranzasRootRef
                .child(c.getUidUsuario())
                .child(c.getMesClave())
                .removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        listaGastos.remove(c);
                        listaFiltrada.remove(c);
                        adapter.notifyDataSetChanged();

                        Toast.makeText(VerGastosComunes.this,
                                "Gasto común eliminado",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(VerGastosComunes.this,
                                "Error al eliminar",
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
