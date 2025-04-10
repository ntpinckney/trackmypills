package com.example.trackmypills;

import org.junit.Test;

import com.example.trackmypills.models.AdminType;
import com.example.trackmypills.models.Frequency;
import com.example.trackmypills.models.Medication;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class MedicationTest {
    @Test
    public void testAddNewMedication() {
        Medication medication = new Medication("Aspirin",
                1.5,
                6.0,
                30.0,
                AdminType.PILLS,
                LocalTime.of(6, 0),
                Frequency.TWO_HOURS);

        assert medication.getName().equals("Aspirin");
    }

    @Test
    public void testUpdateStartTime() {
        Medication medication = new Medication(
                "Tylenol",
                2,
                6.0,
                28.0,
                AdminType.CAPSULES,
                LocalTime.of(8, 0),
                Frequency.FOUR_HOURS);

        medication.setStartTime(LocalTime.of(10, 0));
        assert medication.getStartTime().equals(LocalTime.of(10, 0));
    }
    
}