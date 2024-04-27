package com.calendarapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.platform.LocalContext
import cafe.adriel.voyager.navigator.Navigator
import com.calendarapp.ui.nav.MainScreen
import com.calendarapp.viewmodel.EventDataViewModel

class MainActivity : ComponentActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Navigator(MainScreen(EventDataViewModel(), LocalContext.current))
        }
    }
}