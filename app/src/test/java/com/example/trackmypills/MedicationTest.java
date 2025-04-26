package com.example.trackmypills;

import org.junit.Test;

import com.example.trackmypills.models.AdminType;
import com.example.trackmypills.models.Frequency;
import com.example.trackmypills.models.Medication;

import java.time.LocalDate;
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
                LocalTime.of(6, 0), // 6:00 AM
                Frequency.TWO_HOURS);

        // Checks if the medication was added
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
                LocalTime.of(8, 0), // 8:00 AM
                Frequency.FOUR_HOURS);

        medication.setStartTime(LocalTime.of(10, 0)); // 10:00 AM

        // Checks if the start time was updated
        assert medication.getStartTime().equals(LocalTime.of(10, 0));
    }

    @Test
    public void testNextDosageTimeTomorrow(){
        LocalTime startTime = LocalTime.of(8, 0); // 8:00 AM
        Frequency frequency = Frequency.TWENTY_FOUR_HOURS; // Every 24 hours
        AdminType adminType = AdminType.PILLS; // Pills
        Medication medication = new Medication("Multivitamin", 1, 1, 28.0, adminType, startTime, frequency);

        // Get the next dosage time
        LocalDateTime nextDosageTime = medication.getNextDosageTime();

        // Check if the next dosage time is tomorrow
       LocalDateTime expected = LocalDateTime.of(LocalDate.now().plusDays(1), startTime);
       assert nextDosageTime.equals(expected);
    }

    @Test
    public void testNextDosageTimeToday(){
        LocalTime startTime = LocalTime.of(11, 0); // 11:00 AM
        Frequency frequency = Frequency.FOUR_HOURS; // Every 4 hours
        AdminType adminType = AdminType.CAPSULES; // Capsules
        Medication medication = new Medication("Advil", 1, 1, 28.0, adminType, startTime, frequency);

        // Gets the next dosage time
        LocalDateTime nextDosageTime = medication.getNextDosageTime();

        // Checks if the next dosage time is today
        LocalDateTime expected = LocalDateTime.of(LocalDate.now(), startTime);
        assert nextDosageTime.equals(expected);
    }
    
}