package com.example.newsandlearn.Activity;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.newsandlearn.Fragment.ArticleFragment;
import com.example.newsandlearn.Fragment.GamesFragment;
import com.example.newsandlearn.Fragment.HomeFragment;
import com.example.newsandlearn.Fragment.LearnFragment;
import com.example.newsandlearn.Fragment.ProfileFragment;
import com.example.newsandlearn.R;
import com.example.newsandlearn.Utils.AddLessonDataHelper;
import com.example.newsandlearn.Utils.SampleDataHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import android.widget.Toast;

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

        // Upload sample data for learning modules (chỉ chạy 1 lần)
        android.content.SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        boolean dataUploaded = prefs.getBoolean("sample_data_uploaded", false);

        if (!dataUploaded) {
            Log.d("MainActivity", "Uploading sample data to Firebase...");

            // Upload lessons
            AddLessonDataHelper.addDirectionsLesson();
            AddLessonDataHelper.addRestaurantLesson();

            SampleDataHelper sampleHelper = new SampleDataHelper();
            sampleHelper.generateAllSampleData(new SampleDataHelper.OnCompleteListener() {
                @Override
                public void onSuccess() {
                    Log.d("MainActivity", "✅ Sample data uploaded successfully!");
                    prefs.edit().putBoolean("sample_data_uploaded", true).apply();
                    Toast.makeText(MainActivity.this, "Sample data uploaded!", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(Exception e) {
                    Log.e("MainActivity", "❌ Failed to upload sample data: " + e.getMessage());
                }
            });
        }

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
            } else if (itemId == R.id.nav_learn) {
                selected = new LearnFragment();
            } else if (itemId == R.id.nav_games) {
                selected = new GamesFragment();
            } else if (itemId == R.id.nav_article) {
                selected = new ArticleFragment();
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