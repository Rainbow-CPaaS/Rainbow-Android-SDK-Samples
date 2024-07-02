package com.ale.rainbowsample.activities

import android.widget.ImageView
import com.ale.rainbowsample.components.SearchToolbar

interface SearchCallback {

    fun onSearchOpened()

    fun onSearchClosed()

    fun getSearchToolbar(): SearchToolbar

    fun getSearchButton(): ImageView
}