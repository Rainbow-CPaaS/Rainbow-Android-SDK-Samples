package com.ale.conversations.extensions

import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction

// Extension method to execute a specific operation (func) on a fragment transaction
inline fun FragmentManager.transaction(addToBackStack: Boolean = false, popToBackStack: Boolean = false, func: FragmentTransaction.() -> Unit) {
    val fragmentTransaction = beginTransaction()
    fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
    if (popToBackStack) popBackStack()
    fragmentTransaction.func()
    if (addToBackStack) fragmentTransaction.addToBackStack(null)
    fragmentTransaction.commit()
}