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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;

public class EditarPublicacionActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGEN = 201;

    private TextView txtTituloPantalla, txtCorreoAuto;
    private EditText etTitulo, etDescripcion, etCategoria, etEstadoProducto,
            etUso, etTelefono, etPrecio;
    private ImageView imgPreview;
    private Button btnSeleccionarImagen, btnGuardarPublicacion;

    private Uri imagenSeleccionadaUri;
    private String imagenActualUrl;

    private DatabaseReference refPublicaciones;
    private StorageReference storageRef;
    private ProgressDialog progressDialog;

    private String idPublicacion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_publicacion);   // reutilizamos layout

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Editar publicación");
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        txtTituloPantalla = findViewById(R.id.txtTituloPantalla);
        txtTituloPantalla.setText("Editar publicación");

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
        btnGuardarPublicacion.setText("Guardar cambios");

        refPublicaciones = FirebaseDatabase.getInstance().getReference("Publicaciones");
        storageRef = FirebaseStorage.getInstance()
                .getReference()
                .child("imagenes_publicaciones");

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Guardando cambios...");
        progressDialog.setCanceledOnTouchOutside(false);

        idPublicacion = getIntent().getStringExtra("idPublicacion");
        if (idPublicacion == null) {
            Toast.makeText(this, "Publicación no encontrada", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        cargarDatosPublicacion();

        btnSeleccionarImagen.setOnClickListener(v -> seleccionarImagen());
        btnGuardarPublicacion.setOnClickListener(v -> validarYActualizar());
    }

    private void cargarDatosPublicacion() {
        refPublicaciones.child(idPublicacion)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Publicacion p = snapshot.getValue(Publicacion.class);
                        if (p == null) {
                            Toast.makeText(EditarPublicacionActivity.this,
                                    "No se pudieron cargar los datos", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }

                        etTitulo.setText(p.getTitulo());
                        etDescripcion.setText(p.getDescripcion());
                        etCategoria.setText(p.getCategoria());
                        etEstadoProducto.setText(p.getEstadoProducto());
                        etUso.setText(p.getUso());
                        etTelefono.setText(p.getTelefonoContacto());
                        etPrecio.setText(String.valueOf((int) p.getPrecio()));
                        txtCorreoAuto.setText("Correo: " + p.getCorreoVendedor());

                        imagenActualUrl = p.getImagenUrl();
                        Glide.with(EditarPublicacionActivity.this)
                                .load(imagenActualUrl)
                                .placeholder(R.mipmap.ic_launcher)
                                .into(imgPreview);
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

    private void validarYActualizar() {
        String titulo = etTitulo.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();
        String categoria = etCategoria.getText().toString().trim();
        String estado = etEstadoProducto.getText().toString().trim();
        String uso = etUso.getText().toString().trim();
        String telefono = etTelefono.getText().toString().trim();
        String precioStr = etPrecio.getText().toString().trim();

        if (TextUtils.isEmpty(titulo) || TextUtils.isEmpty(descripcion)
                || TextUtils.isEmpty(categoria) || TextUtils.isEmpty(estado)
                || TextUtils.isEmpty(telefono) || TextUtils.isEmpty(precioStr)) {
            Toast.makeText(this, "Complete todos los campos obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        double precio;
        try {
            precio = Double.parseDouble(precioStr.replace(",", "."));
        } catch (NumberFormatException e) {
            Toast.makeText(this, "El precio debe ser numérico", Toast.LENGTH_SHORT).show();
            return;
        }

        final String tituloFinal = titulo;
        final String descripcionFinal = descripcion;
        final String categoriaFinal = categoria;
        final String estadoFinal = estado;
        final String usoFinal = uso;
        final String telefonoFinal = telefono;
        final double precioFinal = precio;

        if (imagenSeleccionadaUri != null) {
            progressDialog.setMessage("Subiendo imagen...");
            progressDialog.show();

            StorageReference imgRef = storageRef.child(idPublicacion + ".jpg");
            imgRef.putFile(imagenSeleccionadaUri)
                    .addOnSuccessListener(taskSnapshot ->
                            imgRef.getDownloadUrl()
                                    .addOnSuccessListener(uri ->
                                            actualizarPublicacion(tituloFinal, descripcionFinal,
                                                    categoriaFinal, estadoFinal, usoFinal,
                                                    telefonoFinal, precioFinal, uri.toString())
                                    )
                    )
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(EditarPublicacionActivity.this,
                                "Error al subir imagen: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });
        } else {
            actualizarPublicacion(tituloFinal, descripcionFinal, categoriaFinal, estadoFinal,
                    usoFinal, telefonoFinal, precioFinal, imagenActualUrl);
        }
    }

    private void actualizarPublicacion(String titulo, String descripcion, String categoria,
                                       String estado, String uso, String telefono,
                                       double precio, String imagenUrl) {

        progressDialog.setMessage("Guardando cambios...");
        progressDialog.show();

        HashMap<String, Object> datos = new HashMap<>();
        datos.put("titulo", titulo);
        datos.put("descripcion", descripcion);
        datos.put("categoria", categoria);
        datos.put("estadoProducto", estado);
        datos.put("uso", uso);
        datos.put("telefonoContacto", telefono);
        datos.put("precio", precio);
        datos.put("imagenUrl", imagenUrl);

        refPublicaciones.child(idPublicacion).updateChildren(datos)
                .addOnSuccessListener(unused -> {
                    progressDialog.dismiss();
                    Toast.makeText(EditarPublicacionActivity.this,
                            "Publicación actualizada", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(EditarPublicacionActivity.this,
                            "Error al guardar: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }
}
