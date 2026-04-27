package com.example.notesapp.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.*;

import java.util.List;

@Dao
public interface NoteDao {

    // =========================
    // 📌 BASE LISTA
    // =========================

    @Query("SELECT * FROM notes " +
            "WHERE deleted = 0 " +
            "ORDER BY pinned DESC, updatedAt DESC")
    LiveData<List<Note>> observeAllDesc();


    @Query("SELECT * FROM notes " +
            "WHERE deleted = 0 " +
            "ORDER BY pinned DESC, updatedAt ASC")
    LiveData<List<Note>> observeAllAsc();


    @Query("SELECT * FROM notes " +
            "WHERE id = :id AND deleted = 0")
    LiveData<Note> observeById(long id);


    // =========================
    // 🔎 SEARCH
    // =========================

    @Query("SELECT * FROM notes " +
            "WHERE deleted = 0 " +
            "AND (title LIKE :q OR content LIKE :q OR tags LIKE :q) " +
            "ORDER BY pinned DESC, updatedAt DESC")
    LiveData<List<Note>> searchDesc(String q);


    @Query("SELECT * FROM notes " +
            "WHERE deleted = 0 " +
            "AND (title LIKE :q OR content LIKE :q OR tags LIKE :q) " +
            "ORDER BY pinned DESC, updatedAt ASC")
    LiveData<List<Note>> searchAsc(String q);


    // =========================
    // 📥 INSERT / UPDATE
    // =========================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Note note);

    @Update
    int update(Note note);


    // =========================
    // 🗑 SOFT DELETE
    // =========================

    @Query("UPDATE notes " +
            "SET deleted = 1, updatedAt = :timestamp " +
            "WHERE id = :id")
    int softDelete(long id, long timestamp);

    @Query("UPDATE notes " +
            "SET deleted = 0, updatedAt = :timestamp " +
            "WHERE id = :id")
    int restore(long id, long timestamp);


    // =========================
    // 📌 PIN / ARCHIVE
    // =========================

    @Query("UPDATE notes " +
            "SET pinned = :pinned, updatedAt = :timestamp " +
            "WHERE id = :id")
    int setPinned(long id, boolean pinned, long timestamp);

    @Query("UPDATE notes " +
            "SET archived = :archived, updatedAt = :timestamp " +
            "WHERE id = :id")
    int setArchived(long id, boolean archived, long timestamp);


    // =========================
    // 📊 FILTER UI
    // =========================

    @Query("SELECT * FROM notes " +
            "WHERE deleted = 0 AND pinned = 1 " +
            "ORDER BY updatedAt DESC")
    LiveData<List<Note>> observePinned();


    @Query("SELECT * FROM notes " +
            "WHERE deleted = 0 AND archived = 1 " +
            "ORDER BY updatedAt DESC")
    LiveData<List<Note>> observeArchived();


    @Query("SELECT * FROM notes " +
            "WHERE deleted = 0 " +
            "ORDER BY createdAt DESC " +
            "LIMIT :limit")
    LiveData<List<Note>> observeRecent(int limit);


    // =========================
    // 🧩 LEGACY / COMPAT
    // =========================

    @Query("DELETE FROM notes WHERE id = :id")
    int deleteById(long id);

    @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
    LiveData<Note> getNoteById(long id);
}