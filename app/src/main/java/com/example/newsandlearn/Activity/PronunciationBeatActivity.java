package com.example.newsandlearn.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newsandlearn.Adapter.SongSelectionAdapter;
import com.example.newsandlearn.Model.PronunciationSong;
import com.example.newsandlearn.R;
import com.example.newsandlearn.Utils.PronunciationSongLibrary;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.List;

/**
 * Activity để chọn bài hát pronunciation
 */
public class PronunciationBeatActivity extends AppCompatActivity {

    private RecyclerView songsRecyclerView;
    private SongSelectionAdapter adapter;
    private List<PronunciationSong> songs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pronunciation_beat);

        setupToolbar();
        setupRecyclerView();
        loadSongs();
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("🎤 Pronunciation Beat");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        songsRecyclerView = findViewById(R.id.songs_recycler_view);
        songsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void loadSongs() {
        songs = PronunciationSongLibrary.getSampleSongs();
        
        adapter = new SongSelectionAdapter(songs, new SongSelectionAdapter.OnSongClickListener() {
            @Override
            public void onSongClick(PronunciationSong song) {
                if (song.isUnlocked()) {
                    startGame(song);
                } else {
                    Toast.makeText(PronunciationBeatActivity.this, 
                        "🔒 Complete easier songs to unlock!", 
                        Toast.LENGTH_SHORT).show();
                }
            }
        });
        
        songsRecyclerView.setAdapter(adapter);
    }

    private void startGame(PronunciationSong song) {
        Intent intent = new Intent(this, PronunciationGameActivity.class);
        intent.putExtra("SONG_ID", song.getId());
        startActivity(intent);
    }
}
