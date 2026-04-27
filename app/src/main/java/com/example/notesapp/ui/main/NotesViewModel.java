package com.example.notesapp.ui.main;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.notesapp.data.local.AppDatabase;
import com.example.notesapp.data.local.Note;
import com.example.notesapp.data.repository.NoteRepository;

import java.util.List;

public class NotesViewModel extends AndroidViewModel {

    private final NoteRepository repository;

    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");
    private final MutableLiveData<Boolean> orderByDateDesc = new MutableLiveData<>(true);

    private final LiveData<List<Note>> visibleNotes;

    public NotesViewModel(@NonNull Application application) {
        super(application);

        repository = new NoteRepository(
                AppDatabase.getInstance(application).noteDao()
        );

        visibleNotes = Transformations.switchMap(searchQuery, query ->
                Transformations.switchMap(orderByDateDesc, desc -> {

                    return repository.search(query, desc);
                })
        );
    }

    // ===== UI LIST =====
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

    // ===== ORDER =====
    public void toggleOrder() {
        Boolean current = orderByDateDesc.getValue();
        orderByDateDesc.setValue(current == null || !current);
    }

    public LiveData<Boolean> getOrderState() {
        return orderByDateDesc;
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