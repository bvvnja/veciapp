package com.example.proyecto_de_integracion;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MenuUsuario extends AppCompatActivity {

    Button Pagar_Cuentas, MarketPlace, ReservaEstacionamiento, MiPerfil, AcercaDe, CerrarSesion;
    FirebaseAuth firebaseAuth;
    FirebaseUser user;

    TextView NombresPrincipalUsuario, CorreoPrincipalUsuario;

    ProgressBar progressBarDatosU;

    DatabaseReference Usuarios;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_menu_usuario);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("VeciApp");

        NombresPrincipalUsuario = findViewById(R.id.NombresPrincipalUsuario);
        CorreoPrincipalUsuario = findViewById(R.id.CorreoPrincipalUsuario);
        progressBarDatosU = findViewById(R.id.progressBarDatosUsuario);


        Usuarios = FirebaseDatabase.getInstance().getReference("Usuarios");

        CerrarSesion = findViewById(R.id.CerrarSesion);
        Pagar_Cuentas = findViewById(R.id.Pagar_Cuentas);
        MiPerfil = findViewById(R.id.MiPerfil);

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        CerrarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SalirAplicacionUsuario();
            }
        });

        Pagar_Cuentas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MenuUsuario.this, Pagar_Cuentas.class));
            }
        });

        MiPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MenuUsuario.this, PerfilUsuario.class));
            }
        });


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    protected void onStart() {
        ComprobarInicioSesionUsuario();
        super.onStart();
    }

    private void ComprobarInicioSesionUsuario() {
        //si el usuario a iniciado sesion
        if (user!=null){
            CargaDeDatosUsuario();
        }
    }

    private void CargaDeDatosUsuario() {
        Usuarios.child(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //si el usuario existe
                if (snapshot.exists()){
                    //progressbar se oculta
                    progressBarDatosU.setVisibility(GONE);
                    //Los textview se muestran
                    NombresPrincipalUsuario.setVisibility(VISIBLE);
                    CorreoPrincipalUsuario.setVisibility(VISIBLE);

                    //OBTENER LOS DATOS
                    String nombres =""+snapshot.child("nombres").getValue();
                    String correo =""+snapshot.child("correo").getValue();

                    //setear los datos en los respectivos textview
                    NombresPrincipalUsuario.setText(nombres);
                    CorreoPrincipalUsuario.setText(correo);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void SalirAplicacionUsuario() {
        firebaseAuth.signOut();
        startActivity(new Intent(MenuUsuario.this, MainActivity.class));
        Toast.makeText(this, "Cerraste sesion exitosamente", Toast.LENGTH_SHORT).show();
    }
}