package com.example.newsandlearn.Fragment.Admin;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.newsandlearn.R;
import com.example.newsandlearn.Utils.FirebaseDataSeeder;
import com.example.newsandlearn.Utils.GameDataSeeder;
import com.google.android.material.button.MaterialButton;

/**
 * AdminContentFragment - Manage other content (Vocabulary, Videos, Games, etc.)
 */
public class AdminContentFragment extends Fragment {

    private MaterialButton btnSeedVocab, btnSeedVideos, btnSeedGames;
    private ProgressDialog progressDialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_content, container, false);
        
        initializeViews(view);
        
        return view;
    }

    private void initializeViews(View view) {
        btnSeedVocab = view.findViewById(R.id.btn_seed_vocab);
        btnSeedVideos = view.findViewById(R.id.btn_seed_videos);
        btnSeedGames = view.findViewById(R.id.btn_seed_games);
        // Note: btnSeedPhonics removed as seedPhonicsLessons method doesn't exist
        
        // Initialize progress dialog
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setCancelable(false);
        
        btnSeedVocab.setOnClickListener(v -> seedVocabulary());
        btnSeedVideos.setOnClickListener(v -> seedVideos());
        btnSeedGames.setOnClickListener(v -> seedGames());
        // Phonics seeding removed
    }

    private void seedVocabulary() {
        progressDialog.setTitle("Seeding Vocabulary");
        progressDialog.setMessage("Creating vocabulary sets...");
        progressDialog.show();

        FirebaseDataSeeder seeder = new FirebaseDataSeeder();
        seeder.seedVocabularies(new FirebaseDataSeeder.SeedCallback() {
            @Override
            public void onSuccess(String message) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        progressDialog.dismiss();
                        Toast.makeText(getContext(), "✅ " + message, Toast.LENGTH_LONG).show();
                    });
                }
            }

            @Override
            public void onProgress(String message) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> progressDialog.setMessage(message));
                }
            }

            @Override
            public void onFailure(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        progressDialog.dismiss();
                        Toast.makeText(getContext(), "❌ Error: " + error, Toast.LENGTH_LONG).show();
                    });
                }
            }
        });
    }

    private void seedVideos() {
        progressDialog.setTitle("Seeding Videos");
        progressDialog.setMessage("Creating video lessons...");
        progressDialog.show();

        FirebaseDataSeeder seeder = new FirebaseDataSeeder();
        seeder.seedVideoLessons(new FirebaseDataSeeder.SeedCallback() {
            @Override
            public void onSuccess(String message) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        progressDialog.dismiss();
                        Toast.makeText(getContext(), "✅ " + message, Toast.LENGTH_LONG).show();
                    });
                }
            }

            @Override
            public void onProgress(String message) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> progressDialog.setMessage(message));
                }
            }

            @Override
            public void onFailure(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        progressDialog.dismiss();
                        Toast.makeText(getContext(), "❌ Error: " + error, Toast.LENGTH_LONG).show();
                    });
                }
            }
        }, false); // Don't clear existing
    }

    private void seedGames() {
        progressDialog.setTitle("Seeding Games");
        progressDialog.setMessage("Creating game data...");
        progressDialog.show();

        GameDataSeeder.seedAllGameData(new GameDataSeeder.OnCompleteListener() {
            @Override
            public void onSuccess() {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        progressDialog.dismiss();
                        Toast.makeText(getContext(), "✅ Game data seeded successfully!", 
                            Toast.LENGTH_LONG).show();
                    });
                }
            }

            @Override
            public void onFailure(Exception e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        progressDialog.dismiss();
                        Toast.makeText(getContext(), "❌ Error: " + e.getMessage(), 
                            Toast.LENGTH_LONG).show();
                    });
                }
            }
        });
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}
