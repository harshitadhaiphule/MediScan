package com.example.mediscan

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MenuItem
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.util.Locale

class DashboardActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle

    private lateinit var firestore: FirebaseFirestore
    private lateinit var patientAdapter: PatientAdapter
    private val patientList = mutableListOf<Patient>()
    private val filteredList = mutableListOf<Patient>()
    private var listenerRegistration: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        // Toolbar
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Drawer & Navigation
        drawerLayout = findViewById(R.id.drawerLayout)
        val navigationView: NavigationView = findViewById(R.id.navigationView)

        toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navigationView.setNavigationItemSelectedListener { menuItem: MenuItem ->
            when (menuItem.itemId) {
                R.id.nav_patients -> {
                    Toast.makeText(this, "You are already on Patients page", Toast.LENGTH_SHORT).show()
                }
                R.id.nav_doctor_info -> startActivity(Intent(this, DoctorInfoActivity::class.java))
                R.id.nav_hospital_info -> startActivity(Intent(this, HospitalInfoActivity::class.java))
                R.id.nav_add_patient -> startActivity(Intent(this, AddPatientActivity::class.java))
                R.id.nav_settings -> startActivity(Intent(this, SettingsActivity::class.java))
            }
            drawerLayout.closeDrawers()
            true
        }

        // Greeting & Photo (optional)
        val greetingText: TextView = findViewById(R.id.txtGreeting)
        val docPhoto: ImageView = findViewById(R.id.imgDoctor)

        // RecyclerView
        val patientRecyclerView: RecyclerView = findViewById(R.id.patientRecyclerView)
        patientRecyclerView.layoutManager = LinearLayoutManager(this)
        patientAdapter = PatientAdapter(filteredList)
        patientRecyclerView.adapter = patientAdapter

        // Firestore
        firestore = FirebaseFirestore.getInstance()
        listenerRegistration = firestore.collection("patients")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }
                val docs = snapshots ?: return@addSnapshotListener
                patientList.clear()
                for (doc in docs) {
                    val patient = doc.toObject(Patient::class.java)
                    if (patient != null) {
                        patientList.add(patient.copy(id = doc.id))
                        Log.d("Dashboard", "Patient loaded: ${patient.name}, id: ${doc.id}")
                    } else {
                        Log.e("Dashboard", "Failed to parse patient document: ${doc.id}")
                    }
                }
                filteredList.clear()
                filteredList.addAll(patientList)
                patientAdapter.notifyDataSetChanged()
            }

        // Search
        val searchBar: EditText = findViewById(R.id.searchBar)
        searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterPatients(s?.toString().orEmpty())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filterPatients(query: String) {
        val lower = query.lowercase(Locale.ROOT)
        filteredList.clear()
        if (lower.isEmpty()) {
            filteredList.addAll(patientList)
        } else {
            filteredList.addAll(
                patientList.filter { patient ->
                    patient.name?.lowercase(Locale.ROOT)?.contains(lower) == true ||
                            patient.condition?.lowercase(Locale.ROOT)?.contains(lower) == true ||
                            patient.phone?.lowercase(Locale.ROOT)?.contains(lower) == true
                }
            )
        }
        patientAdapter.notifyDataSetChanged()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (toggle.onOptionsItemSelected(item)) true
        else super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        listenerRegistration?.remove()
    }
}
