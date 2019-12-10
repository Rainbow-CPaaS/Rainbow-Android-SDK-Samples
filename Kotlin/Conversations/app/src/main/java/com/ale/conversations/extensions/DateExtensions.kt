package com.ale.conversations.extensions

import java.text.SimpleDateFormat
import java.util.*

// Extension method : display date in specific format
fun Date.prettyFormat(): String {

    val calendarToday = Calendar.getInstance()
    val calendarMessage = Calendar.getInstance()
    val format: SimpleDateFormat

    calendarToday.time = Date()
    calendarMessage.time = this

    format = when {
        calendarMessage.get(Calendar.YEAR) != calendarToday.get(Calendar.YEAR) -> SimpleDateFormat("dd-mm-yyyy hh:mm aaa", Locale.getDefault())
        calendarMessage.get(Calendar.DAY_OF_YEAR) != calendarToday.get(Calendar.DAY_OF_YEAR) -> SimpleDateFormat("dd-mm hh:mm aaa", Locale.getDefault())
        else -> SimpleDateFormat("hh:mm aaa", Locale.getDefault())
    }

    return format.format(this)
}