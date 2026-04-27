package com.example.notesapp.ui.edit;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.notesapp.data.local.AppDatabase;
import com.example.notesapp.data.local.Note;
import com.example.notesapp.data.repository.NoteRepository;


public class EditNoteViewModel extends AndroidViewModel {

    private final NoteRepository repository;

    public EditNoteViewModel(@NonNull Application application) {
        super(application);

        repository = new NoteRepository(
                AppDatabase.getInstance(application).noteDao()
        );
    }

    // ===== LOAD NOTE =====
    public LiveData<Note> getNote(long id) {
        return repository.observeById(id);
    }

    // ===== SAVE =====
    public void save(Note note, Runnable onDone) {
        if (note.id == 0) {
            repository.insert(note, onDone);
        } else {
            repository.update(note, onDone);
        }
    }

    // ===== DELETE =====
    public void delete(Note note, Runnable onDone) {
        repository.delete(note, onDone);
    }
}