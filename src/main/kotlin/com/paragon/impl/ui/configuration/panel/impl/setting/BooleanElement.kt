package com.paragon.impl.ui.configuration.panel.impl.setting

import com.paragon.impl.module.client.ClickGUI
import com.paragon.impl.module.client.Colours
import com.paragon.impl.setting.Setting
import com.paragon.impl.ui.configuration.panel.impl.ModuleElement
import com.paragon.impl.ui.configuration.panel.impl.SettingElement
import com.paragon.impl.ui.util.Click
import com.paragon.util.render.ColourUtil.fade
import com.paragon.util.render.ColourUtil.integrateAlpha
import com.paragon.util.render.RenderUtil
import com.paragon.util.render.font.FontUtil
import me.surge.animation.Animation
import me.surge.animation.ColourAnimation
import net.minecraft.util.math.MathHelper
import java.awt.Color

class BooleanElement(parent: ModuleElement, setting: Setting<Boolean>, x: Float, y: Float, width: Float, height: Float) : SettingElement<Boolean>(parent, setting, x, y, width, height) {

    private val enabled = Animation({ ClickGUI.animationSpeed.value }, false, { ClickGUI.easing.value })

    override fun draw(mouseX: Float, mouseY: Float, mouseDelta: Int) {
        super.draw(mouseX, mouseY, mouseDelta)

        enabled.state = setting.value

        RenderUtil.drawRect(x, y, width, height, hover.getColour())

        RenderUtil.scaleTo(x + 3, y + 5.5f, 0f, 0.7, 0.7, 0.7) {
            FontUtil.drawStringWithShadow(setting.name, x + 3, y + 5.5f, Color.WHITE)
        }

        RenderUtil.drawRoundedRect(x + getRenderableWidth() - 16f, y + 0.5f, 16f, 16f, 3f, Color(50, 50, 50))
        RenderUtil.drawRoundedRect(x + getRenderableWidth() - 16f, y + 0.5f, 16f, 16f, 3f, Colours.mainColour.value.integrateAlpha(255f * enabled.getAnimationFactor().toFloat()))
        RenderUtil.drawRoundedOutline(x + getRenderableWidth() - 12.5f, y + 4f, 9f, 9f, 4f, 2f, Color(70, 70, 70))

        val parentTotalHeight = parent.parent.y + parent.parent.height + parent.parent.moduleHeight

        RenderUtil.pushScissor(
            x + getRenderableWidth() - 11.5f,
            MathHelper.clamp(y + 4f, parent.parent.y + parent.parent.height, 100000f),
            8 * enabled.getAnimationFactor().toFloat(),
            MathHelper.clamp(height - 8f, 0f, parentTotalHeight.toFloat() - (y + 4f))
        )

        RenderUtil.scaleTo(x + getRenderableWidth() - 11.5f, y + 5f, 0f, 0.35, 0.35, 0.35) {
            FontUtil.drawIcon(
                FontUtil.Icon.TICK,
                x + getRenderableWidth() - 11.5f,
                y + 5f,
                Color.WHITE
            )
        }

        RenderUtil.popScissor()

        drawSettings(mouseX, mouseY, mouseDelta)
    }

    override fun mouseClicked(mouseX: Float, mouseY: Float, click: Click) {
        super.mouseClicked(mouseX, mouseY, click)

        if (hover.state) {
            if (click == Click.LEFT) {
                setting.setValue(!setting.value)
            } else if (click == Click.RIGHT) {
                expanded.state = !expanded.state
            }
        }
    }

}