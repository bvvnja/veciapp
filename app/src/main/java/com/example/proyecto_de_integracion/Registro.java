package com.example.proyecto_de_integracion;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class Registro extends AppCompatActivity {

    EditText NombreEt, CorreoEt, ContraseñaEt, ConfirmarContraseñaEt;
    Button RegistrarUsuario;

    FirebaseAuth firebaseAuth;
    ProgressDialog progressDialog;

    String nombre = " ", correo = " ", password = "", confirmarpassword = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registro);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Registrar");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        NombreEt = findViewById(R.id.NombreEt);
        CorreoEt = findViewById(R.id.CorreoEt);
        ContraseñaEt = findViewById(R.id.ContraseñaEt);
        ConfirmarContraseñaEt = findViewById(R.id.ConfirmarContraseñaEt);
        RegistrarUsuario = findViewById(R.id.RegistrarUsuario);

        firebaseAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(Registro.this);
        progressDialog.setTitle("Espere Por favor");
        progressDialog.setCanceledOnTouchOutside(false);

        RegistrarUsuario.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ValidarDatos();

            }
        });


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void ValidarDatos(){
        nombre = NombreEt.getText().toString();
        correo = CorreoEt.getText().toString();
        password = ContraseñaEt.getText().toString();
        confirmarpassword = ConfirmarContraseñaEt.getText().toString();

        if (TextUtils.isEmpty(nombre)){
            Toast.makeText(this, "Ingrese nombre", Toast.LENGTH_SHORT).show();
        }
        else if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()){
            Toast.makeText(this, "Ingrese correo", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(password)){
            Toast.makeText(this, "Ingrese contraseña", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(confirmarpassword)){
            Toast.makeText(this, "Confirme contraseña", Toast.LENGTH_SHORT).show();
        }
        else if (!password.equals(confirmarpassword)){
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
        }
        else {
            CrearCuenta();
        }
    }

    private void CrearCuenta() {
        progressDialog.setMessage("Creando cuenta...");
        progressDialog.show();

        firebaseAuth.createUserWithEmailAndPassword(correo, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user != null) {
                            // Enviar correo de verificación
                            user.sendEmailVerification()
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            GuardarInformacion();
                                            Toast.makeText(Registro.this,
                                                    "Cuenta creada. Revisa tu correo para verificar la cuenta.",
                                                    Toast.LENGTH_LONG).show();

                                            // Iniciar monitoreo automático de verificación
                                            esperarVerificacion();
                                        } else {
                                            progressDialog.dismiss();
                                            Toast.makeText(Registro.this,
                                                    "Error al enviar correo de verificación: " + task.getException().getMessage(),
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(Registro.this,
                                "Error al crear usuario: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Espera automáticamente a que el usuario verifique su correo y lo redirige
     * al menú correspondiente según su tipo (admin o usuario común).
     */
    private void esperarVerificacion() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        new Thread(() -> {
            while (user != null && !user.isEmailVerified()) {
                try {
                    Thread.sleep(3000); // espera 3 segundos
                    user.reload(); // actualiza el estado desde el servidor
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if (user != null && user.isEmailVerified()) {
                runOnUiThread(() -> {
                    Toast.makeText(Registro.this, "Correo verificado con éxito", Toast.LENGTH_SHORT).show();

                    // 🔹 Verificar si es el administrador
                    String correoActual = user.getEmail();
                    if (correoActual != null && correoActual.equalsIgnoreCase("admin@gmail.com")) {
                        // Enviar al menú del administrador
                        Intent intent = new Intent(Registro.this, MenuPrincipal.class);
                        startActivity(intent);
                    } else {
                        // Enviar al menú de usuario común
                        Intent intent = new Intent(Registro.this, MenuUsuario.class);
                        startActivity(intent);
                    }

                    finish();
                });
            }
        }).start();
    }




    private void GuardarInformacion() {
        progressDialog.setMessage("Guardando información...");
        progressDialog.dismiss();

        String uid = firebaseAuth.getUid();

        HashMap<String, String> Datos = new HashMap<>();
        Datos.put("uid", uid);
        Datos.put("correo", correo);
        Datos.put("nombres", nombre);
        Datos.put("password", password);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Usuarios");
        databaseReference.child(uid)
                .setValue(Datos)
                .addOnSuccessListener(unused -> {
                    progressDialog.dismiss();
                    FirebaseUser user = firebaseAuth.getCurrentUser();

                    if (user != null && user.isEmailVerified()) {
                        Toast.makeText(Registro.this,
                                "Cuenta creada con éxito y verificada.",
                                Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(Registro.this, MenuPrincipal.class));
                        finish();
                    } else {
                        Toast.makeText(Registro.this,
                                "Revisa tu correo y verifica tu cuenta antes de continuar.",
                                Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(Registro.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}