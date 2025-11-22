package com.example.proyecto_de_integracion;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class MensajeAdapter extends RecyclerView.Adapter<MensajeAdapter.HolderMensaje> {

    private ArrayList<MensajeChat> mensajes;
    private String uidActual;

    public MensajeAdapter(ArrayList<MensajeChat> mensajes) {
        this.mensajes = mensajes;
        uidActual = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @NonNull
    @Override
    public HolderMensaje onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_mensaje, parent, false);
        return new HolderMensaje(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderMensaje holder, int position) {
        MensajeChat m = mensajes.get(position);
        String quien = m.getRemitenteUid().equals(uidActual) ? "Tú" : m.getRemitenteNombre();
        holder.txtAutor.setText(quien);
        holder.txtTexto.setText(m.getTexto());
    }

    @Override
    public int getItemCount() {
        return mensajes.size();
    }

    static class HolderMensaje extends RecyclerView.ViewHolder {

        TextView txtAutor, txtTexto;

        public HolderMensaje(@NonNull View itemView) {
            super(itemView);
            txtAutor = itemView.findViewById(R.id.txtAutorMensaje);
            txtTexto = itemView.findViewById(R.id.txtTextoMensaje);
        }
    }
}
