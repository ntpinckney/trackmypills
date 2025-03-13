package com.example.trackmypills;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class MedicationAdapter extends RecyclerView.Adapter<MedicationAdapter.MedicationViewHolder> {
    private List<Medication> medications;
    private OnMedicationClickListener listener; // Callback for item clicks
    private static MedicationDao medicationDao;

    public interface OnMedicationClickListener {
        void onMedicationClick(Medication medication);
    }

    public MedicationAdapter(List<Medication> medications,
                             MedicationDao medicationDao) {
        this.medications = medications;
        this.listener = listener;
        this.medicationDao = medicationDao;
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
        holder.bind(medication, listener);
    }

    @Override
    public int getItemCount() {
        return medications == null ? 0 : medications.size();
    }

    class MedicationViewHolder extends RecyclerView.ViewHolder {
        TextView medName, medTime, medDosage;

        public MedicationViewHolder(@NonNull View itemView) {
            super(itemView);
            medName = itemView.findViewById(R.id.med_name);
            medTime = itemView.findViewById(R.id.med_time);
            medDosage = itemView.findViewById(R.id.med_dosage);
        }

        public void bind(Medication medication, OnMedicationClickListener listener) {
            medName.setText(medication.getName());

            String formattedTime = medication.getNextDosageTime().format(DateTimeFormatter.ofPattern("HH:mm")); // 24-hour format
            medTime.setText(formattedTime);
            medDosage.setText(String.format("%d/%d taken", medication.getDosesTaken(), medication.getMaxAmt()));

            itemView.setOnClickListener(v -> {
                if (medication.getDosesTaken() < medication.getMaxAmt()) {
                    medication.setDosesTaken(medication.getDosesTaken() + 1);
                    medicationDao.updateMedication(medication);
                    notifyItemChanged(getAdapterPosition()); // Refreshes the UI
                } else {
                    Toast.makeText(v.getContext(), "Max dose reached", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
