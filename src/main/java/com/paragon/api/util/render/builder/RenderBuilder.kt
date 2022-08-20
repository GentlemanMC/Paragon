package com.paragon.api.util.render.builder

import com.paragon.api.util.Wrapper
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderGlobal
import net.minecraft.util.math.AxisAlignedBB
import org.lwjgl.opengl.GL11.*
import java.awt.Color

/**
 * @author Surge
 * @since 20/08/2022
 */
class RenderBuilder : Wrapper {

    private var depth: Boolean = false
    private var blend: Boolean = false
    private var texture: Boolean = false
    private var alpha: Boolean = false

    private var boundingBox: AxisAlignedBB = AxisAlignedBB(0.0, 0.0, 0.0, 0.0, 0.0, 0.0)

    private var renderMode: BoxRenderMode = BoxRenderMode.BOTH

    private var innerColour: Color = Color(0, 0, 0)
    private var outerColour: Color = Color(0, 0, 0)

    fun start(): RenderBuilder {
        glPushMatrix()
        GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO)
        glEnable(GL_LINE_SMOOTH)
        glHint(GL_LINE_SMOOTH_HINT, GL_NICEST)

        return this
    }

    fun build(offset: Boolean) {
        if (offset) {
            boundingBox = boundingBox.offset(-minecraft.renderManager.viewerPosX, -minecraft.renderManager.viewerPosY, -minecraft.renderManager.viewerPosZ)
        }

        when (renderMode) {
            BoxRenderMode.FILL -> {
                RenderGlobal.renderFilledBox(boundingBox, innerColour.red / 255f, innerColour.green / 255f, innerColour.blue / 255f, innerColour.alpha / 255f)
            }

            BoxRenderMode.OUTLINE -> {
                RenderGlobal.drawSelectionBoundingBox(boundingBox, outerColour.red / 255f, outerColour.green / 255f, outerColour.blue / 255f, outerColour.alpha / 255f)
            }

            BoxRenderMode.BOTH -> {
                RenderGlobal.renderFilledBox(boundingBox, innerColour.red / 255f, innerColour.green / 255f, innerColour.blue / 255f, innerColour.alpha / 255f)
                RenderGlobal.drawSelectionBoundingBox(boundingBox, outerColour.red / 255f, outerColour.green / 255f, outerColour.blue / 255f, outerColour.alpha / 255f)
            }

            BoxRenderMode.NONE -> {}
        }

        if (depth) {
            glDepthMask(true)
            glEnable(GL_DEPTH_TEST)
        }

        if (blend) {
            glEnable(GL_BLEND)
        }

        if (alpha) {
            glEnable(GL_ALPHA_TEST)
        }

        if (texture) {
            glEnable(GL_TEXTURE_2D)
        }

        glDisable(GL_LINE_SMOOTH)
        glPopMatrix()
    }

    fun boundingBox(boundingBoxIn: AxisAlignedBB): RenderBuilder {
        this.boundingBox = boundingBoxIn

        return this
    }

    fun type(typeIn: BoxRenderMode): RenderBuilder {
        renderMode = typeIn

        return this
    }

    fun depth(depthIn: Boolean): RenderBuilder {
        if (depthIn) {
            glDisable(GL_DEPTH_TEST)
            glDepthMask(false)
        }

        depth = depthIn
        return this
    }

    fun blend(blendIn: Boolean): RenderBuilder {
        if (blendIn) {
            glEnable(GL_BLEND)
        }

        blend = blendIn
        return this
    }

    fun texture(textureIn: Boolean): RenderBuilder {
        if (textureIn) {
            glDisable(GL_TEXTURE_2D)
        }

        texture = textureIn
        return this
    }

    fun alpha(alphaIn: Boolean): RenderBuilder {
        if (alphaIn) {
            glDisable(GL_ALPHA_TEST)
        }

        alpha = alphaIn
        return this
    }

    fun lineWidth(lineWidthIn: Float): RenderBuilder {
        glLineWidth(lineWidthIn)
        return this
    }

    fun inner(colour: Color): RenderBuilder {
        innerColour = colour

        return this
    }

    fun outer(colour: Color): RenderBuilder {
        outerColour = colour

        return this
    }

}