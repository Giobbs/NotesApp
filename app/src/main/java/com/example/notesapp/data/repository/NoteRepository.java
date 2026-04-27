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

    // =========================
    // 📌 READ
    // =========================

    public LiveData<List<Note>> observeAll(boolean desc) {
        return desc ? noteDao.observeAllDesc() : noteDao.observeAllAsc();
    }

    public LiveData<List<Note>> search(String query, boolean desc) {
        if (query == null || query.trim().isEmpty()) {
            return observeAll(desc);
        }

        String q = "%" + query + "%";
        return desc ? noteDao.searchDesc(q) : noteDao.searchAsc(q);
    }

    public LiveData<Note> observeById(long id) {
        return noteDao.observeById(id);
    }

    // =========================
    // 📥 WRITE
    // =========================

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

    // =========================
    // 📌 PIN / ARCHIVE
    // =========================

    public LiveData<List<Note>> observePinned() {
        return noteDao.observePinned();
    }

    public LiveData<List<Note>> observeArchived() {
        return noteDao.observeArchived();
    }

    public LiveData<List<Note>> observeRecent(int limit) {
        return noteDao.observeRecent(limit);
    }

    // =========================
    // 🔁 STATE CHANGES
    // =========================

    public void setPinned(long id, boolean pinned, Runnable onComplete) {
        executor.execute(() -> {
            noteDao.setPinned(id, pinned, System.currentTimeMillis());

            if (onComplete != null) onComplete.run();
        });
    }

    public void setArchived(long id, boolean archived, Runnable onComplete) {
        executor.execute(() -> {
            noteDao.setArchived(id, archived, System.currentTimeMillis());

            if (onComplete != null) onComplete.run();
        });
    }

    public void restore(long id, Runnable onComplete) {
        executor.execute(() -> {
            noteDao.restore(id, System.currentTimeMillis());

            if (onComplete != null) onComplete.run();
        });
    }

    // =========================
    // 🗑 HARD DELETE (optional UI/admin)
    // =========================

    public void deleteById(long id, Runnable onComplete) {
        executor.execute(() -> {
            noteDao.deleteById(id);

            if (onComplete != null) onComplete.run();
        });
    }

    // =========================
    // 🔄 COMPAT VIEWMODEL
    // =========================

    public LiveData<List<Note>> observeAllOrdered(boolean desc) {
        return observeAll(desc);
    }

    public LiveData<List<Note>> searchOrdered(String query, boolean desc) {
        return search(query, desc);
    }
}