package com.example.notesapp.ui.main;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notesapp.R;
import com.example.notesapp.data.local.Note;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {

    private List<Note> notes = new ArrayList<>();
    private OnNoteActionListener listener;

    public interface OnNoteActionListener {
        void onNoteClick(Note note);
        void onDelete(Note note);
        void onPin(Note note);

        void onAddTag(Note note, String tag);
    }

    public void setListener(OnNoteActionListener listener) {
        this.listener = listener;
    }

    public void setNotes(List<Note> newNotes) {
        this.notes = newNotes != null ? newNotes : new ArrayList<>();
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
        holder.bind(notes.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    // =========================
    // VIEW HOLDER
    // =========================
    static class NoteViewHolder extends RecyclerView.ViewHolder {

        TextView title, content, updatedAt, tags;
        ImageButton delete, pin;
        MaterialCardView card;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.title);
            content = itemView.findViewById(R.id.content);
            updatedAt = itemView.findViewById(R.id.updatedAt);
            tags = itemView.findViewById(R.id.tags);

            delete = itemView.findViewById(R.id.btnDelete);
            pin = itemView.findViewById(R.id.btnPin);

            card = itemView.findViewById(R.id.cardNote);
        }

        void bind(Note note, OnNoteActionListener listener) {

            if (note == null) return;

            title.setText(note.getTitle());
            content.setText(note.getContent());

            // =========================
            // TAGS (CSV -> UI)
            // =========================
            if (note.getTags() != null && !note.getTags().isEmpty()) {
                tags.setVisibility(View.VISIBLE);
                tags.setText(note.getTags().replace(",", " • "));
            } else {
                tags.setVisibility(View.GONE);
            }

            // =========================
            // TIME
            // =========================
            updatedAt.setText(
                    DateUtils.getRelativeTimeSpanString(
                            note.getUpdatedAt(),
                            System.currentTimeMillis(),
                            DateUtils.MINUTE_IN_MILLIS
                    )
            );

            // =========================
            // CLICK NOTE
            // =========================
            card.setOnClickListener(v -> {
                if (listener != null) listener.onNoteClick(note);
            });

            // =========================
            // DELETE
            // =========================
            delete.setOnClickListener(v -> {
                if (listener != null) listener.onDelete(note);
            });

            // =========================
            // PIN
            // =========================
            pin.setOnClickListener(v -> {
                if (listener != null) listener.onPin(note);
            });

            // =========================
            // ⭐ ADD TAG (MANCAVA → FIX PRINCIPALE)
            // =========================
            ImageButton addTag = itemView.findViewById(R.id.btnAddTag);

            if (addTag != null) {
                addTag.setOnClickListener(v -> {

                    if (listener == null) return;

                    android.app.AlertDialog.Builder builder =
                            new android.app.AlertDialog.Builder(v.getContext());

                    android.widget.EditText input =
                            new android.widget.EditText(v.getContext());

                    input.setHint("es: android");

                    builder.setTitle("Aggiungi tag");
                    builder.setView(input);

                    builder.setPositiveButton("OK", (dialog, which) -> {

                        String newTag = input.getText().toString().trim();

                        if (!newTag.isEmpty()) {
                            listener.onAddTag(note, newTag);
                        }
                    });

                    builder.setNegativeButton("Annulla", null);

                    builder.show();
                });
            }

            // =========================
            // UI STATE PIN
            // =========================
            boolean pinned = note.isPinned();

            pin.setImageResource(
                    pinned ? android.R.drawable.star_on : android.R.drawable.star_off
            );

            card.setStrokeWidth(pinned ? 6 : 0);

            card.setStrokeColor(
                    pinned
                            ? ContextCompat.getColor(itemView.getContext(), android.R.color.holo_orange_light)
                            : ContextCompat.getColor(itemView.getContext(), android.R.color.transparent)
            );
        }
    }
}