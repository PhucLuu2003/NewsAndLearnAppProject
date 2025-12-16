package com.example.newsandlearn.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;


import com.example.newsandlearn.Fragment.RegisterFragment;
import com.example.newsandlearn.R;


import java.util.HashMap;

public class AuthOnboardingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth_onboarding);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.onboarding_container, new RegisterFragment())
                    .commit();
        }
    }
}
