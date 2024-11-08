package com.example.personalhealthcareapplication.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.personalhealthcareapplication.R;
import com.example.personalhealthcareapplication.model.Appointment;

import java.util.List;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.AppointmentViewHolder> {

    private List<Appointment> appointments;
    private OnEditClickListener onEditClickListener;
    private OnDeleteClickListener onDeleteClickListener;

    public AppointmentAdapter(List<Appointment> appointments, OnEditClickListener editClickListener, OnDeleteClickListener deleteClickListener) {
        this.appointments = appointments;
        this.onEditClickListener = editClickListener;
        this.onDeleteClickListener = deleteClickListener;
    }

    @NonNull
    @Override
    public AppointmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_appointment, parent, false);
        return new AppointmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppointmentViewHolder holder, int position) {
        Appointment appointment = appointments.get(position);
        holder.tvDoctorName.setText(appointment.getDoctorName());
        holder.tvAppointmentDate.setText("Date: " + appointment.getDate());
        holder.tvAppointmentTime.setText("Time: " + appointment.getTime());

        // Set edit and delete click listeners
        holder.itemView.findViewById(R.id.btnEdit).setOnClickListener(v -> {
            if (onEditClickListener != null) {
                onEditClickListener.onEditClick(appointment);
            }
        });

        holder.itemView.findViewById(R.id.btnDelete).setOnClickListener(v -> {
            if (onDeleteClickListener != null) {
                onDeleteClickListener.onDeleteClick(appointment);
            }
        });
    }

    @Override
    public int getItemCount() {
        return appointments.size();
    }

    static class AppointmentViewHolder extends RecyclerView.ViewHolder {
        TextView tvDoctorName, tvAppointmentDate, tvAppointmentTime;

        public AppointmentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDoctorName = itemView.findViewById(R.id.tvDoctorName);
            tvAppointmentDate = itemView.findViewById(R.id.tvAppointmentDate);
            tvAppointmentTime = itemView.findViewById(R.id.tvAppointmentTime);
        }
    }

    // Interfaces for edit and delete actions
    public interface OnEditClickListener {
        void onEditClick(Appointment appointment);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(Appointment appointment);
    }
}
