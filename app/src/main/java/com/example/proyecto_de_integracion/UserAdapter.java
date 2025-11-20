package com.example.proyecto_de_integracion;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
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

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context)
                .inflate(R.layout.activity_user_item, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        UserProfile user = userList.get(position);
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public class UserViewHolder extends RecyclerView.ViewHolder {

        TextView userName, userEmail, userDetails, accountStatus;
        Button btnToggleUserStatus, btnEditUser;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);

            userName           = itemView.findViewById(R.id.userName);
            userEmail          = itemView.findViewById(R.id.userEmail);
            userDetails        = itemView.findViewById(R.id.userDetails);
            accountStatus      = itemView.findViewById(R.id.accountStatus);
            btnToggleUserStatus = itemView.findViewById(R.id.btnToggleUserStatus);
            btnEditUser        = itemView.findViewById(R.id.btnEditUser);
        }

        public void bind(UserProfile user) {

            // Datos básicos
            userName.setText(user.getNombre());
            userEmail.setText(user.getCorreo());
            userDetails.setText("Depto: " + user.getNumDepto());

            // Estado de la cuenta
            if (user.isActivo()) {
                accountStatus.setVisibility(View.VISIBLE);
                accountStatus.setText("Cuenta activa");
                accountStatus.setTextColor(
                        ContextCompat.getColor(context, R.color.green));

                btnToggleUserStatus.setText("Desactivar");
                btnToggleUserStatus.setBackgroundTintList(
                        ContextCompat.getColorStateList(context, R.color.red));
            } else {
                accountStatus.setVisibility(View.VISIBLE);
                accountStatus.setText("Cuenta desactivada");
                accountStatus.setTextColor(
                        ContextCompat.getColor(context, R.color.red));

                btnToggleUserStatus.setText("Activar");
                btnToggleUserStatus.setBackgroundTintList(
                        ContextCompat.getColorStateList(context, R.color.green));
            }

            // Botón Activar / Desactivar
            btnToggleUserStatus.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos == RecyclerView.NO_POSITION) return;

                UserProfile currentUser = userList.get(pos);
                toggleUserStatus(currentUser, pos);
            });

            // Botón Editar
            btnEditUser.setOnClickListener(v -> {
                Intent intent = new Intent(context, EditarUsuario.class);
                intent.putExtra("userId", user.getUid());
                context.startActivity(intent);
            });
        }
    }

    /**
     * Cambia el estado "activo" del usuario:
     *  - Actualiza el campo en Firebase.
     *  - Actualiza el objeto local.
     *  - Refresca solo ese ítem en el RecyclerView.
     */
    private void toggleUserStatus(UserProfile user, int position) {

        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("Usuarios")
                .child(user.getUid());

        boolean newStatus = !user.isActivo();  // invertir estado

        userRef.child("activo").setValue(newStatus)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Actualizar modelo local
                        user.setActivo(newStatus);

                        // Notificar cambio solo de este ítem
                        notifyItemChanged(position);

                        Toast.makeText(
                                context,
                                newStatus ? "Cuenta activada" : "Cuenta desactivada",
                                Toast.LENGTH_SHORT
                        ).show();
                    } else {
                        Toast.makeText(
                                context,
                                "Error al actualizar el estado en la base de datos",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }
}
