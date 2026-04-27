package com.example.notesapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
        setContentView(R.layout.activity_main);

        // =========================
        // INIT VIEW (SAFE ORDER)
        // =========================
        searchView = findViewById(R.id.searchView);
        btnSort = findViewById(R.id.btnSort);
        btnPinned = findViewById(R.id.btnPinned);
        btnTagFilter = findViewById(R.id.btnTagFilter);
        btnImportExport = findViewById(R.id.btnImportExport);
        fabAdd = findViewById(R.id.fabAdd);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);

        adapter = new NoteAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(NotesViewModel.class);

        // =========================
        // IMPORT / EXPORT (FIXED)
        // =========================
        btnImportExport.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ImportExportActivity.class);
            startActivity(intent);
        });

        // =========================
        // OBSERVE
        // =========================
        viewModel.getNotes().observe(this, adapter::setNotes);

        // =========================
        // SEARCH
        // =========================
        searchView.setIconifiedByDefault(false);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                viewModel.setSearchQuery(clean(query));
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                String q = clean(newText);
                if (q.length() < 2 && !q.isEmpty()) return true;

                viewModel.setSearchQuery(q);
                return true;
            }
        });

        // =========================
        // SORT
        // =========================
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

        // =========================
        // PIN FILTER
        // =========================
        btnPinned.setOnClickListener(v -> {

            pinnedActive = !pinnedActive;
            viewModel.setShowPinnedOnly(pinnedActive);

            btnPinned.setText(pinnedActive ? "Pinned ON ⭐" : "Pinned OFF");
        });

        // =========================
        // TAG FILTER
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

                String tag = input.getText().toString().trim();

                if (tag.isEmpty()) {
                    viewModel.setTagFilter(null);
                    btnTagFilter.setText("Tag OFF");
                } else {
                    viewModel.setTagFilter(tag);
                    btnTagFilter.setText("Tag: " + tag);
                }
            });

            builder.setNegativeButton("Reset", (dialog, which) -> {
                viewModel.setTagFilter(null);
                btnTagFilter.setText("Tags OFF");
            });

            builder.show();
        });

        // =========================
        // ADD NOTE
        // =========================
        fabAdd.setOnClickListener(v ->
                startActivity(new Intent(this, AddNoteActivity.class))
        );
    }

    private String clean(String s) {
        return s == null ? "" : s.trim();
    }
}