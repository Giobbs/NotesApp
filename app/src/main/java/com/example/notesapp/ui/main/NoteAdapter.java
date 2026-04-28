package com.example.notesapp.ui.main;

import android.app.AlertDialog;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notesapp.R;
import com.example.notesapp.data.local.Note;
import com.google.android.material.card.MaterialCardView;

import java.util.*;

public class NoteAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // =========================
    // TYPES
    // =========================
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_NOTE = 1;

    private List<NoteListItem> items = new ArrayList<>();
    private OnNoteActionListener listener;

    private final Set<Long> selectedNotes = new HashSet<>();
    private String aggregation = "none";

    public void setNotes(List<Note> notes) {
        setNotes(notes, aggregation);
    }
    public void setAggregation(String aggregation) {
        this.aggregation = aggregation;
    }

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
    // MODEL WRAPPER
    // =========================
    private static class NoteListItem {
        int type;
        String header;
        Note note;

        static NoteListItem header(String text) {
            NoteListItem item = new NoteListItem();
            item.type = TYPE_HEADER;
            item.header = text;
            return item;
        }

        static NoteListItem note(Note note) {
            NoteListItem item = new NoteListItem();
            item.type = TYPE_NOTE;
            item.note = note;
            return item;
        }
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
    // SET DATA (CORE LOGIC)
    // =========================
    public void setNotes(List<Note> newNotes, String aggregation) {

        items.clear();

        if (newNotes == null) {
            notifyDataSetChanged();
            return;
        }

        if ("tag".equals(aggregation)) {

            Map<String, List<Note>> map = new HashMap<>();

            for (Note n : newNotes) {
                String tag = (n.getTags() == null || n.getTags().isEmpty())
                        ? "Senza tag"
                        : n.getTags().split(",")[0].trim();

                map.computeIfAbsent(tag, k -> new ArrayList<>()).add(n);
            }

            for (String tag : map.keySet()) {
                items.add(NoteListItem.header("🏷 " + tag));
                for (Note n : map.get(tag)) {
                    items.add(NoteListItem.note(n));
                }
            }

        } else if ("date".equals(aggregation)) {

            long now = System.currentTimeMillis();

            List<Note> today = new ArrayList<>();
            List<Note> week = new ArrayList<>();
            List<Note> older = new ArrayList<>();

            for (Note n : newNotes) {
                long diff = now - n.getUpdatedAt();

                if (diff < 24L * 60 * 60 * 1000) {
                    today.add(n);
                } else if (diff < 7L * 24 * 60 * 60 * 1000) {
                    week.add(n);
                } else {
                    older.add(n);
                }
            }

            if (!today.isEmpty()) {
                items.add(NoteListItem.header("📅 Oggi"));
                today.forEach(n -> items.add(NoteListItem.note(n)));
            }

            if (!week.isEmpty()) {
                items.add(NoteListItem.header("📅 Ultimi 7 giorni"));
                week.forEach(n -> items.add(NoteListItem.note(n)));
            }

            if (!older.isEmpty()) {
                items.add(NoteListItem.header("📅 Più vecchie"));
                older.forEach(n -> items.add(NoteListItem.note(n)));
            }

        } else {
            for (Note n : newNotes) {
                items.add(NoteListItem.note(n));
            }
        }

        notifyDataSetChanged();
    }

    // =========================
    // ADAPTER CORE
    // =========================
    @Override
    public int getItemViewType(int position) {
        return items.get(position).type;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType == TYPE_HEADER) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_header, parent, false);
            return new HeaderViewHolder(v);
        }

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        NoteListItem item = items.get(position);

        if (item.type == TYPE_HEADER) {
            ((HeaderViewHolder) holder).bind(item.header);
        } else {
            ((NoteViewHolder) holder).bind(item.note, listener, mode, selectedNotes);
        }
    }

    // =========================
    // HEADER VIEW HOLDER
    // =========================
    static class HeaderViewHolder extends RecyclerView.ViewHolder {

        TextView title;

        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.headerTitle);
        }

        void bind(String text) {
            title.setText(text);
        }
    }

    // =========================
    // NOTE VIEW HOLDER
    // =========================
    static class NoteViewHolder extends RecyclerView.ViewHolder {

        TextView title, content, updatedAt, tags;
        ImageButton delete, pin, share, addTag;
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
            addTag = itemView.findViewById(R.id.btnAddTag);

            card = itemView.findViewById(R.id.cardNote);
            checkSelect = itemView.findViewById(R.id.checkSelect);
        }

        void bind(Note note, OnNoteActionListener listener, Mode mode, Set<Long> selectedNotes) {

            if (note == null) return;

            boolean selectable = (mode == Mode.SELECTABLE);

            title.setText(note.getTitle());

            content.setText(
                    note.isProtected ? "🔒 Contenuto protetto" : note.getContent()
            );

            // =========================
            // DATE
            // =========================
            java.text.SimpleDateFormat fullDateFormat =
                    new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());

            String created = fullDateFormat.format(new java.util.Date(note.getCreatedAt()));

            CharSequence updated = android.text.format.DateUtils.getRelativeTimeSpanString(
                    note.getUpdatedAt(),
                    System.currentTimeMillis(),
                    android.text.format.DateUtils.MINUTE_IN_MILLIS
            );

            updatedAt.setText("📅 " + created + " • ✏ " + updated);

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
            // CLICK NOTE
            // =========================
            card.setOnClickListener(v -> {
                if (listener != null && !selectable) {
                    listener.onNoteClick(note);
                }
            });

            int actionVisibility = selectable ? View.GONE : View.VISIBLE;

            delete.setVisibility(actionVisibility);
            pin.setVisibility(actionVisibility);
            share.setVisibility(actionVisibility);
            addTag.setVisibility(actionVisibility);

            if (!selectable) {

                delete.setOnClickListener(v -> {
                    if (listener != null) listener.onDelete(note);
                });

                pin.setOnClickListener(v -> {
                    if (listener != null) listener.onPin(note);
                });

                share.setOnClickListener(v -> {
                    if (listener != null) listener.onShare(note);
                });

                addTag.setOnClickListener(v -> {

                    if (listener == null) return;

                    AlertDialog.Builder builder =
                            new AlertDialog.Builder(itemView.getContext());

                    builder.setTitle("Aggiungi tag");

                    EditText input = new EditText(itemView.getContext());
                    builder.setView(input);

                    builder.setPositiveButton("OK", (dialog, which) -> {
                        String tag = input.getText().toString().trim();
                        if (!tag.isEmpty()) {
                            listener.onAddTag(note, tag);
                        }
                    });

                    builder.setNegativeButton("Annulla", null);
                    builder.show();
                });
            }

            checkSelect.setVisibility(selectable ? View.VISIBLE : View.GONE);

            if (selectable) {
                checkSelect.setOnCheckedChangeListener(null);
                checkSelect.setChecked(selectedNotes.contains(note.id));

                checkSelect.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) selectedNotes.add(note.id);
                    else selectedNotes.remove(note.id);
                });

            } else {
                checkSelect.setChecked(false);
            }

            if (!selectable) {
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
            } else {
                card.setStrokeWidth(0);
            }

            card.setAlpha(note.isProtected ? 0.7f : 1f);
        }
    }
}