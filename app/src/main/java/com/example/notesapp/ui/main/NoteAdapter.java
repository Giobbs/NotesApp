package com.example.notesapp.ui.main;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notesapp.R;
import com.example.notesapp.data.local.Note;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {

    private List<Note> notes = new ArrayList<>();
    private OnNoteActionListener listener;

    // =========================
    // LISTENER
    // =========================
    public interface OnNoteActionListener {
        void onNoteClick(Note note);
        void onDelete(Note note);
        void onPin(Note note);
    }

    public void setListener(OnNoteActionListener listener) {
        this.listener = listener;
    }

    public void setNotes(List<Note> newNotes) {
        this.notes = newNotes;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {

        Note note = notes.get(position);
        holder.bind(note, listener);

        holder.updatedAt.setText(
                DateUtils.getRelativeTimeSpanString(
                        note.getUpdatedAt(),
                        System.currentTimeMillis(),
                        DateUtils.MINUTE_IN_MILLIS
                )
        );
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    // =========================
    // VIEW HOLDER
    // =========================
    static class NoteViewHolder extends RecyclerView.ViewHolder {

        TextView title, content, updatedAt;
        ImageButton delete, pin;
        MaterialCardView card;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.title);
            content = itemView.findViewById(R.id.content);
            updatedAt = itemView.findViewById(R.id.updatedAt);

            delete = itemView.findViewById(R.id.btnDelete);
            pin = itemView.findViewById(R.id.btnPin);

            card = itemView.findViewById(R.id.cardNote);
        }

        void bind(Note note, OnNoteActionListener listener) {

            title.setText(note.title);
            content.setText(note.content);

            // =========================
            // CLICK NOTE (EDIT)
            // =========================
            card.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onNoteClick(note);
                }
            });

            // =========================
            // DELETE
            // =========================
            delete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDelete(note);
                }
            });

            // =========================
            // ⭐ PIN TOGGLE (FIX PRINCIPALE)
            // =========================
            pin.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPin(note);
                }
            });

            // =========================
            // UI STATE PIN
            // =========================
            boolean pinned = note.isPinned();

            pin.setImageResource(
                    pinned
                            ? android.R.drawable.star_on
                            : android.R.drawable.star_off
            );

            card.setStrokeWidth(pinned ? 6 : 0);

            card.setStrokeColor(
                    pinned
                            ? itemView.getResources().getColor(android.R.color.holo_orange_light)
                            : itemView.getResources().getColor(android.R.color.transparent)
            );
        }
    }
}