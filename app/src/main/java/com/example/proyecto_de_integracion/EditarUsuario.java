package com.example.proyecto_de_integracion;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class EditarUsuario extends AppCompatActivity {

    private EditText edtName, edtEmail;
    private TextInputEditText edtPassword, edtPasswordConfirm;

    // NUEVOS CAMPOS
    private TextInputEditText edtM2Depto;
    private TextInputEditText edtNumDepto;
    private TextInputEditText edtCoefCo;
    private Button btnSaveAll;

    private Button btnSaveName, btnSavePassword;

    private String userId;              // UID del usuario que se está editando
    private boolean esMiCuenta = false; // true si es el mismo usuario logeado

    private String passwordActualBD = null; // contraseña actual que está guardada en la BD

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_usuario);

        androidx.appcompat.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Editar");
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        // Firebase
        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("Usuarios");

        // UID que viene desde la lista (admin) o nulo
        userId = getIntent().getStringExtra("userId");

        // Si no viene, asumimos "Mi Perfil"
        FirebaseUser current = mAuth.getCurrentUser();
        if (userId == null && current != null) {
            userId = current.getUid();
        }

        // ¿Estoy editando mi propia cuenta?
        if (current != null && userId != null && current.getUid().equals(userId)) {
            esMiCuenta = true;
        }

        // UI
        edtName            = findViewById(R.id.edtName);
        edtEmail           = findViewById(R.id.edtEmail);
        edtPassword        = findViewById(R.id.passwordU);
        edtPasswordConfirm = findViewById(R.id.passwordConfirmU);
        btnSaveName        = findViewById(R.id.btnSaveName);
        btnSavePassword    = findViewById(R.id.btnSavePassword);

        // NUEVOS CAMPOS UI
        edtM2Depto         = findViewById(R.id.edtM2Depto);
        edtNumDepto        = findViewById(R.id.edtNumDepto);
        edtCoefCo          = findViewById(R.id.edtCoefCo);
        btnSaveAll         = findViewById(R.id.btnSaveAll);

        // Correo solo lectura
        edtEmail.setEnabled(false);
        edtEmail.setFocusable(false);
        edtEmail.setClickable(false);

        if (userId != null) {
            loadUserData();
        } else {
            Toast.makeText(this, "Error al obtener el usuario", Toast.LENGTH_SHORT).show();
        }

        btnSaveName.setOnClickListener(v -> saveName());
        btnSavePassword.setOnClickListener(v -> savePassword());

        // Guardar m2depto, numerodepto y coefcopropiedad
        btnSaveAll.setOnClickListener(v -> saveExtraFields());
    }

    private void loadUserData() {
        usersRef.child(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists()) {
                            Toast.makeText(EditarUsuario.this,
                                    "No se encontraron datos del usuario",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        String nombre = snapshot.child("nombres").getValue(String.class);
                        String correo = snapshot.child("correo").getValue(String.class);

                        // password puede ser String, Long, etc.
                        Object passObj = snapshot.child("password").getValue();
                        passwordActualBD = passObj != null ? passObj.toString() : null;

                        // NUEVOS CAMPOS: pueden venir como String, Long, Double...
                        Object m2Obj   = snapshot.child("m2depto").getValue();
                        Object numObj  = snapshot.child("numerodepto").getValue();
                        Object coefObj = snapshot.child("coefcopropiedad").getValue();

                        String m2Depto       = m2Obj   != null ? m2Obj.toString()   : "";
                        String numeroDepto   = numObj  != null ? numObj.toString()  : "";
                        String coefCo        = coefObj != null ? coefObj.toString() : "";

                        if (nombre != null) {
                            edtName.setText(nombre);
                        }

                        if (correo != null) {
                            edtEmail.setText(correo);
                        }

                        // Rellenar placeholders nuevos
                        edtM2Depto.setText(m2Depto);
                        edtNumDepto.setText(numeroDepto);
                        edtCoefCo.setText(coefCo);

                        // Solo precargamos la contraseña si es MI cuenta
                        if (esMiCuenta && passwordActualBD != null) {
                            edtPassword.setText(passwordActualBD);
                            edtPasswordConfirm.setText(passwordActualBD);
                        } else {
                            edtPassword.setText("");
                            edtPasswordConfirm.setText("");
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

    private void saveName() {
        String newName = edtName.getText() != null
                ? edtName.getText().toString().trim()
                : "";

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

    private void savePassword() {
        String newPassword =
                edtPassword.getText() != null ? edtPassword.getText().toString() : "";
        String confirmPassword =
                edtPasswordConfirm.getText() != null ? edtPasswordConfirm.getText().toString() : "";

        if (TextUtils.isEmpty(newPassword) || TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(this, "Debe completar ambos campos de contraseña", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newPassword.length() < 6) {
            Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this,
                    "No hay usuario autenticado. Inicie sesión nuevamente.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (esMiCuenta) {
            // ========== CAMBIAR CONTRASEÑA DE MI PROPIA CUENTA ==========
            currentUser.updatePassword(newPassword)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Actualizar también en la BD
                            usersRef.child(userId).child("password")
                                    .setValue(newPassword)
                                    .addOnCompleteListener(t2 -> {
                                        if (t2.isSuccessful()) {
                                            passwordActualBD = newPassword;
                                            Toast.makeText(EditarUsuario.this,
                                                    "Contraseña actualizada correctamente",
                                                    Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(EditarUsuario.this,
                                                    "Contraseña cambiada, pero no se pudo guardar en la BD",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            Toast.makeText(EditarUsuario.this,
                                    "Error al actualizar la contraseña en Firebase Auth",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });

        } else {
            // ================ ADMIN EDITANDO OTRO USUARIO =================
            // Solo actualizamos la BD. Auth de ese usuario se debe cambiar
            // desde consola o backend con privilegios de administrador.
            usersRef.child(userId).child("password")
                    .setValue(newPassword)
                    .addOnCompleteListener(t2 -> {
                        if (t2.isSuccessful()) {
                            Toast.makeText(EditarUsuario.this,
                                    "Contraseña actualizada en la base de datos.\n" +
                                            "Recuerda actualizarla también en Firebase Auth si quieres que se use para login.",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(EditarUsuario.this,
                                    "Error al guardar la contraseña en la base de datos",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    // Guarda m2depto, numerodepto y coefcopropiedad
    private void saveExtraFields() {
        String m2Depto   = edtM2Depto.getText()  != null ? edtM2Depto.getText().toString().trim()  : "";
        String numDepto  = edtNumDepto.getText() != null ? edtNumDepto.getText().toString().trim() : "";
        String coefCo    = edtCoefCo.getText()   != null ? edtCoefCo.getText().toString().trim()   : "";

        if (TextUtils.isEmpty(m2Depto) ||
                TextUtils.isEmpty(numDepto) ||
                TextUtils.isEmpty(coefCo)) {

            Toast.makeText(this,
                    "Complete M2, número de depto y coeficiente de copropiedad",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("m2depto", m2Depto);
        updates.put("numerodepto", numDepto);
        updates.put("coefcopropiedad", coefCo);

        usersRef.child(userId).updateChildren(updates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(EditarUsuario.this,
                                "Datos de departamento actualizados correctamente",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(EditarUsuario.this,
                                "Error al actualizar los datos de departamento",
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
