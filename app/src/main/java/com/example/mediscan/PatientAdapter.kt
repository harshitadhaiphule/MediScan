package com.example.mediscan

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class PatientAdapter(private val patients: MutableList<Patient>)
    : RecyclerView.Adapter<PatientAdapter.PatientViewHolder>() {

    inner class PatientViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val patientImage: ImageView = itemView.findViewById(R.id.patientImage)
        val patientName: TextView = itemView.findViewById(R.id.patientName)
        val patientAge: TextView = itemView.findViewById(R.id.patientAge)
        val patientCondition: TextView = itemView.findViewById(R.id.patientCondition)
        val patientLastVisit: TextView = itemView.findViewById(R.id.patientLastVisit)
        val patientPhone: TextView = itemView.findViewById(R.id.patientPhone)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PatientViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_patient, parent, false)
        return PatientViewHolder(view)
    }

    override fun onBindViewHolder(holder: PatientViewHolder, position: Int) {
        val patient = patients[position]
        holder.patientName.text = patient.name ?: ""
        holder.patientAge.text = "Age: ${patient.age ?: ""}"
        holder.patientCondition.text = "Condition: ${patient.condition ?: ""}"
        holder.patientLastVisit.text = "Last visit: ${patient.lastVisit ?: ""}"
        holder.patientPhone.text = "Phone: ${patient.phone ?: ""}"
        holder.patientImage.setImageResource(R.drawable.ic_action_profile)

        holder.itemView.setOnClickListener { view ->
            if (!patient.id.isNullOrEmpty()) {
                val intent = Intent(view.context, PatientDetailsActivity::class.java)
                intent.putExtra("docId", patient.id)
                view.context.startActivity(intent)
            } else {
                Toast.makeText(view.context, "Patient ID not found", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun getItemCount(): Int = patients.size
}
