package com.example.trackmypills.ui.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.AutoTransition;
import androidx.transition.ChangeBounds;
import androidx.transition.Transition;
import androidx.transition.TransitionManager;

import com.example.trackmypills.util.DoseManager;
import com.example.trackmypills.models.Frequency;
import com.example.trackmypills.models.Medication;
import com.example.trackmypills.ui.activity.EditMedication;
import com.example.trackmypills.util.NotifUtil;
import com.example.trackmypills.viewmodel.MedicationViewModel;
import com.example.trackmypills.R;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MedicationAdapter extends RecyclerView.Adapter<MedicationAdapter.MedicationViewHolder> {
    private List<Medication> medications;
    private final OnMedicationClickListener listener; // Callback for item clicks
    private final MedicationViewModel viewModel;
    private Context context;
    private int maxTimes; // Maximum number of scheduled times to display
    private int expandedPosition = -1; // Tracks which item is expanded
    private int previousExpandedPosition = -1; // Tracks previous expanded item



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
    public void onBindViewHolder(@NonNull MedicationViewHolder holder, int position) {
        Medication medication = medications.get(position);
        holder.bind(medication, listener); // Assigns binding logic to bind()
    }

    @Override
    public int getItemCount() {
        return medications == null ? 0 : medications.size();
    }

    class MedicationViewHolder extends RecyclerView.ViewHolder {
        TextView medNameTextView, medTimeTextView, missedTimesView, medDosageTextView, totalMedsTextView;
        ImageButton expandButton, takeButton, refillButton, undoButton, editButton, deleteButton;
        LinearLayout expandableView;


        public MedicationViewHolder(@NonNull View itemView) {
            super(itemView);

            // Main interface display
            medNameTextView = itemView.findViewById(R.id.med_name);
            medTimeTextView = itemView.findViewById(R.id.med_time);
            medDosageTextView = itemView.findViewById(R.id.med_dosage);
            totalMedsTextView = itemView.findViewById(R.id.total_meds);
            missedTimesView = itemView.findViewById(R.id.missed_time);
            expandButton = itemView.findViewById(R.id.expand_button);

            // Expandable view interface display
            expandableView = itemView.findViewById(R.id.expandable_view);
            takeButton = itemView.findViewById(R.id.take_button);
            refillButton = itemView.findViewById(R.id.refill_button);
            undoButton = itemView.findViewById(R.id.undo_button);
            editButton = itemView.findViewById(R.id.edit_button);
            deleteButton = itemView.findViewById(R.id.delete_button);

        }

        public void bind(Medication medication, OnMedicationClickListener listener) {
            medNameTextView.setText(medication.getName());
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime nextDosageTime = medication.getNextDosageTime();


            List<LocalDateTime> missed = medication.getMissedDosages();
            if(missed != null && !missed.isEmpty()){
                // Shows only the first three missed times
                List<String> displayTimes = missed.stream()
                        .sorted()
                        .limit(3)
                        .map(time -> time.format(DateTimeFormatter.ofPattern("h:mm a")))
                        .collect(Collectors.toList());

                missedTimesView.setText(String.format("Missed reminders: %s", String.join(", ", displayTimes)));
                missedTimesView.setVisibility(View.VISIBLE);
            } else {
                missedTimesView.setVisibility(View.INVISIBLE);
            }


            // Checks if item is expanded based on global position
            boolean isExpanded = getAdapterPosition() == expandedPosition;
            expandableView.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
            expandableView.clearAnimation(); // Removes leftover animation

            expandButton.setRotation(isExpanded ? 90f : 0f);
            expandButton.clearAnimation(); // Removes leftover animation


            expandButton.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position == RecyclerView.NO_POSITION) return;

                if (expandedPosition == position) {
                    // Collapses current item
                    previousExpandedPosition = expandedPosition;
                    expandedPosition = -1;

                    // Smooth collapse transition
                    TransitionManager.beginDelayedTransition((ViewGroup) itemView, new AutoTransition());


                    // Hides expandable content
                    expandableView.setVisibility(View.GONE);

                    // Animates rotation button to position zero
                    expandButton.animate()
                            .rotation(0f)
                            .setDuration(200)
                            .setInterpolator(new AccelerateDecelerateInterpolator())
                            .start();

                } else {
                    previousExpandedPosition = expandedPosition;
                    expandedPosition = position;

                    // Collapses previous item
                    if (previousExpandedPosition >= 0) {
                        notifyItemChanged(previousExpandedPosition);
                    }

                    // Smooth expand transition
                    TransitionManager.beginDelayedTransition((ViewGroup) itemView, new AutoTransition());

                    // Makes the expandableView trigger and rotates button to 90
                    expandableView.setVisibility(View.VISIBLE);
                    expandButton.animate()
                            .rotation(90f)
                            .setDuration(200)
                            .setInterpolator(new AccelerateDecelerateInterpolator())
                            .start();
                }
            });

            // Validates medQuantity and maxAmt before it sets the texts
            if (medication.getMedQuantity() < 0.01) {
                medication.setMedQuantity(0.01); // Automatically sets it to 0.01 if number is less than 0.01
            }

            if (medication.getMaxAmt() < 0.01) {
                medication.setMaxAmt(0.01); // Automatically sets it to 0.01 if number is less than 0.01
            }

            DoseManager doseManager = new DoseManager(viewModel);

            takeButton.setOnClickListener(v -> {
                SharedPreferences prefs = v.getContext().getSharedPreferences("ExceedPrefs", Context.MODE_PRIVATE);
                boolean dontShowAgain = prefs.getBoolean("don't_show_exceed_dialog", false);
                boolean dosesTakenLessThanMaxAmt = medication.getDosesTaken() < medication.getMaxAmt();
                boolean totalMedsGreaterThanZero = medication.getTotalMeds() > 0;

                boolean allowExceedMax = !dontShowAgain && medication.getDosesTaken() < medication.getMaxAmt();


                DoseManager.DoseResult result = doseManager.takeDose(
                        medication,
                        totalMedsGreaterThanZero,
                        dosesTakenLessThanMaxAmt,
                        dontShowAgain,
                        prefs,
                        allowExceedMax
                );

                // Handles the results of taking doses
                switch (result) {
                    case SUCCESS:
                        // Updates the medication data and notifies the adadpter
                        viewModel.update(medication);
                        notifyItemChanged(getAdapterPosition());
                        break;

                        case OUT_OF_MEDICATION:
                            Toast.makeText(v.getContext(), "No more medication left!", Toast.LENGTH_SHORT).show();
                            break;

                    case MAX_DOSE_REACHED:
                        new AlertDialog.Builder(v.getContext())
                                .setTitle("Max Dose Reached")
                                .setMessage("You have already taken the maximum dosage. Do you want to exceed it?")
                                .setPositiveButton("Yes", (dialog, which) -> {
                                    // Allows exceeding the max dose
                                    doseManager.takeDose(medication, totalMedsGreaterThanZero, dosesTakenLessThanMaxAmt, dontShowAgain, prefs, true);
                                    viewModel.update(medication);
                                    notifyItemChanged(getAdapterPosition());
                                })
                                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                                .setNeutralButton("Don't Show Again", (dialog, which) -> {
                                    // Prevents future pop-ups for exceeding dosage
                                    doseManager.suppressMaxDoseDialog(prefs);
                                    dialog.dismiss();
                                })
                                .show();
                        break;

                    case MAX_DOSE_CONFIRMED:
                        viewModel.update(medication);
                        notifyItemChanged(getAdapterPosition());
                        break;
                }
            });


            refillButton.setOnClickListener(v -> {

                Context context = v.getContext();

                EditText input = new EditText(context);
                input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
                input.setHint("Enter refill amount");

                new AlertDialog.Builder(context)
                        .setTitle("Refill Medication")
                        .setMessage("Enter the amount of medication to refill:")
                        .setView(input)
                        .setPositiveButton("Refill", (dialog, which) -> {
                            String entered = input.getText().toString().trim();
                            if (!entered.isEmpty()) {
                                try {
                                    double refillAmount = Double.parseDouble(entered);
                                    if(refillAmount > 0 ) {
                                        if(refillAmount >= 1000 ) {
                                            Toast.makeText(context, "Refill amount too large", Toast.LENGTH_SHORT).show();
                                            return;
                                        }
                                        doseManager.refillTotalMeds(medication, refillAmount);
                                        Toast.makeText(context, "Refill successful", Toast.LENGTH_SHORT).show();
                                        notifyItemChanged(getAdapterPosition());
                                    } else {
                                        Toast.makeText(context, "Enter a positive number", Toast.LENGTH_SHORT).show();
                                    }
                                } catch (NumberFormatException e) {
                                    Toast.makeText(context, "Invalid input", Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                        .show();
            });


            double initialTotalMeds = medication.getTotalMeds(); // Establishes what the totalMeds are before listener

            // Undoes medication taken in case of mistakes
            undoButton.setOnClickListener(v -> {
                DoseManager.DoseResult result = doseManager.undoDose(medication);

                if (result == DoseManager.DoseResult.UNDO_SUCCESS) {
                    notifyItemChanged(getAdapterPosition());
                } else {
                    Toast.makeText(v.getContext(), "No doses to undo", Toast.LENGTH_SHORT).show();
                }
            });

            // Takes user to edit page
            editButton.setOnClickListener(v -> {
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

            double intervalHours = medication.getFrequency().getIntervalHours();

            // Determines which times to show
            boolean canShowFourTimes =
                    Frequency.THIRTY_MINUTES.getIntervalHours() == intervalHours ||
                            Frequency.ONE_HOUR.getIntervalHours() == intervalHours ||
                            Frequency.TWO_HOURS.getIntervalHours() == intervalHours ||
                            Frequency.FOUR_HOURS.getIntervalHours() == intervalHours ||
                            Frequency.SIX_HOURS.getIntervalHours() == intervalHours;

            boolean canShowThreeTimes =
                    Frequency.EIGHT_HOURS.getIntervalHours() == intervalHours ||
                            Frequency.TEN_HOURS.getIntervalHours() == intervalHours;

            boolean canShowTwoTimes =
                    Frequency.TWELVE_HOURS.getIntervalHours() == intervalHours;

            // Changes times shown based on interval
            if (intervalHours == 0) {
                medTimeTextView.setText("No reminders set.");
                maxTimes = 0;
            } else if (canShowFourTimes) {
                maxTimes = 4;
            } else if (canShowThreeTimes) {
                maxTimes = 3;
            } else if (canShowTwoTimes) {
                maxTimes = 2;
            } else {
                maxTimes = 1; // Shows only one time if interval is set to every 24 hours
            }

            // Gets up to maxTimes upcoming times
            List<LocalDateTime> upcomingTimes = getUpcomingTimes(medication, maxTimes);



            // Formats time
            if (upcomingTimes.isEmpty()) {
                medTimeTextView.setText("No more reminders today.");
            } else {
                SpannableStringBuilder timeDisplay = new SpannableStringBuilder("Upcoming reminder(s):\n");
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:mm a");
                LocalDate today = LocalDate.now(); // For day-to-day comparison

                for (int i = 0; i < upcomingTimes.size(); i++) {
                    String formattedTime = upcomingTimes.get(i).toLocalTime().format(formatter);
                    int start = timeDisplay.length();

                    // Checks if the upcoming time is for the next day
                    if (upcomingTimes.get(i).toLocalDate().isAfter(today)) {
                        formattedTime += " (Next day)";
                    }
                    timeDisplay.append(formattedTime);
                    int end = timeDisplay.length();

                    if (i == 0) {
                        // If is the next upcoming time, make the time bold
                        timeDisplay.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    } else {
                        // All other times will be dimmed
                        int dimmedColor = ContextCompat.getColor(itemView.getContext(), R.color.text_transparent);
                        ForegroundColorSpan span = new ForegroundColorSpan(dimmedColor);
                        timeDisplay.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }

                    // Adds comma if there are more times, but doesn't add it to the last time
                    if (i < upcomingTimes.size() - 1) {
                        timeDisplay.append(", ");
                    }
                }

                // Tells user when the next reminders are
                medTimeTextView.setText(timeDisplay);
            }



            // Informs users how many doses have been taken
            medDosageTextView.setText(String.format("%.2f/%.2f %s taken", medication.getDosesTaken(), medication.getMaxAmt(),
                    medication.getAdminType().getLabel()));

            // Tells user how many pills remain in total
            totalMedsTextView.setText(String.format("%.2f %s remaining", medication.getTotalMeds(),
                    medication.getAdminType().getLabel()));

        }

        private List<LocalDateTime> getUpcomingTimes(Medication medication, int maxTimes) {
            List<LocalDateTime> upcomingTimes = new ArrayList<>();

            double maxDoses = medication.getMaxAmt();
            double dosesTaken = medication.getDosesTaken();
            double intervalHours = medication.getFrequency().getIntervalHours();

            // Gets the current date and time
            LocalDateTime now = LocalDateTime.now();

            // Gets the next time based on start time
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
}