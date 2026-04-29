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

import java.util.ArrayList;
import java.util.List;

public class NotesViewModel extends AndroidViewModel {

    private final NoteRepository repository;

    private final MutableLiveData<Boolean> showPinnedOnly = new MutableLiveData<>(false);
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");
    private final MutableLiveData<SortType> sortType = new MutableLiveData<>(SortType.DATE_DESC);
    private final MutableLiveData<List<String>> tagFilters = new MutableLiveData<>(null);
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
        visibleNotes.addSource(tagFilters, v -> refresh());

        refresh();
    }

    private void refresh() {

        Boolean pinned = showPinnedOnly.getValue();
        String query = searchQuery.getValue();
        SortType sort = sortType.getValue();
        List<String> tags = tagFilters.getValue();

        LiveData<List<Note>> newSource =repository.getNotesMultiTag(
                query,
                sort,
                Boolean.TRUE.equals(pinned),
                tags
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
        searchQuery.setValue(query != null ? query.trim() : "");
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

    public void setTagFilter(String tag) {
        if (tag == null || tag.trim().isEmpty()) {
            tagFilters.setValue(null);
        } else {
            tagFilters.setValue(java.util.List.of(tag.trim().toLowerCase()));
        }
    }
    // =========================
    // CRUD
    // =========================

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

    // =========================
    // FIX: TAG UPDATE (per MainActivity)
    // =========================
    public void updateTags(long noteId, String tags) {
        repository.updateTags(noteId, tags);
    }

    public void setTagFilters(List<String> tags) {

        if (tags == null || tags.isEmpty()) {
            tagFilters.setValue(null);
            return;
        }

        List<String> normalized = new ArrayList<>();

        for (String t : tags) {
            if (t != null && !t.trim().isEmpty()) {
                normalized.add(t.trim().toLowerCase());
            }
        }

        tagFilters.setValue(normalized.isEmpty() ? null : normalized);
    }

    public void restore(long id, Runnable onDone) {
        repository.restore(id, onDone);
    }

}