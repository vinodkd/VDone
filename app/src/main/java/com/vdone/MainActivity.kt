package com.vdone

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.vdone.ui.theme.VDoneTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val repository = (application as VDoneApp).taskRepository
        setContent {
            VDoneTheme {
                VDoneNavHost(repository = repository)
            }
        }
    }
}
