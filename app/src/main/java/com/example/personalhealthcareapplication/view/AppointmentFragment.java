package com.example.personalhealthcareapplication.view;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.personalhealthcareapplication.R;
import com.example.personalhealthcareapplication.model.Appointment;
import com.example.personalhealthcareapplication.notifications.NotificationReceiver;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AppointmentFragment extends Fragment implements AppointmentAdapter.OnEditClickListener, AppointmentAdapter.OnDeleteClickListener {

    private RecyclerView recyclerView;
    private AppointmentAdapter adapter;
    private List<Appointment> appointments;
    private FirebaseFirestore db;
    private String userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_appointment, container, false);
        requestNotificationPermission();
        recyclerView = view.findViewById(R.id.recyclerViewAppointments);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        FloatingActionButton fabAddAppointment = view.findViewById(R.id.fabAddAppointment);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Set up RecyclerView adapter
        appointments = new ArrayList<>();
        adapter = new AppointmentAdapter(appointments, this, this); // Pass listeners
        recyclerView.setAdapter(adapter);

        // Load appointments from Firestore
        loadAppointments();

        // Add new appointment when FAB is clicked
        fabAddAppointment.setOnClickListener(v -> openAddAppointmentDialog(null));

        return view;
    }

    private void loadAppointments() {
        CollectionReference appointmentsRef = db.collection("Users")
                .document(userId)
                .collection("Appointments");

        appointmentsRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                appointments.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Appointment appointment = document.toObject(Appointment.class);
                    appointment.setId(document.getId()); // Set document ID
                    appointments.add(appointment);
                }
                adapter.notifyDataSetChanged();
            } else {
                Toast.makeText(getContext(), "Failed to load appointments", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openAddAppointmentDialog(Appointment appointment) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_appointment, null);

        EditText etDoctorName = dialogView.findViewById(R.id.etDoctorName);
        EditText etDate = dialogView.findViewById(R.id.etDate);
        EditText etTime = dialogView.findViewById(R.id.etTime);

        Calendar calendar = Calendar.getInstance();

        // Populate fields if editing
        if (appointment != null) {
            etDoctorName.setText(appointment.getDoctorName());
            etDate.setText(appointment.getDate());
            etTime.setText(appointment.getTime());
        }

        // Set Date Picker
        etDate.setOnClickListener(v -> {
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    getContext(),
                    (view, year1, month1, dayOfMonth) -> etDate.setText(dayOfMonth + "/" + (month1 + 1) + "/" + year1),
                    year, month, day);
            datePickerDialog.show();
        });

        // Set Time Picker
        etTime.setOnClickListener(v -> {
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    getContext(),
                    (view, hourOfDay, minute1) -> {
                        // Format to 12-hour time with AM/PM
                        Calendar selectedTime = Calendar.getInstance();
                        selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        selectedTime.set(Calendar.MINUTE, minute1);

                        String formattedTime = android.text.format.DateFormat.format("hh:mm a", selectedTime).toString();
                        etTime.setText(formattedTime);
                    },
                    hour, minute, false // Set the last parameter to false to use 12-hour format
            );
            timePickerDialog.show();
        });

        // Show the Add/Edit Appointment Dialog
        new androidx.appcompat.app.AlertDialog.Builder(getContext())
                .setTitle(appointment == null ? "Add New Appointment" : "Edit Appointment")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String doctorName = etDoctorName.getText().toString();
                    String date = etDate.getText().toString();
                    String time = etTime.getText().toString();

                    if (TextUtils.isEmpty(doctorName) || TextUtils.isEmpty(date) || TextUtils.isEmpty(time)) {
                        Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (appointment == null) {
                        saveAppointment(new Appointment(doctorName, date, time));
                    } else {
                        appointment.setDoctorName(doctorName);
                        appointment.setDate(date);
                        appointment.setTime(time);
                        updateAppointment(appointment);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void saveAppointment(Appointment appointment) {
        db.collection("Users")
                .document(userId)
                .collection("Appointments")
                .add(appointment)
                .addOnSuccessListener(documentReference -> {
                    appointment.setId(documentReference.getId());
                    appointments.add(appointment);
                    adapter.notifyItemInserted(appointments.size() - 1);
                    Toast.makeText(getContext(), "Appointment added", Toast.LENGTH_SHORT).show();
                    // Schedule notifications
                    scheduleAppointmentReminders(appointment);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to add appointment", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateAppointment(Appointment appointment) {
        db.collection("Users")
                .document(userId)
                .collection("Appointments")
                .document(appointment.getId())
                .set(appointment)
                .addOnSuccessListener(aVoid -> {
                    // Cancel any existing reminders for this appointment
                    cancelExistingReminders(appointment);

                    // Reload and update appointments in RecyclerView
                    loadAppointments();
                    Toast.makeText(getContext(), "Appointment updated", Toast.LENGTH_SHORT).show();

                    // Reschedule notifications with the updated time
                    scheduleAppointmentReminders(appointment);
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to update appointment", Toast.LENGTH_SHORT).show());
    }

    private void deleteAppointment(Appointment appointment) {
        db.collection("Users")
                .document(userId)
                .collection("Appointments")
                .document(appointment.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    appointments.remove(appointment);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(getContext(), "Appointment deleted", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to delete appointment", Toast.LENGTH_SHORT).show());
    }



    @Override
    public void onEditClick(Appointment appointment) {
        openAddAppointmentDialog(appointment);
    }

    @Override
    public void onDeleteClick(Appointment appointment) {
        deleteAppointment(appointment);
    }
    private void scheduleAppointmentReminders(Appointment appointment) {
        long appointmentTimeMillis = appointment.getAppointmentTimeInMillis(); // Method to get appointment time in millis

        // Reminder times in milliseconds before the appointment
        long[] reminderTimes = {
                2 * 24 * 60 * 60 * 1000, // 2 days
                1 * 24 * 60 * 60 * 1000, // 1 day
                6 * 60 * 60 * 1000,      // 6 hours
                3 * 60 * 60 * 1000,      // 3 hours
                1 * 60 * 60 * 1000       // 1 hour
        };

        for (long reminderTime : reminderTimes) {
            long triggerTime = appointmentTimeMillis - reminderTime;
            if (triggerTime > System.currentTimeMillis()) {
                Log.d("AppointmentFragment", "Scheduling reminder for: " + triggerTime);
                setReminderAlarm(triggerTime, appointment);
            }
        }
    }


    private void setReminderAlarm(long triggerTime, Appointment appointment) {
        Intent intent = new Intent(getContext(), NotificationReceiver.class);
        intent.putExtra("doctorName", appointment.getDoctorName());
        intent.putExtra("appointmentTime", appointment.getDate() + " " + appointment.getTime());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                getContext(),
                (int) (appointment.getId().hashCode() + triggerTime), // Unique ID for each reminder
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
        }
    }
    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (getContext().checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1001);
            }
        }
    }
    private void cancelExistingReminders(Appointment appointment) {
        Intent intent = new Intent(getContext(), NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                getContext(),
                appointment.getId().hashCode(), // Use unique ID based on appointment ID
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent); // Cancel any existing alarms
        }
    }

}
