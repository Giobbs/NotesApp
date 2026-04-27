package com.example.notesapp.data.repository;

import androidx.lifecycle.LiveData;

import com.example.notesapp.data.local.Note;
import com.example.notesapp.data.local.NoteDao;
import com.example.notesapp.data.local.SortType;

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
    // 📌 READ UNIFICATO
    // =========================

    public LiveData<List<Note>> getNotes(
            String query,
            SortType sortType,
            boolean pinnedOnly,
            String tag
    ) {

        String q = (query == null || query.trim().isEmpty())
                ? "%"
                : "%" + query + "%";

        if (pinnedOnly) {
            return noteDao.searchAll(q, sortType.name(), tag);
        }

        return noteDao.searchAll(q, sortType.name(), tag);
    }

    public LiveData<Note> observeById(long id) {
        return noteDao.observeById(id);
    }

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
    // 📥 WRITE
    // =========================

    public void insert(Note note, Runnable onComplete) {
        executor.execute(() -> {
            long now = System.currentTimeMillis();

            if (note.id == 0) {
                note.createdAt = now;
            }

            note.updatedAt = now;

            noteDao.insert(note);

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

    public void deleteById(long id, Runnable onComplete) {
        executor.execute(() -> {
            noteDao.deleteById(id);
            if (onComplete != null) onComplete.run();
        });
    }

    public LiveData<List<Note>> getNotesByTag(String tag) {
        return noteDao.getByTag(tag);
    }
}