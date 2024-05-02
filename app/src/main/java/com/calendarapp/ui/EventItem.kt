package com.calendarapp.ui

import android.content.Context
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.calendarapp.R
import com.calendarapp.data.Event
import com.calendarapp.notif.NotificationService
import com.calendarapp.ui.nav.numberToMonth
import com.calendarapp.viewmodel.EventDataViewModel
import kotlinx.coroutines.launch

@Composable
fun EventItem(
    name: String,
    hour: String,
    date: String,
    event: Event,
    eventVM: EventDataViewModel,
    thisContext: Context,
    onUpdateClick: (Event) -> Unit
) {
    val modifier: Modifier = Modifier
    var isContextMenuVisible by rememberSaveable {
        mutableStateOf(false)
    }
    var itemHeight by remember {
        mutableStateOf(0.dp)
    }
    val density = LocalDensity.current

    val coroutineScope = rememberCoroutineScope()

    val notificationService = NotificationService(thisContext)
    Card(
        elevation = CardDefaults.cardElevation(4.dp),
        colors =
        CardDefaults.cardColors(containerColor = colorResource(id = R.color.card)),
        modifier = modifier
            .onSizeChanged {
                itemHeight = with(density) { it.height.toDp() }
            }
            .padding(12.dp)
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .pointerInput(true) {
                    detectTapGestures(
                        onLongPress = {
                            isContextMenuVisible = true
                        }
                    )
                }
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp, top = 16.dp, bottom = 16.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(text = name, fontSize = 18.sp, fontWeight = FontWeight.W800)
                    Text(text = " at ")
                    Text(text = hour, fontSize = 18.sp, fontWeight = FontWeight.W800)
                }
                if (date.length == 8) {
                    Text(text = "${numberToMonth(date)} ${date[0]}, ${date.slice(4..7)}")
                }
                else if(date.length == 9 && date[1] == '-') {
                    Text(text = "${numberToMonth(date)} ${date[0]}, ${date.slice(5..8)}")
                }
                else if(date.length == 9 && date[2] == '-') {
                    Text(text = "${numberToMonth(date)} ${date.slice(0..1)}, ${date.slice(5..8)}")
                }
                else Text(text = "${numberToMonth(date)} ${date.slice(0..1)}, ${date.slice(6..9)}")
            }
            IconButton(
                onClick = {
                    isContextMenuVisible = true
                },
                modifier = Modifier
                    .padding(end = 8.dp, top = 8.dp)
                    .align(Alignment.CenterVertically)
            ) {
                Icon(imageVector = Icons.Default.MoreVert, contentDescription = null)
            }
        }
        DropdownMenu(
            expanded = isContextMenuVisible,
            onDismissRequest = {isContextMenuVisible = false},
            modifier = modifier.padding(4.dp),
            offset = DpOffset(x = (260).dp, y = (-20).dp)
        ) {
            DropdownMenuItem(
                text = { Text(text = "Edit") },
                onClick = {
                    onUpdateClick(event)
                    isContextMenuVisible = false
                })
            DropdownMenuItem(
                text = { Text(text = "Delete") },
                onClick = {
                    coroutineScope.launch {
                        isContextMenuVisible = false
                        eventVM.delete(event)
                        notificationService.cancel(event)
                    }
                }
            )
        }
    }
}