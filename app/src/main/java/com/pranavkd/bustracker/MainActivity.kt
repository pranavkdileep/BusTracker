package com.pranavkd.bustracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.pranavkd.bustracker.ui.theme.BusTrackerTheme
import androidx.lifecycle.ViewModelProvider
import com.pranavkd.bustracker.ChatLogic.ChatViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        val viewmodel = ViewModelProvider(this).get(ChatViewModel::class.java)
        super.onCreate(savedInstanceState)
        setContent {
            BusTrackerTheme {
                NaveMain(viewmodel)
            }
        }
    }
}
