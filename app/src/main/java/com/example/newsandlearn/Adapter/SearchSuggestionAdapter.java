package com.example.newsandlearn.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newsandlearn.Model.Article;
import com.example.newsandlearn.R;

import java.util.ArrayList;
import java.util.List;

public class SearchSuggestionAdapter extends RecyclerView.Adapter<SearchSuggestionAdapter.ViewHolder> {

    private List<Article> suggestions = new ArrayList<>();
    private OnSuggestionClickListener listener;

    public interface OnSuggestionClickListener {
        void onSuggestionClick(Article article);
    }

    public SearchSuggestionAdapter(OnSuggestionClickListener listener) {
        this.listener = listener;
    }

    public void setSuggestions(List<Article> suggestions) {
        this.suggestions = suggestions;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_search_suggestion, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Article article = suggestions.get(position);
        
        holder.titleText.setText(article.getTitle());
        
        // Show category or source as subtitle
        String subtitle = "";
        if (article.getCategory() != null && !article.getCategory().isEmpty()) {
            subtitle = article.getCategory();
        }
        if (article.getLevel() != null && !article.getLevel().isEmpty()) {
            if (!subtitle.isEmpty()) subtitle += " • ";
            subtitle += article.getLevel();
        }
        
        holder.subtitleText.setText(subtitle);
        holder.subtitleText.setVisibility(subtitle.isEmpty() ? View.GONE : View.VISIBLE);
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSuggestionClick(article);
            }
        });
    }

    @Override
    public int getItemCount() {
        return suggestions.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView iconView;
        TextView titleText;
        TextView subtitleText;

        ViewHolder(View itemView) {
            super(itemView);
            iconView = itemView.findViewById(R.id.suggestion_icon);
            titleText = itemView.findViewById(R.id.suggestion_title);
            subtitleText = itemView.findViewById(R.id.suggestion_subtitle);
        }
    }
}
