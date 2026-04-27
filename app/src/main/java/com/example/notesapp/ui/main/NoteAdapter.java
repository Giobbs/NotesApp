package com.example.notesapp.ui.main;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notesapp.R;
import com.example.notesapp.data.local.Note;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {

    private List<Note> notes = new ArrayList<>();
    private OnNoteActionListener listener;

    // 🔥 Listener unico (più scalabile)
    public interface OnNoteActionListener {
        void onNoteClick(Note note);
        void onDelete(Note note);
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

        TextView title, content;
        ImageView delete;
        MaterialCardView card;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.title);
            content = itemView.findViewById(R.id.content);
            delete = itemView.findViewById(R.id.btnDelete);
            card = itemView.findViewById(R.id.cardNote);
        }

        void bind(Note note, OnNoteActionListener listener) {

            title.setText(note.title);
            content.setText(note.content);

            // 🔥 CLICK ITEM → EDIT
            card.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onNoteClick(note);
                }
            });

            // 🗑 DELETE
            delete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDelete(note);
                }
            });
        }
    }
}