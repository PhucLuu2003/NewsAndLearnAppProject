package com.example.newsandlearn.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newsandlearn.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MillionaireLadderAdapter extends RecyclerView.Adapter<MillionaireLadderAdapter.TierViewHolder> {

    public static class TierItem {
        public final int tier; // 1..15
        public final int prize;

        public TierItem(int tier, int prize) {
            this.tier = tier;
            this.prize = prize;
        }
    }

    private final Context context;
    private final List<TierItem> items = new ArrayList<>();

    private int currentTier = 1;

    public MillionaireLadderAdapter(Context context) {
        this.context = context;
    }

    public void setItems(List<TierItem> newItems) {
        items.clear();
        if (newItems != null)
            items.addAll(newItems);
        notifyDataSetChanged();
    }

    public void setCurrentTier(int tier) {
        currentTier = tier;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TierViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_millionaire_ladder, parent, false);
        return new TierViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TierViewHolder holder, int position) {
        TierItem item = items.get(position);

        holder.tierNumber.setText(String.format(Locale.getDefault(), "%d", item.tier));
        holder.prizeValue.setText(String.format(Locale.getDefault(), "$%,d", item.prize));

        boolean isCurrent = item.tier == currentTier;
        boolean isSafe = item.tier == 5 || item.tier == 10;

        holder.container.setBackgroundResource(
                isCurrent ? R.drawable.bg_millionaire_ladder_current
                        : (isSafe ? R.drawable.bg_millionaire_ladder_safe : R.drawable.bg_millionaire_ladder_normal));

        holder.tierNumber.setTextColor(
                ContextCompat.getColor(context, isCurrent ? R.color.text_on_primary : R.color.text_primary));
        holder.prizeValue.setTextColor(
                ContextCompat.getColor(context, isCurrent ? R.color.text_on_primary : R.color.text_secondary));

        holder.safeBadge.setVisibility(isSafe ? View.VISIBLE : View.GONE);
        if (isSafe) {
            holder.safeBadge.setText(item.tier == 5 ? "SAFE" : "SAFE");
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class TierViewHolder extends RecyclerView.ViewHolder {
        View container;
        TextView tierNumber;
        TextView prizeValue;
        TextView safeBadge;

        public TierViewHolder(@NonNull View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.ladder_item_container);
            tierNumber = itemView.findViewById(R.id.tv_ladder_tier);
            prizeValue = itemView.findViewById(R.id.tv_ladder_prize);
            safeBadge = itemView.findViewById(R.id.tv_ladder_safe);
        }
    }
}
