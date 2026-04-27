package com.example.notesapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notesapp.ui.main.NoteAdapter;
import com.example.notesapp.ui.main.NotesViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

public class MainActivity extends AppCompatActivity {

    private NotesViewModel viewModel;
    private NoteAdapter adapter;

    private FloatingActionButton fabAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // UI
        fabAdd = findViewById(R.id.fabAdd);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);

        // Recycler setup
        adapter = new NoteAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // ViewModel
        viewModel = new ViewModelProvider(this).get(NotesViewModel.class);

        viewModel.notes().observe(this, notes -> {
            adapter.setNotes(notes);
        });

        // FAB click
        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddNoteActivity.class);
            startActivity(intent);
        });

        // delete + undo
        adapter.setListener(note -> {
            viewModel.delete(note, null);

            Snackbar.make(recyclerView, "Nota eliminata", Snackbar.LENGTH_LONG)
                    .setAction("UNDO", v -> viewModel.insert(note, null))
                    .show();
        });
    }
}