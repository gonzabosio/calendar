package com.calendarapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calendarapp.data.Event
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.ext.isManaged
import io.realm.kotlin.ext.query
import io.realm.kotlin.notifications.ResultsChange
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EventDataViewModel : ViewModel() {
    private val config = RealmConfiguration.create(schema = setOf(Event::class))
    private val realm: Realm = Realm.open(config)

    private val _listOfEvents = MutableStateFlow<List<Event>>(listOf())
    val listOfEvents: StateFlow<List<Event>> = _listOfEvents.asStateFlow()

    private val _dateSuppText = MutableStateFlow("")
    val dateSuppText: StateFlow<String> = _dateSuppText

    private val _titleSuppText = MutableStateFlow("")
    val titleSuppText: StateFlow<String> = _titleSuppText

    private val _confirmButton = MutableStateFlow(false)
    val confirmButton: StateFlow<Boolean> = _confirmButton
    init {
        viewModelScope.launch {
            read()
        }
    }
    suspend fun create(_title: String,_date:String, _hour: String, _reminder: Long) {
        realm.write {
            val event = Event().apply {
                title = _title
                date = _date
                hour = _hour
                reminder = _reminder
            }
            val managedEvent = copyToRealm(event)
            if (managedEvent.isManaged()) {
                Log.d("crud_tag","The object IS administrated by Realm")
            } else {
                Log.d("crud_tag","The object IS NOT administrated by Realm")
            }
        }
    }
    private suspend fun read() {
        CoroutineScope(Dispatchers.Main).launch {
            val flow: Flow<ResultsChange<Event>> = realm.query<Event>().asFlow()
            flow.collect { eventChange ->
                val events = eventChange.list.map { it }
                _listOfEvents.value = events
            }
        }
    }
    suspend fun update(event: Event, newTitle: String, newDate: String, newHour: String, newReminder: Long) {
        realm.write {
            val liveEvent = query<Event>("_id == $0", event._id).find().first()
            liveEvent.title = newTitle
            liveEvent.date = newDate
            liveEvent.hour= newHour
            liveEvent.reminder = newReminder
        }
    }
    suspend fun delete(event: Event) {
        realm.write {
            val eventToDelete: Event = query<Event>("_id == $0", event._id).find().first()
            delete(eventToDelete)
        }
    }

    fun adviceForTitleText(title: String) {
        if (title == "") {
            _titleSuppText.value = "Title is empty"
            _confirmButton.value = false
        }
        else {
            _titleSuppText.value = ""
            _confirmButton.value = true
        }
    }
    fun isDateTextRight(date: String, dateLength: Int): Boolean {
        var isValid = false
        if (dateLength == 8) {
            isValid = true
        }
        else if(dateLength == 9 && date[1] == '-') {
            if(date.slice(2..3).toInt() <= 12) {
                isValid = true
            }
        }
        else if(dateLength == 9 && date[2] == '-') {
            if(date.slice(0..1).toInt() <= 31) {
                isValid = true
            }
        }
        else if(dateLength == 10) {
            if(date.slice(0..1).toInt() <= 31 && date.slice(3..4).toInt() <= 12) {
                isValid = true
            }
        }
        if (!date.takeLast(4).all { it.isDigit() }) {
            isValid = false
        }
        return isValid
    }
    fun adviceForDateText(date: String) {
        if(date.length == 8 && date[1] == '-' && date[3] == '-') {
            _dateSuppText.value = ""
            _confirmButton.value = true
        }
        else if(date.length == 9 && date[1] == '-' && date[4] == '-') {
            _dateSuppText.value = ""
            _confirmButton.value = true
        }
        else if (date.length == 9 && date[2] == '-' && date[4] == '-') {
            _dateSuppText.value = ""
            _confirmButton.value = true
        }
        else if (date.length == 9 && date[2] == '-' && date[5] == '-') {
            _dateSuppText.value = ""
            _confirmButton.value = true
        }
        else if (date.length == 10 && date[2] == '-' && date[5] == '-') {
            _dateSuppText.value = ""
            _confirmButton.value = true
        }
        else {
            _dateSuppText.value = "The date is not valid"
            _confirmButton.value = false
        }
    }
}