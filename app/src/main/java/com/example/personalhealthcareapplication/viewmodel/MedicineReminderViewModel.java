package com.example.personalhealthcareapplication.viewmodel;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.example.personalhealthcareapplication.model.MedicineReminder;
import com.example.personalhealthcareapplication.notifications.MedicineNotificationReceiver;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class MedicineReminderViewModel extends AndroidViewModel {
    private final FirebaseFirestore db;
    private final String userId;

    public MedicineReminderViewModel(@NonNull Application application) {
        super(application);
        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    public void scheduleMedicineReminder(Context context, MedicineReminder reminder) {
        Intent intent = new Intent(context, MedicineNotificationReceiver.class);
        intent.putExtra("medicineName", reminder.getMedicineName());
        intent.putExtra("quantity", reminder.getQuantity());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                (int) reminder.getReminderTimeInMillis(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    reminder.getReminderTimeInMillis(),
                    pendingIntent
            );
        }
    }

    public void saveReminder(MedicineReminder reminder) {
        db.collection("Users")
                .document(userId)
                .collection("MedicineReminders")
                .add(reminder);
    }

//`    public void getReminders() {
//        db.collection("Users")
//                .document(userId)
//                .collection("MedicineReminders")
//                .get()
//                .addOnSuccessListener(querySnapshot -> {
//                    List<MedicineReminder> reminders = new ArrayList<>();
//                    for (DocumentSnapshot document : querySnapshot) {
//                        MedicineReminder reminder = document.toObject(MedicineReminder.class);
//                        if (reminder != null) {
//                            reminders.add(reminder);
//                        }
//                    }
//                    listener.onRemindersLoaded(reminders);
//                })
//                .addOnFailureListener(e -> {
//                    // Handle failure
//                });
//    }`

    public interface OnRemindersLoadedListener {
        void onRemindersLoaded(List<MedicineReminder> reminders);
    }
    public Task<List<MedicineReminder>> getReminders() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        return db.collection("Users")
                .document(userId)
                .collection("MedicineReminders")
                .get()
                .continueWith(task -> {
                    List<MedicineReminder> reminders = new ArrayList<>();
                    for (DocumentSnapshot document : task.getResult()) {
                        MedicineReminder reminder = document.toObject(MedicineReminder.class);
                        reminders.add(reminder);
                    }
                    return reminders;
                });
    }
}
