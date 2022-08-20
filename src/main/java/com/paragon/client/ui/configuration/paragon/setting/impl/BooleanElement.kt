package com.paragon.client.ui.configuration.paragon.setting.impl

import com.paragon.api.setting.Setting
import com.paragon.api.util.render.ColourUtil.fade
import com.paragon.api.util.render.ColourUtil.integrateAlpha
import com.paragon.api.util.render.RenderUtil
import com.paragon.api.util.render.font.FontUtil
import com.paragon.client.ui.configuration.paragon.module.ModuleElement
import com.paragon.client.ui.configuration.paragon.setting.SettingElement
import com.paragon.client.ui.util.Click
import me.surge.animation.Animation
import me.surge.animation.Easing
import net.minecraft.util.math.MathHelper
import java.awt.Color

/**
 * @author Surge
 * @since 06/08/2022
 */
class BooleanElement(setting: Setting<Boolean>, module: ModuleElement, x: Float, y: Float, width: Float, height: Float) : SettingElement<Boolean>(setting, module, x, y, width, height) {

    private val enabled: Animation = Animation({ 200f }, setting.value, { Easing.LINEAR })

    override fun draw(mouseX: Float, mouseY: Float, mouseDelta: Int) {
        enabled.state = setting.value

        RenderUtil.drawRect(x, y, width, height, Color(53, 53, 74).fade(Color(64, 64, 92), hover.getAnimationFactor()).rgb)

        RenderUtil.scaleTo(x + 5, y + 7, 0f, 0.5, 0.5, 0.5) {
            if (hover.getAnimationFactor() > 0.5) {
                FontUtil.drawStringWithShadow(if (setting.value) "Enabled" else "Disabled", x + 5, y + 7 + (7 * hover.getAnimationFactor()).toFloat(), Color(255, 0, 0).fade(Color(0, 255, 0), enabled.getAnimationFactor()).integrateAlpha(MathHelper.clamp(255 * hover.getAnimationFactor(), 5.0, 255.0).toFloat()).rgb)
            }
        }

        RenderUtil.scaleTo(x + 5, y + 5, 0f, 0.7, 0.7, 0.7) {
            FontUtil.drawStringWithShadow(setting.name, x + 5, y + 5 - (3 * hover.getAnimationFactor()).toFloat(), Color.GRAY.brighter().fade(Color.WHITE, enabled.getAnimationFactor()).rgb)
        }

        super.draw(mouseX, mouseY, mouseDelta)
    }

    override fun mouseClicked(mouseX: Float, mouseY: Float, click: Click) {
        super.mouseClicked(mouseX, mouseY, click)

        if (click == Click.LEFT && hover.state) {
            setting.setValue(!setting.value)
        }
    }

}