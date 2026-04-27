package com.example.notesapp;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.notesapp.data.local.Note;
import com.example.notesapp.ui.main.NotesViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class EditNoteActivity extends AppCompatActivity {

    public static final String EXTRA_NOTE_ID = "note_id";

    private NotesViewModel viewModel;

    private EditText etTitle;
    private EditText etContent;
    private FloatingActionButton btnSave;

    private Note currentNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_note);

        // =========================
        // VIEWMODEL
        // =========================
        viewModel = new ViewModelProvider(this).get(NotesViewModel.class);

        // =========================
        // UI
        // =========================
        etTitle = findViewById(R.id.etTitle);
        etContent = findViewById(R.id.etContent);
        btnSave = findViewById(R.id.btnSave);

        // =========================
        // NOTE ID SAFE READ
        // =========================
        long noteId = getIntent().getLongExtra(EXTRA_NOTE_ID, -1L);

        if (noteId <= 0) {
            Toast.makeText(this, "ID nota non valido", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // =========================
        // LOAD NOTE (SAFE)
        // =========================
        viewModel.getNoteById(noteId).observe(this, note -> {

            if (note == null) {
                Toast.makeText(this, "Nota non trovata", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            currentNote = note;

            // Evita null crash silenziosi
            etTitle.setText(note.title != null ? note.title : "");
            etContent.setText(note.content != null ? note.content : "");
        });

        // =========================
        // SAVE
        // =========================
        btnSave.setOnClickListener(v -> saveNote());
    }

    private void saveNote() {

        if (currentNote == null) {
            Toast.makeText(this, "Errore caricamento nota", Toast.LENGTH_SHORT).show();
            return;
        }

        String title = etTitle.getText() != null
                ? etTitle.getText().toString().trim()
                : "";

        String content = etContent.getText() != null
                ? etContent.getText().toString().trim()
                : "";

        if (title.isEmpty()) {
            etTitle.setError("Inserisci un titolo");
            return;
        }

        // 🔐 FIX CRITICO: LEGGI SWITCH
        android.widget.Switch sw = findViewById(R.id.switchProtect);
        currentNote.isProtected = sw.isChecked();

        currentNote.title = title;

        if (currentNote.isProtected) {
            currentNote.encryptedContent = content;
            currentNote.content = ""; // evita leak
        } else {
            currentNote.content = content;
            currentNote.encryptedContent = null;
        }

        viewModel.update(currentNote);

        Toast.makeText(this, "Nota aggiornata", Toast.LENGTH_SHORT).show();
        finish();
    }}