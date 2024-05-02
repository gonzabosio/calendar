package com.calendarapp.ui.nav

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import cafe.adriel.voyager.core.screen.Screen
import com.calendarapp.R
import com.calendarapp.data.Event
import com.calendarapp.notif.NotificationService
import com.calendarapp.ui.EventItem
import com.calendarapp.ui.theme.CalendarAppTheme
import com.calendarapp.viewmodel.EventDataViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.TimeZone

class MainScreen(
    private val eventVM: EventDataViewModel,
    private val context: Context,
    private val thisContext: Context
) : Screen {
    @SuppressLint("CoroutineCreationDuringComposition", "SimpleDateFormat",
        "StateFlowValueCalledInComposition", "DefaultLocale"
    )
    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            CalendarAppTheme {
                var hasNotificationPermission by remember {
                    mutableStateOf(false)
                }
                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission(),
                    onResult = { isGranted ->
                        hasNotificationPermission = isGranted
                    }
                )
                LaunchedEffect(key1 = true) {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                val modifier: Modifier = Modifier
                val scaffoldState = rememberBottomSheetScaffoldState()
                val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC-3"))
                val datePickerState = rememberDatePickerState(
                    yearRange = (calendar[Calendar.YEAR] - 100..calendar[Calendar.YEAR] + 100),
                    initialDisplayMode = DisplayMode.Picker
                )
                val timePickerState = rememberTimePickerState()
                var showTimePicker by remember {
                    mutableStateOf(false)
                }
                var myDate by remember {
                    mutableStateOf("")
                }
                var myTime by remember {
                    mutableStateOf("00:00")
                }
                var title by remember {
                    mutableStateOf("")
                }
                val coroutineScope = rememberCoroutineScope()
                val events by eventVM.listOfEvents.collectAsState()
                var showEventAdder by remember {
                    mutableStateOf(false)
                }
                var showEventEditor by remember {
                    mutableStateOf(false)
                }
                var eventItem by remember {
                    mutableStateOf(Event())
                }
                val dateSuppText by eventVM.dateSuppText.collectAsState()
                val canConfirm by eventVM.confirmButton.collectAsState()
                val titleSuppText by eventVM.titleSuppText.collectAsState()

                val scheduler = NotificationService(context)
                BottomSheetScaffold(
                    sheetPeekHeight = 190.dp,
                    scaffoldState = scaffoldState,
                    sheetContent = {
                        LazyColumn {
                            item {
                                Text(
                                    text = "Next Event",
                                    Modifier.padding(start = 24.dp, bottom = 6.dp)
                                )
                            }
                            items(events) {event->
                                EventItem(name = event.title, hour = event.hour, date = event.date, event, eventVM, thisContext,
                                    onUpdateClick = {
                                        coroutineScope.launch {
                                            eventItem = event
                                            myTime = event.hour
                                            myDate = event.date
                                            title = event.title
                                            showEventEditor = true
                                        }
                                    }
                                )
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
                            modifier = Modifier.padding(top = 24.dp),
                        )
                        Spacer(modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 8.dp, end = 8.dp, bottom = 16.dp)
                            .height(1.dp)
                            .background(color = colorResource(id = R.color.card)))
                        if (showTimePicker) {
                            Dialog(
                                onDismissRequest = { showTimePicker = false },
                                content = {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .background(
                                                color = MaterialTheme.colorScheme.background,
                                                shape = RoundedCornerShape(20.dp)
                                            )
                                            .padding(
                                                top = 40.dp,
                                                bottom = 40.dp,
                                                start = 15.dp,
                                                end = 15.dp
                                            ),

                                        ) {
                                        TimePicker(
                                            state = timePickerState
                                        )
                                        Button(
                                            onClick = {
                                                val formattedHour = String.format("%02d", timePickerState.hour)
                                                val formattedMinute = String.format("%02d", timePickerState.minute)
                                                myTime = "$formattedHour:$formattedMinute"
                                                showTimePicker = false
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(50.dp)
                                                .padding(top = 8.dp),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Text(text = "Accept", fontSize = 16.sp)
                                        }
                                    }
                                }
                            )
                        }
                        Row(
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            ExtendedFloatingActionButton(
                                onClick = {
                                    showEventAdder = true
                                    myDate = millisToYearsMonthsDays(datePickerState.selectedDateMillis)
                                },
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .padding(16.dp)
                                    .height(70.dp)
                                    .width(170.dp)
                                    .fillMaxHeight()
                            ) {
                                Text(text = "Add Event", modifier.padding(end = 8.dp))
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    modifier.size(30.dp)
                                )
                            }

                        }
                    if (showEventAdder) {
                        eventVM.adviceForTitleText(title)
                        Dialog(
                            onDismissRequest = {
                                showEventAdder = false
                                title = ""
                            },
                            content = {
                                Column(
                                    modifier = Modifier
                                        .background(
                                            color = MaterialTheme.colorScheme.background,
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .fillMaxWidth()
                                        .height(270.dp)
                                        .padding(top = 18.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    OutlinedTextField(
                                        value = title,
                                        onValueChange = {
                                            title = it
                                            eventVM.adviceForTitleText(title)
                                        },
                                        label = {
                                            if(myDate.length == 8) {
                                                Text(
                                                    text = "Event ${numberToMonth(myDate)} ${myDate[0]}, ${myDate.slice(4..7)}"
                                                )
                                            }
                                            else if(myDate.length == 9 && myDate[1] == '-') {
                                                Text(
                                                    text = "Event ${numberToMonth(myDate)} ${myDate[0]}, ${myDate.slice(5..8)}"
                                                )
                                            }
                                            else if(myDate.length == 9 && myDate[2] == '-') {
                                                Text(
                                                    text = "Event ${numberToMonth(myDate)} ${myDate.slice(0..1)}, ${myDate.slice(5..8)}"
                                                )
                                            }
                                            else {
                                                Text(
                                                    text = "Event ${numberToMonth(myDate)} ${myDate.slice(0..1)}, ${myDate.slice(6..9)}"
                                                )
                                            }
                                        },
                                        supportingText = { Text(text = titleSuppText)},
                                        singleLine = true
                                    )
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = myTime,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier
                                                .border(
                                                    width = 1.dp,
                                                    color = Color.LightGray,
                                                    shape = RoundedCornerShape(5.dp)
                                                )
                                                .width(80.dp)
                                                .padding(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        ExtendedFloatingActionButton(
                                            onClick = { showTimePicker = true },
                                            containerColor = MaterialTheme.colorScheme.background,
                                            modifier = Modifier
                                                .padding(end = 8.dp)
                                                .width(100.dp)
                                                .height(50.dp)
                                        ) {
                                            Text(
                                                text = "Edit", fontSize = 16.sp,
                                                modifier = Modifier.padding(end = 6.dp)
                                            )
                                            Icon(
                                                painter = painterResource(id = R.drawable.ic_clock),
                                                contentDescription = null
                                            )
                                        }
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        Button(onClick = {
                                            myDate = millisToYearsMonthsDays(datePickerState.selectedDateMillis)
                                            val toParse = "$myDate $myTime"
                                            val formatter = SimpleDateFormat("dd-MM-yyyy HH:mm")
                                            val date: Date = formatter.parse(toParse)
                                            val millis: Long = date.time
                                            coroutineScope.launch {
                                                eventVM.create(title, myDate, myTime, millis)
                                                val instantiate = CoroutineScope(Dispatchers.Main).async {
                                                    eventItem.title = title
                                                    eventItem.date = myDate
                                                    eventItem.hour = myTime
                                                    eventItem.reminder = millis
                                                }
                                                instantiate.await()
                                                scheduler.schedule(eventItem)
                                                showEventAdder = false
                                            }
                                        },  shape = RoundedCornerShape(14.dp),
                                            enabled = canConfirm,
                                            modifier = Modifier
                                                .padding(top = 24.dp, end = 16.dp)
                                                .width(120.dp)
                                                .height(50.dp)
                                        ) {
                                            Text(text = "Accept", fontSize = 18.sp, fontWeight = FontWeight.SemiBold ,modifier = Modifier)
                                        }
                                    }
                                }
                            }
                        )
                    }
                        if (showEventEditor) {
                            eventVM.adviceForDateText(myDate)
                            eventVM.adviceForTitleText(title)
                            Dialog(
                                onDismissRequest = {
                                    showEventEditor = false
                                    title = ""
                               },
                                content = {
                                    Column(
                                        modifier = Modifier
                                            .background(
                                                color = MaterialTheme.colorScheme.background,
                                                shape = RoundedCornerShape(10.dp)
                                            )
                                            .fillMaxWidth()
                                            .height(350.dp)
                                            .padding(top = 18.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        OutlinedTextField(
                                            value = title,
                                            onValueChange = {
                                                title = it
                                                eventVM.adviceForTitleText(title)
                                            },
                                            supportingText = { Text(text = titleSuppText)},
                                            singleLine = true
                                        )
                                        OutlinedTextField(
                                            value = myDate,
                                            onValueChange = {
                                                myDate = it
                                                eventVM.adviceForDateText(myDate)
                                            },
                                            label = {
                                                Text(
                                                    text = "New Date"
                                                )
                                            },
                                            placeholder = { Text(text = "DD-MM-YYYY")},
                                            supportingText = { Text(text = dateSuppText, color = colorResource(id = R.color.error))},
                                        )

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(end = 12.dp),
                                            horizontalArrangement = Arrangement.End,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = myTime,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier
                                                    .border(
                                                        width = 1.dp,
                                                        color = Color.LightGray,
                                                        shape = RoundedCornerShape(5.dp)
                                                    )
                                                    .width(80.dp)
                                                    .padding(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            ExtendedFloatingActionButton(
                                                onClick = { showTimePicker = true },
                                                containerColor = MaterialTheme.colorScheme.background,
                                                modifier = Modifier
                                                    .padding(end = 8.dp)
                                                    .width(100.dp)
                                                    .height(50.dp)
                                            ) {
                                                Text(
                                                    text = "Edit", fontSize = 16.sp,
                                                    modifier = Modifier.padding(end = 6.dp)
                                                )
                                                Icon(
                                                    painter = painterResource(id = R.drawable.ic_clock),
                                                    contentDescription = null
                                                )
                                            }
                                        }
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.End
                                        ) {
                                            Button(onClick = {
                                                if (eventVM.isDateTextRight(myDate, myDate.length)) {
                                                    coroutineScope.launch {
                                                        val toParse = "$myDate $myTime"
                                                        val formatter = SimpleDateFormat("dd-MM-yyyy HH:mm")
                                                        val date: Date = formatter.parse(toParse)
                                                        val millis: Long = date.time
                                                        eventVM.update(eventItem, title, myDate, myTime, millis)
                                                        eventItem = eventVM.getEventById(eventItem._id)
                                                        scheduler.cancel(eventItem)
                                                        scheduler.schedule(eventItem)
                                                        title = ""
                                                    }
                                                }
                                                else {
                                                    Toast.makeText(context, "Date values are not valid", Toast.LENGTH_SHORT).show()
                                                }
                                                showEventEditor = false
                                            },  shape = RoundedCornerShape(14.dp),
                                                enabled = canConfirm,
                                                modifier = Modifier
                                                    .padding(top = 36.dp, end = 20.dp)
                                                    .width(120.dp)
                                                    .height(50.dp)
                                            ) {
                                                Text(text = "Confirm", fontSize = 18.sp, fontWeight = FontWeight.SemiBold ,modifier = Modifier)
                                            }
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
fun millisToYearsMonthsDays(millis: Long?): String {
    val calendar = Calendar.getInstance().apply {
        if (millis != null) {
            timeInMillis = millis
        }
    }
    var date: String
    if (calendar.get(Calendar.DAY_OF_MONTH) == calendar.getActualMaximum(Calendar.DAY_OF_MONTH)) {
        date = "1-"
        if(calendar.get(Calendar.MONTH) == 11) {
            date += "1-"
            date += "${calendar.get(Calendar.YEAR)+1}"
        }
        else {
            date += "${calendar.get(Calendar.MONTH)+2}-"
            date += "${calendar.get(Calendar.YEAR)}"
        }
    }
    else {
        date = "${calendar.get(Calendar.DAY_OF_MONTH)+1}-"
        date += "${calendar.get(Calendar.MONTH)+1}-"
        date += "${calendar.get(Calendar.YEAR)}"
    }

    return date
}
fun numberToMonth(date: String): String {
    var month = ""
    var sliceRange: IntRange = (0..0)
    var index = 0
    if (date.length == 8) {
        index = 2
    }
    else if(date.length == 9 && date[1] == '-') {
        sliceRange = (2..3)
    }
    else if(date.length == 9 && date[2] == '-') {
        index = 3
    }
    else sliceRange = (3..4)
    if(index == 0) {
        when (date.slice(sliceRange)) {
            "10" -> month = "Oct"
            "11" -> month = "Nov"
            "12" -> month = "Dec"
        }
    }
    else {
        when (date[index]) {
            '1' -> month = "Jan"
            '2' -> month = "Feb"
            '3' -> month = "Mar"
            '4' -> month = "Apr"
            '5' -> month = "May"
            '6' -> month = "Jun"
            '7' -> month = "Jul"
            '8' -> month = "Aug"
            '9' -> month = "Sep"
        }
    }
    return month
}