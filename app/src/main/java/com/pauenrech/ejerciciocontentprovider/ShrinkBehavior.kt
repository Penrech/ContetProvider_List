package com.pauenrech.ejerciciocontentprovider

import android.content.Context
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import android.support.v4.view.ViewCompat
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator

/*
*
* Toda esta clase es para hacer que mi custom floating button, que es una card en realidad, tenga el mismo comportamiento
* que un FAB y se mueva acorde al snackbar cuando aparece o desaparece
*
* */
class ShrinkBehavior(context: Context, attributeSet: AttributeSet)
    : CoordinatorLayout.Behavior<View>(context, attributeSet){

    override fun layoutDependsOn(parent: CoordinatorLayout, child: View, dependency: View): Boolean {
        return dependency is Snackbar.SnackbarLayout
    }

    override fun onDependentViewChanged(parent: CoordinatorLayout, child: View, dependency: View): Boolean {

        val distanceY = getViewOffsetForSnackbar(parent, child)
        child.translationY = - distanceY
        return true
    }

    override fun onDependentViewRemoved(parent: CoordinatorLayout, child: View, dependency: View) {
        val animation = ViewCompat.animate(child).translationY(0f)
        animation.duration = 50
        animation.interpolator = AccelerateDecelerateInterpolator()
        animation.start()
        super.onDependentViewRemoved(parent, child, dependency)
    }

    private fun getViewOffsetForSnackbar(parent: CoordinatorLayout, view: View): Float{
        var maxOffset = 0f
        val dependencies = parent.getDependencies(view)

        dependencies.forEach { dependency ->
            if (dependency is Snackbar.SnackbarLayout && parent.doViewsOverlap(view, dependency)){
                maxOffset = Math.max(maxOffset, (dependency.translationY - dependency.height) * -1)
            }
        }

        return maxOffset
    }
}