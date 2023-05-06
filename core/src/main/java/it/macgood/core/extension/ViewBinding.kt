package it.macgood.core.extension

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.viewbinding.ViewBinding
import kotlin.reflect.KProperty

inline fun <T : ViewBinding> Activity.viewBinding(crossinline binding: (LayoutInflater) -> T) =
    lazy(LazyThreadSafetyMode.NONE) {
        binding.invoke(layoutInflater)
    }