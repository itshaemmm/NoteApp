package com.example.nplikasinote.data;

import androidx.room.ColumnInfo; // Tambahkan ini
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "note_table")
public class Note {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String title;
    private String content;
    private long timestamp;

    @ColumnInfo(name = "reminder_time") // Anotasi untuk nama kolom di database
    private long reminderTime; // Field baru untuk waktu reminder

    // Constructor
    public Note(String title, String content, long timestamp) {
        this.title = title;
        this.content = content;
        this.timestamp = timestamp;
        this.reminderTime = 0; // Default tidak ada reminder
    }

    // Setter for ID (karena autogenerate)
    public void setId(int id) {
        this.id = id;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public long getTimestamp() {
        return timestamp;
    }

    // Getter dan Setter untuk reminderTime
    public long getReminderTime() {
        return reminderTime;
    }

    public void setReminderTime(long reminderTime) {
        this.reminderTime = reminderTime;
    }
}