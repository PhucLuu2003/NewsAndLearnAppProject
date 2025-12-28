package com.example.newsandlearn.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newsandlearn.Model.VocabularyWithProgress;
import com.example.newsandlearn.R;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PracticeVocabularySelectAdapter extends RecyclerView.Adapter<PracticeVocabularySelectAdapter.ViewHolder> {

    public interface OnSelectionChangedListener {
        void onSelectionChanged(int selectedCount);
    }

    private final List<VocabularyWithProgress> items;
    private final Set<String> selectedIds = new HashSet<>();
    private final OnSelectionChangedListener selectionListener;

    public PracticeVocabularySelectAdapter(List<VocabularyWithProgress> items,
            OnSelectionChangedListener selectionListener) {
        this.items = items;
        this.selectionListener = selectionListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_practice_vocab_select, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        VocabularyWithProgress item = items.get(position);
        String vocabId = item.getId();

        holder.wordText.setText(item.getWord());
        holder.translationText.setText(item.getTranslation());

        boolean isChecked = vocabId != null && selectedIds.contains(vocabId);
        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setChecked(isChecked);

        View.OnClickListener toggle = v -> {
            if (vocabId == null)
                return;
            if (selectedIds.contains(vocabId)) {
                selectedIds.remove(vocabId);
            } else {
                selectedIds.add(vocabId);
            }
            notifyItemChanged(holder.getBindingAdapterPosition());
            if (selectionListener != null)
                selectionListener.onSelectionChanged(selectedIds.size());
        };

        holder.itemView.setOnClickListener(toggle);
        holder.checkBox.setOnClickListener(toggle);
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    public Set<String> getSelectedIds() {
        return new HashSet<>(selectedIds);
    }

    public void clearSelection() {
        selectedIds.clear();
        notifyDataSetChanged();
        if (selectionListener != null)
            selectionListener.onSelectionChanged(0);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView wordText;
        TextView translationText;
        CheckBox checkBox;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            wordText = itemView.findViewById(R.id.tv_word);
            translationText = itemView.findViewById(R.id.tv_translation);
            checkBox = itemView.findViewById(R.id.cb_select);
        }
    }
}
