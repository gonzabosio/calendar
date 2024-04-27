package com.calendarapp.ui.nav

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import cafe.adriel.voyager.core.screen.Screen
import com.calendarapp.R
import com.calendarapp.ui.theme.CalendarAppTheme
import java.util.Calendar
import java.util.TimeZone

class MainScreen: Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            CalendarAppTheme {

                val scaffoldState = rememberBottomSheetScaffoldState()
                val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                val list = mutableListOf<Int>()
                for(i in 1..20) {
                    list.add(i)
                }
                val datePickerState = rememberDatePickerState(
                    yearRange = (calendar[Calendar.YEAR]-100..calendar[Calendar.YEAR]+100),
                    initialDisplayMode = DisplayMode.Picker
                )
                val timePickerState = rememberTimePickerState()
                var showTimePicker by remember {
                    mutableStateOf(false)
                }
                Log.d("Timestamp",
                    ((calendar[Calendar.YEAR]-100..calendar[Calendar.YEAR]+100).toString())
                )

                BottomSheetScaffold(
                    sheetPeekHeight = 160.dp,
                    scaffoldState = scaffoldState,
                    sheetContent = {
                        LazyColumn {
                            item {
                                Text(text = "Next Event", Modifier.padding(start = 16.dp, bottom = 8.dp))
                            }
                            items(list.size) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(8.dp)
                                        .height(50.dp)
                                ) {
                                    Text(text = "Event ${list[it]}", Modifier.padding(8.dp))
                                }
                            }
                        }
                    }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        DatePicker(
                            state = datePickerState,
                            title = null,
                            modifier = Modifier.padding(top = 24.dp)
                        )
                        Log.d("Timestamp:", "Selected date timestamp: ${datePickerState.selectedDateMillis ?: "no selection"}")
                        OutlinedButton(
                            onClick = {showTimePicker = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 8.dp, end = 8.dp)
                                .height(60.dp),
                            shape = RoundedCornerShape(15.dp)
                        ) {
                            Text(text = "Select hour", fontSize = 16.sp)
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(
                                painter = painterResource(id = R.drawable.ic_clock),
                                contentDescription = null,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        if(showTimePicker) {
                            Dialog(
                                onDismissRequest = { showTimePicker = false },
                                content = {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                        .background(color = MaterialTheme.colorScheme.background, shape = RoundedCornerShape(20.dp))
                                            .padding(top = 40.dp, bottom = 40.dp, start = 15.dp, end = 15.dp),

                                        ) {
                                        TimePicker(
                                            state = timePickerState,
                                        )
                                        Button(
                                            onClick = {
                                                showTimePicker = false
                                            },
                                            modifier = Modifier.fillMaxWidth()
                                                .height(50.dp).
                                                padding(top = 8.dp),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Text(text = "Accept", fontSize = 16.sp)
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}