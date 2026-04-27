package com.example.notesapp.data.repository;


import androidx.lifecycle.LiveData;


import com.example.notesapp.data.local.Note;
import com.example.notesapp.data.local.NoteDao;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NoteRepository {

    private final NoteDao noteDao;
    private final ExecutorService executor;

    public NoteRepository(NoteDao noteDao) {
        this.noteDao = noteDao;
        this.executor = Executors.newSingleThreadExecutor();
    }

    // ===== READ OPERATIONS (LiveData già thread-safe) =====

    public LiveData<List<Note>> observeAll() {
        return noteDao.observeAll();
    }

    public LiveData<Note> observeById(long id) {
        return noteDao.observeById(id);
    }

    public LiveData<List<Note>> search(String query) {
        return noteDao.search("%" + query + "%");
    }

    // ===== WRITE OPERATIONS (sempre async) =====

    public void insert(Note note, Runnable onComplete) {
        executor.execute(() -> {
            long now = System.currentTimeMillis();

            if (note.id == 0) {
                note.createdAt = now;
            }

            note.updatedAt = now;

            long id = noteDao.insert(note);
            note.id = id;

            if (onComplete != null) onComplete.run();
        });
    }

    public void update(Note note, Runnable onComplete) {
        executor.execute(() -> {
            note.updatedAt = System.currentTimeMillis();
            noteDao.update(note);

            if (onComplete != null) onComplete.run();
        });
    }

    public void delete(Note note, Runnable onComplete) {
        executor.execute(() -> {
            noteDao.softDelete(note.id, System.currentTimeMillis());

            if (onComplete != null) onComplete.run();
        });
    }


}