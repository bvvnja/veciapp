package com.example.proyecto_de_integracion;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class PublicacionAdapter extends RecyclerView.Adapter<PublicacionAdapter.HolderPublicacion> {

    private Context context;
    private ArrayList<Publicacion> publicaciones;

    public PublicacionAdapter(Context context, ArrayList<Publicacion> publicaciones) {
        this.context = context;
        this.publicaciones = publicaciones;
    }

    @NonNull
    @Override
    public HolderPublicacion onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_publicacion, parent, false);
        return new HolderPublicacion(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderPublicacion holder, int position) {
        Publicacion pub = publicaciones.get(position);

        holder.txtTitulo.setText(pub.getTitulo());
        holder.txtCategoria.setText(pub.getCategoria());
        holder.txtPrecio.setText("$" + (int) pub.getPrecio());

        Glide.with(context)
                .load(pub.getImagenUrl())
                .placeholder(R.mipmap.ic_launcher)
                .into(holder.imgProducto);

        View.OnClickListener listener = v -> {
            Intent intent = new Intent(context, DetallePublicacionActivity.class);
            intent.putExtra("idPublicacion", pub.getIdPublicacion());
            context.startActivity(intent);
        };

        holder.itemView.setOnClickListener(listener);
        holder.btnVerDetalle.setOnClickListener(listener);
    }

    @Override
    public int getItemCount() {
        return publicaciones.size();
    }

    static class HolderPublicacion extends RecyclerView.ViewHolder {

        ImageView imgProducto;
        TextView txtTitulo, txtCategoria, txtPrecio;
        Button btnVerDetalle;

        public HolderPublicacion(@NonNull View itemView) {
            super(itemView);
            imgProducto = itemView.findViewById(R.id.imgProducto);
            txtTitulo = itemView.findViewById(R.id.txtTituloProducto);
            txtCategoria = itemView.findViewById(R.id.txtCategoriaProducto);
            txtPrecio = itemView.findViewById(R.id.txtPrecioProducto);
            btnVerDetalle = itemView.findViewById(R.id.btnVerDetalle);
        }
    }
}
