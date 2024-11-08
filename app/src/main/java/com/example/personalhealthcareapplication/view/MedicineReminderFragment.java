package com.example.personalhealthcareapplication.view;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.personalhealthcareapplication.R;
import com.example.personalhealthcareapplication.model.MedicineReminder;
import com.example.personalhealthcareapplication.view.MedicineReminderAdapter;
import com.example.personalhealthcareapplication.viewmodel.MedicineReminderViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MedicineReminderFragment extends Fragment {
    private RecyclerView recyclerView;
    private MedicineReminderAdapter adapter;
    private MedicineReminderViewModel viewModel;
    private List<MedicineReminder> reminders;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_medicine_reminder, container, false);

        recyclerView = view.findViewById(R.id.rvMedicineReminders);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        FloatingActionButton fabAddReminder = view.findViewById(R.id.fabAddMedicineReminder);

        reminders = new ArrayList<>();
        adapter = new MedicineReminderAdapter(reminders);
        recyclerView.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(MedicineReminderViewModel.class);

        fabAddReminder.setOnClickListener(v -> openAddReminderDialog());

        loadReminders();

        return view;
    }

    private void openAddReminderDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_medicine_reminder, null);

        EditText etMedicineName = dialogView.findViewById(R.id.etMedicineName);
        EditText etQuantity = dialogView.findViewById(R.id.etQuantity);
        Spinner spnMedicineType = dialogView.findViewById(R.id.spnMedicineType);
        EditText etTime = dialogView.findViewById(R.id.etTime);

        Calendar calendar = Calendar.getInstance();

        etTime.setOnClickListener(v -> {
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    getContext(),
                    (view, hourOfDay, minute1) -> {
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        calendar.set(Calendar.MINUTE, minute1);
                        etTime.setText(android.text.format.DateFormat.format("hh:mm a", calendar));
                    },
                    hour,
                    minute,
                    false
            );
            timePickerDialog.show();
        });

        new AlertDialog.Builder(getContext())
                .setTitle("Add New Medicine Reminder")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String medicineName = etMedicineName.getText().toString();
                    String quantity = etQuantity.getText().toString();
                    String medicineType = spnMedicineType.getSelectedItem().toString();
                    String time = etTime.getText().toString();

                    if (TextUtils.isEmpty(medicineName) || TextUtils.isEmpty(quantity) || TextUtils.isEmpty(time)) {
                        Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Set reminder time in milliseconds
                    long reminderTimeInMillis = calendar.getTimeInMillis();

                    MedicineReminder reminder = new MedicineReminder(medicineName, quantity + " " + medicineType, reminderTimeInMillis);
                    viewModel.saveReminder(reminder);
                    viewModel.scheduleMedicineReminder(getContext(), reminder);

                    // Load reminders again to refresh the RecyclerView
                    loadReminders();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void loadReminders() {
        viewModel.getReminders().addOnSuccessListener(reminderList -> {
            if (reminderList != null) {
                reminders.clear();
                reminders.addAll(reminderList);
                adapter.setReminders(reminders);
                Log.d("MedicineReminderFragment", "Reminders loaded and adapter notified. Size: " + reminders.size());
            }
        }).addOnFailureListener(e -> Log.e("MedicineReminderFragment", "Error loading reminders", e));
    }
}
