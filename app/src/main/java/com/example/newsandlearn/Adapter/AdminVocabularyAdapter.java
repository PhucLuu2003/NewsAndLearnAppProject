package com.example.newsandlearn.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newsandlearn.Model.Vocabulary;
import com.example.newsandlearn.R;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

/**
 * AdminVocabularyAdapter - Adapter for vocabulary list in admin panel
 */
public class AdminVocabularyAdapter extends RecyclerView.Adapter<AdminVocabularyAdapter.ViewHolder> {

    private Context context;
    private List<Vocabulary> vocabularies;
    private OnVocabularyActionListener listener;

    public interface OnVocabularyActionListener {
        void onAction(Vocabulary vocabulary, String action);
    }

    public AdminVocabularyAdapter(Context context, List<Vocabulary> vocabularies, OnVocabularyActionListener listener) {
        this.context = context;
        this.vocabularies = vocabularies;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_vocabulary, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Vocabulary vocab = vocabularies.get(position);
        
        holder.word.setText(vocab.getWord());
        holder.translation.setText(vocab.getTranslation());
        holder.phonetic.setText(vocab.getPronunciation() != null && !vocab.getPronunciation().isEmpty() 
            ? "/" + vocab.getPronunciation() + "/" : "");
        holder.category.setText(vocab.getCategory() != null ? vocab.getCategory() : "General");
        
        // Action buttons
        holder.btnView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAction(vocab, "view");
            }
        });
        
        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAction(vocab, "edit");
            }
        });
        
        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAction(vocab, "delete");
            }
        });
    }

    @Override
    public int getItemCount() {
        return vocabularies.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView card;
        TextView word, translation, phonetic, category;
        ImageButton btnView, btnEdit, btnDelete;

        ViewHolder(View itemView) {
            super(itemView);
            card = (MaterialCardView) itemView;
            word = itemView.findViewById(R.id.vocab_word);
            translation = itemView.findViewById(R.id.vocab_translation);
            phonetic = itemView.findViewById(R.id.vocab_phonetic);
            category = itemView.findViewById(R.id.vocab_category);
            btnView = itemView.findViewById(R.id.btn_view);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}
