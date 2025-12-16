package com.example.newsandlearn.Fragment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.newsandlearn.Activity.LoginActivity;
import com.example.newsandlearn.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class RegisterFragment extends Fragment {

    private Button btnSignup;
    private EditText inputName, inputEmail, inputPassword;
    private AlertDialog loadingDialog;

    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;

    public RegisterFragment() {
        // Required empty constructor
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);

        // UI
        android.widget.ImageView backArrow = view.findViewById(R.id.back_arrow);
        btnSignup = view.findViewById(R.id.btnCountinuetoRegister);
        inputName = view.findViewById(R.id.name_input);
        inputEmail = view.findViewById(R.id.email_input);
        inputPassword = view.findViewById(R.id.password_input);

        TextView tvHaveAccount = view.findViewById(R.id.tvHaveAccount);
        tvHaveAccount.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), LoginActivity.class))
        );
        backArrow.setOnClickListener(v -> {
            requireActivity().getOnBackPressedDispatcher().onBackPressed();
        });
        // Firebase
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        setupLoadingDialog();

        btnSignup.setOnClickListener(v -> createAccount());

        return view;
    }

    private void setupLoadingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_loading, null);
        builder.setView(dialogView);
        builder.setCancelable(false);
        loadingDialog = builder.create();
    }

    private void createAccount() {
        String name = inputName.getText().toString().trim();
        String email = inputEmail.getText().toString().trim();
        String password = inputPassword.getText().toString().trim();

        if (TextUtils.isEmpty(name) ||
                TextUtils.isEmpty(email) ||
                TextUtils.isEmpty(password)) {

            Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(getContext(), "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        loadingDialog.show();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        saveUserToFirestore(name, email);
                    } else {
                        loadingDialog.dismiss();
                        Toast.makeText(getContext(),
                                task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserToFirestore(String name, String email) {
        String uid = mAuth.getCurrentUser().getUid();

        HashMap<String, Object> userMap = new HashMap<>();
        userMap.put("uid", uid);
        userMap.put("name", name);
        userMap.put("email", email);
        userMap.put("role", 0);

        firestore.collection("Users")
                .document(uid)
                .set(userMap)
                .addOnSuccessListener(unused -> {
                    loadingDialog.dismiss();

                    // 🔥 NEXT STEP: go to LevelSelectionFragment
                    requireActivity()
                            .getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.onboarding_container, new LevelSelectionFragment())
                            .addToBackStack(null)
                            .commit();
                })
                .addOnFailureListener(e -> {
                    loadingDialog.dismiss();
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}

