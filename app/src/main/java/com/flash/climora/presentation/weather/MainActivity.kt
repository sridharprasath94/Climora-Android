package com.flash.climora.presentation.weather

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.flash.climora.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, WeatherFragment())
                .commit()
        }
    }
}
