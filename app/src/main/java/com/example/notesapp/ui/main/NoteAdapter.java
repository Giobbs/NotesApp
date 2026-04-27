package com.example.notesapp.ui.main;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notesapp.R;
import com.example.notesapp.data.local.Note;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {

    // =========================
    // DATA
    // =========================
    private List<Note> notes = new ArrayList<>();
    private OnNoteActionListener listener;

    // =========================
    // SELECTION (IMPORT/EXPORT)
    // =========================
    private Set<Long> selectedNotes = new HashSet<>();

    // =========================
    // MODE
    // =========================
    public enum Mode {
        NORMAL,
        SELECTABLE
    }

    private Mode mode = Mode.NORMAL;

    public void setMode(Mode mode) {
        this.mode = mode;
        notifyDataSetChanged();
    }

    public Set<Long> getSelectedNotes() {
        return selectedNotes;
    }

    // =========================
    // LISTENER
    // =========================
    public interface OnNoteActionListener {
        void onNoteClick(Note note);
        void onDelete(Note note);
        void onPin(Note note);
        void onShare(Note note);
        void onAddTag(Note note, String tag);
    }

    public void setListener(OnNoteActionListener listener) {
        this.listener = listener;
    }

    // =========================
    // DATA UPDATE
    // =========================
    public void setNotes(List<Note> newNotes) {
        this.notes = (newNotes != null) ? newNotes : new ArrayList<>();
        notifyDataSetChanged();
    }

    // =========================
    // ADAPTER
    // =========================
    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        holder.bind(notes.get(position), listener, mode, selectedNotes);
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
        ImageButton delete, pin, share;
        MaterialCardView card;
        CheckBox checkSelect;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.title);
            content = itemView.findViewById(R.id.content);
            updatedAt = itemView.findViewById(R.id.updatedAt);
            tags = itemView.findViewById(R.id.tags);

            delete = itemView.findViewById(R.id.btnDelete);
            pin = itemView.findViewById(R.id.btnPin);
            share = itemView.findViewById(R.id.btnShare);

            card = itemView.findViewById(R.id.cardNote);

            checkSelect = itemView.findViewById(R.id.checkSelect);
        }

        void bind(
                Note note,
                OnNoteActionListener listener,
                Mode mode,
                Set<Long> selectedNotes
        ) {

            if (note == null) return;

            // =========================
            // BASIC DATA
            // =========================
            title.setText(note.getTitle());
            content.setText(note.getContent());

            updatedAt.setText(
                    DateUtils.getRelativeTimeSpanString(
                            note.getUpdatedAt(),
                            System.currentTimeMillis(),
                            DateUtils.MINUTE_IN_MILLIS
                    )
            );

            // =========================
            // TAGS
            // =========================
            if (note.getTags() != null && !note.getTags().isEmpty()) {
                tags.setVisibility(View.VISIBLE);
                tags.setText(note.getTags().replace(",", " • "));
            } else {
                tags.setVisibility(View.GONE);
            }

            // =========================
            // CLICK
            // =========================
            card.setOnClickListener(v -> {
                if (listener != null) listener.onNoteClick(note);
            });

            delete.setOnClickListener(v -> {
                if (listener != null) listener.onDelete(note);
            });

            pin.setOnClickListener(v -> {
                if (listener != null) listener.onPin(note);
            });

            share.setOnClickListener(v -> {
                if (listener != null) listener.onShare(note);
            });

            // =========================
            // SELECTABLE MODE
            // =========================
            if (mode == Mode.SELECTABLE) {

                checkSelect.setVisibility(View.VISIBLE);

                checkSelect.setOnCheckedChangeListener(null);
                checkSelect.setChecked(selectedNotes.contains(note.id));

                checkSelect.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        selectedNotes.add(note.id);
                    } else {
                        selectedNotes.remove(note.id);
                    }
                });

            } else {
                checkSelect.setVisibility(View.GONE);
                checkSelect.setChecked(false);
            }

            // =========================
            // PIN UI
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