package com.example.notesapp.data.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.notesapp.data.local.Note;
import com.example.notesapp.data.local.NoteDao;
import com.example.notesapp.data.local.SortType;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class NoteRepositoryTest {

    private NoteDao noteDao;
    private NoteRepository repository;

    @Before
    public void setup() {
        noteDao = mock(NoteDao.class);
        repository = new NoteRepository(noteDao);
    }

    @Test
    public void getNotes_withBlankQuery_passesNullQueryToDao() {
        LiveData<java.util.List<Note>> expected = new MutableLiveData<>(Collections.emptyList());
        when(noteDao.getFilteredNotes(anyBoolean(), any(), anyString(), anyString())).thenReturn(expected);

        repository.getNotes("   ", SortType.DATE_DESC, true, "work");
        verify(noteDao, times(1)).getFilteredNotes(true, null, "work", SortType.DATE_DESC.name());
    }

    @Test
    public void getNotes_withTextQuery_trimsQueryBeforeDaoCall() {
        LiveData<java.util.List<Note>> expected = new MutableLiveData<>(Collections.emptyList());
        when(noteDao.getFilteredNotes(anyBoolean(), any(), anyString(), anyString())).thenReturn(expected);

        repository.getNotes("  my query  ", SortType.TITLE_ASC, false, "");

        verify(noteDao, times(1)).getFilteredNotes(false, "my query", "", SortType.TITLE_ASC.name());
    }

    @Test
    public void insert_newNote_setsCreatedAndUpdatedAt_andCallsDaoInsert() throws InterruptedException {
        Note note = new Note();
        note.id = 0;

        CountDownLatch latch = new CountDownLatch(1);

        repository.insert(note, latch::countDown);

        boolean completed = latch.await(2, TimeUnit.SECONDS);
        assertEquals(true, completed);

        verify(noteDao, times(1)).insert(note);
        assertTrue(note.getCreatedAt() > 0);
        assertTrue(note.getUpdatedAt() >= note.getCreatedAt());
    }

    @Test
    public void insert_existingNote_keepsCreatedAt_andUpdatesUpdatedAt() throws InterruptedException {
        Note note = new Note();
        note.id = 10;
        note.createdAt = 12345L;

        CountDownLatch latch = new CountDownLatch(1);

        repository.insert(note, latch::countDown);

        boolean completed = latch.await(2, TimeUnit.SECONDS);
        assertEquals(true, completed);

        verify(noteDao, times(1)).insert(note);
        assertEquals(12345L, note.getCreatedAt());
        assertTrue(note.getUpdatedAt() > 0);
    }

    @Test
    public void delete_callsSoftDeleteWithGivenId() throws InterruptedException {
        Note note = new Note();
        note.id = 42L;
        CountDownLatch latch = new CountDownLatch(1);

        repository.delete(note, latch::countDown);

        boolean completed = latch.await(2, TimeUnit.SECONDS);
        assertEquals(true, completed);

        ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);
        verify(noteDao, times(1)).softDelete(idCaptor.capture(), anyLong());
        assertEquals(42L, idCaptor.getValue().longValue());
    }

    @Test
    public void restore_callsDaoRestoreWithGivenId() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        repository.restore(77L, latch::countDown);

        boolean completed = latch.await(2, TimeUnit.SECONDS);
        assertEquals(true, completed);

        verify(noteDao, times(1)).restore(eq(77L), anyLong());
    }

    @Test
    public void deleteById_callsHardDelete() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        repository.deleteById(55L, latch::countDown);

        boolean completed = latch.await(2, TimeUnit.SECONDS);
        assertEquals(true, completed);

        verify(noteDao, times(1)).deleteById(55L);
    }

    @Test
    public void updateTags_callsDaoWithTagsAndTimestamp() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        repository.updateTags(88L, "home,urgent", latch::countDown);

        boolean completed = latch.await(2, TimeUnit.SECONDS);
        assertEquals(true, completed);

        verify(noteDao, times(1)).updateTags(eq(88L), eq("home,urgent"), anyLong());
    }
}
