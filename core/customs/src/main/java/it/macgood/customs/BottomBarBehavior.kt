package it.macgood.customs

import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.math.MathUtils
import androidx.core.view.ViewCompat

class BottomBarBehavior : CoordinatorLayout.Behavior<BottomBar>() {

    override fun onStartNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: BottomBar,
        directTargetChild: View,
        target: View,
        axes: Int,
        type: Int
    ): Boolean {
        return axes == ViewCompat.SCROLL_AXIS_VERTICAL
    }

    override fun onNestedPreScroll(
        coordinatorLayout: CoordinatorLayout,
        child: BottomBar,
        target: View,
        dx: Int,
        dy: Int,
        consumed: IntArray,
        type: Int
    ) {
        val offset = MathUtils.clamp(child.translationY + dy, 0f, child.minHeight.toFloat())
        if (offset != child.translationY) child.translationY = offset
        super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type)
    }

}