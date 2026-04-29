package com.example.notesapp.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.*;

import java.util.List;

@Dao
public interface NoteDao {

    // =========================
    // 📌 BASE + SORT UNIFICATO
    // =========================

    @Query("SELECT * FROM notes " +
            "WHERE deleted = 0 " +
            "ORDER BY pinned DESC, " +
            "CASE WHEN :sort = 'TITLE_ASC' THEN title END ASC, " +
            "CASE WHEN :sort = 'TITLE_DESC' THEN title END DESC, " +
            "CASE WHEN :sort = 'DATE_ASC' THEN updatedAt END ASC, " +
            "CASE WHEN :sort = 'DATE_DESC' THEN updatedAt END DESC")
    LiveData<List<Note>> observeAll(String sort);

    // =========================
    // 🔎 SEARCH + SORT UNIFICATO
    // =========================

    @Query("SELECT * FROM notes " +
            "WHERE deleted = 0 " +
            "AND (title LIKE :q OR content LIKE :q OR tags LIKE :q) " +
            "ORDER BY pinned DESC, " +
            "CASE WHEN :sort = 'TITLE_ASC' THEN title END ASC, " +
            "CASE WHEN :sort = 'TITLE_DESC' THEN title END DESC, " +
            "CASE WHEN :sort = 'DATE_ASC' THEN updatedAt END ASC, " +
            "CASE WHEN :sort = 'DATE_DESC' THEN updatedAt END DESC")
    LiveData<List<Note>> search(String q, String sort);

    // =========================
    // 📌 SINGLE NOTE
    // =========================

    @Query("SELECT * FROM notes WHERE id = :id AND deleted = 0")
    LiveData<Note> observeById(long id);

    @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
    LiveData<Note> getNoteById(long id);

    // =========================
    // 📥 INSERT / UPDATE
    // =========================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Note note);

    @Update
    int update(Note note);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Note> notes);

    // =========================
    // 🗑 SOFT DELETE
    // =========================

    @Query("UPDATE notes SET deleted = 1, updatedAt = :timestamp WHERE id = :id")
    int softDelete(long id, long timestamp);

    @Query("UPDATE notes SET deleted = 0, updatedAt = :timestamp WHERE id = :id")
    int restore(long id, long timestamp);

    // =========================
    // 📌 PIN / ARCHIVE
    // =========================

    @Query("UPDATE notes SET pinned = :pinned, updatedAt = :timestamp WHERE id = :id")
    int setPinned(long id, boolean pinned, long timestamp);

    @Query("UPDATE notes SET archived = :archived, updatedAt = :timestamp WHERE id = :id")
    int setArchived(long id, boolean archived, long timestamp);

    // =========================
    // 📊 FILTER UI
    // =========================

    @Query("SELECT * FROM notes WHERE deleted = 0 AND pinned = 1 ORDER BY updatedAt DESC")
    LiveData<List<Note>> observePinned();

    @Query("SELECT * FROM notes WHERE deleted = 0 AND archived = 1 ORDER BY updatedAt DESC")
    LiveData<List<Note>> observeArchived();

    @Query("SELECT * FROM notes WHERE deleted = 0 ORDER BY createdAt DESC LIMIT :limit")
    LiveData<List<Note>> observeRecent(int limit);

    // =========================
    // 🗑 HARD DELETE
    // =========================

    @Query("DELETE FROM notes WHERE id = :id")
    int deleteById(long id);

    @Query("SELECT * FROM notes " +
            "WHERE deleted = 0 AND pinned = 1 " +
            "AND (title LIKE :q OR content LIKE :q OR tags LIKE :q) " +
            "ORDER BY " +
            "CASE WHEN :sort = 'TITLE_ASC' THEN title END ASC, " +
            "CASE WHEN :sort = 'TITLE_DESC' THEN title END DESC, " +
            "CASE WHEN :sort = 'DATE_ASC' THEN updatedAt END ASC, " +
            "CASE WHEN :sort = 'DATE_DESC' THEN updatedAt END DESC")
    LiveData<List<Note>> searchPinned(String q, String sort);

    @Query("SELECT * FROM notes WHERE deleted = 0 AND (',' || tags || ',') LIKE '%,' || :tag || ',%'")
    LiveData<List<Note>> getByTag(String tag);

    @Query("SELECT * FROM notes " +
            "WHERE deleted = 0 " +
            "AND (title LIKE :q OR content LIKE :q OR tags LIKE :q) " +
            "AND (:tag IS NULL OR :tag = '' OR (',' || tags || ',') LIKE '%,' || :tag || ',%') " +
            "ORDER BY pinned DESC, " +
            "CASE WHEN :sort = 'TITLE_ASC' THEN title END ASC, " +
            "CASE WHEN :sort = 'TITLE_DESC' THEN title END DESC, " +
            "CASE WHEN :sort = 'DATE_ASC' THEN updatedAt END ASC, " +
            "CASE WHEN :sort = 'DATE_DESC' THEN updatedAt END DESC")
    LiveData<List<Note>> searchAll(String q, String sort, String tag);

    @Query("UPDATE notes SET tags = :tags, updatedAt = :timestamp WHERE id = :id")
    int updateTags(long id, String tags, long timestamp);


    @Query("""
SELECT * FROM notes
WHERE deleted = 0

AND (:pinnedOnly = 0 OR pinned = 1)

AND (:query IS NULL OR 
     title LIKE '%' || :query || '%' OR 
     content LIKE '%' || :query || '%')

ORDER BY 
    pinned DESC,
    CASE WHEN :sort = 'TITLE_ASC' THEN title END ASC,
    CASE WHEN :sort = 'TITLE_DESC' THEN title END DESC,
    CASE WHEN :sort = 'DATE_ASC' THEN updatedAt END ASC,
    CASE WHEN :sort = 'DATE_DESC' THEN updatedAt END DESC
""")
    LiveData<List<Note>> getFilteredNotes(
            boolean pinnedOnly,
            String query,
            String sort
    );

    @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
    Note getNoteByIdSync(long id);

    @Query("SELECT * FROM notes ORDER BY id DESC LIMIT 3")
    List<Note> getRecentNotes();

    @Query("""
SELECT * FROM notes
WHERE deleted = 0
ORDER BY pinned DESC, updatedAt DESC
""")
    List<Note> getAllNotesSync();
}