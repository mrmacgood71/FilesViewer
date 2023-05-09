package it.macgood.customs

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.shape.MaterialShapeDrawable

class BottomBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), CoordinatorLayout.AttachedBehavior {

    init {
        View.inflate(context, R.layout.layout_bottombar, this)
        val materialBackground = MaterialShapeDrawable.createWithElevationOverlay(context)
        materialBackground.elevation = elevation
        background = materialBackground
    }

    override fun getBehavior(): CoordinatorLayout.Behavior<*> {
        return BottomBarBehavior()
    }
}