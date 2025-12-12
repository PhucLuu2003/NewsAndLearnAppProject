package com.example.newsandlearn.Fragment;

import android.animation.ObjectAnimator;
import android.content.ClipData;
import android.content.Context;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newsandlearn.Adapter.DraggableWordsAdapter;
import com.example.newsandlearn.Model.Question;
import com.example.newsandlearn.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment for Drag-and-Drop fill-in-the-blank questions
 * Features:
 * - Draggable word chips
 * - Drop zone with visual feedback
 * - Elevation effect during drag
 * - Smooth animations
 */
public class DragDropQuestionFragment extends Fragment {

    private static final String ARG_QUESTION = "question";

    private Question question;
    private OnQuestionAnsweredListener listener;

    private TextView textBeforeBlank, textAfterBlank, droppedWord, explanationText;
    private FrameLayout dropZone;
    private RecyclerView draggableWordsContainer;
    private MaterialButton submitButton, continueButton;
    private MaterialCardView explanationCard;
    private DraggableWordsAdapter adapter;

    private String selectedAnswer = null;
    private boolean isAnswered = false;

    public interface OnQuestionAnsweredListener {
        void onQuestionAnswered(boolean isCorrect);

        void onContinueClicked();
    }

    public static DragDropQuestionFragment newInstance(Question question) {
        DragDropQuestionFragment fragment = new DragDropQuestionFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_QUESTION, question);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnQuestionAnsweredListener) {
            listener = (OnQuestionAnsweredListener) context;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            question = (Question) getArguments().getSerializable(ARG_QUESTION);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_drag_drop_question, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeViews(view);
        setupQuestion();
        setupDragAndDrop();
        setupListeners();
    }

    private void initializeViews(View view) {
        textBeforeBlank = view.findViewById(R.id.text_before_blank);
        textAfterBlank = view.findViewById(R.id.text_after_blank);
        dropZone = view.findViewById(R.id.drop_zone);
        droppedWord = view.findViewById(R.id.dropped_word);
        draggableWordsContainer = view.findViewById(R.id.draggable_words_container);
        submitButton = view.findViewById(R.id.submit_button);
        continueButton = view.findViewById(R.id.continue_button);
        explanationCard = view.findViewById(R.id.explanation_card);
        explanationText = view.findViewById(R.id.explanation_text);
    }

    private void setupQuestion() {
        // Split question text into before and after blank
        String[] parts = question.calculateSplitQuestionText();
        textBeforeBlank.setText(parts[0]);
        textAfterBlank.setText(parts[1]);

        // Setup draggable words RecyclerView
        List<String> options = new ArrayList<>(question.getOptions());
        adapter = new DraggableWordsAdapter(options);
        draggableWordsContainer.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        draggableWordsContainer.setAdapter(adapter);
    }

    private void setupDragAndDrop() {
        // Set up drop zone to accept drops
        dropZone.setOnDragListener((v, event) -> {
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    return true;

                case DragEvent.ACTION_DRAG_ENTERED:
                    // Highlight drop zone
                    animateDropZoneHighlight(true);
                    return true;

                case DragEvent.ACTION_DRAG_EXITED:
                    // Remove highlight
                    animateDropZoneHighlight(false);
                    return true;

                case DragEvent.ACTION_DROP:
                    // Get dropped data
                    ClipData clipData = event.getClipData();
                    if (clipData != null && clipData.getItemCount() > 0) {
                        String word = clipData.getItemAt(0).getText().toString();
                        onWordDropped(word);
                    }
                    animateDropZoneHighlight(false);
                    return true;

                case DragEvent.ACTION_DRAG_ENDED:
                    animateDropZoneHighlight(false);
                    return true;

                default:
                    return false;
            }
        });
    }

    private void onWordDropped(String word) {
        selectedAnswer = word;
        droppedWord.setText(word);
        droppedWord.setVisibility(View.VISIBLE);

        // Animate the dropped word
        droppedWord.setScaleX(0.5f);
        droppedWord.setScaleY(0.5f);
        droppedWord.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(300)
                .setInterpolator(new OvershootInterpolator())
                .start();

        // Enable submit button
        submitButton.setEnabled(true);
    }

    private void animateDropZoneHighlight(boolean highlight) {
        if (highlight) {
            dropZone.setScaleX(1.05f);
            dropZone.setScaleY(1.05f);
            dropZone.setAlpha(1f);
        } else {
            dropZone.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(200)
                    .start();
        }
    }

    private void setupListeners() {
        submitButton.setOnClickListener(v -> checkAnswer());
        continueButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onContinueClicked();
            }
        });
    }

    private void checkAnswer() {
        if (isAnswered || selectedAnswer == null)
            return;

        isAnswered = true;
        boolean isCorrect = question.isCorrect(selectedAnswer);

        // Disable further interaction
        submitButton.setEnabled(false);
        adapter.setEnabled(false);

        // Notify listener
        if (listener != null) {
            listener.onQuestionAnswered(isCorrect);
        }

        if (isCorrect) {
            // Show correct state
            droppedWord.setTextColor(getResources().getColor(R.color.green));

            // Show continue button after delay
            new android.os.Handler().postDelayed(() -> {
                submitButton.setVisibility(View.GONE);
                continueButton.setVisibility(View.VISIBLE);
                animateButtonAppearance(continueButton);
            }, 1200);
        } else {
            // Show incorrect state
            droppedWord.setTextColor(getResources().getColor(R.color.red));

            // Show explanation
            explanationText.setText(question.getExplanation());
            explanationCard.setVisibility(View.VISIBLE);
            animateExplanationAppearance();

            // Show continue button
            submitButton.setVisibility(View.GONE);
            continueButton.setVisibility(View.VISIBLE);
            animateButtonAppearance(continueButton);
        }
    }

    private void animateExplanationAppearance() {
        explanationCard.setAlpha(0f);
        explanationCard.setTranslationY(20f);
        explanationCard.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(400)
                .setInterpolator(new OvershootInterpolator())
                .start();
    }

    private void animateButtonAppearance(View button) {
        button.setScaleX(0.8f);
        button.setScaleY(0.8f);
        button.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(300)
                .setInterpolator(new OvershootInterpolator())
                .start();
    }
}