package com.example.notesapp;

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

import java.util.List;
import java.util.Set;

public class ImportExportActivity extends AppCompatActivity {

    private Button btnExport;
    private Button btnSelectAll;
    private TextView txtResult;

    private RecyclerView recyclerNotes;

    private NoteDao noteDao;
    private NoteAdapter adapter;

    private List<Note> notes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_export);

        // =========================
        // VIEW
        // =========================
        btnExport = findViewById(R.id.btnExport);
        btnSelectAll = findViewById(R.id.btnSelectAll);
        txtResult = findViewById(R.id.txtResult);
        recyclerNotes = findViewById(R.id.recyclerNotes);

        noteDao = AppDatabase.getInstance(this).noteDao();

        // =========================
        // ADAPTER SETUP
        // =========================
        adapter = new NoteAdapter();
        adapter.setMode(NoteAdapter.Mode.SELECTABLE);

        recyclerNotes.setLayoutManager(new LinearLayoutManager(this));
        recyclerNotes.setAdapter(adapter);

        // =========================
        // LOAD NOTES
        // =========================
        new Thread(() -> {
            notes = noteDao.getAllNotesSync();

            runOnUiThread(() -> adapter.setNotes(notes));
        }).start();

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
        // EXPORT SELECTED
        // =========================
        btnExport.setOnClickListener(v -> {

            try {
                Set<Long> selected = adapter.getSelectedNotes();

                JSONArray array = new JSONArray();

                for (Note n : notes) {

                    if (!selected.contains(n.id)) continue;

                    NoteExportDto dto = new NoteExportDto();
                    dto.id = n.id;
                    dto.uuid = n.uuid;
                    dto.title = n.title;
                    dto.content = n.content;
                    dto.updatedAt = n.updatedAt;
                    dto.tags = n.getTags();

                    JSONObject obj = new JSONObject();
                    obj.put("id", dto.id);
                    obj.put("uuid", dto.uuid);
                    obj.put("title", dto.title);
                    obj.put("content", dto.content);
                    obj.put("updatedAt", dto.updatedAt);
                    obj.put("tags", dto.tags);

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
        });
    }
}