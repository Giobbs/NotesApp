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

    public NotesViewModel(@NonNull Application application) {
        super(application);

        repository = new NoteRepository(
                AppDatabase.getInstance(application).noteDao()
        );
    }

    // ===== LISTA BASE =====
    public LiveData<List<Note>> notes() {
        return repository.observeAll();
    }

    // ===== SEARCH =====
    public LiveData<List<Note>> searchResults() {
        return Transformations.switchMap(searchQuery, query -> {
            if (query == null || query.trim().isEmpty()) {
                return repository.observeAll();
            }
            return repository.search(query);
        });
    }

    // ===== STATE UPDATE =====
    public void setSearchQuery(String query) {
        searchQuery.setValue(query);
    }

    public LiveData<String> getSearchQuery() {
        return searchQuery;
    }

    // ===== ORDER STATE (base semplice) =====
    public void toggleOrder() {
        Boolean current = orderByDateDesc.getValue();
        orderByDateDesc.setValue(current == null || !current);
    }

    public LiveData<Boolean> getOrderState() {
        return orderByDateDesc;
    }

    // ===== ACCESS REPOSITORY (write operations) =====
    public void delete(Note note, Runnable onDone) {
        repository.delete(note, onDone);
    }

    // ===== INSERT =====
    public void insert(Note note, Runnable onDone) {
        repository.insert(note, onDone);
    }
}