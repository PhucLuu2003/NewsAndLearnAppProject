package com.example.newsandlearn.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newsandlearn.Model.MemoryPalace;
import com.example.newsandlearn.R;

import java.util.List;

/**
 * Adapter for displaying palace rooms in a grid
 */
public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.RoomViewHolder> {

    private List<MemoryPalace.Room> rooms;
    private OnRoomClickListener listener;

    public interface OnRoomClickListener {
        void onRoomClick(MemoryPalace.Room room, int position);
    }

    public RoomAdapter(List<MemoryPalace.Room> rooms, OnRoomClickListener listener) {
        this.rooms = rooms;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_room_card, parent, false);
        return new RoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoomViewHolder holder, int position) {
        MemoryPalace.Room room = rooms.get(position);
        holder.bind(room, position);
    }

    @Override
    public int getItemCount() {
        return rooms.size();
    }

    class RoomViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView emojiText, nameText, statusText;

        RoomViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.room_card);
            emojiText = itemView.findViewById(R.id.room_emoji);
            nameText = itemView.findViewById(R.id.room_name);
            statusText = itemView.findViewById(R.id.room_status);
        }

        void bind(MemoryPalace.Room room, int position) {
            emojiText.setText(room.getEmoji());
            nameText.setText(room.getName());
            
            if (room.hasWord()) {
                statusText.setText("✓ " + room.getWordMemory().getWord());
                statusText.setVisibility(View.VISIBLE);
                cardView.setCardBackgroundColor(itemView.getContext().getColor(R.color.success_light));
                
                // Add checkmark badge overlay
                emojiText.setText(room.getEmoji() + " ✓");
            } else {
                statusText.setText("Empty");
                statusText.setVisibility(View.VISIBLE);
                cardView.setCardBackgroundColor(itemView.getContext().getColor(R.color.surface_primary));
                emojiText.setText(room.getEmoji());
            }

            // Click animation
            cardView.setOnClickListener(v -> {
                v.animate()
                    .scaleX(0.9f)
                    .scaleY(0.9f)
                    .setDuration(100)
                    .withEndAction(() -> {
                        v.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(100)
                            .withEndAction(() -> {
                                if (listener != null) {
                                    listener.onRoomClick(room, position);
                                }
                            })
                            .start();
                    })
                    .start();
            });

            // Entrance animation
            itemView.setAlpha(0f);
            itemView.setTranslationY(50f);
            itemView.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(400)
                .setStartDelay(position * 50L)
                .start();
        }
    }
}
