package com.example.newsandlearn.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newsandlearn.R;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AdminDocumentAdapter extends RecyclerView.Adapter<AdminDocumentAdapter.ViewHolder> {

    public interface OnDocumentClickListener {
        void onClick(DocumentSnapshot doc);
    }

    private final List<DocumentSnapshot> docs = new ArrayList<>();
    private final OnDocumentClickListener listener;

    public AdminDocumentAdapter(OnDocumentClickListener listener) {
        this.listener = listener;
    }

    public void setDocuments(List<DocumentSnapshot> documents) {
        docs.clear();
        if (documents != null) {
            docs.addAll(documents);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DocumentSnapshot doc = docs.get(position);
        holder.title.setText(pickTitle(doc));
        holder.subtitle.setVisibility(View.VISIBLE);
        holder.subtitle.setText(doc.getId());
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onClick(doc);
            }
        });
    }

    @Override
    public int getItemCount() {
        return docs.size();
    }

    private String pickTitle(DocumentSnapshot doc) {
        if (doc == null)
            return "(null)";
        String[] candidates = new String[] { "title", "name", "question", "word", "topic", "category", "levelName",
                "id" };
        for (String key : candidates) {
            Object v = doc.get(key);
            if (v instanceof String) {
                String s = ((String) v).trim();
                if (!s.isEmpty())
                    return s;
            }
        }
        return "(document)";
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView subtitle;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.row_title);
            subtitle = itemView.findViewById(R.id.row_subtitle);
        }
    }
}
