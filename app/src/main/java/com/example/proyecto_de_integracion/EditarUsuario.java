package com.example.proyecto_de_integracion;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class EditarUsuario extends AppCompatActivity {

    private TextInputEditText edtName, edtEmail, edtPassword, edtPasswordConfirm;
    private Button btnSaveName, btnSavePassword;

    private String userId;

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_usuario);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Editar perfil");
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        // Inicializar Firebase
        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("Usuarios");

        // Obtener userId desde el Intent
        userId = getIntent().getStringExtra("userId");

        // Referencias UI
        edtName            = findViewById(R.id.edtName);
        edtEmail           = findViewById(R.id.edtEmail);
        edtPassword        = findViewById(R.id.passwordU);
        edtPasswordConfirm = findViewById(R.id.passwordConfirmU);
        btnSaveName        = findViewById(R.id.btnSaveName);
        btnSavePassword    = findViewById(R.id.btnSavePassword);

        // Correo solo lectura (ya está deshabilitado en XML, esto es doble seguridad)
        if (edtEmail != null) {
            edtEmail.setEnabled(false);
        }

        if (userId != null) {
            loadUserData();
        } else {
            Toast.makeText(this, "Error al obtener el usuario", Toast.LENGTH_SHORT).show();
        }

        btnSaveName.setOnClickListener(v -> saveName());
        btnSavePassword.setOnClickListener(v -> savePassword());
    }

    private void loadUserData() {
        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String nombre   = snapshot.child("nombres").getValue(String.class);
                    String correo   = snapshot.child("correo").getValue(String.class);
                    String password = snapshot.child("password").getValue(String.class);

                    if (nombre != null)   edtName.setText(nombre);
                    if (correo != null)   edtEmail.setText(correo);
                    if (password != null) {
                        edtPassword.setText(password);
                        edtPasswordConfirm.setText(password); // precargar ambas
                    }
                } else {
                    Toast.makeText(EditarUsuario.this,
                            "No se encontraron datos del usuario",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EditarUsuario.this,
                        "Error al cargar los datos",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Guardar solo el nombre en la BD
    private void saveName() {
        String newName = edtName.getText() != null ? edtName.getText().toString().trim() : "";

        if (TextUtils.isEmpty(newName)) {
            Toast.makeText(this, "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show();
            return;
        }

        usersRef.child(userId).child("nombres")
                .setValue(newName)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(EditarUsuario.this,
                                "Nombre actualizado correctamente",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(EditarUsuario.this,
                                "Error al actualizar el nombre",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Guardar nueva contraseña (FirebaseAuth + BD)
    private void savePassword() {
        String newPassword     = edtPassword.getText() != null ? edtPassword.getText().toString() : "";
        String confirmPassword = edtPasswordConfirm.getText() != null ? edtPasswordConfirm.getText().toString() : "";

        if (TextUtils.isEmpty(newPassword) || TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(this, "Debe completar ambos campos de contraseña", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this,
                    "No hay usuario autenticado. Inicie sesión nuevamente.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        currentUser.updatePassword(newPassword)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Actualizar también en la BD
                            usersRef.child(userId).child("password")
                                    .setValue(newPassword)
                                    .addOnCompleteListener(t2 -> {
                                        if (t2.isSuccessful()) {
                                            Toast.makeText(EditarUsuario.this,
                                                    "Contraseña actualizada correctamente",
                                                    Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(EditarUsuario.this,
                                                    "Error al guardar la contraseña en la base de datos",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            Toast.makeText(EditarUsuario.this,
                                    "Error al actualizar la contraseña en Firebase Auth",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}
