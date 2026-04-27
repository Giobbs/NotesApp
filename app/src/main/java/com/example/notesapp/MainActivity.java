package com.example.notesapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notesapp.data.local.Note;
import com.example.notesapp.data.local.SortType;
import com.example.notesapp.ui.main.NoteAdapter;
import com.example.notesapp.ui.main.NotesViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    private NotesViewModel viewModel;
    private NoteAdapter adapter;

    private SearchView searchView;
    private Button btnSort;
    private Button btnPinned;
    private Button btnTagFilter;
    private Button btnImportExport;
    private FloatingActionButton fabAdd;
    private ImageButton btnSettings;

    private boolean pinnedActive = false;

    private enum SortState {
        DATE_DESC,
        DATE_ASC,
        TITLE
    }

    private SortState sortState = SortState.DATE_DESC;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences prefs = getSharedPreferences("app_settings", MODE_PRIVATE);
        SettingsActivity.applyTheme(prefs);
        setContentView(R.layout.activity_main);

        initViews();
        setupRecycler();
        setupViewModel();
        setupAdapter();
        setupActions();
    }

    private void initViews() {
        searchView = findViewById(R.id.searchView);
        btnSort = findViewById(R.id.btnSort);
        btnPinned = findViewById(R.id.btnPinned);
        btnTagFilter = findViewById(R.id.btnTagFilter);
        btnImportExport = findViewById(R.id.btnImportExport);
        fabAdd = findViewById(R.id.fabAdd);
        btnSettings = findViewById(R.id.btnSettings);
    }

    private void setupRecycler() {
        RecyclerView recyclerView = findViewById(R.id.recyclerView);

        adapter = new NoteAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(NotesViewModel.class);
        viewModel.getNotes().observe(this, adapter::setNotes);

         viewModel.setShowPinnedOnly(false);
        btnPinned.setText("Pinned OFF");
    }

    private void setupAdapter() {

        adapter.setListener(new NoteAdapter.OnNoteActionListener() {

            @Override
            public void onNoteClick(Note note) {
                Intent intent = new Intent(MainActivity.this, EditNoteActivity.class);
                intent.putExtra(EditNoteActivity.EXTRA_NOTE_ID, note.id);
                startActivity(intent);
            }

            @Override
            public void onDelete(Note note) {
                viewModel.delete(note, null);
            }

            @Override
            public void onPin(Note note) {
                viewModel.setPinned(note.id, !note.isPinned());
            }

            @Override
            public void onShare(Note note) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT,
                        note.title + "\n\n" + note.content);
                startActivity(Intent.createChooser(shareIntent, "Condividi nota"));
            }

            @Override
            public void onAddTag(Note note, String tag) {
                viewModel.updateTags(note.id, tag);
            }
        });
    }

    private void setupActions() {

        btnImportExport.setOnClickListener(v ->
                startActivity(new Intent(this, ImportExportActivity.class))
        );

        searchView.setIconifiedByDefault(false);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                viewModel.setSearchQuery(clean(query));
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                viewModel.setSearchQuery(clean(newText));
                return true;
            }
        });

        btnSort.setOnClickListener(v -> {

            switch (sortState) {

                case DATE_DESC:
                    sortState = SortState.DATE_ASC;
                    viewModel.setSortType(SortType.DATE_ASC);
                    btnSort.setText("Data ↑");
                    break;

                case DATE_ASC:
                    sortState = SortState.TITLE;
                    viewModel.setSortType(SortType.TITLE_ASC);
                    btnSort.setText("Titolo");
                    break;

                default:
                    sortState = SortState.DATE_DESC;
                    viewModel.setSortType(SortType.DATE_DESC);
                    btnSort.setText("Data ↓");
                    break;
            }
        });

        btnPinned.setOnClickListener(v -> {

            pinnedActive = !pinnedActive;

            viewModel.setShowPinnedOnly(pinnedActive);

            btnPinned.setText(
                    pinnedActive ? "Pinned ON ⭐" : "Pinned OFF"
            );
        });

        // =========================
        // 🔥 TAG FILTER FIX DEFINITIVO
        // =========================
        btnTagFilter.setOnClickListener(v -> {

            android.app.AlertDialog.Builder builder =
                    new android.app.AlertDialog.Builder(this);

            builder.setTitle("Filtra per tag");

            final android.widget.EditText input =
                    new android.widget.EditText(this);

            input.setHint("es: android, java, work");

            builder.setView(input);

            builder.setPositiveButton("Filtra", (dialog, which) -> {

                String tag = clean(input.getText().toString());

                if (tag.isEmpty()) {
                    viewModel.setTagFilter(null); // 🔥 SOLO NULL = OFF
                    btnTagFilter.setText("Tag OFF");
                } else {
                    viewModel.setTagFilter(tag);
                    btnTagFilter.setText("Tag: " + tag);
                }
            });

            builder.setNegativeButton("Reset", (dialog, which) -> {
                viewModel.setTagFilter(null); // 🔥 FIX
                btnTagFilter.setText("Tags OFF");
            });

            builder.show();
        });

        fabAdd.setOnClickListener(v ->
                startActivity(new Intent(this, AddNoteActivity.class))
        );

        btnSettings.setOnClickListener(v ->
                startActivity(new Intent(this, SettingsActivity.class))
        );
    }

    private String clean(String s) {
        return s == null ? "" : s.trim();
    }
}