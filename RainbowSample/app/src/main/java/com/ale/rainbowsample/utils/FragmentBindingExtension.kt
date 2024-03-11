package com.ale.rainbowsample.utils

import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.ale.rainbow.RBLog
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

fun <T> Fragment.viewLifecycle(
    onDestroy: ((T) -> Unit)? = null
): ReadWriteProperty<Fragment, T> =
    object : ReadWriteProperty<Fragment, T>, DefaultLifecycleObserver {

        private var binding: T? = null

        init {
            RBLog.info("FragmentExtension", ">viewLifecycle init")
            // Observe the view lifecycle of the Fragment.
            // The view lifecycle owner is null before onCreateView and after onDestroyView.
            // The observer is automatically removed after the onDestroy event.
            this@viewLifecycle
                .viewLifecycleOwnerLiveData
                .observe(this@viewLifecycle) { owner: LifecycleOwner? ->
                    owner?.lifecycle?.addObserver(this)
                }
        }

        override fun onDestroy(owner: LifecycleOwner) {
            onDestroy?.invoke(requireNotNull(binding))
            binding = null
        }

        override fun getValue(
            thisRef: Fragment,
            property: KProperty<*>
        ): T {
            return this.binding ?: kotlin.run {
                RBLog.error("FragmentExtension", "binding is null")
                error("Called before onCreateView or after onDestroyView.")
            }
        }

        override fun setValue(
            thisRef: Fragment,
            property: KProperty<*>,
            value: T
        ) {
            this.binding = value
        }
    }