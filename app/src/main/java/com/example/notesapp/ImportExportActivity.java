package com.example.notesapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notesapp.data.local.AppDatabase;
import com.example.notesapp.data.local.Note;
import com.example.notesapp.data.local.NoteDao;
import com.example.notesapp.ui.main.NoteAdapter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ImportExportActivity extends AppCompatActivity {

    private Button btnExport;
    private Button btnImport;
    private Button btnSelectAll;
    private TextView txtResult;

    private RecyclerView recyclerNotes;

    private NoteDao noteDao;
    private NoteAdapter adapter;

    private List<Note> notes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences(SettingsActivity.PREFS_NAME, MODE_PRIVATE);
        SettingsActivity.applyTheme(prefs);

        setContentView(R.layout.activity_import_export);

        btnExport = findViewById(R.id.btnExport);
        btnImport = findViewById(R.id.btnImport);
        btnSelectAll = findViewById(R.id.btnSelectAll);
        txtResult = findViewById(R.id.txtResult);
        recyclerNotes = findViewById(R.id.recyclerNotes);

        noteDao = AppDatabase.getInstance(this).noteDao();

        adapter = new NoteAdapter();

        adapter.setMode(NoteAdapter.Mode.IMPORT_EXPORT);
        adapter.setListener(new NoteAdapter.OnNoteActionListener() {
            @Override
            public void onNoteClick(Note note) {
            }

            @Override
            public void onDelete(Note note) {
                if (note == null) return;

                new Thread(() -> {
                    noteDao.deleteById(note.id);
                    runOnUiThread(() -> {
                        Toast.makeText(
                                ImportExportActivity.this,
                                "Nota eliminata definitivamente",
                                Toast.LENGTH_SHORT
                        ).show();
                        loadNotesWithSettings();
                    });
                }).start();
            }

            @Override
            public void onPin(Note note) {
            }

            @Override
            public void onShare(Note note) {
            }

            @Override
            public void onAddTag(Note note, String tag) {
            }

             public void onRestore(Note note) {
                if (note == null || !note.isDeleted()) return;

                new Thread(() -> {
                    noteDao.restore(note.id, System.currentTimeMillis());
                    runOnUiThread(() -> {
                        Toast.makeText(
                                ImportExportActivity.this,
                                "Nota ripristinata",
                                Toast.LENGTH_SHORT
                        ).show();
                        loadNotesWithSettings();
                    });
                }).start();
            }
        });

        recyclerNotes.setLayoutManager(new LinearLayoutManager(this));
        recyclerNotes.setAdapter(adapter);

        loadNotesWithSettings();

        btnSelectAll.setOnClickListener(v -> {

            Set<Long> selected = adapter.getSelectedNotes();
            if (notes == null) return;

            if (selected.size() == notes.size()) {
                selected.clear();
            } else {
                for (Note n : notes) {
                    selected.add(n.id);
                }
            }

            adapter.notifyDataSetChanged();
        });

        btnExport.setOnClickListener(v -> exportSelected());
        btnImport.setOnClickListener(v -> importJson());
    }

    // =========================
    // LOAD
    // =========================
    private void loadNotesWithSettings() {

        new Thread(() -> {

            notes = noteDao.getAllNotesSync();

            SharedPreferences prefs = getSharedPreferences(SettingsActivity.PREFS_NAME, MODE_PRIVATE);
            String aggregation = prefs.getString(SettingsActivity.KEY_AGGREGATION, "none");

            runOnUiThread(() -> adapter.setNotes(notes, aggregation));

        }).start();
    }

    // =========================
    // EXPORT
    // =========================
    private void exportSelected() {

        try {
            Set<Long> selected = adapter.getSelectedNotes();

            JSONArray array = new JSONArray();

            for (Note n : notes) {
                if (!selected.contains(n.id)) continue;

                JSONObject obj = new JSONObject();
                obj.put("id", n.id);
                obj.put("uuid", n.uuid);
                obj.put("title", n.title);
                obj.put("content", n.content);
                obj.put("updatedAt", n.updatedAt);
                obj.put("tags", n.getTags());

                array.put(obj);
            }

            txtResult.setText(array.toString(2));

            Toast.makeText(this,
                    "Export: " + array.length() + " note",
                    Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            txtResult.setText("Errore export: " + e.getMessage());
        }
    }

    // =========================
    // IMPORT
    // =========================
    private void importJson() {

        EditText input = new EditText(this);
        input.setHint("Incolla JSON qui");

        new AlertDialog.Builder(this)
                .setTitle("Import JSON")
                .setMessage("Incolla il JSON delle note")
                .setView(input)
                .setPositiveButton("Importa", (dialog, which) -> {

                    String json = input.getText().toString();

                    if (json.trim().isEmpty()) {
                        toast("JSON vuoto");
                        return;
                    }

                    try {
                        JSONArray arr = new JSONArray(json);

                        List<Note> list = new ArrayList<>();

                        for (int i = 0; i < arr.length(); i++) {

                            JSONObject o = arr.getJSONObject(i);

                            Note n = new Note();
                            n.uuid = o.optString("uuid");
                            n.title = o.optString("title");
                            n.content = o.optString("content");
                            n.updatedAt = System.currentTimeMillis();
                            n.setTags(o.optString("tags"));

                            list.add(n);
                        }

                        new Thread(() -> {
                            noteDao.insertAll(list);

                            runOnUiThread(() -> {
                                toast("Import completato: " + list.size() + " note");
                                loadNotesWithSettings();
                            });

                        }).start();

                    } catch (Exception e) {
                        toast("JSON non valido");
                    }
                })
                .setNegativeButton("Annulla", null)
                .show();
    }

    private void hardDelete() {

        Set<Long> sel = adapter.getSelectedNotes();

        if (sel.isEmpty()) {
            toast("Nessuna selezione");
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Delete permanent?")
                .setMessage("Irreversibile")
                .setPositiveButton("Delete", (d, w) -> {

                    new Thread(() -> {
                        for (Note n : notes) {
                            if (sel.contains(n.id)) {
                                noteDao.deleteById(n.id);
                            }
                        }

                        runOnUiThread(() -> {
                            sel.clear();
                            loadNotesWithSettings();
                            toast("Deleted");
                        });
                    }).start();

                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void toast(String m) {
        Toast.makeText(this, m, Toast.LENGTH_SHORT).show();
    }
}