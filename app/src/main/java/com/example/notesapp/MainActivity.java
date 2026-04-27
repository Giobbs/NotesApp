package com.example.notesapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notesapp.data.local.Note;
import com.example.notesapp.ui.main.NoteAdapter;
import com.example.notesapp.ui.main.NotesViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

public class MainActivity extends AppCompatActivity {

    private static final long DEBOUNCE_MS = 300;
    private NotesViewModel viewModel;
    private NoteAdapter adapter;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // =========================
        // VIEW
        // =========================
        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        SearchView searchView = findViewById(R.id.searchView);
        searchView.setIconifiedByDefault(false);
        searchView.setSubmitButtonEnabled(false);
        searchView.clearFocus();
        // =========================
        // RECYCLER
        // =========================
        adapter = new NoteAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // =========================
        //         VIEWMODEL
        // =========================
        viewModel = new ViewModelProvider(this).get(NotesViewModel.class);

        viewModel.getNotes().observe(this, notes -> {
            adapter.setNotes(notes);
        });

        // =========================
        //          SEARCH
        // =========================
        searchView.setIconifiedByDefault(false);
        searchView.setQueryHint("Cerca note...");
        searchView.setSubmitButtonEnabled(false);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                viewModel.setSearchQuery(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                setSearchingState(!newText.isEmpty(), recyclerView);

                handler.removeCallbacks(searchRunnable);

                searchRunnable = () -> {
                    viewModel.setSearchQuery(newText.trim());
                };

                handler.postDelayed(searchRunnable, DEBOUNCE_MS);

                return true;
            }
        });

        searchView.setOnCloseListener(() -> {
            viewModel.setSearchQuery("");
            setSearchingState(false, recyclerView);
            return false;
        });

        // =========================
        // FAB ADD NOTE
        // =========================
        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddNoteActivity.class);
            startActivity(intent);
        });

        // =========================
        // ADAPTER ACTIONS
        // =========================
        adapter.setListener(new NoteAdapter.OnNoteActionListener() {

            @Override
            public void onNoteClick(Note note) {
                Intent intent = new Intent(MainActivity.this, EditNoteActivity.class);
                intent.putExtra(EditNoteActivity.EXTRA_NOTE_ID, note.id);
                startActivity(intent);
            }

            @Override
            public void onDelete(Note note) {
                viewModel.delete(note, () -> {
                    Snackbar.make(findViewById(R.id.recyclerView),
                                    "Nota eliminata",
                                    Snackbar.LENGTH_LONG)
                            .setAction("UNDO", v -> viewModel.insert(note, null))
                            .show();
                });
            }
        });
    }

    private void setSearchingState(boolean active, RecyclerView recyclerView) {
        recyclerView.animate()
                .alpha(active ? 0.6f : 1f)
                .setDuration(150)
                .start();
    }
}