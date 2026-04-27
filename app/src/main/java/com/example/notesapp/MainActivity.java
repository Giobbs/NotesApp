package com.example.notesapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notesapp.data.local.Note; // 🔥 MANCAVA
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
        // VIEW INIT
        // =========================
        searchView = findViewById(R.id.searchView);
        btnSort = findViewById(R.id.btnSort);
        btnPinned = findViewById(R.id.btnPinned);
        fabAdd = findViewById(R.id.fabAdd);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);

        // =========================
        // RECYCLER
        // =========================
        adapter = new NoteAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // =========================
        // VIEWMODEL
        // =========================
        viewModel = new ViewModelProvider(this).get(NotesViewModel.class);

        // LISTENER NOTE
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

                boolean newPinned = false;

                if (note != null) {
                    newPinned = !note.isPinned(); // oppure note.pinned
                }

                viewModel.setPinned(note.id, newPinned);
            }
        });

        // OBSERVE DATA
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
                viewModel.setSearchQuery(clean(newText));
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

                case TITLE:
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