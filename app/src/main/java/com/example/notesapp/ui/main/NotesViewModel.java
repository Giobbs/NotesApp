package com.example.notesapp.ui.main;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.notesapp.data.local.AppDatabase;
import com.example.notesapp.data.local.Note;
import com.example.notesapp.data.local.SortType;
import com.example.notesapp.data.repository.NoteRepository;

import java.util.List;

public class NotesViewModel extends AndroidViewModel {

    private final NoteRepository repository;

    // ===== STATE =====
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");
    private final MutableLiveData<SortType> sortType = new MutableLiveData<>(SortType.DATE_DESC);

    // ===== DATA =====
    private final LiveData<List<Note>> visibleNotes;

    public NotesViewModel(@NonNull Application application) {
        super(application);

        repository = new NoteRepository(
                AppDatabase.getInstance(application).noteDao()
        );

        visibleNotes = Transformations.switchMap(searchQuery, query ->
                Transformations.switchMap(sortType, sort ->
                        repository.search(query, sort)
                )
        );
    }

    // ===== LIST =====
    public LiveData<List<Note>> getNotes() {
        return visibleNotes;
    }

    // ===== SEARCH =====
    public void setSearchQuery(String query) {
        searchQuery.setValue(query);
    }

    public LiveData<String> getSearchQuery() {
        return searchQuery;
    }

    // ===== SORT =====
    public void setSortType(SortType type) {
        sortType.setValue(type);
    }

    public LiveData<SortType> getSortType() {
        return sortType;
    }

    // Toggle utile UI (facoltativo)
    public void toggleSortDirection() {
        SortType current = sortType.getValue();

        if (current == null || current == SortType.DATE_DESC) {
            sortType.setValue(SortType.DATE_ASC);
        } else if (current == SortType.DATE_ASC) {
            sortType.setValue(SortType.TITLE_ASC);
        } else {
            sortType.setValue(SortType.DATE_DESC);
        }
    }

    // ===== CRUD =====
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
}