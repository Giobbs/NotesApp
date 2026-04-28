package com.example.notesapp.ui.main;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notesapp.R;
import com.example.notesapp.data.local.Note;
import com.google.android.material.card.MaterialCardView;

import java.util.*;

public class NoteAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_NOTE = 1;
    private Context context;
    private final Set<String> expandedHeaders = new HashSet<>();

    private List<NoteListItem> items = new ArrayList<>();
    private List<Note> lastNotes = new ArrayList<>();

    private OnNoteActionListener listener;
    private final Set<Long> selectedNotes = new HashSet<>();

    private String aggregation = "none";
    public NoteAdapter(Context context) {
        this.context = context;
    }
    public NoteAdapter() {
    }
    public String getAggregation() {
        return aggregation;

    }

    public void setAggregation(String aggregation) {
        this.aggregation = aggregation;
        rebuild();
    }

    public enum Mode {
        NORMAL,
        IMPORT_EXPORT, SELECTABLE
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
    // MODEL
    // =========================
    private static class NoteListItem {
        int type;
        String header;
        String groupKey;
        Note note;

        static NoteListItem header(String text, String key) {
            NoteListItem item = new NoteListItem();
            item.type = TYPE_HEADER;
            item.header = text;
            item.groupKey = key;
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
        default void onRestore(Note note) {
        }
    }

    public void setListener(OnNoteActionListener listener) {
        this.listener = listener;
    }

    // =========================
    // SET NOTES
    // =========================
    public void setNotes(List<Note> newNotes, String aggregation) {
        this.lastNotes = new ArrayList<>(newNotes != null ? newNotes : new ArrayList<>());
        this.aggregation = aggregation != null ? aggregation : "none";
        rebuild();
    }

    private void rebuild() {
        items.clear();

        if (lastNotes.isEmpty()) {
            notifyDataSetChanged();
            return;
        }

        switch (aggregation) {

            case "tag":
                buildByTag();
                break;

            case "date":
                buildByDate();
                break;

            default:
                buildFlat();
                break;
        }

        notifyDataSetChanged();
    }

    private void buildFlat() {
        for (Note n : lastNotes) {
            items.add(NoteListItem.note(n));
        }
    }

    private void buildByTag() {

        Map<String, List<Note>> map = new LinkedHashMap<>();

        for (Note n : lastNotes) {

            List<String> tags = n.getTagList();

            if (tags.isEmpty()) {
                map.computeIfAbsent("Senza tag", k -> new ArrayList<>()).add(n);
            } else {
                for (String tag : tags) {
                    map.computeIfAbsent(tag, k -> new ArrayList<>()).add(n);
                }
            }
        }

        for (Map.Entry<String, List<Note>> e : map.entrySet()) {

            String key = e.getKey();

            items.add(NoteListItem.header("🏷 " + key, key));

            if (expandedHeaders.contains(key)) {
                for (Note n : e.getValue()) {
                    items.add(NoteListItem.note(n));
                }
            }
        }
    }
    private void buildByDate() {

        long now = System.currentTimeMillis();

        List<Note> today = new ArrayList<>();
        List<Note> week = new ArrayList<>();
        List<Note> older = new ArrayList<>();

        for (Note n : lastNotes) {

            long diff = now - n.getUpdatedAt();

            if (diff < 24L * 60 * 60 * 1000) today.add(n);
            else if (diff < 7L * 24 * 60 * 60 * 1000) week.add(n);
            else older.add(n);
        }

        addGroup("TODAY", "📅 Oggi", today);
        addGroup("WEEK", "📅 Ultimi 7 giorni", week);
        addGroup("OLDER", "📅 Più vecchie", older);
    }

    private void addGroup(String key, String title, List<Note> list) {

        if (list.isEmpty()) return;

        items.add(NoteListItem.header(title, key));

        if (expandedHeaders.contains(key)) {
            for (Note n : list) {
                items.add(NoteListItem.note(n));
            }
        }
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
            ((HeaderViewHolder) holder).bind(item.groupKey, item.header);
        } else {
            ((NoteViewHolder) holder).bind(item.note, mode);
        }
    }

    // =========================
    // HEADER
    // =========================
    class HeaderViewHolder extends RecyclerView.ViewHolder {

        TextView title;

        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.headerTitle);
        }

        void bind(String key, String text) {

            title.setText(text);

            itemView.setOnClickListener(v -> {
                if (expandedHeaders.contains(key)) {
                    expandedHeaders.remove(key);
                } else {
                    expandedHeaders.add(key);
                }
                rebuild();
            });
        }
    }

    // =========================
    // NOTE
    // =========================
    class NoteViewHolder extends RecyclerView.ViewHolder {

        TextView title, content, updatedAt;
        CheckBox checkSelect;
        MaterialCardView card;
        ImageButton btnShare, btnPin, btnDelete, btnTag, btnRestore;
        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.title);
            content = itemView.findViewById(R.id.content);
            updatedAt = itemView.findViewById(R.id.updatedAt);
            checkSelect = itemView.findViewById(R.id.checkSelect);
            card = itemView.findViewById(R.id.cardNote);

            btnShare = itemView.findViewById(R.id.btnShare);
            btnPin = itemView.findViewById(R.id.btnPin);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnTag = itemView.findViewById(R.id.btnAddTag);
            btnRestore = itemView.findViewById(R.id.btnRestore);
        }

        void bind(Note note, Mode mode) {

            if (note == null) return;

            boolean selectable = mode == Mode.SELECTABLE || mode == Mode.IMPORT_EXPORT;
            boolean importExportMode = mode == Mode.IMPORT_EXPORT;
            boolean pinned = note.isPinned();

            // =========================
            // TEXT
            // =========================
            title.setText(note.getTitle());
            if (note.isProtected) {
                content.setText("🔒 Tocca per sbloccare");
            } else {
                content.setText(note.getSafeContent());
            }
            long now = System.currentTimeMillis();

            long created = note.getCreatedAt();
            String createdText;

            if (created <= 0) {
                createdText = "—";
            } else {
                createdText = new java.text.SimpleDateFormat(
                        "dd/MM/yyyy",
                        java.util.Locale.getDefault()
                ).format(new java.util.Date(created));
            }

            long updated = note.getUpdatedAt();
            long diffMillis = now - updated;

            long minutes = diffMillis / (60 * 1000);
            long hours = diffMillis / (60 * 60 * 1000);
            long days = diffMillis / (24 * 60 * 60 * 1000);

            String modifiedText;

            if (minutes < 1) {
                modifiedText = "ora";
            } else if (minutes < 60) {
                modifiedText = minutes + " min fa";
            } else if (hours < 24) {
                modifiedText = hours + " h fa";
            } else {
                modifiedText = days + " gg fa";
            }

            updatedAt.setText("📅 " + createdText + " • ✏️ " + modifiedText);

            // =========================
            // SELECT
            // =========================
            checkSelect.setVisibility(selectable ? View.VISIBLE : View.GONE);
            checkSelect.setChecked(selectedNotes.contains(note.id));
            checkSelect.setOnCheckedChangeListener(null);
            checkSelect.setChecked(selectedNotes.contains(note.id));
            btnPin.setVisibility(importExportMode ? View.GONE : View.VISIBLE);
            btnTag.setVisibility(importExportMode ? View.GONE : View.VISIBLE);
            btnShare.setVisibility(importExportMode ? View.GONE : View.VISIBLE);
            btnRestore.setVisibility(importExportMode ? View.VISIBLE : View.GONE);
            btnRestore.setEnabled(importExportMode && note.isDeleted());
            btnRestore.setAlpha(note.isDeleted() ? 1f : 0.4f);
            checkSelect.setOnCheckedChangeListener((buttonView, isChecked) -> {

                if (isChecked) {
                    selectedNotes.add(note.id);
                } else {
                    selectedNotes.remove(note.id);
                }
            });
            // =========================
            // CARD CLICK
            // =========================
            card.setOnClickListener(v -> {
                if (!selectable && listener != null) {
                    listener.onNoteClick(note);
                }
            });

            // =========================
            // RESET PIN STATE
            // =========================
            btnPin.clearColorFilter();
            btnPin.setScaleX(1f);
            btnPin.setScaleY(1f);
            btnPin.setAlpha(0.5f);
            card.setStrokeWidth(0);

            // =========================
            // APPLY PIN STATE
            // =========================
            if (pinned) {
                btnPin.setColorFilter(android.graphics.Color.parseColor("#FFC107"));
                btnPin.setScaleX(1.1f);
                btnPin.setScaleY(1.1f);
                btnPin.setAlpha(1f);

                card.setStrokeWidth(4);
                card.setStrokeColor(android.graphics.Color.parseColor("#FF9800"));
            }

            btnPin.setImageResource(
                    pinned
                            ? android.R.drawable.btn_star_big_on
                            : android.R.drawable.btn_star_big_off
            );

            // =========================
            // ACTIONS
            // =========================
            btnShare.setOnClickListener(v -> {
                if (listener != null) listener.onShare(note);
            });

            btnDelete.setOnClickListener(v -> {
                if (listener != null) listener.onDelete(note);
            });

             btnPin.setOnClickListener(v -> {

                if (listener == null) return;

                v.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);

                v.animate()
                        .scaleX(0.85f)
                        .scaleY(0.85f)
                        .setDuration(80)
                        .withEndAction(() -> v.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(120)
                                .start())
                        .start();

                int pos = getBindingAdapterPosition();
                if (pos == RecyclerView.NO_POSITION) return;

                Note currentNote = items.get(pos).note;

                listener.onPin(currentNote);
            });

            // =========================
            // TAG
            // =========================
            btnTag.setOnClickListener(v -> {

                if (listener == null) return;

                android.app.AlertDialog.Builder builder =
                        new android.app.AlertDialog.Builder(v.getContext());

                final android.widget.EditText input =
                        new android.widget.EditText(v.getContext());

                input.setHint("Nuovo tag");

                builder.setTitle("Aggiungi tag");
                builder.setView(input);

                builder.setPositiveButton("OK", (dialog, which) -> {
                    String tag = input.getText().toString().trim();
                    if (!tag.isEmpty()) {
                        if (listener != null) {
                            listener.onAddTag(note, tag);
                        }
                    }
                });

                builder.setNegativeButton("Annulla", null);
                builder.show();
            });

            btnRestore.setOnClickListener(v -> {
                if (listener != null) listener.onRestore(note);
            });

            // =========================
            // PROTECTION
            // =========================
            card.setAlpha(note.isProtected ? 0.7f : 1f);
        }    }
}