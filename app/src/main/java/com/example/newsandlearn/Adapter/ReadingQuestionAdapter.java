package com.example.newsandlearn.Adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newsandlearn.Model.ReadingQuestion;
import com.example.newsandlearn.R;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReadingQuestionAdapter extends RecyclerView.Adapter<ReadingQuestionAdapter.QuestionViewHolder> {

    private List<ReadingQuestion> questions;
    private Map<Integer, String> userAnswers;

    public ReadingQuestionAdapter(List<ReadingQuestion> questions) {
        this.questions = questions;
        this.userAnswers = new HashMap<>();
    }

    @NonNull
    @Override
    public QuestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reading_question, parent, false);
        return new QuestionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuestionViewHolder holder, int position) {
        ReadingQuestion question = questions.get(position);
        holder.bind(question, position);
    }

    @Override
    public int getItemCount() {
        return questions != null ? questions.size() : 0;
    }

    public Map<Integer, String> getUserAnswers() {
        return userAnswers;
    }

    public int calculateScore() {
        if (questions == null || questions.isEmpty()) return 0;

        int correctCount = 0;
        for (int i = 0; i < questions.size(); i++) {
            String userAnswer = userAnswers.get(i);
            if (userAnswer != null && questions.get(i).checkAnswer(userAnswer)) {
                correctCount++;
            }
        }
        return (correctCount * 100) / questions.size();
    }

    class QuestionViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView questionNumber;
        TextView questionText;
        RadioGroup optionsGroup;

        QuestionViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (CardView) itemView;
            questionNumber = itemView.findViewById(R.id.question_number);
            questionText = itemView.findViewById(R.id.question_text);
            optionsGroup = itemView.findViewById(R.id.options_group);
        }

        void bind(ReadingQuestion question, int position) {
            questionNumber.setText("Question " + (position + 1));
            questionText.setText(question.getQuestionText());

            // Clear previous options
            optionsGroup.removeAllViews();
            optionsGroup.clearCheck();

            // Add options as radio buttons
            if (question.getOptions() != null) {
                for (int i = 0; i < question.getOptions().size(); i++) {
                    String option = question.getOptions().get(i);
                    RadioButton radioButton = createRadioButton(option);
                    optionsGroup.addView(radioButton);
                }
            }

            // Set listener for answer selection
            optionsGroup.setOnCheckedChangeListener((group, checkedId) -> {
                RadioButton selectedButton = group.findViewById(checkedId);
                if (selectedButton != null) {
                    String selectedAnswer = selectedButton.getText().toString();
                    userAnswers.put(position, selectedAnswer);

                    // Visual feedback
                    cardView.setCardElevation(8f);
                    cardView.postDelayed(() -> cardView.setCardElevation(4f), 200);
                }
            });

            // Restore previous answer if exists
            String previousAnswer = userAnswers.get(position);
            if (previousAnswer != null) {
                for (int i = 0; i < optionsGroup.getChildCount(); i++) {
                    RadioButton rb = (RadioButton) optionsGroup.getChildAt(i);
                    if (rb.getText().toString().equals(previousAnswer)) {
                        rb.setChecked(true);
                        break;
                    }
                }
            }
        }

        private RadioButton createRadioButton(String text) {
            RadioButton radioButton = new RadioButton(itemView.getContext());
            radioButton.setId(View.generateViewId());
            radioButton.setText(text);
            radioButton.setTextSize(15);
            radioButton.setTextColor(itemView.getContext().getResources().getColor(R.color.text_primary));

            // Padding and margins
            int paddingDp = (int) (12 * itemView.getContext().getResources().getDisplayMetrics().density);
            int marginDp = (int) (8 * itemView.getContext().getResources().getDisplayMetrics().density);
            radioButton.setPadding(paddingDp, paddingDp, paddingDp, paddingDp);

            RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(
                    RadioGroup.LayoutParams.MATCH_PARENT,
                    RadioGroup.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, marginDp / 2, 0, marginDp / 2);
            radioButton.setLayoutParams(params);

            // Style
            radioButton.setButtonTintList(itemView.getContext().getResources().getColorStateList(R.color.primary));

            return radioButton;
        }
    }
}