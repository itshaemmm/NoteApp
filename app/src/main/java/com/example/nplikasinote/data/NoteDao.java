package com.example.nplikasinote.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface NoteDao {

    @Insert
    void insert(Note note);

    @Update
    void update(Note note);

    @Delete
    void delete(Note note);

    @Query("SELECT * FROM note_table ORDER BY timestamp DESC")
    LiveData<List<Note>> getAllNotes();

    // --- METODE BARU UNTUK PENCARIAN ---
    @Query("SELECT * FROM note_table WHERE title LIKE :query OR content LIKE :query ORDER BY timestamp DESC")
    LiveData<List<Note>> searchNotes(String query);
}
