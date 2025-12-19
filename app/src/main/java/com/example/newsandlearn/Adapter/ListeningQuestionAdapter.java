package com.example.newsandlearn.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newsandlearn.Model.ListeningQuestion;
import com.example.newsandlearn.R;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListeningQuestionAdapter extends RecyclerView.Adapter<ListeningQuestionAdapter.QuestionViewHolder> {

    private Context context;
    private List<ListeningQuestion> questions;
    private Map<Integer, String> userAnswers; // position -> selected answer

    public ListeningQuestionAdapter(Context context, List<ListeningQuestion> questions) {
        this.context = context;
        this.questions = questions;
        this.userAnswers = new HashMap<>();
    }

    @NonNull
    @Override
    public QuestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_listening_question, parent, false);
        return new QuestionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuestionViewHolder holder, int position) {
        ListeningQuestion question = questions.get(position);
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
        int correct = 0;
        for (int i = 0; i < questions.size(); i++) {
            String userAnswer = userAnswers.get(i);
            if (userAnswer != null && userAnswer.equals(questions.get(i).getCorrectAnswer())) {
                correct++;
            }
        }
        return questions.size() > 0 ? (correct * 100) / questions.size() : 0;
    }

    class QuestionViewHolder extends RecyclerView.ViewHolder {
        TextView questionNumber, questionText;
        RadioGroup optionsGroup;
        RadioButton option1, option2, option3, option4;

        public QuestionViewHolder(@NonNull View itemView) {
            super(itemView);
            questionNumber = itemView.findViewById(R.id.question_number);
            questionText = itemView.findViewById(R.id.question_text);
            optionsGroup = itemView.findViewById(R.id.options_group);
            option1 = itemView.findViewById(R.id.option_1);
            option2 = itemView.findViewById(R.id.option_2);
            option3 = itemView.findViewById(R.id.option_3);
            option4 = itemView.findViewById(R.id.option_4);
        }

        public void bind(ListeningQuestion question, int position) {
            questionNumber.setText("Question " + (position + 1));
            questionText.setText(question.getQuestionText());

            // Set options
            List<String> options = question.getOptions();
            if (options != null && options.size() >= 4) {
                option1.setText(options.get(0));
                option2.setText(options.get(1));
                option3.setText(options.get(2));
                option4.setText(options.get(3));
                
                option1.setVisibility(View.VISIBLE);
                option2.setVisibility(View.VISIBLE);
                option3.setVisibility(View.VISIBLE);
                option4.setVisibility(View.VISIBLE);
            } else if (options != null && options.size() == 3) {
                option1.setText(options.get(0));
                option2.setText(options.get(1));
                option3.setText(options.get(2));
                option4.setVisibility(View.GONE);
            } else if (options != null && options.size() == 2) {
                option1.setText(options.get(0));
                option2.setText(options.get(1));
                option3.setVisibility(View.GONE);
                option4.setVisibility(View.GONE);
            }

            // Clear previous selection
            optionsGroup.clearCheck();

            // Restore user's previous answer if exists
            String previousAnswer = userAnswers.get(position);
            if (previousAnswer != null) {
                if (previousAnswer.equals(option1.getText().toString())) {
                    option1.setChecked(true);
                } else if (previousAnswer.equals(option2.getText().toString())) {
                    option2.setChecked(true);
                } else if (previousAnswer.equals(option3.getText().toString())) {
                    option3.setChecked(true);
                } else if (previousAnswer.equals(option4.getText().toString())) {
                    option4.setChecked(true);
                }
            }

            // Listen for answer selection
            optionsGroup.setOnCheckedChangeListener((group, checkedId) -> {
                RadioButton selectedButton = itemView.findViewById(checkedId);
                if (selectedButton != null) {
                    userAnswers.put(position, selectedButton.getText().toString());
                }
            });
        }
    }
}
