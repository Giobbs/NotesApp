package com.example.notesapp.ui.main;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.notesapp.data.local.AppDatabase;
import com.example.notesapp.data.local.Note;
import com.example.notesapp.data.local.SortType;
import com.example.notesapp.data.repository.NoteRepository;

import java.util.List;
public class NotesViewModel extends AndroidViewModel {

    private final NoteRepository repository;

    private final MutableLiveData<Boolean> showPinnedOnly = new MutableLiveData<>(false);
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");
    private final MutableLiveData<SortType> sortType = new MutableLiveData<>(SortType.DATE_DESC);

    private final MediatorLiveData<List<Note>> visibleNotes = new MediatorLiveData<>();

    private LiveData<List<Note>> currentSource;

    public NotesViewModel(@NonNull Application application) {
        super(application);

        repository = new NoteRepository(
                AppDatabase.getInstance(application).noteDao()
        );

        visibleNotes.addSource(showPinnedOnly, v -> refresh());
        visibleNotes.addSource(searchQuery, v -> refresh());
        visibleNotes.addSource(sortType, v -> refresh());

        refresh();
    }

    private void refresh() {

        Boolean pinned = showPinnedOnly.getValue();
        String query = searchQuery.getValue();
        SortType sort = sortType.getValue();

        LiveData<List<Note>> newSource = repository.getNotes(
                query,
                sort,
                Boolean.TRUE.equals(pinned)
        );

        if (currentSource != null) {
            visibleNotes.removeSource(currentSource);
        }

        currentSource = newSource;

        visibleNotes.addSource(currentSource, visibleNotes::setValue);
    }

    // =========================
    // API
    // =========================

    public LiveData<List<Note>> getNotes() {
        return visibleNotes;
    }

    public void setSearchQuery(String query) {
        searchQuery.setValue(query);
    }

    public void setSortType(SortType type) {
        sortType.setValue(type);
    }

    public void setShowPinnedOnly(boolean value) {
        showPinnedOnly.setValue(value);
    }

    public LiveData<Boolean> getShowPinnedOnly() {
        return showPinnedOnly;
    }

    // CRUD
    public void delete(Note note, Runnable onDone) {
        repository.delete(note, onDone);
    }

    public void insert(Note note, Runnable onDone) {
        repository.insert(note, onDone);
    }

    public void update(Note note) {
        repository.update(note, null);
    }

    public LiveData<Note> getNoteById(long id) {
        return repository.observeById(id);
    }

    public void setPinned(long id, boolean pinned) {
        repository.setPinned(id, pinned, null);
    }
}