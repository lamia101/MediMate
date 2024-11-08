package com.example.personalhealthcareapplication.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.personalhealthcareapplication.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class Home extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextView tvDrawerName, tvDrawerWelcome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        drawerLayout = findViewById(R.id.drawerLayout);
        NavigationView navigationView = findViewById(R.id.navigationView);
        View headerView = navigationView.getHeaderView(0);
        tvDrawerName = headerView.findViewById(R.id.tvDrawerName);
        tvDrawerWelcome = headerView.findViewById(R.id.tvDrawerWelcome);

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Set up top app bar
        MaterialToolbar topAppBar = findViewById(R.id.topAppBar);
        topAppBar.setNavigationOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        // Set up the navigation drawer item click listener
        navigationView.setNavigationItemSelectedListener(this::handleNavigationItemSelected);

        // Load default fragment (Home or Appointments)
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment()); // Load HomeFragment by default
            navigationView.setCheckedItem(R.id.nav_home);
        }

        // Fetch and display user data if logged in
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            fetchUserInfo(user.getUid());
        } else {
            navigateToLogin();
        }
    }

    private boolean handleNavigationItemSelected(@NonNull MenuItem item) {
        Fragment selectedFragment = null;
        if (item.getItemId() == R.id.nav_home) {
            selectedFragment = new HomeFragment();
        } else if (item.getItemId() == R.id.nav_profile) {
            selectedFragment = new ProfileFragment();
        } else if (item.getItemId() == R.id.nav_logout) {
            logoutUser();
            return true;
        } else if (item.getItemId() == R.id.nav_appointment) {
            selectedFragment = new AppointmentFragment();
        } else if(item.getItemId() == R.id.nav_medicine_reminder){
            selectedFragment = new MedicineReminderFragment();
        }else{
            Toast.makeText(this, "Unknown Option", Toast.LENGTH_SHORT).show();
        }

        if (selectedFragment != null) {
            loadFragment(selectedFragment);
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }

    private void fetchUserInfo(String uid) {
        db.collection("Users").document(uid)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            String name = document.getString("name");
                            tvDrawerName.setText(name != null ? name : "User");
                            tvDrawerWelcome.setText("Welcome");
                        } else {
                            Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Failed to fetch user data", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void navigateToLogin() {
        startActivity(new Intent(Home.this, LoginActivity.class));
        finish();
    }

    private void logoutUser() {
        mAuth.signOut();
        navigateToLogin();
    }
}
