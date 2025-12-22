package com.example.newsandlearn.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newsandlearn.Adapter.RoomAdapter;
import com.example.newsandlearn.Model.MemoryPalace;
import com.example.newsandlearn.R;
import com.google.android.material.button.MaterialButton;

import java.util.Map;

/**
 * Palace Rooms Activity - Shows all rooms in the selected palace
 */
public class PalaceRoomsActivity extends AppCompatActivity implements RoomAdapter.OnRoomClickListener {

    private TextView palaceTitleText, progressText;
    private RecyclerView roomsRecyclerView;
    private MaterialButton startWalkButton;
    private RoomAdapter roomAdapter;
    private MemoryPalace palace;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_palace_rooms);

        String palaceType = getIntent().getStringExtra("PALACE_TYPE");
        initializePalace(palaceType);
        initializeViews();
        setupRecyclerView();
        loadWordsFromFirebase(); // Load from Firebase
        animateEntrance();
    }

    private void initializePalace(String palaceType) {
        switch (palaceType) {
            case "HOME":
                palace = MemoryPalace.createHomePalace();
                break;
            case "SCHOOL":
                palace = MemoryPalace.createSchoolPalace();
                break;
            case "MALL":
                palace = MemoryPalace.createMallPalace();
                break;
            default:
                palace = MemoryPalace.createHomePalace();
        }
    }

    private void initializeViews() {
        palaceTitleText = findViewById(R.id.palace_title);
        progressText = findViewById(R.id.progress_text);
        roomsRecyclerView = findViewById(R.id.rooms_recycler);
        startWalkButton = findViewById(R.id.start_walk_button);

        palaceTitleText.setText(palace.getName());
        updateProgress();

        startWalkButton.setOnClickListener(v -> startMemoryWalk());
    }

    private void setupRecyclerView() {
        roomAdapter = new RoomAdapter(palace.getRooms(), this);
        roomsRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        roomsRecyclerView.setAdapter(roomAdapter);
    }

    private void updateProgress() {
        int wordsPlaced = 0;
        for (MemoryPalace.Room room : palace.getRooms()) {
            if (room.hasWord()) wordsPlaced++;
        }
        progressText.setText(wordsPlaced + "/" + palace.getRooms().size() + " words placed");
    }

    @Override
    public void onRoomClick(MemoryPalace.Room room, int position) {
        // Animate click
        Intent intent = new Intent(this, WordPlacementActivity.class);
        intent.putExtra("PALACE_ID", palace.getId());
        intent.putExtra("ROOM_ID", room.getId());
        intent.putExtra("ROOM_NAME", room.getName());
        intent.putExtra("ROOM_EMOJI", room.getEmoji());
        intent.putExtra("ROOM_POSITION", position);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private void startMemoryWalk() {
        int wordsPlaced = 0;
        for (MemoryPalace.Room room : palace.getRooms()) {
            if (room.hasWord()) wordsPlaced++;
        }

        if (wordsPlaced == 0) {
            Toast.makeText(this, "Place some words first!", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, MemoryWalkActivity.class);
        intent.putExtra("PALACE_ID", palace.getId());
        intent.putExtra("TOTAL_ROOMS", palace.getRooms().size());
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    private void loadWordsFromFirebase() {
        com.example.newsandlearn.Utils.MemoryPalaceFirebase firebase = 
            new com.example.newsandlearn.Utils.MemoryPalaceFirebase();
        
        firebase.loadAllRooms(palace.getId(), palace.getRooms().size(), 
            roomsData -> {
                // Update palace with loaded data
                for (Map.Entry<Integer, MemoryPalace.WordMemory> entry : roomsData.entrySet()) {
                    palace.addWordToRoom(entry.getKey(), entry.getValue());
                }
                updateProgress();
                if (roomAdapter != null) {
                    roomAdapter.notifyDataSetChanged();
                }
            });
    }

    private void animateEntrance() {
        // Title animation
        palaceTitleText.setAlpha(0f);
        palaceTitleText.setTranslationY(-50f);
        palaceTitleText.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(500)
            .start();

        // Progress animation
        progressText.setAlpha(0f);
        new Handler().postDelayed(() -> {
            progressText.animate()
                .alpha(1f)
                .setDuration(500)
                .start();
        }, 200);

        // RecyclerView fade in
        roomsRecyclerView.setAlpha(0f);
        new Handler().postDelayed(() -> {
            roomsRecyclerView.animate()
                .alpha(1f)
                .setDuration(600)
                .start();
        }, 400);

        // Button slide up
        startWalkButton.setTranslationY(100f);
        startWalkButton.setAlpha(0f);
        new Handler().postDelayed(() -> {
            startWalkButton.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(500)
                .start();
        }, 600);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload from Firebase when returning from WordPlacementActivity
        loadWordsFromFirebase();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
