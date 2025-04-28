package com.example.trackmypills.util;

import android.content.SharedPreferences;

import com.example.trackmypills.models.Medication;
import com.example.trackmypills.viewmodel.MedicationViewModel;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class DoseManager {

    private final MedicationViewModel viewModel;

    // Constructor that accepts MedicationViewModel
    public DoseManager(MedicationViewModel viewModel) {
        this.viewModel = viewModel;
    }

    public enum DoseResult {
        SUCCESS,
        OUT_OF_MEDICATION,
        MAX_DOSE_REACHED,
        MAX_DOSE_CONFIRMED,
        UNDO_SUCCESS,
        UNDO_ERROR
    }

    // Dose taking method
    public DoseResult takeDose(Medication medication, boolean totalMedsGreaterThanZero,
                               boolean dosesTakenLessThanMaxAmt, boolean dontShowAgain,
                               SharedPreferences prefs, boolean allowExceedMax) {

        if (totalMedsGreaterThanZero && (dosesTakenLessThanMaxAmt || dontShowAgain || allowExceedMax)) {
            medication.setDosesTaken(medication.getDosesTaken() + medication.getMedQuantity());
            medication.addTakenTimes(LocalDateTime.now());
            medication.getMissedDosages().clear();

            if (medication.getTotalMeds() >= medication.getMedQuantity()) {
                medication.setTotalMeds(medication.getTotalMeds() - medication.getMedQuantity());
            } else {
                medication.setTotalMeds(0);
            }

            viewModel.update(medication);
            return allowExceedMax ? DoseResult.MAX_DOSE_CONFIRMED : DoseResult.SUCCESS;
        } else if (medication.getTotalMeds() == 0) {
            return DoseResult.OUT_OF_MEDICATION;
        } else {
            return DoseResult.MAX_DOSE_REACHED;
        }
    }


    // Undoes doses taken
    public DoseResult undoDose(Medication medication) {
        double dosesTaken = medication.getDosesTaken();

        // Ensures there is at least one dose taken
        if (dosesTaken > 0) {
            // Decrements dosesTaken by medQuantity
            double newDosesTaken = dosesTaken - medication.getMedQuantity();

            if (newDosesTaken < 0) {
                newDosesTaken = 0;
            }
            //  Adds the amount back to totalMeds
            double updateTotalMeds = medication.getTotalMeds() + medication.getMedQuantity();
            // Updates doses taken and total meds with the new values
            medication.setDosesTaken(newDosesTaken);
            medication.setTotalMeds(updateTotalMeds);

            List<LocalDateTime> takenTimes = new ArrayList<>(medication.getTakenTimes());
            if (!takenTimes.isEmpty()) {
                LocalDateTime lastTakenTime = takenTimes.get(takenTimes.size() - 1);
                takenTimes.remove(lastTakenTime); // Removes it from list
                medication.setTakenTimes(takenTimes);

                List<LocalDateTime> missedDosages = new ArrayList<>(medication.getMissedDosages());
                if (missedDosages.contains(lastTakenTime)) {
                    missedDosages.remove(lastTakenTime); // Removes the undone dose from missedDosages
                    medication.setMissedDosages(missedDosages); // Updates the missed dosages
                }

            }
            viewModel.update(medication); // Updates the medication in ViewModel
            return DoseResult.UNDO_SUCCESS;
        } else {
            return DoseResult.UNDO_ERROR; // No doses taken to undo
        }
    }

    // Refills total meds
    public void refillTotalMeds(Medication medication, double refillAmount) {
        if (refillAmount <= 0){
            return;
        }
        double updatedTotalMeds = medication.getTotalMeds() + refillAmount;
        medication.setTotalMeds(updatedTotalMeds);
        viewModel.update(medication);
    }

    public List<LocalDateTime> getExpectedDoseTimes(Medication medication, LocalDate date) {
        List<LocalDateTime> expected = new ArrayList<>();
        LocalDateTime startDateTime = LocalDateTime.of(date, medication.getStartTime());

        // If the time has already passed, sets it to the next day
        if (startDateTime.isBefore(LocalDateTime.now())) {
            startDateTime = startDateTime.plusDays(1);
        }

        double intervalHours = medication.getFrequency().getIntervalHours();
        long intervalMinutes = (long) (intervalHours * 60);

        // Fills up expected times based on max dose per day
        for (int i = 0; i < medication.getMaxAmt(); i++) {
            expected.add(startDateTime);
            startDateTime = startDateTime.plusMinutes(intervalMinutes);
        }
        return expected;
    }

    public void checkMissedDoses(Medication medication, LocalDate date) {
        List<LocalDateTime> expectedTimes = getExpectedDoseTimes(medication, date);
        List<LocalDateTime> taken = medication.getTakenTimes() !=
                null ? medication.getTakenTimes() : new ArrayList<>();
        List<LocalDateTime> missed = medication.getMissedDosages()
                != null ? new ArrayList<>(medication.getMissedDosages()) : new ArrayList<>();

        for (LocalDateTime expected : expectedTimes) {
            final long ALLOWED_DELAY_MINUTES = 15; // Maximum delay in minutes
            boolean takenClose = taken.stream()
                    .anyMatch(t ->Math.abs(Duration.between(expected, t).toMinutes()) < ALLOWED_DELAY_MINUTES);

            // Only adds missed doses if time is passed ALLOW_DELAY_MINUTES value
            if (!takenClose && !missed.contains(expected) && expected.isBefore(LocalDateTime.now().minusMinutes(ALLOWED_DELAY_MINUTES))) {
                missed.add(expected);
            }
        }

        medication.setMissedDosages(missed);
        viewModel.update(medication);
    }

    // Resets medication for new day
    public static void resetMedicationForNewDay(Medication medication, MedicationViewModel viewModel) {
        LocalDate today = LocalDate.now();
        LocalTime startTime = medication.getStartTime();
        LocalDateTime scheduledResetTime = LocalDateTime.of(today, startTime);
        LocalDateTime now = LocalDateTime.now();

        // Checks if last reset date is null or before today and if it's past the scheduled reset time
        if (medication.getLastResetDate() == null || medication.getLastResetDate().isBefore(today)
        &&  now.isAfter(scheduledResetTime)) {
            medication.setDosesTaken(0);
            medication.setTakenTimes(new ArrayList<>());
            medication.setMissedDosages(new ArrayList<>());
            medication.setLastResetDate(today);
            medication.setNextDosageTime(LocalDateTime.of(today, medication.getStartTime()));

            // Updates medication in ViewModel
            viewModel.update(medication);
        }
    }
    // Suppresses the "don't show exceed dialog" flag
    public void suppressMaxDoseDialog(SharedPreferences prefs) {
        prefs.edit().putBoolean("don't_show_exceed_dialog", true).apply();
    }
}
