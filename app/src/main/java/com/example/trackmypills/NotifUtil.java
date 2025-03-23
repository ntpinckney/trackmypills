package com.example.trackmypills;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class NotifUtil {

    public static void scheduleNotification(Context context, Medication medication) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.putExtra("medication_name", medication.getName());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                medication.getId(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        LocalDateTime nextDoseTime = LocalDateTime.of(LocalDate.now(), medication.getNextDosageTime());
//        long triggerTime = System.currentTimeMillis() + 5000; // Testing purposes. Will send a notification in 5 seconds.
        long triggerTime = nextDoseTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

        if (alarmManager != null) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
            Log.d("NotifUtil", "Notification scheduled for medication: " + medication.getName());
        } else {
            Log.e("NotifUtil", "AlarmManager is null!");
        }

    }
    public static void cancelNotification(Context context, Medication medication) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, ReminderReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                medication.getId(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }
}