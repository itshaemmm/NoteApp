package com.example.nplikasinote.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.appcompat.widget.SearchView; // Import SearchView baru

import com.example.nplikasinote.R;
import com.example.nplikasinote.data.Note;
import com.example.nplikasinote.viewmodel.NoteViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class NoteFragment extends Fragment {

    private NoteViewModel noteViewModel;
    private NoteListAdapter noteListAdapter;
    private SearchView searchView; // Deklarasi SearchView

    public NoteFragment() {
        // Diperlukan constructor kosong
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate layout untuk fragment ini
        return inflater.inflate(R.layout.fragment_note, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        FloatingActionButton fab = view.findViewById(R.id.fab);
        searchView = view.findViewById(R.id.search_view); // Inisialisasi SearchView

        // Inisialisasi adapter
        noteListAdapter = new NoteListAdapter();

        // Mengatur StaggeredGridLayoutManager dengan 2 kolom dan orientasi vertikal
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        recyclerView.setAdapter(noteListAdapter);

        // Inisialisasi ViewModel
        noteViewModel = new ViewModelProvider(this).get(NoteViewModel.class);

        // Observasi data notes dari ViewModel
        // Awalnya, kita akan menampilkan semua catatan
        noteViewModel.getAllNotes().observe(getViewLifecycleOwner(), notes -> {
            // Ketika data notes berubah, update adapter
            noteListAdapter.setNotes(notes);
        });

        // --- FUNGSI SEARCHVIEW DIMULAI DI SINI ---
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Ketika user menekan enter/submit pencarian
                performSearch(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Ketika teks pencarian berubah (setiap kali user mengetik)
                // Kita bisa langsung memanggil pencarian di sini untuk hasil instan
                performSearch(newText);
                return true;
            }
        });

        // Opsional: Untuk mengembalikan semua catatan saat SearchView ditutup
        searchView.setOnCloseListener(() -> {
            noteViewModel.getAllNotes().observe(getViewLifecycleOwner(), notes -> {
                noteListAdapter.setNotes(notes);
            });
            return false; // Mengembalikan false agar event default juga berjalan
        });
        // --- FUNGSI SEARCHVIEW BERAKHIR DI SINI ---

        // Handle klik pada item di RecyclerView
        noteListAdapter.setOnItemClickListener(note -> {
            Bundle args = new Bundle();
            if (note != null) {
                args.putInt("noteId", note.getId());
                args.putString("title", note.getTitle());
                args.putString("content", note.getContent());
                args.putLong("reminderTime", note.getReminderTime());
                AddEditNoteFragment editFragment = new AddEditNoteFragment();
                editFragment.setArguments(args);

                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, editFragment)
                            .addToBackStack(null)
                            .commit();
                }
            }
        });

        // Handle klik pada FloatingActionButton untuk menambah note baru
        fab.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new AddEditNoteFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });
    }

    /**
     * Metode bantu untuk melakukan pencarian berdasarkan query.
     * Jika query kosong, akan menampilkan semua catatan.
     *
     * @param query Teks pencarian yang dimasukkan pengguna.
     */
    private void performSearch(String query) {
        if (query.isEmpty()) {
            // Jika query kosong, tampilkan semua catatan
            noteViewModel.getAllNotes().observe(getViewLifecycleOwner(), notes -> {
                noteListAdapter.setNotes(notes);
            });
        } else {
            // Lakukan pencarian berdasarkan query
            noteViewModel.searchNotes(query).observe(getViewLifecycleOwner(), notes -> {
                noteListAdapter.setNotes(notes);
            });
        }
    }
}