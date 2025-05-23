package com.example.trackmypills;

import static org.mockito.Mockito.*;

import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.app.PendingIntent;

import com.example.trackmypills.models.Medication;
import com.example.trackmypills.util.NotifUtil;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.LocalDateTime;

@ExtendWith(MockitoExtension.class)  // Using JUnit 5 with MockitoExtension
public class NotifUtilTest {

    @Mock
    Context context;

    @Mock
    AlarmManager alarmManager;

    @Mock
    Medication medication;

    @Test
    public void testScheduleNotification() {
        // Mocking AlarmManager and Medication
        when(context.getSystemService(Context.ALARM_SERVICE)).thenReturn(alarmManager);
        when(medication.getNextDosageTime()).thenReturn(LocalDateTime.now().plusHours(1));

        // Schedules notification
        NotifUtil.scheduleNotification(context, medication);

        // Verify if alarm was set
        verify(alarmManager, times(1)).setExactAndAllowWhileIdle(anyInt(), anyLong(), any(PendingIntent.class));
    }
}
