package com.example.proyecto_de_integracion;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<UserProfile> userList;
    private Context context;

    public UserAdapter(List<UserProfile> userList) {
        this.userList = userList;
    }

    @Override
    public UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.activity_user_item, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(UserViewHolder holder, int position) {
        UserProfile user = userList.get(position);

        // Mostrar los datos del usuario
        holder.userName.setText(user.getNombre());
        holder.userEmail.setText(user.getCorreo());

        // Mostrar el número de departamento
        holder.userDetails.setText("Depto: " + user.getNumDepto());  // Mostrar el número del departamento

        // Si la cuenta está activa o desactivada
        if (user.isActivo()) {
            holder.accountStatus.setVisibility(View.GONE);  // Ocultar el mensaje de cuenta desactivada
            holder.btnToggleUserStatus.setText("Desactivar");  // Cambiar el texto del botón a "Desactivar"
            holder.btnToggleUserStatus.setBackgroundTintList(context.getResources().getColorStateList(R.color.red));  // Rojo
        } else {
            holder.accountStatus.setVisibility(View.VISIBLE);  // Mostrar el mensaje de cuenta desactivada
            holder.accountStatus.setText("Cuenta desactivada");  // Establecer el texto de "Cuenta desactivada"
            holder.btnToggleUserStatus.setText("Activar");  // Cambiar el texto del botón a "Activar"
            holder.btnToggleUserStatus.setBackgroundTintList(context.getResources().getColorStateList(R.color.green));  // Verde
        }

        // Acción para activar/desactivar la cuenta
        holder.btnToggleUserStatus.setOnClickListener(v -> {
            toggleUserStatus(user, position);  // Cambiar el estado de la cuenta
        });

        // Acción para editar el usuario
        holder.btnEditUser.setOnClickListener(v -> {
            // Crear la intención para abrir la actividad de edición
            Intent intent = new Intent(context, EditarUsuario.class);
            intent.putExtra("userId", user.getUid());  // Pasar el UID del usuario para que se pueda editar
            context.startActivity(intent);  // Iniciar la actividad de edición
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView userName, userEmail, userDetails, accountStatus;
        Button btnToggleUserStatus, btnEditUser;

        public UserViewHolder(View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.userName);
            userEmail = itemView.findViewById(R.id.userEmail);
            userDetails = itemView.findViewById(R.id.userDetails);
            accountStatus = itemView.findViewById(R.id.accountStatus);  // Para mostrar "Cuenta desactivada"
            btnToggleUserStatus = itemView.findViewById(R.id.btnToggleUserStatus);
            btnEditUser = itemView.findViewById(R.id.btnEditUser);  // Botón de editar
        }
    }

    // Método para activar o desactivar un usuario
    private void toggleUserStatus(UserProfile user, int position) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Usuarios").child(user.getUid());
        boolean newStatus = !user.isActivo();  // Cambiar el estado (si está activo, lo desactivamos y viceversa)

        // Cambiar el estado de "activo" en la base de datos
        userRef.child("activo").setValue(newStatus);

        // Actualizamos el estado en la lista local
        user.setActivo(newStatus);  // Cambiar el estado de la cuenta en la lista local

        // Mostrar el mensaje correspondiente
        if (newStatus) {
            Toast.makeText(context, "Cuenta activada", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Cuenta desactivada", Toast.LENGTH_SHORT).show();
        }

        // Notificar al adaptador que solo ese ítem cambió
        notifyItemChanged(position);  // Esto actualizará solo el ítem que cambió
    }
}
