package com.example.proyecto_de_integracion;

import android.os.Bundle;
import android.text.TextUtils;
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

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class PerfilUsuarioAdmin extends AppCompatActivity {

    private EditText edtName, edtEmail;
    private TextInputEditText edtPassword, edtPasswordConfirm;
    private Button btnSaveName, btnSavePassword;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference usersRef;

    private String userId;  // UID del admin (usuario actual)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_perfil_usuario_admin);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Mi perfil");
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        // Firebase
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        usersRef = FirebaseDatabase.getInstance().getReference("Usuarios");

        if (currentUser == null) {
            Toast.makeText(this,
                    "No hay sesión activa. Inicie sesión nuevamente.",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // El admin edita SU PROPIO perfil
        userId = currentUser.getUid();

        // Referencias UI
        edtName            = findViewById(R.id.edtName);
        edtEmail           = findViewById(R.id.edtEmail);
        edtPassword        = findViewById(R.id.passwordU);
        edtPasswordConfirm = findViewById(R.id.passwordConfirmU);
        btnSaveName        = findViewById(R.id.btnSaveName);
        btnSavePassword    = findViewById(R.id.btnSavePassword);

        // Correo solo lectura
        edtEmail.setEnabled(false);
        edtEmail.setFocusable(false);
        edtEmail.setClickable(false);

        // Cargar datos del admin
        loadAdminData();

        // Guardar nombre
        btnSaveName.setOnClickListener(v -> saveName());

        // Guardar contraseña (BD + Auth)
        btnSavePassword.setOnClickListener(v -> savePassword());
    }

    /** Carga nombre desde BD y correo desde FirebaseAuth del admin actual. */
    private void loadAdminData() {
        // Correo desde Auth (siempre es fiable)
        String correoAuth = currentUser.getEmail();
        if (correoAuth != null) {
            edtEmail.setText(correoAuth);
        }

        // Nombre desde la Realtime Database
        usersRef.child(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists()) {
                            // Si no está en BD, no cerramos la pantalla; solo avisamos
                            Toast.makeText(PerfilUsuarioAdmin.this,
                                    "No se encontró el perfil en la base de datos",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        String nombre = snapshot.child("nombres").getValue(String.class);
                        if (nombre != null) {
                            edtName.setText(nombre);
                        }
                        // No leemos ni mostramos la contraseña por seguridad
                        edtPassword.setText("");
                        edtPasswordConfirm.setText("");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(PerfilUsuarioAdmin.this,
                                "Error al cargar los datos",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /** Actualiza el nombre del admin en la BD. */
    private void saveName() {
        String newName = edtName.getText() != null
                ? edtName.getText().toString().trim()
                : "";

        if (TextUtils.isEmpty(newName)) {
            Toast.makeText(this,
                    "El nombre no puede estar vacío",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        usersRef.child(userId).child("nombres")
                .setValue(newName)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(PerfilUsuarioAdmin.this,
                                "Nombre actualizado correctamente",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(PerfilUsuarioAdmin.this,
                                "Error al actualizar el nombre",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Actualiza la contraseña del admin:
     * 1) en Firebase Auth (currentUser.updatePassword)
     * 2) en Realtime Database (Usuarios/{uid}/password)
     */
    private void savePassword() {
        String newPassword = edtPassword.getText() != null
                ? edtPassword.getText().toString()
                : "";
        String confirmPassword = edtPasswordConfirm.getText() != null
                ? edtPasswordConfirm.getText().toString()
                : "";

        if (TextUtils.isEmpty(newPassword) || TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(this,
                    "Debe completar ambos campos de contraseña",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(this,
                    "Las contraseñas no coinciden",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (newPassword.length() < 6) {
            Toast.makeText(this,
                    "La contraseña debe tener al menos 6 caracteres",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // 1) Actualizar en Firebase Auth
        currentUser.updatePassword(newPassword)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // 2) Si Auth ok, actualizar en Realtime Database
                        usersRef.child(userId).child("password")
                                .setValue(newPassword)
                                .addOnCompleteListener(t2 -> {
                                    if (t2.isSuccessful()) {
                                        Toast.makeText(PerfilUsuarioAdmin.this,
                                                "Contraseña actualizada correctamente",
                                                Toast.LENGTH_LONG).show();

                                        // Opcional: limpiar campos
                                        edtPassword.setText("");
                                        edtPasswordConfirm.setText("");
                                    } else {
                                        Toast.makeText(PerfilUsuarioAdmin.this,
                                                "Contraseña cambiada en Auth, pero hubo un error al guardar en la BD",
                                                Toast.LENGTH_LONG).show();
                                    }
                                });
                    } else {
                        Exception e = task.getException();
                        if (e instanceof FirebaseAuthRecentLoginRequiredException) {
                            Toast.makeText(PerfilUsuarioAdmin.this,
                                    "Por seguridad, inicia sesión nuevamente y luego cambia la contraseña.",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(PerfilUsuarioAdmin.this,
                                    "Error al actualizar la contraseña en Firebase Auth",
                                    Toast.LENGTH_LONG).show();
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
