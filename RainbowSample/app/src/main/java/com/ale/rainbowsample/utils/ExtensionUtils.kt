package com.ale.rainbowsample.utils

import android.app.Activity
import android.content.Context
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.util.TypedValue
import android.view.View
import android.view.ViewTreeObserver.OnWindowFocusChangeListener
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.model.KeyPath
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

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

fun View.focusAndOpenKeyboard() {
    requestFocus()
    if (hasWindowFocus()) {
        openKeyboard()
    } else {
        getViewTreeObserver().addOnWindowFocusChangeListener(object : OnWindowFocusChangeListener {
            override fun onWindowFocusChanged(hasFocus: Boolean) {
                if (hasFocus) {
                    openKeyboard()
                    getViewTreeObserver().removeOnWindowFocusChangeListener(this)
                }
            }
        })
    }
}

fun View.openKeyboard() {
    if (isFocused) {
        post {
            (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?)?.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
        }
    }
}

fun View.closeKeyboard() {
    (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?)?.hideSoftInputFromWindow(windowToken, 0)
}

fun Date.toRelativeTimeString(): String {
    val now = Date()
    val diffInMillis = now.time - this.time

    val minutes = TimeUnit.MILLISECONDS.toMinutes(diffInMillis)
    val hours = TimeUnit.MILLISECONDS.toHours(diffInMillis)
    val days = TimeUnit.MILLISECONDS.toDays(diffInMillis)

    return when {
        minutes < 1 -> "just now"
        minutes < 60 -> "$minutes minute${if (minutes > 1) "s" else ""} ago"
        hours < 24 -> "$hours hour${if (hours > 1) "s" else ""} ago"
        days == 1L -> "yesterday"
        days < 7 -> "$days day${if (days > 1) "s" else ""} ago"
        else -> java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(this)
    }
}