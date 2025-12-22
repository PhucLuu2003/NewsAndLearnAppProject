package com.example.newsandlearn.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newsandlearn.Activity.VocabularyRPGActivity;
import com.example.newsandlearn.R;
import com.google.android.material.card.MaterialCardView;

/**
 * GamesFragment - Danh sách các mini-games
 */
public class GamesFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_games, container, false);

        // Setup game cards
        setupGameCards(view);

        return view;
    }

    private void setupGameCards(View view) {
        // Vocabulary RPG Card
        MaterialCardView vocabRPGCard = view.findViewById(R.id.card_vocab_rpg);
        vocabRPGCard.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), VocabularyRPGActivity.class);
            startActivity(intent);
        });

        // Pronunciation Beat Card
        MaterialCardView pronunciationBeatCard = view.findViewById(R.id.card_pronunciation_beat);
        pronunciationBeatCard.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), com.example.newsandlearn.Activity.PronunciationBeatActivity.class);
            startActivity(intent);
        });

        // TODO: Thêm các game khác ở đây
        // MaterialCardView grammarQuizCard = view.findViewById(R.id.card_grammar_quiz);
        // MaterialCardView listeningGameCard = view.findViewById(R.id.card_listening_game);
    }
}
