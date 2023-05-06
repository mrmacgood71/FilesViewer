package it.macgood.core.fragment

import android.R
import android.util.TypedValue
import androidx.fragment.app.Fragment


fun Fragment.themeTextColor(attr: Int) : Int {
    val typedValue = TypedValue()
    requireContext().theme.resolveAttribute(attr, typedValue, true)
    val color = typedValue.data
    return color
}