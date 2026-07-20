package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.ViewModelProvider
import com.example.ui.TajrubahAppContent
import com.example.ui.TajrubahViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    val viewModel = ViewModelProvider(this)[TajrubahViewModel::class.java]

    setContent {
      MyApplicationTheme {
        // Listen for Toast messages from the ViewModel and display them as standard Android toasts
        LaunchedEffect(Unit) {
          viewModel.toastMessage.collect { msg ->
            Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
          }
        }

        TajrubahAppContent(viewModel = viewModel)
      }
    }
  }
}
