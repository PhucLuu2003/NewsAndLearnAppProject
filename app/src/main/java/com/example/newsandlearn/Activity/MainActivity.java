package com.example.newsandlearn.Activity;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.newsandlearn.Fragment.FavoriteFragment;
import com.example.newsandlearn.Fragment.HomeFragment;
import com.example.newsandlearn.Fragment.ProfileFragment;
import com.example.newsandlearn.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Collections;


public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    BottomNavigationView bottomNav;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        db.collection("test").document("hello")
                .set(Collections.singletonMap("msg", "Firebase is connected!"))
                .addOnSuccessListener(a -> Log.d("FIREBASE", "SUCCESS"))
                .addOnFailureListener(e -> Log.e("FIREBASE", "ERROR"));

        bottomNav = findViewById(R.id.bottom_nav);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, new HomeFragment())
                .commit();

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selected = null;
            int itemId = item.getItemId(); // Get the ID once

            // Replacing the switch statement with if-else if-else
            if (itemId == R.id.nav_home) {
                selected = new HomeFragment();
            } else if (itemId == R.id.nav_favorite) {
                selected = new FavoriteFragment();
            } else if (itemId == R.id.nav_profile) {
                selected = new ProfileFragment();
            }

            // Note: If selected is still null, no navigation will occur.
            if (selected != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, selected)
                        .commit();
            }

            return true;
        });
    }
}