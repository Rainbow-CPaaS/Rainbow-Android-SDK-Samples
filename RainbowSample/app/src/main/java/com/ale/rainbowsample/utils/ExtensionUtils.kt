package com.ale.rainbowsample.utils

import android.app.Activity
import android.content.Context
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date

fun Fragment.hideKeyboard() = activity?.hideKeyboard()

fun Activity.hideKeyboard() {
    val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    var view = currentFocus
    if (view == null) {
        view = View(this)
    }
    imm.hideSoftInputFromWindow(view.windowToken, 0)
}

fun <T> Fragment.collectLifecycleFlow(flow: Flow<T>, collect: suspend (T) -> Unit) {
    viewLifecycleOwner.lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            flow.collect(collect)
        }
    }
}

fun Fragment.showSnackBar(message: String, dismissCallback: (() -> Unit)? = null) {
    val snackBar = Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG).apply {
        addCallback(object : Snackbar.Callback() {
            override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                dismissCallback?.invoke()
            }
        })
    }

    val textView = snackBar.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
    textView.maxLines = 5

    snackBar.show()
}

fun Date?.toShortFormat(): String {
    if (this == null) return ""

    val date = this.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
    val currentDate = LocalDateTime.now()

    val formatter : DateTimeFormatter = if (date.year != currentDate.year) {
        DateTimeFormatter.ofPattern("dd/MM/yy")
    } else if (date.dayOfMonth != currentDate.dayOfMonth) {
        DateTimeFormatter.ofPattern("d MMM")
    } else {
        DateTimeFormatter.ofPattern("HH:mm")
    }

    return date.format(formatter)
}

fun Context.getThemeColor(resId: Int): Int {
    val typedValue = TypedValue()

    val a = this.obtainStyledAttributes(typedValue.data, intArrayOf(resId))
    val color = a.getColor(0, 0)

    a.recycle()

    return color
}