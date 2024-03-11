package com.ale.rainbowsample.utils

import android.graphics.Rect
import android.view.View
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView

open class HorizontalMarginItemDecoration : RecyclerView.ItemDecoration() {

    var isLastItemDecorated = false
    var dividerThickness: Int = 0


    override fun getItemOffsets(
        outRect: Rect, view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        outRect[0, 0, 0] = 0
        // Only add offset if there's a divider displayed.
        if (shouldDrawDivider(parent, view)) {
            if (ViewCompat.getLayoutDirection(view) == ViewCompat.LAYOUT_DIRECTION_RTL) {
                outRect.left = dividerThickness
            } else {
                outRect.right = dividerThickness
            }
        }
    }

    private fun shouldDrawDivider(parent: RecyclerView, child: View): Boolean {
        val position = parent.getChildAdapterPosition(child)
        val adapter = parent.adapter
        val isLastItem = adapter != null && position == adapter.itemCount - 1
        return (position != RecyclerView.NO_POSITION && (!isLastItem || isLastItemDecorated)
                && shouldDrawDivider(position, adapter))
    }

    protected fun shouldDrawDivider(position: Int, adapter: RecyclerView.Adapter<*>?): Boolean {
        return true
    }
}