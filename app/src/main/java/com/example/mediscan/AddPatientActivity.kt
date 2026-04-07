package com.example.mediscan

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.*

class AddPatientActivity : AppCompatActivity() {

    // --- Patient form fields ---
    private lateinit var etFullName: TextInputEditText
    private lateinit var etDob: TextInputEditText
    private lateinit var etPhone: TextInputEditText
    private lateinit var etGender: TextInputEditText
    private lateinit var etAddress: TextInputEditText
    private lateinit var etCondition: TextInputEditText
    private lateinit var etLastVisit: TextInputEditText
    private lateinit var etAllergies: TextInputEditText
    private lateinit var etHistory: TextInputEditText
    private lateinit var etInsurance: TextInputEditText
    private lateinit var etPolicy: TextInputEditText
    private lateinit var etNotes: TextInputEditText

    private lateinit var btnScan: MaterialButton
    private lateinit var btnRegister: MaterialButton

    // --- Firebase ---
    private val db = FirebaseFirestore.getInstance()
    private val storageRef = FirebaseStorage.getInstance().reference

    private var patientDocId: String? = null   // Firestore doc id for this patient

    // --- Camera & PDF launchers ---
    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val photoUri = result.data!!.data
            if (photoUri != null) uploadFileToStorage(photoUri, "image")
        }
    }

    private val pdfLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val pdfUri = result.data!!.data
            if (pdfUri != null) uploadFileToStorage(pdfUri, "pdf")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_patient)

        // Bind views
        etFullName = findViewById(R.id.et_full_name)
        etDob = findViewById(R.id.et_dob)
        etPhone = findViewById(R.id.et_phone)
        etGender = findViewById(R.id.et_gender)
        etAddress = findViewById(R.id.et_address)
        etCondition = findViewById(R.id.et_condition)
        etLastVisit = findViewById(R.id.et_last_visit)
        etAllergies = findViewById(R.id.et_allergies)
        etHistory = findViewById(R.id.et_history)
        etInsurance = findViewById(R.id.et_insurance)
        etPolicy = findViewById(R.id.et_policy)
        etNotes = findViewById(R.id.et_notes)

        btnScan = findViewById(R.id.scan_documents_button)
        btnRegister = findViewById(R.id.register_button)

        btnScan.setOnClickListener { showScanDialog() }
        btnRegister.setOnClickListener {
            if (validateFields()) {
                savePatientToFirestore()
            }
        }
    }

    // -------- Document scanning / uploading --------

    private fun showScanDialog() {
        val options = arrayOf("Scan using Camera", "Attach PDF")
        AlertDialog.Builder(this)
            .setTitle("Upload Document")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openCamera()
                    1 -> openPdfPicker()
                }
            }.show()
    }

    private fun openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 100)
        } else {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            cameraLauncher.launch(intent)
        }
    }

    private fun openPdfPicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "application/pdf"
        pdfLauncher.launch(intent)
    }

    private fun uploadFileToStorage(uri: Uri, type: String) {
        if (patientDocId == null) {
            // Create a patient doc ID if not saved yet
            patientDocId = db.collection("patients").document().id
        }

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = if (type == "image") "scan_$timeStamp.jpg" else "document_$timeStamp.pdf"

        val fileRef = storageRef.child("patient_docs/$patientDocId/$fileName")
        val uploadTask = fileRef.putFile(uri)

        uploadTask.addOnSuccessListener {
            fileRef.downloadUrl.addOnSuccessListener { downloadUri ->
                saveDocumentReference(downloadUri.toString(), type)
            }
            Toast.makeText(this, "Upload Successful", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(this, "Upload Failed: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveDocumentReference(downloadUrl: String, type: String) {
        val docData = mapOf(
            "url" to downloadUrl,
            "type" to type,
            "uploadedAt" to System.currentTimeMillis()
        )
        db.collection("patients")
            .document(patientDocId!!)
            .collection("documents")
            .add(docData)
    }

    // -------- Save patient info --------

    private fun savePatientToFirestore() {
        val age = 0 // Replace with age calculation if desired

        val patient = Patient(
            name = etFullName.text.toString().trim(),
            age = age,
            condition = etCondition.text.toString().trim(),
            lastVisit = etLastVisit.text.toString().trim(),
            phone = etPhone.text.toString().trim(),
            address = etAddress.text.toString().trim(),
            priority = "",
            gender = etGender.text.toString().trim(),
            allergies = etAllergies.text.toString().trim(),
            history = etHistory.text.toString().trim(),
            insurance = etInsurance.text.toString().trim(),
            policy = etPolicy.text.toString().trim(),
            notes = etNotes.text.toString().trim()
        )

        if (patientDocId == null) {
            // If no doc exists yet, add a new one
            db.collection("patients")
                .add(patient)
                .addOnSuccessListener { docRef ->
                    patientDocId = docRef.id
                    Toast.makeText(this, "Patient Registered Successfully", Toast.LENGTH_LONG).show()
                    clearFields()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
        } else {
            // If we already created the doc for uploading files
            db.collection("patients").document(patientDocId!!)
                .set(patient)
                .addOnSuccessListener {
                    Toast.makeText(this, "Patient Registered Successfully", Toast.LENGTH_LONG).show()
                    clearFields()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }

    // -------- Validation & clearing --------

    private fun validateFields(): Boolean {
        if (etFullName.text.isNullOrEmpty()) { etFullName.error = "Full Name is required"; etFullName.requestFocus(); return false }
        if (etDob.text.isNullOrEmpty())      { etDob.error = "Date of Birth is required"; etDob.requestFocus(); return false }
        if (etPhone.text.isNullOrEmpty())    { etPhone.error = "Phone Number is required"; etPhone.requestFocus(); return false }
        if (etCondition.text.isNullOrEmpty()){ etCondition.error = "Primary Condition is required"; etCondition.requestFocus(); return false }
        return true
    }

    private fun clearFields() {
        etFullName.text?.clear()
        etDob.text?.clear()
        etPhone.text?.clear()
        etGender.text?.clear()
        etAddress.text?.clear()
        etCondition.text?.clear()
        etLastVisit.text?.clear()
        etAllergies.text?.clear()
        etHistory.text?.clear()
        etInsurance.text?.clear()
        etPolicy.text?.clear()
        etNotes.text?.clear()
    }
}
