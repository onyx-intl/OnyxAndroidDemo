package com.android.onyx.demo.utils

import android.graphics.Rect

/**
 * Created by zhuzeng on 08/08/2017.
 */
object RectUtils {
    class RectResult {
        lateinit var parent: Rect
        lateinit var child: Rect
        var inParent: Array<Rect?> = arrayOfNulls(4)
        var inChild: Array<Rect?> = arrayOfNulls(4)

        fun reset() {
            inParent = arrayOfNulls(4)
            inChild = arrayOfNulls(4)
        }
    }

    fun getTopEdgeInterset(result: RectResult): Boolean {
        val child = result.child
        val parent = result.parent
        if (child.width() < parent.width()) {
            result.inChild[0] = Rect(
                child.left, child.top, child.width(), parent.top - child.top
            )
            result.inParent[0] = Rect(
                child.left, parent.top, child.width(), child.bottom - parent.top
            )
        } else {
            result.inChild[0] = Rect(
                child.left, child.top, child.width(), parent.bottom - child.top
            )
        }
        return true
    }

    fun getTopLeftInterset(result: RectResult): Boolean {
        val child = result.child
        val parent = result.parent
        result.inChild[0] = Rect(child.left, child.top, child.right, parent.top - child.top)
        result.inChild[1] = Rect(
            child.left, parent.top, parent.left - child.left, child.bottom - parent.top
        )
        result.inParent[0] = Rect(
            parent.left, parent.top, child.right - parent.left, child.bottom - parent.top
        )
        return true
    }

    fun getTopRightInterset(result: RectResult): Boolean {
        val child = result.child
        val parent = result.parent
        result.inChild[0] = Rect(child.left, child.top, child.right, parent.top - child.top)
        result.inChild[1] = Rect(
            parent.left, parent.top, child.right - parent.left, child.bottom - parent.top
        )
        result.inParent[0] = Rect(
            child.left, parent.top, parent.left - child.left, child.bottom - parent.top
        )
        return true
    }

    fun getBottomLeftInterset(result: RectResult): Boolean {
        val child = result.child
        val parent = result.parent
        result.inChild[0] = Rect(child.left, child.top, child.right, parent.top - child.top)
        result.inChild[1] = Rect(
            child.left, parent.bottom, child.width(), child.bottom - parent.bottom
        )
        result.inParent[0] = Rect(
            parent.left, child.top, child.right - parent.left, parent.bottom - child.top
        )
        return true
    }

    fun getBottomRightInterset(result: RectResult): Boolean {
        val child = result.child
        val parent = result.parent
        result.reset()
        result.inChild[0] = Rect(
            parent.right, child.top, child.right - parent.right, parent.bottom - child.top
        )
        result.inChild[1] = Rect(
            child.left, parent.bottom, child.width(), child.bottom - parent.bottom
        )
        result.inParent[0] = Rect(
            child.left, child.top, parent.right - parent.left, parent.bottom - child.top
        )
        return true
    }
}
