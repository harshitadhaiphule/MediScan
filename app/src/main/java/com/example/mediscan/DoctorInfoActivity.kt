package com.example.mediscan

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class DoctorInfoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doctor_info)

        // If you want a toolbar with back arrow:
        supportActionBar?.title = "Doctor Information"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
