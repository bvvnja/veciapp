package com.example.proyecto_de_integracion;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class Registro extends AppCompatActivity {

    EditText NombreEt, CorreoEt, ContraseñaEt, ConfirmarContraseñaEt,
            runEt, NumeroDeptoEt, CoefcopropiedadEt, M2DptoEt;
    Button RegistrarUsuario;
    Switch switchActivo;          // <- NUEVO

    FirebaseAuth firebaseAuth;
    ProgressDialog progressDialog;

    // Referencia a "Usuarios" para validar RUT duplicado
    private DatabaseReference refUsuarios;

    String nombre = " ", correo = " ", password = "", confirmarpassword = "",
            run = "", numerodepto = "", coefcopropiedad = "", m2depto = "";
    boolean activo = true;        // <- NUEVO (por defecto activo)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registro);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Registrar");
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        NombreEt = findViewById(R.id.NombreEt);
        CorreoEt = findViewById(R.id.CorreoEt);
        ContraseñaEt = findViewById(R.id.ContraseñaEt);
        ConfirmarContraseñaEt = findViewById(R.id.ConfirmarContraseñaEt);
        RegistrarUsuario = findViewById(R.id.RegistrarUsuario);
        runEt = findViewById(R.id.runEt);
        NumeroDeptoEt = findViewById(R.id.NumeroDeptoEt);
        CoefcopropiedadEt = findViewById(R.id.CoefcopropiedadEt);
        M2DptoEt = findViewById(R.id.M2DptoEt);
        switchActivo = findViewById(R.id.switchActivo);   // <- NUEVO

        firebaseAuth = FirebaseAuth.getInstance();
        refUsuarios = FirebaseDatabase.getInstance().getReference("Usuarios");

        progressDialog = new ProgressDialog(Registro.this);
        progressDialog.setTitle("Espere Por favor");
        progressDialog.setCanceledOnTouchOutside(false);

        RegistrarUsuario.setOnClickListener(view -> ValidarDatos());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void ValidarDatos() {
        nombre = NombreEt.getText().toString().trim();
        correo = CorreoEt.getText().toString().trim();
        password = ContraseñaEt.getText().toString();
        confirmarpassword = ConfirmarContraseñaEt.getText().toString();
        run = runEt.getText().toString().trim();
        numerodepto = NumeroDeptoEt.getText().toString().trim();
        coefcopropiedad = CoefcopropiedadEt.getText().toString().trim();
        m2depto = M2DptoEt.getText().toString().trim();
        activo = switchActivo.isChecked();   // <- lee true/false del switch

        if (TextUtils.isEmpty(nombre)) {
            Toast.makeText(this, "Ingrese nombre", Toast.LENGTH_SHORT).show();
        } else if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            Toast.makeText(this, "Ingrese correo válido", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(run)) {
            Toast.makeText(this, "Ingrese un RUT", Toast.LENGTH_SHORT).show();
        } else if (!esRutValido(run)) {
            Toast.makeText(this, "Ingrese un RUT válido", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(numerodepto)) {
            Toast.makeText(this, "Ingrese un Número de departamento", Toast.LENGTH_SHORT).show();
        }
        // SOLO NÚMEROS EN NÚMERO DEPTO
        else if (!numerodepto.matches("\\d+")) {
            Toast.makeText(this, "El número de departamento solo debe contener dígitos", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(coefcopropiedad)) {
            Toast.makeText(this, "Ingrese coeficiente de copropiedad", Toast.LENGTH_SHORT).show();
        } else {
            // Validar coeficiente numérico y rango (0,1]
            double coef;
            try {
                coef = Double.parseDouble(coefcopropiedad.replace(",", "."));
            } catch (NumberFormatException e) {
                Toast.makeText(this,
                        "El coeficiente de copropiedad debe ser numérico (use punto para decimales)",
                        Toast.LENGTH_LONG).show();
                return;
            }

            if (coef <= 0 || coef > 1) {
                Toast.makeText(this,
                        "El coeficiente de copropiedad debe estar entre 0 y 1",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(m2depto)) {
                Toast.makeText(this, "Ingrese metros cuadrados del departamento", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validar m2 numérico y positivo
            double m2;
            try {
                m2 = Double.parseDouble(m2depto.replace(",", "."));
            } catch (NumberFormatException e) {
                Toast.makeText(this,
                        "Los metros cuadrados deben ser numéricos (use punto para decimales)",
                        Toast.LENGTH_LONG).show();
                return;
            }

            if (m2 <= 0) {
                Toast.makeText(this,
                        "Los metros cuadrados deben ser mayores a 0",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Ingrese contraseña", Toast.LENGTH_SHORT).show();
            } else if (TextUtils.isEmpty(confirmarpassword)) {
                Toast.makeText(this, "Confirme contraseña", Toast.LENGTH_SHORT).show();
            } else if (!password.equals(confirmarpassword)) {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
            } else {
                // Si todo es válido, seguimos con la verificación de RUT en Firebase
                verificarRutNoDuplicado(run);
            }
        }
    }

    /**
     * Valida RUT chileno con dígito verificador.
     */
    private boolean esRutValido(String rut) {
        if (rut == null) return false;

        rut = rut.toUpperCase().replace(".", "").replace("-", "");

        if (rut.length() < 2) return false;

        String cuerpo = rut.substring(0, rut.length() - 1);
        char dvIngresado = rut.charAt(rut.length() - 1);

        if (!cuerpo.matches("\\d+")) return false;

        int suma = 0;
        int factor = 2;

        for (int i = cuerpo.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(cuerpo.charAt(i));
            suma += digit * factor;
            factor++;
            if (factor > 7) {
                factor = 2;
            }
        }

        int resto = suma % 11;
        int dvCalculadoNum = 11 - resto;
        char dvCalculado;

        if (dvCalculadoNum == 11) {
            dvCalculado = '0';
        } else if (dvCalculadoNum == 10) {
            dvCalculado = 'K';
        } else {
            dvCalculado = (char) (dvCalculadoNum + '0');
        }

        return dvIngresado == dvCalculado;
    }

    /**
     * Verifica en Firebase que el RUT no esté ya registrado.
     */
    private void verificarRutNoDuplicado(String runIngresado) {

        String rutLimpio = runIngresado.toUpperCase().replace(".", "").replace("-", "");

        progressDialog.setMessage("Verificando RUT...");
        progressDialog.show();

        refUsuarios.orderByChild("run").equalTo(rutLimpio)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            progressDialog.dismiss();
                            Toast.makeText(Registro.this,
                                    "El RUT ya se encuentra registrado",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            // No existe, se continúa con la creación de cuenta
                            CrearCuenta();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progressDialog.dismiss();
                        Toast.makeText(Registro.this,
                                "Error al verificar RUT: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
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
                                                    "Error al enviar correo de verificación: "
                                                            + task.getException().getMessage(),
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
        // aquí podrías usar show() si quieres ver este estado aparte
        progressDialog.dismiss();

        String uid = firebaseAuth.getUid();
        // Guardamos siempre el RUT en formato limpio
        String rutLimpio = run.toUpperCase().replace(".", "").replace("-", "");

        // Cambiamos a HashMap<String, Object> para poder guardar boolean
        HashMap<String, Object> Datos = new HashMap<>();
        Datos.put("uid", uid);
        Datos.put("correo", correo);
        Datos.put("nombres", nombre);
        Datos.put("password", password);
        Datos.put("run", rutLimpio);
        Datos.put("numerodepto", numerodepto);
        Datos.put("coefcopropiedad", coefcopropiedad);
        Datos.put("m2depto", m2depto);
        Datos.put("activo", activo);        // <- TRUE si switch activado, FALSE si no

        DatabaseReference databaseReference =
                FirebaseDatabase.getInstance().getReference("Usuarios");

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
