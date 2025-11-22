package com.example.proyecto_de_integracion;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class DetallePublicacionActivity extends AppCompatActivity {

    private ImageView imgDetalleProducto;
    private TextView txtDetalleTitulo, txtDetalleCategoria, txtDetallePrecio,
            txtDetalleEstado, txtDetalleUso, txtDetalleDescripcion, txtDetalleContacto;

    private LinearLayout layoutBotonesDueno, layoutChat;
    private Button btnEditarPublicacion, btnDesactivarPublicacion, btnEliminarPublicacion;
    private Button btnChatVendedor;
    private RecyclerView recyclerChat;
    private EditText etMensaje;
    private Button btnEnviarMensaje;

    private String idPublicacion;
    private Publicacion publicacionActual;

    private FirebaseAuth auth;
    private DatabaseReference refPublicaciones, refChats, refUsuarios;
    private String uidActual, nombreActual;

    private final ArrayList<MensajeChat> listaMensajes = new ArrayList<>();
    private MensajeAdapter mensajeAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_publicacion);

        // Views
        imgDetalleProducto = findViewById(R.id.imgDetalleProducto);
        txtDetalleTitulo = findViewById(R.id.txtDetalleTitulo);
        txtDetalleCategoria = findViewById(R.id.txtDetalleCategoria);
        txtDetallePrecio = findViewById(R.id.txtDetallePrecio);
        txtDetalleEstado = findViewById(R.id.txtDetalleEstado);
        txtDetalleUso = findViewById(R.id.txtDetalleUso);
        txtDetalleDescripcion = findViewById(R.id.txtDetalleDescripcion);
        txtDetalleContacto = findViewById(R.id.txtDetalleContacto);

        layoutBotonesDueno = findViewById(R.id.layoutBotonesDueno);
        layoutChat = findViewById(R.id.layoutChat);

        btnEditarPublicacion = findViewById(R.id.btnEditarPublicacion);
        btnDesactivarPublicacion = findViewById(R.id.btnDesactivarPublicacion);
        btnEliminarPublicacion = findViewById(R.id.btnEliminarPublicacion);
        btnChatVendedor = findViewById(R.id.btnChatVendedor);

        recyclerChat = findViewById(R.id.recyclerChat);
        etMensaje = findViewById(R.id.etMensaje);
        btnEnviarMensaje = findViewById(R.id.btnEnviarMensaje);

        // Recycler del chat
        recyclerChat.setLayoutManager(new LinearLayoutManager(this));
        mensajeAdapter = new MensajeAdapter(listaMensajes);
        recyclerChat.setAdapter(mensajeAdapter);

        // Firebase
        auth = FirebaseAuth.getInstance();
        uidActual = auth.getCurrentUser().getUid();
        refPublicaciones = FirebaseDatabase.getInstance().getReference("Publicaciones");
        refUsuarios = FirebaseDatabase.getInstance().getReference("Usuarios");

        // ID de la publicación
        idPublicacion = getIntent().getStringExtra("idPublicacion");
        if (idPublicacion == null) {
            Toast.makeText(this, "Publicación no encontrada", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        refChats = FirebaseDatabase.getInstance().getReference("Chats").child(idPublicacion);

        cargarNombreActual();
        cargarDetallePublicacion();
        escucharMensajes();

        // Mostrar chat al presionar el botón
        btnChatVendedor.setOnClickListener(v -> layoutChat.setVisibility(LinearLayout.VISIBLE));

        // Enviar mensaje
        btnEnviarMensaje.setOnClickListener(v -> enviarMensaje());

        // Editar publicación
        btnEditarPublicacion.setOnClickListener(v -> {
            if (publicacionActual == null) return;
            android.content.Intent intent =
                    new android.content.Intent(DetallePublicacionActivity.this,
                            EditarPublicacionActivity.class);
            intent.putExtra("idPublicacion", idPublicacion);
            startActivity(intent);
        });

        // Activar / Desactivar publicación
        btnDesactivarPublicacion.setOnClickListener(v -> {
            if (publicacionActual == null) return;
            boolean nuevoEstado = !publicacionActual.isActivo();
            cambiarActivo(nuevoEstado);
        });

        // Eliminar publicación
        btnEliminarPublicacion.setOnClickListener(v -> eliminarPublicacion());
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarDetallePublicacion();
    }

    private void cargarNombreActual() {
        refUsuarios.child(uidActual).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                nombreActual = snapshot.child("nombres").getValue(String.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void cargarDetallePublicacion() {
        refPublicaciones.child(idPublicacion)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        publicacionActual = snapshot.getValue(Publicacion.class);
                        if (publicacionActual == null) return;

                        // Imagen
                        Glide.with(DetallePublicacionActivity.this)
                                .load(publicacionActual.getImagenUrl())
                                .placeholder(R.mipmap.ic_launcher)
                                .into(imgDetalleProducto);

                        // Texto principal
                        txtDetalleTitulo.setText(publicacionActual.getTitulo());
                        txtDetalleDescripcion.setText(publicacionActual.getDescripcion());
                        txtDetalleCategoria.setText("Categoría: " + publicacionActual.getCategoria());
                        txtDetalleEstado.setText("Estado: " + publicacionActual.getEstadoProducto());
                        txtDetalleUso.setText("Uso: " + publicacionActual.getUso());
                        txtDetalleContacto.setText(
                                "Teléfono: " + publicacionActual.getTelefonoContacto() +
                                        "\nCorreo: " + publicacionActual.getCorreoVendedor());
                        txtDetallePrecio.setText("$" + (int) publicacionActual.getPrecio());

                        // Si soy dueño, muestro botones
                        if (uidActual.equals(publicacionActual.getUidVendedor())) {
                            layoutBotonesDueno.setVisibility(LinearLayout.VISIBLE);
                            actualizarTextoBotonActivo(publicacionActual.isActivo());
                        } else {
                            layoutBotonesDueno.setVisibility(LinearLayout.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
                });
    }

    /**
     * Cambia texto y fondo del botón de activar/desactivar usando drawables.
     * Activo  -> texto "Desactivar", fondo rojo.
     * Inactivo -> texto "Activar", fondo verde.
     */
    private void actualizarTextoBotonActivo(boolean estaActivo) {
        if (estaActivo) {
            btnDesactivarPublicacion.setText("Desactivar");
            btnDesactivarPublicacion.setBackgroundResource(R.drawable.bg_btn_danger);
            btnDesactivarPublicacion.setTextColor(Color.WHITE);
        } else {
            btnDesactivarPublicacion.setText("Activar");
            btnDesactivarPublicacion.setBackgroundResource(R.drawable.bg_btn_success);
            btnDesactivarPublicacion.setTextColor(Color.WHITE);
        }
    }

    private void escucharMensajes() {
        refChats.orderByChild("timestamp")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        listaMensajes.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            MensajeChat m = ds.getValue(MensajeChat.class);
                            if (m != null) listaMensajes.add(m);
                        }
                        mensajeAdapter.notifyDataSetChanged();
                        if (!listaMensajes.isEmpty()) {
                            recyclerChat.scrollToPosition(listaMensajes.size() - 1);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
                });
    }

    private void enviarMensaje() {
        String texto = etMensaje.getText().toString().trim();
        if (TextUtils.isEmpty(texto)) return;

        String idMensaje = refChats.push().getKey();
        if (idMensaje == null) return;

        long ts = System.currentTimeMillis();

        HashMap<String, Object> datos = new HashMap<>();
        datos.put("idMensaje", idMensaje);
        datos.put("idPublicacion", idPublicacion);
        datos.put("remitenteUid", uidActual);
        datos.put("remitenteNombre", nombreActual);
        datos.put("texto", texto);
        datos.put("timestamp", ts);

        refChats.child(idMensaje).setValue(datos)
                .addOnSuccessListener(unused -> etMensaje.setText(""))
                .addOnFailureListener(e ->
                        Toast.makeText(DetallePublicacionActivity.this,
                                "Error al enviar mensaje: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
    }

    private void cambiarActivo(boolean nuevoEstado) {
        if (publicacionActual == null) return;

        refPublicaciones.child(idPublicacion).child("activo").setValue(nuevoEstado)
                .addOnSuccessListener(unused -> {
                    publicacionActual.setActivo(nuevoEstado);
                    actualizarTextoBotonActivo(nuevoEstado);
                    String msg = nuevoEstado ? "Publicación activada" : "Publicación desactivada";
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void eliminarPublicacion() {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar publicación")
                .setMessage("¿Seguro que quieres eliminar esta publicación?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    refPublicaciones.child(idPublicacion).removeValue()
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(this, "Publicación eliminada", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}
