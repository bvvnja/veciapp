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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Login extends AppCompatActivity {

    EditText CorreoLogin, PassLogin;
    Button Btn_Logeo;

    ProgressDialog progressDialog;
    FirebaseAuth firebaseAuth;

    String correo = "", password = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Login");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        CorreoLogin = findViewById(R.id.CorreoLogin);
        PassLogin = findViewById(R.id.PassLogin);
        Btn_Logeo = findViewById(R.id.Btn_Logeo);

        firebaseAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(Login.this);
        progressDialog.setTitle("Espere por favor");
        progressDialog.setCanceledOnTouchOutside(false);

        Btn_Logeo.setOnClickListener(new View.OnClickListener() {
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

    private void ValidarDatos() {
        correo = CorreoLogin.getText().toString().trim();
        password = PassLogin.getText().toString().trim();

        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            Toast.makeText(this, "Correo inválido", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Ingrese contraseña", Toast.LENGTH_SHORT).show();
        } else {
            LoginDeUsuario();
        }
    }

    private void LoginDeUsuario() {
        progressDialog.setMessage("Iniciando sesión...");
        progressDialog.show();

        firebaseAuth.signInWithEmailAndPassword(correo, password)
                .addOnCompleteListener(Login.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.dismiss();

                        if (task.isSuccessful()) {
                            FirebaseUser user = firebaseAuth.getCurrentUser();

                            if (user != null) {
                                user.reload().addOnCompleteListener(reloadTask -> {
                                    String correoActual = user.getEmail();

                                    // ✅ Si el usuario es el admin, permitir acceso aunque no esté verificado
                                    if (correoActual != null && correoActual.equalsIgnoreCase("admin@gmail.com")) {
                                        Toast.makeText(Login.this,
                                                "Bienvenido Administrador",
                                                Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(Login.this, MenuPrincipal.class));
                                        finish();
                                        return;
                                    }

                                    // ⚠️ Para los demás, verificar si la cuenta está activa
                                    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Usuarios").child(user.getUid());
                                    userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            Boolean isActive = dataSnapshot.child("activo").getValue(Boolean.class); // Usamos Boolean en lugar de boolean

                                            // Si la cuenta está desactivada
                                            if (isActive == null || !isActive) {
                                                Toast.makeText(Login.this,
                                                        "Cuenta desactivada. Comunícate con el administrador para activarla.",
                                                        Toast.LENGTH_LONG).show();
                                                firebaseAuth.signOut();  // Cerrar sesión si la cuenta está desactivada
                                                return;
                                            }

                                            // ⚠️ Verificar que el correo esté confirmado
                                            if (!user.isEmailVerified()) {
                                                Toast.makeText(Login.this,
                                                        "Debe verificar su correo antes de iniciar sesión.",
                                                        Toast.LENGTH_LONG).show();
                                                firebaseAuth.signOut();
                                                return;
                                            }

                                            // 🔹 Si el correo está verificado y la cuenta está activa
                                            Toast.makeText(Login.this,
                                                    "Bienvenido(a): " + correoActual,
                                                    Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(Login.this, MenuUsuario.class));
                                            finish();
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {
                                            Toast.makeText(Login.this, "Error al verificar el estado de la cuenta", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                });
                            }
                        } else {
                            Toast.makeText(Login.this,
                                    "Verifique si el correo y la contraseña son correctos.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(Login.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}
