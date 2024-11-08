// NotificationReceiver.java
package com.example.personalhealthcareapplication.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.personalhealthcareapplication.R;

public class NotificationReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "AppointmentReminderChannel";

    @Override
    public void onReceive(Context context, Intent intent) {
        String doctorName = intent.getStringExtra("doctorName");
        String appointmentTime = intent.getStringExtra("appointmentTime");

        // Log received data for debugging
        Log.d("NotificationReceiver", "Received notification for: " + doctorName + " at " + appointmentTime);

        createNotificationChannel(context);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Upcoming Appointment")
                .setContentText("You have an appointment with Dr. " + doctorName + " at " + appointmentTime)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("You have an appointment with Dr. " + doctorName + " at " + appointmentTime))
                .setAutoCancel(true); // Dismiss notification when tapped

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, builder.build());
    }
    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Appointment Reminders",
                    NotificationManager.IMPORTANCE_HIGH // Use HIGH for visible notifications
            );
            channel.setDescription("Notifications for upcoming appointments");

            // Only create the channel if it doesn't already exist
            if (notificationManager != null && notificationManager.getNotificationChannel(CHANNEL_ID) == null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

}
