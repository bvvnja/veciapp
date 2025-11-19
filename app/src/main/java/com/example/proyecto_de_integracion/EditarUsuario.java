package com.example.proyecto_de_integracion;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class EditarUsuario extends AppCompatActivity {

    private EditText edtName, edtEmail, edtPassword, edtCoefPropiedad, edtNumDepto;
    private Button btnSaveChanges;
    private String userId;

    // Inicializar FirebaseAuth
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_usuario);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Editar");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        // Inicializar los elementos de la interfaz
        edtName = findViewById(R.id.edtName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.passwordU);
        edtCoefPropiedad = findViewById(R.id.edtCoefPropiedad);
        edtNumDepto = findViewById(R.id.edtNumDepto);
        btnSaveChanges = findViewById(R.id.btnSaveChanges);

        // Obtener el ID del usuario desde el Intent
        userId = getIntent().getStringExtra("userId");
        if (userId != null) {
            loadUserData(userId); // Cargar los datos si el userId no es nulo
        } else {
            Toast.makeText(EditarUsuario.this, "Error al obtener el usuario", Toast.LENGTH_SHORT).show();
        }

        // Inicializar FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        // Cargar los datos del usuario
        loadUserData(userId);

        // Guardar los cambios cuando el usuario presione "Guardar Cambios"
        btnSaveChanges.setOnClickListener(v -> saveUserData());
    }

    private void loadUserData(String userId) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Usuarios").child(userId);
        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Obtener los datos del usuario
                String nombre = task.getResult().child("nombres").getValue(String.class);
                String correo = task.getResult().child("correo").getValue(String.class);
                String password = task.getResult().child("password").getValue(String.class);

                // Obtener coefPropiedad y numDepto como double desde Firebase
                Double coefPropiedad = task.getResult().child("coefcopropiedad").getValue(Double.class);  // Cambiado a Double
                Double m2depto = task.getResult().child("m2depto").getValue(Double.class);  // Cambiado a Double

                // Asignar los valores a los EditText
                edtName.setText(nombre);
                edtEmail.setText(correo);
                edtPassword.setText(password);

                // Convertir Double de nuevo a String para mostrar en los EditText
                if (coefPropiedad != null) {
                    edtCoefPropiedad.setText(String.valueOf(coefPropiedad));
                }

                if (m2depto != null) {
                    edtNumDepto.setText(String.valueOf(m2depto));
                }

            } else {
                Toast.makeText(EditarUsuario.this, "Error al cargar los datos", Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void saveUserData() {
        String newName = edtName.getText().toString().trim();
        String newEmail = edtEmail.getText().toString().trim();
        String newCoefPropiedad = edtCoefPropiedad.getText().toString().trim();
        String newNumDepto = edtNumDepto.getText().toString().trim();
        String newPassword = edtPassword.getText().toString().trim();

        // Validar que no haya campos vacíos
        if (TextUtils.isEmpty(newName) || TextUtils.isEmpty(newEmail) || TextUtils.isEmpty(newCoefPropiedad) || TextUtils.isEmpty(newNumDepto) || TextUtils.isEmpty(newPassword)) {
            Toast.makeText(this, "Todos los campos son requeridos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Convertir coefPropiedad y numDepto a double si son numéricos
        double coefPropiedadDouble = Double.parseDouble(newCoefPropiedad);  // Asegúrate de que la entrada es numérica
        double m2deptoDouble = Double.parseDouble(newNumDepto);  // Asegúrate de que la entrada es numérica

        // Actualizar en Firebase Authentication
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Actualizar el correo en Firebase Authentication
        currentUser.updateEmail(newEmail).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Si el correo se actualiza correctamente en Firebase Authentication
                // Actualizar la contraseña en Firebase Authentication
                currentUser.updatePassword(newPassword).addOnCompleteListener(passwordTask -> {
                    if (passwordTask.isSuccessful()) {
                        // Contraseña actualizada correctamente en Firebase Authentication

                        // Ahora, actualizamos los datos en Firebase Database
                        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Usuarios").child(userId);
                        userRef.child("nombres").setValue(newName);
                        userRef.child("correo").setValue(newEmail);
                        userRef.child("coefcopropiedad").setValue(coefPropiedadDouble);  // Guardar como double
                        userRef.child("m2depto").setValue(m2deptoDouble);  // Guardar como double
                        userRef.child("password").setValue(newPassword);

                        Toast.makeText(EditarUsuario.this, "Datos actualizados correctamente", Toast.LENGTH_SHORT).show();
                        finish();  // Cerrar la actividad
                    } else {
                        // Si no se puede actualizar la contraseña
                        Toast.makeText(EditarUsuario.this, "Error al actualizar la contraseña", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                // Si no se puede actualizar el correo
                Toast.makeText(EditarUsuario.this, "Error al actualizar el correo", Toast.LENGTH_SHORT).show();
            }
        });
    }



    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

}
