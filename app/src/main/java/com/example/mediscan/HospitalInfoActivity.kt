package com.example.mediscan

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class HospitalInfoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // This is the crucial line that links the Kotlin code to the XML layout file
        setContentView(R.layout.activity_hospital_info)
    }
}