package it.macgood.core.recyclerview

import android.graphics.drawable.Drawable
import android.view.animation.AnimationUtils
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView
import it.macgood.core.R

class CustomItemAnimator : DefaultItemAnimator() {

    override fun animateAdd(holder: RecyclerView.ViewHolder?): Boolean {

        holder?.itemView?.animation = AnimationUtils.loadAnimation(
            holder?.itemView?.context,
            R.anim.scale_in
        )

        return super.animateAdd(holder)
    }

    override fun animateRemove(holder: RecyclerView.ViewHolder?): Boolean {

        holder?.itemView?.animation = AnimationUtils.loadAnimation(
            holder?.itemView?.context,
            R.anim.scale_out
        )

        return super.animateRemove(holder)
    }
}
