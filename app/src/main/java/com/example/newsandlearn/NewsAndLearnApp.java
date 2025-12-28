package com.example.newsandlearn;

import android.app.Application;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

public class NewsAndLearnApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Ensure Firestore uses persistent local cache for better UX on slow/offline
        // networks.
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        firestore.setFirestoreSettings(settings);
    }
}
