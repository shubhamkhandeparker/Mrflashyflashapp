package com.example.mrflashyflashapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.mrflashyflashapp.ui.theme.MRFlashyflashAppTheme
import com.example.mrflashyflashapp.ui.screens.FlashlightScreen
import com.example.mrflashyflashapp.data.manager.PermissionManager


class MainActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MRFlashyflashAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = colorScheme.background
                ) {
                    FlashlightScreen()

                }
            }
        }
    }
}