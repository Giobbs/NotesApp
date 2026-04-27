package com.example.notesapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notesapp.data.local.Note;
import com.example.notesapp.ui.main.NoteAdapter;
import com.example.notesapp.ui.main.NotesViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

public class MainActivity extends AppCompatActivity {

    private NotesViewModel viewModel;
    private NoteAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);
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

        viewModel.notes().observe(this, notes -> {
            adapter.setNotes(notes);
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
                android.util.Log.d("DEBUG_CLICK", "CLICK NOTE ID = " + note.id);

                Intent intent = new Intent(MainActivity.this, EditNoteActivity.class);
                intent.putExtra(EditNoteActivity.EXTRA_NOTE_ID, note.id);

                android.util.Log.d("DEBUG_CLICK", "START ACTIVITY");
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


}