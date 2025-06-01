package com.example.nplikasinote.ui;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
// import android.util.Log; // Tidak digunakan, bisa dihapus
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton; // Tambahkan ini
import android.widget.LinearLayout; // Tambahkan ini
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.nplikasinote.R;
import com.example.nplikasinote.data.Note;
import com.example.nplikasinote.receiver.NoteReminderReceiver;
import com.example.nplikasinote.viewmodel.NoteViewModel;

import java.util.Calendar;

public class AddEditNoteFragment extends Fragment {

    private EditText editTitle, editContent;
    private NoteViewModel noteViewModel;
    private int noteId = -1;

    private long selectedReminderTimeInMillis = -1;
    private Calendar selectedCalendar;

    // Deklarasi View baru
    private ImageButton imageButtonSave, imageButtonDelete, imageButtonSetReminder;
    private LinearLayout layoutReminderOptionsContainer;
    private Button buttonPickDate, buttonPickTime;
    private TextView textReminderDatetime;


    public AddEditNoteFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_edit_note, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState); // Panggil super

        // Inisialisasi View
        editTitle = view.findViewById(R.id.edit_title);
        editContent = view.findViewById(R.id.edit_content);

        // View baru
        imageButtonSave = view.findViewById(R.id.button_save_icon);
        imageButtonDelete = view.findViewById(R.id.button_delete_icon);
        imageButtonSetReminder = view.findViewById(R.id.button_set_reminder_icon);
        layoutReminderOptionsContainer = view.findViewById(R.id.layout_reminder_options_container);
        buttonPickDate = view.findViewById(R.id.button_pick_date); // ID masih sama
        buttonPickTime = view.findViewById(R.id.button_pick_time); // ID masih sama
        textReminderDatetime = view.findViewById(R.id.text_reminder_datetime); // ID masih sama

        noteViewModel = new ViewModelProvider(this).get(NoteViewModel.class);

        Bundle args = getArguments();
        if (args != null) {
            noteId = args.getInt("noteId", -1);
            String title = args.getString("title", "");
            String content = args.getString("content", "");

            editTitle.setText(title);
            editContent.setText(content);

            if (noteId != -1) {
                imageButtonDelete.setVisibility(View.VISIBLE);
            } else {
                imageButtonDelete.setVisibility(View.GONE);
            }
        } else {
            imageButtonDelete.setVisibility(View.GONE);
        }

        // Tombol Pilih Tanggal (Listener tetap sama)
        buttonPickDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            if (selectedCalendar != null) { // Gunakan waktu yang sudah dipilih jika ada
                calendar = selectedCalendar;
            }
            DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                    (view1, year, month, dayOfMonth) -> {
                        if (selectedCalendar == null) {
                            selectedCalendar = Calendar.getInstance();
                        }
                        selectedCalendar.set(Calendar.YEAR, year);
                        selectedCalendar.set(Calendar.MONTH, month);
                        selectedCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        updateReminderText(textReminderDatetime);
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });

        // Tombol Pilih Waktu (Listener tetap sama)
        buttonPickTime.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            if (selectedCalendar != null) { // Gunakan waktu yang sudah dipilih jika ada
                calendar = selectedCalendar;
            }
            TimePickerDialog timePickerDialog = new TimePickerDialog(requireContext(),
                    (view12, hourOfDay, minute) -> {
                        if (selectedCalendar == null) {
                            // Jika tanggal belum dipilih, set tanggal ke hari ini
                            selectedCalendar = Calendar.getInstance();
                            // Reset detik dan milidetik agar tidak mempengaruhi perbandingan waktu
                            selectedCalendar.set(Calendar.SECOND, 0);
                            selectedCalendar.set(Calendar.MILLISECOND, 0);
                        }
                        selectedCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        selectedCalendar.set(Calendar.MINUTE, minute);
                        selectedCalendar.set(Calendar.SECOND, 0); // Penting untuk konsistensi
                        selectedReminderTimeInMillis = selectedCalendar.getTimeInMillis();
                        updateReminderText(textReminderDatetime);
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true // is24HourView
            );
            timePickerDialog.show();
        });

        // Tombol Simpan (ICON)
        imageButtonSave.setOnClickListener(v -> {
            saveNote();
        });

        // Tombol Set Reminder (ICON) - sekarang hanya untuk toggle visibility
        imageButtonSetReminder.setOnClickListener(v -> {
            if (layoutReminderOptionsContainer.getVisibility() == View.GONE) {
                layoutReminderOptionsContainer.setVisibility(View.VISIBLE);
                // Anda bisa mengubah ikon Set Reminder di sini jika mau (misal jadi 'alarm on')
                // imageButtonSetReminder.setImageResource(R.drawable.ic_alarm_on_vector);
            } else {
                layoutReminderOptionsContainer.setVisibility(View.GONE);
            }
        });

        // Tombol Hapus (ICON)
        imageButtonDelete.setOnClickListener(v -> {
            if (noteId != -1) { // Pastikan noteId valid
                // Batalkan reminder yang mungkin ada sebelum menghapus
                if (selectedReminderTimeInMillis > 0) { // Asumsi jika > 0 berarti ada reminder
                    cancelReminder(noteId != -1 ? noteId : (int) System.currentTimeMillis()); // Gunakan requestCode yang sama
                }

                Note toDelete = new Note(editTitle.getText().toString(), editContent.getText().toString(), System.currentTimeMillis());
                toDelete.setId(noteId);
                noteViewModel.delete(toDelete);
                Toast.makeText(getContext(), "Catatan dihapus", Toast.LENGTH_SHORT).show();
                requireActivity().getSupportFragmentManager().popBackStack();
            }
        });
    }

    private void saveNote() {
        String title = editTitle.getText().toString().trim();
        String content = editContent.getText().toString().trim();

        if (TextUtils.isEmpty(title) && TextUtils.isEmpty(content)) {
            Toast.makeText(getContext(), "Judul atau isi tidak boleh kosong", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(title)) title = "Tanpa Judul"; // Atau berikan judul default

        long timestamp = System.currentTimeMillis();
        Note note;

        if (noteId == -1) {
            note = new Note(title, content, timestamp);
            // Set ID setelah insert agar bisa digunakan untuk reminder jika note baru
            // Ini perlu penyesuaian jika Anda ingin set reminder untuk note yang belum pernah disimpan
            // Untuk sementara, reminder diset setelah note disimpan
        } else {
            note = new Note(title, content, timestamp);
            note.setId(noteId);
        }

        // Logika untuk set atau update reminder
        if (layoutReminderOptionsContainer.getVisibility() == View.VISIBLE && selectedReminderTimeInMillis > 0) {
            if (selectedReminderTimeInMillis <= System.currentTimeMillis()) {
                Toast.makeText(getContext(), "Waktu reminder harus di masa depan", Toast.LENGTH_SHORT).show();
                return; // Jangan simpan/update jika reminder tidak valid
            }

            // Cek permission exact alarm sebelum set reminder
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                AlarmManager alarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
                if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                    Toast.makeText(getContext(), "Aktifkan izin \"Alarm & pengingat\" di pengaturan aplikasi agar reminder berfungsi", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                    // Anda bisa menambahkan nama paket aplikasi Anda agar langsung ke pengaturan aplikasi Anda
                    // intent.setData(Uri.parse("package:" + requireContext().getPackageName()));
                    startActivity(intent);
                    return; // Jangan lanjut setReminder dulu sampai user aktifkan izin
                }
            }
            int uniqueRequestCode = noteId != -1 ? noteId : (int) (timestamp % Integer.MAX_VALUE);
            setReminder(selectedReminderTimeInMillis, title, content, uniqueRequestCode);
            note.setReminderTime(selectedReminderTimeInMillis); // Simpan waktu reminder ke note
            Toast.makeText(getContext(), "Reminder diset", Toast.LENGTH_SHORT).show();
        } else if (noteId != -1 && layoutReminderOptionsContainer.getVisibility() == View.GONE) {
            note.setReminderTime(0); // Tandai tidak ada reminder
        }


        // Simpan atau Update Note ke Database
        if (noteId == -1) {
            noteViewModel.insert(note); // Insert mengembalikan ID, bisa berguna
            Toast.makeText(getContext(), "Catatan disimpan", Toast.LENGTH_SHORT).show();
        } else {
            noteViewModel.update(note);
            Toast.makeText(getContext(), "Catatan diperbarui", Toast.LENGTH_SHORT).show();
        }
        requireActivity().getSupportFragmentManager().popBackStack();
    }


    private void updateReminderText(TextView textView) {
        if (selectedCalendar != null) {
            // Pastikan detik dan milidetik di-nol-kan untuk konsistensi
            selectedCalendar.set(Calendar.SECOND, 0);
            selectedCalendar.set(Calendar.MILLISECOND, 0);
            selectedReminderTimeInMillis = selectedCalendar.getTimeInMillis();

            String formattedDate = android.text.format.DateFormat.getDateFormat(getContext()).format(selectedCalendar.getTime());
            String formattedTime = android.text.format.DateFormat.getTimeFormat(getContext()).format(selectedCalendar.getTime());
            textView.setText("Waktu reminder: " + formattedDate + " " + formattedTime);
        } else {
            textView.setText("Belum ada tanggal & waktu dipilih");
        }
    }

    // Modifikasi setReminder untuk menerima requestCode
    private void setReminder(long timeInMillis, String title, String content, int requestCode) {
        // Cek permission sudah dilakukan di saveNote()

        Intent intent = new Intent(requireContext(), NoteReminderReceiver.class);
        // Pastikan action unik jika ada beberapa reminder dari note yang sama (meskipun umumnya 1 note 1 reminder)
        intent.setAction("com.example.nplikasinote.ACTION_REMINDER_" + requestCode);
        intent.putExtra("note_id", requestCode); // Kirim ID note (atau request code sebagai ID)
        intent.putExtra("note_title", title);
        intent.putExtra("note_content", content);


        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                requireContext(),
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
                }
            } catch (SecurityException se) {
                Toast.makeText(getContext(), "Tidak dapat menjadwalkan alarm. Periksa izin.", Toast.LENGTH_LONG).show();
                // Mungkin perlu fallback atau notifikasi lebih lanjut ke pengguna
            }
        }
    }

    private void cancelReminder(int requestCode) {
        Intent intent = new Intent(requireContext(), NoteReminderReceiver.class);
        intent.setAction("com.example.nplikasinote.ACTION_REMINDER_" + requestCode); // Action harus sama persis

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                requireContext(),
                requestCode, // Request code harus sama
                intent,
                PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE // FLAG_NO_CREATE untuk mengecek apakah ada, lalu batalkan
        );

        AlarmManager alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null && pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel(); // Juga batalkan PendingIntent itu sendiri
            Toast.makeText(getContext(), "Reminder dibatalkan", Toast.LENGTH_SHORT).show();
        }
    }
}