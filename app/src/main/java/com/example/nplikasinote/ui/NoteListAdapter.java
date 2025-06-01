package com.example.nplikasinote.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nplikasinote.R;
import com.example.nplikasinote.data.Note;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale; // Import Locale

public class NoteListAdapter extends RecyclerView.Adapter<NoteListAdapter.NoteViewHolder> {

    private List<Note> noteList = new ArrayList<>();
    private List<Note> noteListFull = new ArrayList<>(); // Salinan lengkap dari daftar catatan
    private OnItemClickListener listener;

    public void setNotes(List<Note> notes) {
        this.noteList = new ArrayList<>(notes); // Buat salinan baru
        this.noteListFull = new ArrayList<>(notes); // Simpan salinan lengkap
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note note = noteList.get(position);
        holder.textTitle.setText(note.getTitle());
        holder.textContent.setText(note.getContent());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(note);
            }
        });
    }

    @Override
    public int getItemCount() {
        return noteList.size();
    }

    public Note getNoteAt(int position) {
        if (position >= 0 && position < noteList.size()) {
            return noteList.get(position);
        }
        return null;
    }

    // Metode untuk memfilter daftar catatan
    public void filter(String text) {
        noteList.clear();
        if (text.isEmpty()) {
            noteList.addAll(noteListFull); // Jika teks kosong, tampilkan semua catatan
        } else {
            text = text.toLowerCase(Locale.getDefault()); // Konversi ke huruf kecil untuk pencocokan case-insensitive
            for (Note item : noteListFull) {
                if (item.getTitle().toLowerCase(Locale.getDefault()).contains(text) ||
                        item.getContent().toLowerCase(Locale.getDefault()).contains(text)) {
                    noteList.add(item);
                }
            }
        }
        notifyDataSetChanged(); // Beri tahu RecyclerView bahwa data telah berubah
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {
        private TextView textTitle;
        private TextView textContent;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.noteTitle);
            textContent = itemView.findViewById(R.id.noteContent);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(Note note);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}