package com.onyx.android.eink.pen.demo.erase.util

import com.onyx.android.eink.pen.demo.PenBundle
import com.onyx.android.eink.pen.demo.erase.data.EraseTypes
import com.onyx.android.sdk.pen.TouchHelper
import com.onyx.android.sdk.pen.style.StrokeStyle
import com.onyx.android.sdk.utils.ReflectUtil
import kotlin.concurrent.Volatile

object EraserTrackHelper {
    private const val VIEW_UPDATE_HELPER_CLASS = "android.onyx.ViewUpdateHelper"
    private const val METHOD_SET_ERASER_RAW_DRAWING_ENABLED = "setEraserRawDrawingEnabled"

    @Volatile
    private var supportsMoveStrokeSfTrack: Boolean? = null

    fun supportsMoveStrokeSfTrack(): Boolean {
        var cached = supportsMoveStrokeSfTrack
        if (cached != null) {
            return cached
        }
        synchronized(EraserTrackHelper::class.java) {
            cached = supportsMoveStrokeSfTrack
            if (cached != null) {
                return cached
            }
            cached = probeSupportsMoveStrokeSfTrack()
            supportsMoveStrokeSfTrack = cached
            return cached
        }
    }

    private fun probeSupportsMoveStrokeSfTrack(): Boolean {
        val viewUpdateHelperClass =
            ReflectUtil.classForName(VIEW_UPDATE_HELPER_CLASS) ?: return false
        val method = ReflectUtil.getMethodSafely(
            viewUpdateHelperClass,
            METHOD_SET_ERASER_RAW_DRAWING_ENABLED,
            Boolean::class.javaPrimitiveType,
            Int::class.javaPrimitiveType
        )
        return method != null
    }

    fun defaultTrackEnabled(eraseType: Int): Boolean {
        return when (eraseType) {
            EraseTypes.ERASER_AREA -> true
            EraseTypes.ERASER_MOVE, EraseTypes.ERASER_STROKE -> supportsMoveStrokeSfTrack()
            else -> false
        }
    }

    fun resolveLayer(bundle: PenBundle, eraseType: Int): Layer {
        if (!bundle.isDisplayEraseTrack(eraseType)) {
            return Layer.NONE
        }
        if (eraseType == EraseTypes.ERASER_AREA) {
            return Layer.SF
        }
        if (EraseTypes.isMoveOrStrokeErase(eraseType)) {
            return if (supportsMoveStrokeSfTrack()) Layer.SF else Layer.APP
        }
        return Layer.NONE
    }

    fun useSfTrack(bundle: PenBundle, eraseType: Int): Boolean {
        return resolveLayer(bundle, eraseType) == Layer.SF
    }

    fun useAppTrack(bundle: PenBundle, eraseType: Int): Boolean {
        return resolveLayer(bundle, eraseType) == Layer.APP
    }

    fun shouldForceRawDrawing(bundle: PenBundle, eraseType: Int): Boolean {
        return useSfTrack(bundle, eraseType)
    }

    fun eraserStrokeStyle(eraseType: Int): Int {
        if (EraseTypes.isMoveOrStrokeErase(eraseType)) {
            return StrokeStyle.SOFT_ERASER
        }
        return TouchHelper.STROKE_STYLE_DASH
    }

    enum class Layer {
        SF, APP, NONE
    }
}
