package com.example.mediscan

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore

class PatientDetailsActivity : AppCompatActivity() {

    private lateinit var etName: TextInputEditText
    private lateinit var etAge: TextInputEditText
    private lateinit var etGender: TextInputEditText
    private lateinit var etCondition: TextInputEditText
    private lateinit var etLastVisit: TextInputEditText
    private lateinit var etPhone: TextInputEditText
    private lateinit var etAddress: TextInputEditText
    private lateinit var etPriority: TextInputEditText
    private lateinit var etAllergies: TextInputEditText
    private lateinit var etHistory: TextInputEditText
    private lateinit var etInsurance: TextInputEditText
    private lateinit var etPolicy: TextInputEditText
    private lateinit var etNotes: TextInputEditText

    private lateinit var btnEditSave: MaterialButton
    private lateinit var btnDelete: MaterialButton
    private lateinit var ivFileImage: ImageView
    private lateinit var btnOpenPdf: MaterialButton

    private lateinit var db: FirebaseFirestore
    private lateinit var docId: String

    private var editMode = false
    private val editIcon = R.drawable.ic_edit

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_details)

        // ---------- Initialize views ----------
        etName = findViewById(R.id.et_name)
        etAge = findViewById(R.id.et_age)
        etGender = findViewById(R.id.et_gender)
        etCondition = findViewById(R.id.et_condition)
        etLastVisit = findViewById(R.id.et_last_visit)
        etPhone = findViewById(R.id.et_phone)
        etAddress = findViewById(R.id.et_address)
        etPriority = findViewById(R.id.et_priority)
        etAllergies = findViewById(R.id.et_allergies)
        etHistory = findViewById(R.id.et_history)
        etInsurance = findViewById(R.id.et_insurance)
        etPolicy = findViewById(R.id.et_policy)
        etNotes = findViewById(R.id.et_notes)

        btnEditSave = findViewById(R.id.btn_edit_save)
        btnDelete   = findViewById(R.id.btn_delete)
        ivFileImage = findViewById(R.id.patient_file_image)
        btnOpenPdf  = findViewById(R.id.btn_open_pdf)

        db = FirebaseFirestore.getInstance()
        docId = intent.getStringExtra("docId") ?: ""

        if (docId.isEmpty()) {
            Toast.makeText(this, "No patient document ID provided", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        loadPatientData()

        btnEditSave.setOnClickListener {
            if (editMode) saveChanges()
            else {
                setEditable(true)
                btnEditSave.text = "SAVE"
                btnEditSave.icon = null
                editMode = true
            }
        }

        btnDelete.setOnClickListener {
            confirmAndDelete()
        }
    }

    // ---------- Load patient info ----------
    private fun loadPatientData() {
        db.collection("patients").document(docId)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    etName.setText(snapshot.getString("name") ?: "")
                    etAge.setText(snapshot.getLong("age")?.toString() ?: "")
                    etGender.setText(snapshot.getString("gender") ?: "")
                    etCondition.setText(snapshot.getString("condition") ?: "")
                    etLastVisit.setText(snapshot.getString("lastVisit") ?: "")
                    etPhone.setText(snapshot.getString("phone") ?: "")
                    etAddress.setText(snapshot.getString("address") ?: "")
                    etPriority.setText(snapshot.getString("priority") ?: "")
                    etAllergies.setText(snapshot.getString("allergies") ?: "")
                    etHistory.setText(snapshot.getString("history") ?: "")
                    etInsurance.setText(snapshot.getString("insurance") ?: "")
                    etPolicy.setText(snapshot.getString("policy") ?: "")
                    etNotes.setText(snapshot.getString("notes") ?: "")

                    // ---------- NEW: show uploaded file ----------
                    val fileUrl  = snapshot.getString("fileUrl")
                    val fileType = snapshot.getString("fileType") // "image" or "pdf"

                    if (!fileUrl.isNullOrEmpty()) {
                        if (fileType == "image") {
                            ivFileImage.visibility = View.VISIBLE
                            btnOpenPdf.visibility  = View.GONE
                            Glide.with(this)
                                .load(fileUrl)
                                .placeholder(R.drawable.ic_image_placeholder) // optional placeholder
                                .into(ivFileImage)
                        } else if (fileType == "pdf") {
                            ivFileImage.visibility = View.GONE
                            btnOpenPdf.visibility  = View.VISIBLE
                            btnOpenPdf.setOnClickListener {
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    setDataAndType(Uri.parse(fileUrl), "application/pdf")
                                    flags = Intent.FLAG_ACTIVITY_NO_HISTORY
                                }
                                startActivity(intent)
                            }
                        }
                    }
                } else {
                    Toast.makeText(this, "Patient data not found", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setEditable(enable: Boolean) {
        etName.isEnabled = enable
        etAge.isEnabled = enable
        etGender.isEnabled = enable
        etCondition.isEnabled = enable
        etLastVisit.isEnabled = enable
        etPhone.isEnabled = enable
        etAddress.isEnabled = enable
        etPriority.isEnabled = enable
        etAllergies.isEnabled = enable
        etHistory.isEnabled = enable
        etInsurance.isEnabled = enable
        etPolicy.isEnabled = enable
        etNotes.isEnabled = enable
    }

    private fun saveChanges() {
        val updates = mapOf(
            "name"      to etName.text.toString().trim(),
            "age"       to (etAge.text.toString().trim().toIntOrNull() ?: 0),
            "gender"    to etGender.text.toString().trim(),
            "condition" to etCondition.text.toString().trim(),
            "lastVisit" to etLastVisit.text.toString().trim(),
            "phone"     to etPhone.text.toString().trim(),
            "address"   to etAddress.text.toString().trim(),
            "priority"  to etPriority.text.toString().trim(),
            "allergies" to etAllergies.text.toString().trim(),
            "history"   to etHistory.text.toString().trim(),
            "insurance" to etInsurance.text.toString().trim(),
            "policy"    to etPolicy.text.toString().trim(),
            "notes"     to etNotes.text.toString().trim()
        )

        db.collection("patients").document(docId)
            .update(updates)
            .addOnSuccessListener {
                Toast.makeText(this, "Details updated", Toast.LENGTH_SHORT).show()
                setEditable(false)
                btnEditSave.text = "EDIT DETAILS"
                btnEditSave.setIconResource(editIcon)
                editMode = false
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Update failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun confirmAndDelete() {
        AlertDialog.Builder(this)
            .setTitle("Delete Patient")
            .setMessage("Are you sure you want to delete this patient record?")
            .setPositiveButton("Delete") { _: DialogInterface, _: Int ->
                deletePatient()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deletePatient() {
        db.collection("patients").document(docId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Patient deleted", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Delete failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
