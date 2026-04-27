package com.example.notesapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.notesapp.data.local.Note;
import com.example.notesapp.ui.main.NotesViewModel;

public class AddNoteActivity extends AppCompatActivity {

    private EditText etTitle, etContent;
    private Button btnSave;

    private NotesViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);

        // UI
        etTitle = findViewById(R.id.etTitle);
        etContent = findViewById(R.id.etContent);
        btnSave = findViewById(R.id.btnSave);

        // ViewModel
        viewModel = new ViewModelProvider(this).get(NotesViewModel.class);

        // click salva
        btnSave.setOnClickListener(v -> saveNote());
    }

    private void saveNote() {
        String title = etTitle.getText().toString().trim();
        String content = etContent.getText().toString().trim();

        if (title.isEmpty() && content.isEmpty()) {
            Toast.makeText(this, "Inserisci qualcosa", Toast.LENGTH_SHORT).show();
            return;
        }

        Note note = new Note();
        note.setTitle(title);
        note.setProtected(false);
        note.setContent(content);
        // salva su DB
        viewModel.insert(note, () -> runOnUiThread(() -> {
            Toast.makeText(this, "Nota salvata", Toast.LENGTH_SHORT).show();
            finish(); // torna alla lista
        }));
    }
}