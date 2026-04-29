package com.example.notesapp;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.notesapp.data.local.Note;
import com.example.notesapp.security.CryptoManager;
import com.example.notesapp.ui.main.NotesViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class AddNoteActivity extends AppCompatActivity {

    private EditText etTitle, etContent;
    private Switch switchProtect;
    private FloatingActionButton btnSave;

    private NotesViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);

        // UI
        etTitle = findViewById(R.id.etTitle);
        etContent = findViewById(R.id.etContent);
        switchProtect = findViewById(R.id.switchProtect);
        btnSave = findViewById(R.id.btnSave);

        // ViewModel
        viewModel = new ViewModelProvider(this).get(NotesViewModel.class);

        // Save
        btnSave.setOnClickListener(v -> saveNote());
    }

    private void saveNote() {

        String title = etTitle.getText().toString().trim();
        String content = etContent.getText().toString().trim();
        boolean isProtected = switchProtect.isChecked();

        if (title.isEmpty() && content.isEmpty()) {
            Toast.makeText(this, "Inserisci qualcosa", Toast.LENGTH_SHORT).show();
            return;
        }

        Note note = new Note();
        note.setTitle(title);
        note.setProtected(isProtected);

        if (isProtected) {
            note.encryptedContent = CryptoManager.encrypt(content);
            note.setContent("");
        } else {
            note.setContent(content);
            note.encryptedContent = null;
        }

        viewModel.insert(note, () -> runOnUiThread(() -> {
            Toast.makeText(this, "Nota salvata", Toast.LENGTH_SHORT).show();
            finish();
        }));
    }
}