package it.macgood.core.fragment

import android.graphics.drawable.Drawable
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar

open class BaseFragment : Fragment() {

    fun makeToast(message: String?) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    fun makeSnackbar(view: View, message: String) {
        Snackbar.make(view, message, Snackbar.LENGTH_SHORT)
            .setAnchorView(view)
            .show()
    }

    fun getDrawable(id: Int) : Drawable? {
        return ResourcesCompat.getDrawable(resources, id, requireActivity().theme)
    }

    fun getColor(id: Int): Int {
        return resources.getColor(id, requireActivity().theme)
    }

}