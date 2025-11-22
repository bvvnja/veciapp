package com.example.proyecto_de_integracion;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MisPublicacionesActivity extends AppCompatActivity {

    private RecyclerView recyclerMisPublicaciones;
    private Button btnCrearPublicacion;
    private TextView txtSinPublicaciones;

    private ArrayList<Publicacion> listaMisPublicaciones = new ArrayList<>();
    private PublicacionAdapter adapter;

    private DatabaseReference refPublicaciones;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mis_publicaciones);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Mis publicaciones");
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        recyclerMisPublicaciones = findViewById(R.id.recyclerMisPublicaciones);
        btnCrearPublicacion = findViewById(R.id.btnCrearPublicacion);
        txtSinPublicaciones = findViewById(R.id.txtSinPublicaciones);

        recyclerMisPublicaciones.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PublicacionAdapter(this, listaMisPublicaciones);
        recyclerMisPublicaciones.setAdapter(adapter);

        auth = FirebaseAuth.getInstance();
        refPublicaciones = FirebaseDatabase.getInstance().getReference("Publicaciones");

        btnCrearPublicacion.setOnClickListener(v ->
                startActivity(new Intent(MisPublicacionesActivity.this, CrearPublicacionActivity.class))
        );

        cargarMisPublicaciones();
    }

    private void cargarMisPublicaciones() {
        String uid = auth.getCurrentUser().getUid();

        refPublicaciones.orderByChild("uidVendedor").equalTo(uid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        listaMisPublicaciones.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            Publicacion p = ds.getValue(Publicacion.class);
                            if (p != null) listaMisPublicaciones.add(0, p);
                        }

                        if (listaMisPublicaciones.isEmpty()) {
                            txtSinPublicaciones.setVisibility(View.VISIBLE);
                            recyclerMisPublicaciones.setVisibility(View.GONE);
                        } else {
                            txtSinPublicaciones.setVisibility(View.GONE);
                            recyclerMisPublicaciones.setVisibility(View.VISIBLE);
                        }

                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }
}
