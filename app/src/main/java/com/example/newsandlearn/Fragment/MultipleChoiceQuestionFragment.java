package com.example.newsandlearn.Fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newsandlearn.Adapter.MultipleChoiceAdapter;
import com.example.newsandlearn.Model.Question;
import com.example.newsandlearn.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

/**
 * Fragment for Multiple Choice questions
 * Features:
 * - Pill-shaped option buttons
 * - Press effect on selection
 * - Color feedback (green/red)
 * - Smooth animations
 */
public class MultipleChoiceQuestionFragment extends Fragment implements
        MultipleChoiceAdapter.OnOptionSelectedListener {

    private static final String ARG_QUESTION = "question";

    private Question question;
    private OnQuestionAnsweredListener listener;

    private TextView questionText, explanationText;
    private RecyclerView optionsContainer;
    private MaterialButton continueButton;
    private MaterialCardView explanationCard;
    private MultipleChoiceAdapter adapter;

    private boolean isAnswered = false;

    public interface OnQuestionAnsweredListener {
        void onQuestionAnswered(boolean isCorrect);

        void onContinueClicked();
    }

    public static MultipleChoiceQuestionFragment newInstance(Question question) {
        MultipleChoiceQuestionFragment fragment = new MultipleChoiceQuestionFragment();
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
        return inflater.inflate(R.layout.fragment_multiple_choice_question, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeViews(view);
        setupQuestion();
        setupListeners();
    }

    private void initializeViews(View view) {
        questionText = view.findViewById(R.id.question_text);
        optionsContainer = view.findViewById(R.id.options_container);
        continueButton = view.findViewById(R.id.continue_button);
        explanationCard = view.findViewById(R.id.explanation_card);
        explanationText = view.findViewById(R.id.explanation_text);
    }

    private void setupQuestion() {
        questionText.setText(question.getQuestionText());

        // Setup options RecyclerView
        adapter = new MultipleChoiceAdapter(
                question.getOptions(),
                question.getCorrectAnswer(),
                this);
        optionsContainer.setLayoutManager(new LinearLayoutManager(getContext()));
        optionsContainer.setAdapter(adapter);
    }

    private void setupListeners() {
        continueButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onContinueClicked();
            }
        });
    }

    @Override
    public void onOptionSelected(String selectedOption, boolean isCorrect) {
        if (isAnswered)
            return;

        isAnswered = true;

        // Notify listener
        if (listener != null) {
            listener.onQuestionAnswered(isCorrect);
        }

        if (isCorrect) {
            // Show continue button after delay
            new Handler().postDelayed(() -> {
                continueButton.setVisibility(View.VISIBLE);
                animateButtonAppearance();
            }, 1200);
        } else {
            // Show explanation
            explanationText.setText(question.getExplanation());
            explanationCard.setVisibility(View.VISIBLE);
            animateExplanationAppearance();

            // Show continue button
            new Handler().postDelayed(() -> {
                continueButton.setVisibility(View.VISIBLE);
                animateButtonAppearance();
            }, 1500);
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

    private void animateButtonAppearance() {
        continueButton.setScaleX(0.8f);
        continueButton.setScaleY(0.8f);
        continueButton.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(300)
                .setInterpolator(new OvershootInterpolator())
                .start();
    }
}
