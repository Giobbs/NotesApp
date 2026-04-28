package com.example.notesapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notesapp.data.local.AppDatabase;
import com.example.notesapp.data.local.Note;
import com.example.notesapp.data.local.NoteDao;
import com.example.notesapp.dto.NoteExportDto;
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

    private List<Note> notes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences(SettingsActivity.PREFS_NAME, MODE_PRIVATE);
        SettingsActivity.applyTheme(prefs);

        setContentView(R.layout.activity_import_export);

        // =========================
        // VIEW
        // =========================
        btnExport = findViewById(R.id.btnExport);
        btnImport = findViewById(R.id.btnImport); // AGGIUNGILO XML
        btnSelectAll = findViewById(R.id.btnSelectAll);
        txtResult = findViewById(R.id.txtResult);
        recyclerNotes = findViewById(R.id.recyclerNotes);

        noteDao = AppDatabase.getInstance(this).noteDao();

        // =========================
        // ADAPTER
        // =========================
        adapter = new NoteAdapter();
        adapter.setMode(NoteAdapter.Mode.SELECTABLE);

        recyclerNotes.setLayoutManager(new LinearLayoutManager(this));
        recyclerNotes.setAdapter(adapter);

        // =========================
        // LOAD + AGGREGAZIONE
        // =========================
        loadNotesWithSettings();

        // =========================
        // SELECT ALL
        // =========================
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

        // =========================
        // EXPORT
        // =========================
        btnExport.setOnClickListener(v -> exportSelected());

        // =========================
        // IMPORT
        // =========================
        btnImport.setOnClickListener(v -> importJson());
    }

    // =========================
    // LOAD NOTES + SETTINGS
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

            String json = array.toString(2);
            txtResult.setText(json);

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

        try {
            String input = txtResult.getText().toString();

            if (input.isEmpty()) {
                Toast.makeText(this, "Incolla prima un JSON", Toast.LENGTH_SHORT).show();
                return;
            }

            JSONArray array = new JSONArray(input);

            List<Note> toInsert = new ArrayList<>();

            for (int i = 0; i < array.length(); i++) {

                JSONObject obj = array.getJSONObject(i);

                Note n = new Note();

                n.uuid = obj.optString("uuid");
                n.title = obj.optString("title");
                n.content = obj.optString("content");
                n.updatedAt = obj.optLong("updatedAt", System.currentTimeMillis());
                n.setTags(obj.optString("tags"));

                toInsert.add(n);
            }

            new Thread(() -> {
                noteDao.insertAll(toInsert);

                runOnUiThread(() -> {
                    Toast.makeText(this,
                            "Importate: " + toInsert.size() + " note",
                            Toast.LENGTH_SHORT).show();

                    txtResult.setText("");
                    loadNotesWithSettings();
                });

            }).start();

        } catch (Exception e) {
            txtResult.setText("Errore import: " + e.getMessage());
        }
    }
}