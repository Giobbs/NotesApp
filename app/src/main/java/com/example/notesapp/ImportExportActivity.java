package com.example.notesapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ImportExportActivity extends AppCompatActivity {

    private Button btnExport;
    private Button btnImport;
    private Button btnSelectAll;
    private TextView txtResult;
    private Button btnDeleteSelected;
    private RecyclerView recyclerNotes;

    private NoteDao noteDao;
    private NoteAdapter adapter;

    private List<Note> notes = new ArrayList<>();
    private String pendingExportJson = "";

    private final ActivityResultLauncher<String> saveExportLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.CreateDocument("application/json"),
                    this::saveExportToUri
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences(SettingsActivity.PREFS_NAME, MODE_PRIVATE);
        SettingsActivity.applyTheme(prefs);

        setContentView(R.layout.activity_import_export);

        btnExport = findViewById(R.id.btnExport);
        btnImport = findViewById(R.id.btnImport);
        btnSelectAll = findViewById(R.id.btnSelectAll);
//        txtResult = findViewById(R.id.txtResult);
        recyclerNotes = findViewById(R.id.recyclerNotes);
        btnDeleteSelected = findViewById(R.id.btnDeleteSelected);
        noteDao = AppDatabase.getInstance(this).noteDao();

        adapter = new NoteAdapter();
        adapter.setMode(NoteAdapter.Mode.IMPORT_EXPORT);

        adapter.setListener(new NoteAdapter.OnNoteActionListener() {

            @Override
            public void onNoteClick(Note note) {}

            @Override
            public void onDelete(Note note) {
                if (note == null) return;

                new android.app.AlertDialog.Builder(ImportExportActivity.this)
                        .setTitle("Eliminazione definitiva")
                        .setMessage("Sei sicuro di voler eliminare definitivamente questa nota?")
                        .setPositiveButton("Elimina", (dialog, which) -> {

                            new Thread(() -> {
                                noteDao.deleteById(note.id);

                                runOnUiThread(() -> {
                                    toast("Nota eliminata definitivamente");
                                    loadNotesWithSettings();
                                });

                            }).start();

                        })
                        .setNegativeButton("Annulla", null)
                        .show();
            }

            @Override
            public void onPin(Note note) {}

            @Override
            public void onShare(Note note) {}

            @Override
            public void onAddTag(Note note, String tag) {}

            @Override
            public void onRestore(Note note) {
                if (note == null || !note.isDeleted()) return;

                new Thread(() -> {
                    noteDao.restore(note.id, System.currentTimeMillis());
                    runOnUiThread(() -> {
                        toast("Nota ripristinata");
                        loadNotesWithSettings();
                    });
                }).start();
            }
        });

        recyclerNotes.setLayoutManager(new LinearLayoutManager(this));
        recyclerNotes.setAdapter(adapter);

        loadNotesWithSettings();

        btnSelectAll.setOnClickListener(v -> toggleSelectAll());
        btnExport.setOnClickListener(v -> exportSelected());
        btnImport.setOnClickListener(v -> importJson());
        btnDeleteSelected.setOnClickListener(v -> deleteSelected());
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
    // SELECT ALL
    // =========================
    private void toggleSelectAll() {
        if (notes == null || notes.isEmpty()) return;

        Set<Long> selected = adapter.getSelectedNotes();

        if (selected.size() == notes.size()) {
            selected.clear();
        } else {
            selected.clear();
            for (Note n : notes) {
                selected.add(n.id);
            }
        }

        adapter.notifyDataSetChanged();
    }

    // =========================
    // EXPORT
    // =========================
    private void exportSelected() {

        try {
            Set<Long> selected = adapter.getSelectedNotes();

            if (selected.isEmpty()) {
                toast("Seleziona almeno una nota");
                return;
            }

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
                obj.put("createdAt", n.createdAt);
                array.put(obj);
            }

            String json = array.toString(2);
            pendingExportJson = json;
            txtResult.setText(json);

            toast("Export: " + array.length() + " note");

            showExportOptionsDialog();

        } catch (Exception e) {
            txtResult.setText("Errore export: " + e.getMessage());
        }
    }

    private void showExportOptionsDialog() {

        String[] options = {
                "Salva sul dispositivo (.json)",
                "Condividi"
        };

        new AlertDialog.Builder(this)
                .setTitle("Esporta note")
                .setItems(options, (dialog, which) -> {

                    if (which == 0) {
                        String filename = String.format(
                                Locale.getDefault(),
                                "notes_export_%d.json",
                                System.currentTimeMillis()
                        );
                        saveExportLauncher.launch(filename);

                    } else if (which == 1) {
                        shareExportJson();
                    }
                })
                .setNegativeButton("Annulla", null)
                .show();
    }

    private void shareExportJson() {
        if (pendingExportJson == null || pendingExportJson.trim().isEmpty()) {
            toast("Nessun export disponibile");
            return;
        }

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("application/json");
        intent.putExtra(Intent.EXTRA_SUBJECT, "Export Note");
        intent.putExtra(Intent.EXTRA_TEXT, pendingExportJson);

        startActivity(Intent.createChooser(intent, "Condividi export"));
    }

    // =========================
    // SAVE FILE
    // =========================
    private void saveExportToUri(Uri uri) {
        if (uri == null) return;

        if (pendingExportJson == null || pendingExportJson.trim().isEmpty()) {
            toast("Nessun export disponibile");
            return;
        }

        new Thread(() -> {
            try (OutputStream os = getContentResolver().openOutputStream(uri)) {

                if (os == null) throw new IllegalStateException("OutputStream nullo");

                os.write(pendingExportJson.getBytes(StandardCharsets.UTF_8));
                os.flush();

                runOnUiThread(() -> toast("File salvato"));

            } catch (Exception e) {
                runOnUiThread(() ->
                        toast("Errore salvataggio: " + e.getMessage())
                );
            }
        }).start();
    }

    // =========================
    // IMPORT
    // =========================
    private void importJson() {

        EditText input = new EditText(this);
        input.setHint("Incolla JSON qui");

        new AlertDialog.Builder(this)
                .setTitle("Import JSON")
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
                            long now = System.currentTimeMillis();

                            Note n = new Note();
                            n.uuid = o.optString("uuid");
                            n.title = o.optString("title");
                            n.content = o.optString("content");
                            n.createdAt = o.optLong("createdAt", now);
                            n.updatedAt = now;
                            n.setTags(o.optString("tags"));

                            list.add(n);
                        }

                        new Thread(() -> {
                            noteDao.insertAll(list);

                            runOnUiThread(() -> {
                                toast("Import completato: " + list.size());
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
    private void deleteSelected() {

        Set<Long> selected = adapter.getSelectedNotes();

        if (selected == null || selected.isEmpty()) {
            toast("Nessuna nota selezionata");
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Eliminazione multipla")
                .setMessage("Vuoi eliminare " + selected.size() + " note?")
                .setPositiveButton("Elimina", (dialog, which) -> {

                    new Thread(() -> {

                        for (Long id : selected) {
                            noteDao.deleteById(id);
                        }

                        runOnUiThread(() -> {
                            toast("Note eliminate: " + selected.size());
                            selected.clear();
                            adapter.getSelectedNotes().clear();
                            adapter.notifyDataSetChanged();
                            loadNotesWithSettings();
                        });

                    }).start();

                })
                .setNegativeButton("Annulla", null)
                .show();
    }
    // =========================
    // UTIL
    // =========================
    private void toast(String m) {
        Toast.makeText(this, m, Toast.LENGTH_SHORT).show();
    }
}