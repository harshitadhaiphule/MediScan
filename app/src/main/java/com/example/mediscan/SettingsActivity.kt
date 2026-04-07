package com.example.mediscan

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.switchmaterial.SwitchMaterial

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val darkModeSwitch = findViewById<SwitchMaterial>(R.id.dark_mode_switch)
        val remindersSwitch = findViewById<SwitchMaterial>(R.id.reminders_switch)
        val biometricsSwitch = findViewById<SwitchMaterial>(R.id.biometrics_switch)

        // Set listeners for each switch
        darkModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                Toast.makeText(this, "Dark mode enabled.", Toast.LENGTH_SHORT).show()
                // Change app theme to dark mode here
            } else {
                Toast.makeText(this, "Dark mode disabled.", Toast.LENGTH_SHORT).show()
                // Change app theme to light mode here
            }
        }

        remindersSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                Toast.makeText(this, "Appointment reminders enabled.", Toast.LENGTH_SHORT).show()
                // Enable notification service
            } else {
                Toast.makeText(this, "Appointment reminders disabled.", Toast.LENGTH_SHORT).show()
                // Disable notification service
            }
        }

        biometricsSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                Toast.makeText(this, "Biometric login enabled.", Toast.LENGTH_SHORT).show()
                // Prompt user for biometric enrollment
            } else {
                Toast.makeText(this, "Biometric login disabled.", Toast.LENGTH_SHORT).show()
                // Disable biometric login
            }
        }
    }
}