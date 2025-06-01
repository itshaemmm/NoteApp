package com.example.nplikasinote.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.nplikasinote.data.Note;
import com.example.nplikasinote.repository.NoteRepository;

import java.util.List;

public class NoteViewModel extends AndroidViewModel {

    private NoteRepository repository;
    private LiveData<List<Note>> allNotes;

    public NoteViewModel(@NonNull Application application) {
        super(application);
        repository = new NoteRepository(application);
        allNotes = repository.getAllNotes();
    }

    public LiveData<List<Note>> getAllNotes() {
        return allNotes;
    }

    public void insert(Note note) {
        repository.insert(note);
    }

    public void update(Note note) {
        repository.update(note);
    }

    public void delete(Note note) {
        repository.delete(note);
    }

    // --- METODE BARU UNTUK PENCARIAN ---
    public LiveData<List<Note>> searchNotes(String query) {
        return repository.searchNotes(query);
    }
}
