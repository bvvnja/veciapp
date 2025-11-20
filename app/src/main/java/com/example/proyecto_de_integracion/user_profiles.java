package com.example.proyecto_de_integracion;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
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
    private List<UserProfile> userList;
    private List<UserProfile> filteredUserList;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profiles);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Mis Usuarios");
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        recyclerViewUsers = findViewById(R.id.recyclerViewUsers);
        recyclerViewUsers.setLayoutManager(new LinearLayoutManager(this));

        userList = new ArrayList<>();
        filteredUserList = new ArrayList<>();

        // El adaptador trabaja sobre la lista filtrada
        userAdapter = new UserAdapter(filteredUserList);
        recyclerViewUsers.setAdapter(userAdapter);

        usersRef = FirebaseDatabase.getInstance().getReference("Usuarios");

        getUsersFromDatabase();

        SearchView searchView = findViewById(R.id.searchViewUsers);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false; // no hacemos nada especial al presionar enter
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterUsers(newText);
                return true;
            }
        });
    }

    private void getUsersFromDatabase() {
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                filteredUserList.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    String uid    = snapshot.getKey();
                    String nombre = snapshot.child("nombres").getValue(String.class);
                    String correo = snapshot.child("correo").getValue(String.class);

                    Object coefObj  = snapshot.child("coefcopropiedad").getValue();
                    Object deptoObj = snapshot.child("numerodepto").getValue();

                    String coefPropiedad = coefObj != null ? coefObj.toString() : "";
                    String numDepto      = deptoObj != null ? deptoObj.toString() : "";

                    String password = snapshot.child("password").getValue(String.class);

                    // Leer "activo" (puede venir como boolean o como String "true"/"false")
                    Object activoValue = snapshot.child("activo").getValue();
                    boolean isActive = true; // por defecto, si no hay campo, se considera activo

                    if (activoValue != null) {
                        if (activoValue instanceof Boolean) {
                            isActive = (Boolean) activoValue;
                        } else {
                            isActive = Boolean.parseBoolean(activoValue.toString());
                        }
                    }

                    if (nombre != null &&
                            correo != null &&
                            password != null &&
                            !correo.equalsIgnoreCase("admin@gmail.com")) {

                        UserProfile user = new UserProfile(
                                uid,
                                nombre,
                                correo,
                                coefPropiedad,
                                numDepto,
                                password,
                                isActive
                        );

                        userList.add(user);
                        filteredUserList.add(user);
                    }
                }

                userAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(user_profiles.this,
                        "Error al cargar los usuarios",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterUsers(String query) {
        filteredUserList.clear();

        if (query == null || query.trim().isEmpty()) {
            filteredUserList.addAll(userList);
        } else {
            String lowerQuery = query.toLowerCase();
            for (UserProfile user : userList) {
                if (user.getNombre().toLowerCase().contains(lowerQuery) ||
                        user.getCorreo().toLowerCase().contains(lowerQuery)) {
                    filteredUserList.add(user);
                }
            }
        }

        userAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}
