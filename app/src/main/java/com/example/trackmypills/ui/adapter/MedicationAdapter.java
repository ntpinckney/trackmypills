package com.example.trackmypills.ui.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trackmypills.models.Medication;
import com.example.trackmypills.ui.activity.EditMedication;
import com.example.trackmypills.util.NotifUtil;
import com.example.trackmypills.viewmodel.MedicationViewModel;
import com.example.trackmypills.R;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class MedicationAdapter extends RecyclerView.Adapter<MedicationAdapter.MedicationViewHolder> {
    private List<Medication> medications;
    private OnMedicationClickListener listener; // Callback for item clicks

    private MedicationViewModel viewModel;

    private Context context;
    private static final int MAX_TIMES = 4; // Maximum number of scheduled times to display


    public interface OnMedicationClickListener {
        void onMedicationClick(Medication medication);
    }

    public MedicationAdapter(List<Medication> medications,
                             MedicationViewModel viewModel, OnMedicationClickListener listener) {
        this.medications = medications;
        this.listener = listener;
        this.viewModel = viewModel;
    }

    public void setMedications(List<Medication> newMedications) {
        if (newMedications != null) {
            this.medications = newMedications;
            notifyDataSetChanged();
        }
    }

    @NonNull
    @Override
    public MedicationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_medication, parent, false);
        return new MedicationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MedicationViewHolder holder, int position){
        Medication medication = medications.get(position);
        holder.bind(medication, listener); // Assigns binding logic to bind()
    }

    @Override
    public int getItemCount() {
        return medications == null ? 0 : medications.size();
    }

    class MedicationViewHolder extends RecyclerView.ViewHolder {
        TextView medNameTextView, medTimeTextView, medDosageTextView, totalMedsTextView;
        ImageButton expandButton, takeButton, undoButton, editButton,
        deleteButton;
        LinearLayout expandableView;

        boolean isExpanded;
        public MedicationViewHolder(@NonNull View itemView) {
            super(itemView);

            // Main interface display
            medNameTextView = itemView.findViewById(R.id.med_name);
            medTimeTextView = itemView.findViewById(R.id.med_time);
            medDosageTextView = itemView.findViewById(R.id.med_dosage);
            totalMedsTextView = itemView.findViewById(R.id.total_meds);
            expandButton = itemView.findViewById(R.id.expand_button);

            // Expandable view interface display
            expandableView = itemView.findViewById(R.id.expandable_view);
            takeButton = itemView.findViewById(R.id.take_button);
            undoButton = itemView.findViewById(R.id.undo_button);
            editButton = itemView.findViewById(R.id.edit_button);
            deleteButton = itemView.findViewById(R.id.delete_button);

        }

        public void bind(Medication medication, OnMedicationClickListener listener) {
            medNameTextView.setText(medication.getName());
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime nextDosageTime = medication.getNextDosageTime();
            expandableView.setVisibility(isExpanded ? View.VISIBLE : View.GONE);

            // Animations
            final Animation rotateClockwise = AnimationUtils.loadAnimation(itemView.getContext(), R.anim.rotate_clockwise);
            final Animation rotateCounterclockwise = AnimationUtils.loadAnimation(itemView.getContext(), R.anim.rotate_counterclockwise);
            final Animation collapse = AnimationUtils.loadAnimation(itemView.getContext(), R.anim.collapse_animation);
            final Animation expand = AnimationUtils.loadAnimation(itemView.getContext(), R.anim.expand_animation);

            expandButton.setOnClickListener(v -> {
                isExpanded = !isExpanded;
                if (isExpanded) {
                    expandableView.setVisibility(View.VISIBLE);
                    expandableView.startAnimation(expand);
                    expandButton.startAnimation(rotateClockwise);
                    expandButton.setRotation(90);
                } else {
                    expandableView.startAnimation(collapse);
                    collapse.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) { }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            expandableView.setVisibility(View.GONE);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) { }
                    });
                    expandButton.startAnimation(rotateCounterclockwise);
                    expandButton.setRotation(0);
                }
            });

            // Validates medQuantity and maxAmt before it sets the texts
            if (medication.getMedQuantity() < 0.01) {
                medication.setMedQuantity(0.01); // Automatically sets it to 0.01 if number is less than 0.01
            }

            if(medication.getMaxAmt() < 0.01) {
                medication.setMaxAmt(0.01); // Automatically sets it to 0.01 if number is less than 0.01
            }

            takeButton.setOnClickListener(v -> {
                SharedPreferences prefs = v.getContext().getSharedPreferences("ExceedPrefs", Context.MODE_PRIVATE);
                boolean dontShowAgain = prefs.getBoolean("don't_show_exceed_dialog", false);
                boolean dosesTakenLessThanMaxAmt = medication.getDosesTaken() < medication.getMaxAmt();
                boolean totalMedsGreaterThanZero = medication.getTotalMeds() > 0;

                takeDose(medication, v, totalMedsGreaterThanZero, dosesTakenLessThanMaxAmt, dontShowAgain, prefs);
            });

            double initialTotalMeds = medication.getTotalMeds(); // Establishes what the totalMeds are before listener

            // Undoes medication taken in case of mistakes
            undoButton.setOnClickListener(v -> {
                double dosesTaken = medication.getDosesTaken();

                // Ensures there is at least one dose taken
                if (dosesTaken > 0) {
                    // Decrements dosesTaken by medQuantity
                    double newDosesTaken = dosesTaken - medication.getMedQuantity();

                    // Ensures dosesTaken does not go below zero
                    if (newDosesTaken < 0) {
                        newDosesTaken = 0;
                    }

                    // Adds the amount back to totalMedsTextView per does undone
                    double amountToAddBack = medication.getMedQuantity();

                    // Ensures that adding back does not exceed the initial totalMedsTextView
                    double updatedTotalMeds = medication.getTotalMeds() + amountToAddBack;
                    medication.setTotalMeds(Math.min(updatedTotalMeds, initialTotalMeds));

                    // Updates doses taken and total meds with the new values
                    medication.setDosesTaken(newDosesTaken);
                    medication.setTotalMeds(updatedTotalMeds);

                    // Updates the medication in ViewModel
                    viewModel.update(medication);
                    notifyItemChanged(getAdapterPosition()); // Refreshes UI
                } else {
                    Toast.makeText(v.getContext(), "No doses taken to undo!", Toast.LENGTH_SHORT).show();
                }
            });

            // Takes user to edit page
            editButton.setOnClickListener(v-> {
                Intent intent = new Intent(itemView.getContext(), EditMedication.class);
                intent.putExtra("medication_id", medication.getId());
                itemView.getContext().startActivity(intent);
            });

            // Deletes entries
            deleteButton.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    new AlertDialog.Builder(v.getContext())
                            .setTitle("Delete Medication")
                            .setMessage(String.format("Are you sure you want to delete %s? " +
                                    "This cannot be undone.", medication.getName()))
                            .setPositiveButton("Yes", (dialog, which) -> {
                                Medication medToDelete = medications.get(position);
                                medications.remove(position); // Removes from list
                                viewModel.delete(medToDelete); // Removes from database
                                NotifUtil.cancelNotification(v.getContext(), medToDelete); // Cancels notifications
                                Toast.makeText(v.getContext(), String.format("%s deleted", medToDelete.getName()), Toast.LENGTH_SHORT).show();
                                notifyItemRemoved(position); // Refreshes UI
                            })
                            .setNegativeButton("No", (dialog, which) ->
                                    dialog.dismiss()) // Does not delete medication
                            .show();
                }
            });

            // Gets up to four upcoming times
            List<LocalDateTime> upcomingTimes = getUpcomingTimes(medication, MAX_TIMES);

            // Formats time
            if(upcomingTimes.isEmpty()){
                medTimeTextView.setText("No more reminders today.");
            } else {
                StringBuilder timeDisplay = new StringBuilder("Upcoming reminders:\n");
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:mm a");
                for (LocalDateTime time : upcomingTimes) {
                    timeDisplay.append(time.format(formatter)).append(" ");
                }
                // Tells user when the next reminders are
                medTimeTextView.setText(timeDisplay.toString().trim());
            }


            // Informs users how many doses have been taken
            medDosageTextView.setText(String.format("%.2f/%.2f %s taken", medication.getDosesTaken(), medication.getMaxAmt(),
                medication.getAdminType().getLabel()));

            // Tells user how many pills remain in total
            totalMedsTextView.setText(String.format("%.2f %s remaining", medication.getTotalMeds(),
                    medication.getAdminType().getLabel()));

        }

        private void takeDose(Medication medication, View v, boolean totalMedsGreaterThanZero, boolean dosesTakenLessThanMaxAmt, boolean dontShowAgain, SharedPreferences prefs) {

            /* If the doses taken is lower than the maximum amount, totalMedsTextView isn't 0, or "Don't Show Again"
            is selected, medQuantity increases dosesTaken */
            if (totalMedsGreaterThanZero && (dosesTakenLessThanMaxAmt || dontShowAgain)) {
                medication.setDosesTaken(medication.getDosesTaken() + medication.getMedQuantity());
            // Ensures totalMedsTextView never goes negative
            if (medication.getTotalMeds() >= medication.getMedQuantity()) {
                medication.setTotalMeds(medication.getTotalMeds() - medication.getMedQuantity());
            } else {
                medication.setTotalMeds(0);
                Toast.makeText(v.getContext(), "You are out of medication!", Toast.LENGTH_LONG).show();
            }

            viewModel.update(medication);
            notifyItemChanged(getAdapterPosition());

        } else if (medication.getTotalMeds() == 0 ){
            // Blocks further dosage increments if there are no more meds left
            Toast.makeText(v.getContext(), "You are out of medication!", Toast.LENGTH_LONG).show();

        } else {
            new AlertDialog.Builder(v.getContext())
                    .setTitle("Max Dose Reached")
                    .setMessage("You have already taken the maximum dosage. Do you want to exceed it?")
                    .setPositiveButton("Yes", (dialog, which) -> {

                        // Allows exceeding max dosage, but only if there are meds left
                        if (totalMedsGreaterThanZero) {
                            medication.setDosesTaken(medication.getDosesTaken() + medication.getMedQuantity());

                            if (medication.getTotalMeds() >= medication.getMedQuantity()) {
                                medication.setTotalMeds(medication.getTotalMeds() - medication.getMedQuantity());
                            } else {
                                medication.setTotalMeds(0);
                                Toast.makeText(v.getContext(), "You are out of medication!", Toast.LENGTH_LONG).show();
                            }
                        }
                        getUpcomingTimes(medication, MAX_TIMES);
                        viewModel.update(medication);
                        notifyDataSetChanged();
                    })
                    .setNegativeButton("No", (dialog, which) -> {
                        // "No" does nothing
                        dialog.dismiss();
                    })
                    .setNeutralButton("Don't Show Again", ((dialog, which) -> {
                        // "Don't Show Again" prevents future pop-ups when exceeding dosage
                        prefs.edit().putBoolean("don't_show_exceed_dialog", true).apply();
                        dialog.dismiss();
                    }))
                    .show();
            }
        }
    }

    // ALWAYS USE MAX_TIMES for maxTimes OR VALUES MAY NOT DISPLAY CORRECTLY IN APP
    private List<LocalDateTime> getUpcomingTimes(Medication medication, int maxTimes) {
        List<LocalDateTime> upcomingTimes = new ArrayList<>();

        double maxDoses = medication.getMaxAmt();
        double dosesTaken = medication.getDosesTaken();
        double intervalHours = medication.getFrequency().getIntervalHours();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextTime = LocalDateTime.of(now.toLocalDate(), medication.getStartTime());

        // Skips to the next valid time
        while (!nextTime.isAfter(now)) {
            nextTime = nextTime.plusMinutes((long) (intervalHours * 60));
        }

        // Adds upcoming times while not exceeding max doses or max times
        while (upcomingTimes.size() < maxTimes && dosesTaken < maxDoses) {
            upcomingTimes.add(nextTime);
            nextTime = nextTime.plusMinutes((long) (intervalHours * 60));
        }
        return upcomingTimes;
    }
}