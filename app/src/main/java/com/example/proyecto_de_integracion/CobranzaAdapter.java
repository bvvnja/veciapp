package com.example.proyecto_de_integracion;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CobranzaAdapter extends RecyclerView.Adapter<CobranzaAdapter.CobranzaViewHolder> {

    private List<Cobranza> cobranzaList;

    public CobranzaAdapter(List<Cobranza> cobranzaList) {
        this.cobranzaList = cobranzaList;
    }

    @Override
    public CobranzaViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Infla el layout del item del RecyclerView
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_item_cobranza, parent, false);
        return new CobranzaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CobranzaViewHolder holder, int position) {
        Cobranza cobranza = cobranzaList.get(position);

        // Muestra los datos de la cobranza en los TextViews
        holder.txtMesCobranza.setText(cobranza.getMesNombre() + " " + cobranza.getAnio());
        holder.txtEstadoCobranza.setText(cobranza.getEstadoPago());
        holder.txtDeptoCobranza.setText("Depto " + cobranza.getNumeroDepto());
        holder.txtMontoCobranza.setText("$" + String.format("%,.2f", cobranza.getMonto()));
        holder.txtDescripcionCobranza.setText(cobranza.getDescripcion());
    }

    @Override
    public int getItemCount() {
        return cobranzaList.size();
    }

    public static class CobranzaViewHolder extends RecyclerView.ViewHolder {
        TextView txtMesCobranza, txtEstadoCobranza, txtDeptoCobranza, txtMontoCobranza, txtDescripcionCobranza;

        public CobranzaViewHolder(View itemView) {
            super(itemView);
            txtMesCobranza = itemView.findViewById(R.id.txtMesCobranza);
            txtEstadoCobranza = itemView.findViewById(R.id.txtEstadoCobranza);
            txtDeptoCobranza = itemView.findViewById(R.id.txtDeptoCobranza);
            txtMontoCobranza = itemView.findViewById(R.id.txtMontoCobranza);
            txtDescripcionCobranza = itemView.findViewById(R.id.txtDescripcionCobranza);
        }
    }
}


