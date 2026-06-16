package com.Infusory.task

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import com.Infusory.task.ui.theme._3DtaskTheme
import com.Infusory.task.ui.viewer.ModelViewerApp

/**
 * Built by Kalyani Meshram.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            _3DtaskTheme {
                ModelViewerApp(modifier = Modifier.fillMaxSize())
            }
        }
    }
}
