package com.example.proyecto_de_integracion;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;

public class CrearPublicacionActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGEN = 101;

    private TextView txtTituloPantalla;
    private EditText etTitulo, etDescripcion, etCategoria, etEstadoProducto,
            etUso, etTelefono, etPrecio;
    private TextView txtCorreoAuto;
    private ImageView imgPreview;
    private Button btnSeleccionarImagen, btnGuardarPublicacion;

    private Uri imagenSeleccionadaUri;

    private FirebaseAuth auth;
    private DatabaseReference refUsuarios, refPublicaciones;
    private StorageReference storageRef;

    private String correoUsuario, nombreUsuario;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Crear Publicación");
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }
        setContentView(R.layout.activity_crear_publicacion);

        txtTituloPantalla = findViewById(R.id.txtTituloPantalla);
        txtTituloPantalla.setText("Crear publicación");

        etTitulo = findViewById(R.id.etTitulo);
        etDescripcion = findViewById(R.id.etDescripcion);
        etCategoria = findViewById(R.id.etCategoria);
        etEstadoProducto = findViewById(R.id.etEstadoProducto);
        etUso = findViewById(R.id.etUso);
        etTelefono = findViewById(R.id.etTelefono);
        etPrecio = findViewById(R.id.etPrecio);
        txtCorreoAuto = findViewById(R.id.txtCorreoAuto);
        imgPreview = findViewById(R.id.imgPreview);
        btnSeleccionarImagen = findViewById(R.id.btnSeleccionarImagen);
        btnGuardarPublicacion = findViewById(R.id.btnGuardarPublicacion);

        auth = FirebaseAuth.getInstance();
        refUsuarios = FirebaseDatabase.getInstance().getReference("Usuarios");
        refPublicaciones = FirebaseDatabase.getInstance().getReference("Publicaciones");
        storageRef = FirebaseStorage.getInstance()
                .getReference()
                .child("imagenes_publicaciones");

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Publicando...");
        progressDialog.setCanceledOnTouchOutside(false);

        cargarDatosUsuario();

        btnSeleccionarImagen.setOnClickListener(v -> seleccionarImagen());
        btnGuardarPublicacion.setOnClickListener(v -> validarYGuardar());
    }

    private void cargarDatosUsuario() {
        String uid = auth.getCurrentUser().getUid();
        refUsuarios.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                correoUsuario = snapshot.child("correo").getValue(String.class);
                nombreUsuario = snapshot.child("nombres").getValue(String.class);

                if (correoUsuario != null) {
                    txtCorreoAuto.setText("Correo: " + correoUsuario);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void seleccionarImagen() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Seleccionar imagen"), REQUEST_IMAGEN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGEN && resultCode == RESULT_OK && data != null) {
            imagenSeleccionadaUri = data.getData();
            Glide.with(this).load(imagenSeleccionadaUri).into(imgPreview);
        }
    }

    private void validarYGuardar() {
        String titulo = etTitulo.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();
        String categoria = etCategoria.getText().toString().trim();
        String estado = etEstadoProducto.getText().toString().trim();
        String uso = etUso.getText().toString().trim();
        String telefono = etTelefono.getText().toString().trim();
        String precioStr = etPrecio.getText().toString().trim();

        if (TextUtils.isEmpty(titulo) ||
                TextUtils.isEmpty(descripcion) ||
                TextUtils.isEmpty(categoria) ||
                TextUtils.isEmpty(estado) ||
                TextUtils.isEmpty(telefono) ||
                TextUtils.isEmpty(precioStr)) {
            Toast.makeText(this, "Complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        double precio;
        try {
            precio = Double.parseDouble(precioStr.replace(",", "."));
        } catch (NumberFormatException e) {
            Toast.makeText(this, "El precio debe ser numérico", Toast.LENGTH_SHORT).show();
            return;
        }

        if (imagenSeleccionadaUri == null) {
            Toast.makeText(this, "Seleccione una imagen", Toast.LENGTH_SHORT).show();
            return;
        }

        final String tituloFinal = titulo;
        final String descripcionFinal = descripcion;
        final String categoriaFinal = categoria;
        final String estadoFinal = estado;
        final String usoFinal = uso;
        final String telefonoFinal = telefono;
        final double precioFinal = precio;

        String idGenerado = refPublicaciones.push().getKey();
        if (idGenerado == null) {
            idGenerado = String.valueOf(System.currentTimeMillis());
        }
        final String idPublicacionFinal = idGenerado;

        progressDialog.setMessage("Subiendo imagen...");
        progressDialog.show();

        StorageReference imgRef = storageRef.child(idPublicacionFinal + ".jpg");
        imgRef.putFile(imagenSeleccionadaUri)
                .addOnSuccessListener(taskSnapshot ->
                        imgRef.getDownloadUrl()
                                .addOnSuccessListener(uri ->
                                        guardarPublicacionEnDb(
                                                idPublicacionFinal,
                                                tituloFinal,
                                                descripcionFinal,
                                                categoriaFinal,
                                                estadoFinal,
                                                usoFinal,
                                                telefonoFinal,
                                                precioFinal,
                                                uri.toString()
                                        )
                                )
                                .addOnFailureListener(e -> {
                                    progressDialog.dismiss();
                                    Toast.makeText(CrearPublicacionActivity.this,
                                            "Error al obtener URL: " + e.getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                })
                )
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(CrearPublicacionActivity.this,
                            "Error al subir imagen: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void guardarPublicacionEnDb(String idPublicacion, String titulo, String descripcion,
                                        String categoria, String estado, String uso,
                                        String telefono, double precio, String imagenUrl) {

        progressDialog.setMessage("Guardando publicación...");

        String uid = auth.getCurrentUser().getUid();
        long timestamp = System.currentTimeMillis();

        HashMap<String, Object> datos = new HashMap<>();
        datos.put("idPublicacion", idPublicacion);
        datos.put("uidVendedor", uid);
        datos.put("nombreVendedor", nombreUsuario);
        datos.put("correoVendedor", correoUsuario);
        datos.put("telefonoContacto", telefono);
        datos.put("titulo", titulo);
        datos.put("descripcion", descripcion);
        datos.put("categoria", categoria);
        datos.put("estadoProducto", estado);
        datos.put("uso", uso);
        datos.put("precio", precio);
        datos.put("imagenUrl", imagenUrl);
        datos.put("activo", true);
        datos.put("timestamp", timestamp);

        refPublicaciones.child(idPublicacion).setValue(datos)
                .addOnSuccessListener(unused -> {
                    progressDialog.dismiss();
                    Toast.makeText(CrearPublicacionActivity.this,
                            "Publicación creada correctamente",
                            Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(CrearPublicacionActivity.this,
                            "Error al guardar: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }
}
