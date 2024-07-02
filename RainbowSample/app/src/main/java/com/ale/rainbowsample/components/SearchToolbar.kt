package com.ale.rainbowsample.components

import android.content.Context
import android.graphics.PointF
import android.util.AttributeSet
import android.view.View
import android.view.ViewAnimationUtils
import android.widget.EditText
import androidx.annotation.StringRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.animation.addListener
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import com.ale.rainbowsample.R
import com.ale.rainbowsample.utils.closeKeyboard
import com.ale.rainbowsample.utils.focusAndOpenKeyboard

class SearchToolbar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    private val input: EditText

    private val circularRevealPoint = PointF()

    var listener: Listener? = null

    init {
        inflate(context, R.layout.search_toolbar, this)

        input = findViewById(R.id.search_input)

        val close = findViewById<View>(R.id.search_close)
        val clear = findViewById<View>(R.id.search_clear)

        close.setOnClickListener { collapse() }
        clear.setOnClickListener { input.setText("") }
        input.addTextChangedListener(afterTextChanged = {
            clear.isVisible = !it.isNullOrBlank()
            listener?.onSearchTextChange(it?.toString() ?: "")
        })
    }

    fun setSearchInputHint(@StringRes hintStringRes: Int) {
        input.setHint(hintStringRes)
    }

    fun display(x: Float, y: Float) {
        if (!isVisible) {
            circularRevealPoint.set(x, y)

            val animator = ViewAnimationUtils.createCircularReveal(this, x.toInt(), y.toInt(), 0f, width.toFloat())
            animator.duration = 400

            visibility = VISIBLE
            input.focusAndOpenKeyboard()
            animator.start()
        }
    }

    fun collapse() {
        if (visibility == VISIBLE) {
            listener?.onSearchClosed()
            input.closeKeyboard()

            val animator = ViewAnimationUtils.createCircularReveal(this, circularRevealPoint.x.toInt(), circularRevealPoint.y.toInt(), width.toFloat(), 0f)
            animator.duration = 400

            animator.addListener(onEnd = {
                visibility = INVISIBLE
            })
            animator.start()
        }
    }

    fun clearText() {
        input.setText("")
    }

    interface Listener {
        fun onSearchTextChange(text: String)
        fun onSearchClosed()
    }
}