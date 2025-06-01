package com.example.nplikasinote.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.nplikasinote.data.Note;
import com.example.nplikasinote.data.NoteDao;
import com.example.nplikasinote.data.NoteDatabase;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NoteRepository {
    private NoteDao noteDao;
    private LiveData<List<Note>> allNotes;

    // Thread pool untuk operasi DB agar tidak di main thread
    private static final int NUMBER_OF_THREADS = 4;
    private static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public NoteRepository(Application application) {
        NoteDatabase db = NoteDatabase.getDatabase(application);
        noteDao = db.noteDao();
        allNotes = noteDao.getAllNotes();
    }

    public LiveData<List<Note>> getAllNotes() {
        return allNotes;
    }

    public void insert(Note note) {
        databaseWriteExecutor.execute(() -> noteDao.insert(note));
    }

    public void update(Note note) {
        databaseWriteExecutor.execute(() -> noteDao.update(note));
    }

    public void delete(Note note) {
        databaseWriteExecutor.execute(() -> noteDao.delete(note));
    }

    // --- METODE BARU UNTUK PENCARIAN ---
    public LiveData<List<Note>> searchNotes(String query) {
        return noteDao.searchNotes("%" + query + "%"); // Tambahkan wildcard untuk pencarian parsial
    }
}
