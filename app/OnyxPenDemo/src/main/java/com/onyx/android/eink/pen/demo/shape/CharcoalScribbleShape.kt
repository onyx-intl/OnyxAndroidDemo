package com.onyx.android.eink.pen.demo.shape

import com.onyx.android.eink.pen.demo.data.ShapeFactory
import com.onyx.android.eink.pen.demo.helper.RendererHelper
import com.onyx.android.eink.pen.demo.util.RendererUtils
import com.onyx.android.sdk.data.PenConstant
import com.onyx.android.sdk.data.note.PenAttrs
import com.onyx.android.sdk.data.note.PenTexture
import com.onyx.android.sdk.data.note.ShapeCreateArgs
import com.onyx.android.sdk.data.note.TiltConfig
import com.onyx.android.sdk.device.Device
import com.onyx.android.sdk.pen.NeoCharcoalPenV2Wrapper
import com.onyx.android.sdk.pen.NeoCharcoalPenWrapper
import com.onyx.android.sdk.pen.PenRenderArgs

class CharcoalScribbleShape : Shape() {
    override fun render(renderContext: RendererHelper.RenderContext) {
        val points = touchPointList?.points ?: return
        applyStrokeStyle(renderContext)

        val renderStrokeWidth = getRenderStrokeWidth()
        val createArgs = createShapeCreateArgs()
        val canvas = renderContext.canvas ?: return

        val renderArgs = PenRenderArgs().setCreateArgs(createArgs).setCanvas(canvas)
            .setPenType(ShapeFactory.getCharcoalPenType(getTexture())).setColor(getStrokeColor())
            .setErase(isTransparent()).setTiltEnabled(isTiltEnabled(createArgs))
            .setPaint(renderContext.paint)
            .setScreenMatrix(RendererUtils.getPointMatrix(renderContext))
            .setStrokeWidth(renderStrokeWidth).setPoints(points)

        // Mirror Kepler's CharcoalRender: select V1 vs V2 wrapper by texture.
        if (getTexture() == PenTexture.CHARCOAL_SHAPE_V2) {
            if (renderStrokeWidth <= PenConstant.CHARCOAL_SHAPE_DRAW_NORMAL_SCALE_WIDTH_THRESHOLD) {
                NeoCharcoalPenV2Wrapper.drawNormalStroke(renderArgs)
            } else {
                renderArgs.renderMatrix = RendererUtils.getPointMatrix(renderContext)
                NeoCharcoalPenV2Wrapper.drawBigStroke(renderArgs)
            }
        } else {
            if (renderStrokeWidth <= PenConstant.CHARCOAL_SHAPE_DRAW_NORMAL_SCALE_WIDTH_THRESHOLD) {
                NeoCharcoalPenWrapper.drawNormalStroke(renderArgs)
            } else {
                renderArgs.renderMatrix = RendererUtils.getPointMatrix(renderContext)
                NeoCharcoalPenWrapper.drawBigStroke(renderArgs)
            }
        }
    }

    private fun createShapeCreateArgs(): ShapeCreateArgs {
        return ShapeCreateArgs().setPenAttrs(PenAttrs().setTexture(getTexture()))
            .setTiltConfig(loadTiltConfig())
    }

    private fun loadTiltConfig(): TiltConfig? {
        val strokeStyle = ShapeFactory.getStrokeStyle(ShapeFactory.SHAPE_CHARCOAL_SCRIBBLE, getTexture())
        val parameters = Device.currentDevice().getStrokeParameters(strokeStyle)
        if (parameters == null || parameters.size < 2) {
            return null
        }
        return TiltConfig().setTiltEnabled(parameters[0] != 0f).setTiltScale(parameters[1])
    }

    private fun isTiltEnabled(createArgs: ShapeCreateArgs): Boolean {
        val tiltConfig = createArgs.getTiltConfig() ?: return false
        return tiltConfig.isTiltEnabled
    }
}
