package com.example.newsandlearn.Adapter;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newsandlearn.Model.ReadingReviewItem;
import com.example.newsandlearn.R;

import java.util.List;

public class ReadingReviewAdapter extends RecyclerView.Adapter<ReadingReviewAdapter.ReviewViewHolder> {

    private final List<ReadingReviewItem> items;

    public ReadingReviewAdapter(List<ReadingReviewItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reading_review_question, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    static class ReviewViewHolder extends RecyclerView.ViewHolder {
        private final TextView qNumber;
        private final TextView status;
        private final TextView question;
        private final TextView yourAnswer;
        private final TextView correctAnswer;
        private final TextView explanation;
        private final TextView evidence;

        ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            qNumber = itemView.findViewById(R.id.review_q_number);
            status = itemView.findViewById(R.id.review_status);
            question = itemView.findViewById(R.id.review_question);
            yourAnswer = itemView.findViewById(R.id.review_your_answer);
            correctAnswer = itemView.findViewById(R.id.review_correct_answer);
            explanation = itemView.findViewById(R.id.review_explanation);
            evidence = itemView.findViewById(R.id.review_evidence);
        }

        void bind(ReadingReviewItem item) {
            qNumber.setText("Question " + (item.getIndex() + 1));
            status.setText(item.isCorrect() ? "Correct" : "Incorrect");

            question.setText(item.getQuestionText() != null ? item.getQuestionText() : "");

            String userAnswerText = item.getUserAnswer() != null ? item.getUserAnswer() : "(no answer)";
            yourAnswer.setText("Your answer: " + userAnswerText);

            String correctAnswerText = item.getCorrectAnswer() != null ? item.getCorrectAnswer() : "";
            correctAnswer.setText("Correct answer: " + correctAnswerText);

            String expl = item.getExplanation();
            if (expl == null || expl.trim().isEmpty()) {
                expl = "Explanation: This answer matches the passage.";
            } else {
                expl = "Explanation: " + expl;
            }
            explanation.setText(expl);

            String ev = item.getEvidence();
            if (ev == null || ev.trim().isEmpty()) {
                evidence.setText("Evidence: (not found)");
            } else {
                String label = "Evidence: ";
                SpannableString span = new SpannableString(label + ev);
                span.setSpan(new UnderlineSpan(), label.length(), label.length() + ev.length(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                evidence.setText(span);
            }
        }
    }
}
