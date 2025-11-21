package com.example.proyecto_de_integracion;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class GastoComunAdapter extends RecyclerView.Adapter<GastoComunAdapter.GastoViewHolder> {

    public interface OnGastoActionListener {
        void onEditar(Cobranza cobranza);
        void onEliminar(Cobranza cobranza);
    }

    // Lista que realmente dibuja el adapter
    private final List<Cobranza> listaGastos;
    private final OnGastoActionListener listener;
    private final String correoUsuario;   // si quieres mostrar el correo fijo

    public GastoComunAdapter(List<Cobranza> listaGastos,
                             String correoUsuario,
                             OnGastoActionListener listener) {
        this.listaGastos   = listaGastos;
        this.correoUsuario = correoUsuario;
        this.listener      = listener;
    }

    // 👉 Método para actualizar datos desde la Activity
    public void setData(List<Cobranza> nuevosGastos) {
        listaGastos.clear();
        listaGastos.addAll(nuevosGastos);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public GastoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_gasto_comun, parent, false);
        return new GastoViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull GastoViewHolder holder, int position) {
        Cobranza c = listaGastos.get(position);

        // Nombre
        holder.txtNombre.setText(
                c.getNombres() != null ? c.getNombres() : "Sin nombre"
        );

        // Correo (si quieres puedes cambiarlo a c.getCorreo() si tu modelo lo tiene)
        holder.txtCorreo.setText(
                correoUsuario != null ? correoUsuario : "correo no disponible"
        );

        // Monto
        holder.txtMonto.setText("$ " + c.getMonto());

        // Fecha (Mes + Año)
        String fecha = "";
        if (c.getMesNombre() != null) {
            fecha = c.getMesNombre() + " " + c.getAnio();
        }
        holder.txtFecha.setText(fecha);

        // Estado
        String estado = c.getEstadoPago() != null ? c.getEstadoPago() : "Desconocido";
        holder.txtEstado.setText("Estado: " + estado);

        if ("Pagado".equalsIgnoreCase(estado)) {
            holder.txtEstado.setTextColor(0xFF4CAF50); // Verde
        } else {
            holder.txtEstado.setTextColor(0xFFFF9800); // Naranjo
        }

        holder.btnEditar.setOnClickListener(v -> {
            if (listener != null) listener.onEditar(c);
        });

        holder.btnEliminar.setOnClickListener(v -> {
            if (listener != null) listener.onEliminar(c);
        });
    }

    @Override
    public int getItemCount() {
        return listaGastos.size();
    }

    public static class GastoViewHolder extends RecyclerView.ViewHolder {

        TextView txtNombre, txtCorreo, txtMonto, txtFecha, txtEstado;
        Button btnEditar, btnEliminar;

        public GastoViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNombre   = itemView.findViewById(R.id.txtNombreGasto);
            txtCorreo   = itemView.findViewById(R.id.txtCorreoGasto);
            txtMonto    = itemView.findViewById(R.id.txtMontoGasto);
            txtFecha    = itemView.findViewById(R.id.txtFechaGasto);
            txtEstado   = itemView.findViewById(R.id.txtEstadoGasto);
            btnEditar   = itemView.findViewById(R.id.btnEditarGasto);
            btnEliminar = itemView.findViewById(R.id.btnEliminarGasto);
        }
    }
}
