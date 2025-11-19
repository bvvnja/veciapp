package com.example.proyecto_de_integracion;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.widget.SearchView;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class user_profiles extends AppCompatActivity {

    private RecyclerView recyclerViewUsers;
    private UserAdapter userAdapter;
    private List<UserProfile> userList; // Lista de usuarios completa
    private List<UserProfile> filteredUserList; // Lista de usuarios filtrados
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profiles);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Mis Usuarios");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        // Conectar el RecyclerView con el layout
        recyclerViewUsers = findViewById(R.id.recyclerViewUsers);
        recyclerViewUsers.setLayoutManager(new LinearLayoutManager(this));  // Establecer un LayoutManager para el RecyclerView

        // Inicializar las listas de usuarios
        userList = new ArrayList<>();
        filteredUserList = new ArrayList<>();  // Lista de usuarios filtrados
        userAdapter = new UserAdapter(filteredUserList);  // Usar la lista filtrada en el adaptador
        recyclerViewUsers.setAdapter(userAdapter);

        // Conectar con la base de datos de Firebase
        usersRef = FirebaseDatabase.getInstance().getReference("Usuarios");

        // Obtener los usuarios desde Firebase
        getUsersFromDatabase();

        // Configurar el SearchView para la búsqueda
        SearchView searchView = findViewById(R.id.searchViewUsers);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;  // No es necesario hacer nada al pulsar enter
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Filtrar la lista de usuarios según el texto ingresado
                filterUsers(newText);
                return true;
            }
        });
    }

    private void getUsersFromDatabase() {
        // Leer los datos de la base de datos Firebase
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userList.clear();  // Limpiar la lista antes de agregar los nuevos datos
                filteredUserList.clear();  // Limpiar la lista filtrada

                // Recorrer cada usuario en la base de datos
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String uid = snapshot.getKey();
                    String nombre = snapshot.child("nombres").getValue(String.class);
                    String correo = snapshot.child("correo").getValue(String.class);

                    // Convertir a String para evitar el error de tipo
                    String coefPropiedad = String.valueOf(snapshot.child("coefcopropiedad").getValue());  // Usar String.valueOf() para convertir cualquier tipo numérico
                    String numDepto = String.valueOf(snapshot.child("numerodepto").getValue());  // Lo mismo para numDepto

                    String password = snapshot.child("password").getValue(String.class);

                    // Verificar si los campos esenciales no son nulos
                    if (nombre != null && correo != null && coefPropiedad != null && numDepto != null && password != null) {
                        // Leer si está activo o desactivado
                        Boolean isActive = snapshot.child("activo").getValue(Boolean.class);
                        if (isActive == null) {
                            isActive = false;  // Asignar "false" si no hay valor para "activo"
                        }

                        // Crear un objeto UserProfile y agregarlo a la lista solo si no es el admin
                        if (!correo.equals("admin@gmail.com")) {
                            UserProfile user = new UserProfile(uid, nombre, correo, coefPropiedad, numDepto, password, isActive);
                            userList.add(user);
                            filteredUserList.add(user);  // Agregar a la lista filtrada también
                        }
                    }
                }

                // Notificar al adaptador que los datos se han actualizado
                userAdapter.notifyDataSetChanged();  // Notificar que la lista ha cambiado
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(user_profiles.this, "Error al cargar los usuarios", Toast.LENGTH_SHORT).show();
            }
        });
    }




    // Método para filtrar los usuarios según la consulta
    private void filterUsers(String query) {
        filteredUserList.clear();
        if (query.isEmpty()) {
            filteredUserList.addAll(userList); // Si no hay consulta, mostramos todos los usuarios
        } else {
            for (UserProfile user : userList) {
                if (user.getNombre().toLowerCase().contains(query.toLowerCase()) ||
                        user.getCorreo().toLowerCase().contains(query.toLowerCase())) {
                    filteredUserList.add(user);
                }
            }
        }
        userAdapter.notifyDataSetChanged();  // Notificar al adaptador que la lista filtrada ha cambiado
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}
