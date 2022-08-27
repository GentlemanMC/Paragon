package com.paragon.client.ui.configuration.zeroday.element.setting

import com.paragon.Paragon
import com.paragon.api.event.client.SettingUpdateEvent
import com.paragon.api.setting.Bind
import com.paragon.api.setting.Setting
import com.paragon.api.util.render.RenderUtil.drawRect
import com.paragon.api.util.render.font.FontUtil.drawStringWithShadow
import com.paragon.api.util.render.font.FontUtil.getStringWidth
import com.paragon.client.ui.configuration.zeroday.element.Element
import com.paragon.client.ui.configuration.zeroday.element.module.ModuleElement
import com.paragon.client.ui.util.Click
import me.surge.animation.Animation
import me.surge.animation.Easing
import net.minecraft.util.ChatAllowedCharacters
import net.minecraft.util.text.TextFormatting
import org.lwjgl.input.Keyboard
import org.lwjgl.opengl.GL11.*
import java.awt.Color
import java.util.function.Consumer

class StringElement(layer: Int, setting: Setting<String?>, moduleElement: ModuleElement, x: Float, y: Float, width: Float, height: Float) : Element(layer, x, y, width, height) {
    val setting: Setting<String?>
    private var focused = false
    private val listeningAnimation = Animation({ 200f }, false) { Easing.LINEAR }

    init {
        parent = moduleElement.parent
        this.setting = setting
        setting.subsettings.forEach(Consumer { subsetting: Setting<*> ->
            if (subsetting.value is Boolean) {
                subElements.add(BooleanElement(layer + 1, (subsetting as Setting<Boolean?>), moduleElement, x, y, width, height))
            }
            else if (subsetting.value is Enum<*>) {
                subElements.add(EnumElement(layer + 1, (subsetting as Setting<Enum<*>?>), moduleElement, x, y, width, height))
            }
            else if (subsetting.value is Number) {
                subElements.add(SliderElement(layer + 1, (subsetting as Setting<Number?>), moduleElement, x, y, width, height))
            }
            else if (subsetting.value is Bind) {
                subElements.add(BindElement(layer + 1, (subsetting as Setting<Bind?>), moduleElement, x, y, width, height))
            }
            else if (subsetting.value is Color) {
                subElements.add(ColourElement(layer + 1, subsetting as Setting<Color>, moduleElement, x, y, width, height))
            }
            else if (subsetting.value is String) {
                subElements.add(StringElement(layer + 1, subsetting as Setting<String?>, moduleElement, x, y, width, height))
            }
        })
    }

    override fun render(mouseX: Int, mouseY: Int, dWheel: Int) {
        if (setting.isVisible()) {
            listeningAnimation.state = focused
            drawRect(x, y, width, height, Color(40, 40, 45).rgb)
            drawRect(x + layer, y, width - layer * 2, height, Color((40 + 30 * hover.getAnimationFactor()).toInt(), (40 + 30 * hover.getAnimationFactor()).toInt(), (45 + 30 * hover.getAnimationFactor()).toInt()).rgb)
            drawRect(x + layer, y, 1f, (height * listeningAnimation.getAnimationFactor()).toFloat(), Color.HSBtoRGB(parent.leftHue / 360, 1f, (0.5f + 0.25f * hover.getAnimationFactor()).toFloat()))
            drawStringWithShadow(setting.name, x + layer * 2 + 5, y + height / 2 - 3.5f, -0x1)

            glPushMatrix()
            glScalef(0.8f, 0.8f, 0.8f)

            run {
                val scaleFactor = 1 / 0.8f
                val side = (x + width - getStringWidth(this.setting.value + if (focused) "_" else "") * 0.8f - 5) * scaleFactor
                drawStringWithShadow(TextFormatting.GRAY.toString() + " " + this.setting.value + if (focused) "_" else "", side, (y + 5f) * scaleFactor, -1)
            }

            glPopMatrix()

            super.render(mouseX, mouseY, dWheel)
        }
        else {
            focused = false
        }
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, click: Click) {
        if (setting.isVisible()) {
            if (isHovered(mouseX, mouseY) && parent.isElementVisible(this)) {
                if (click == Click.LEFT) {
                    focused = !focused
                }
            }
            super.mouseClicked(mouseX, mouseY, click)
        }
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, click: Click) {
        if (setting.isVisible()) {
            super.mouseReleased(mouseX, mouseY, click)
        }
    }

    override fun keyTyped(keyCode: Int, keyChar: Char) {
        if (setting.isVisible()) {
            if (focused) {
                if (keyCode == Keyboard.KEY_BACK) {
                    if (setting.value!!.isNotEmpty()) {
                        setting.setValue(setting.value!!.substring(0, setting.value!!.length - 1))
                        Paragon.INSTANCE.eventBus.post(SettingUpdateEvent(setting))
                    }
                }

                else if (keyCode == Keyboard.KEY_RETURN) {
                    focused = false
                }

                else if (ChatAllowedCharacters.isAllowedCharacter(keyChar)) {
                    setting.setValue(setting.value + keyChar)
                    Paragon.INSTANCE.eventBus.post(SettingUpdateEvent(setting))
                }
            }
            super.keyTyped(keyCode, keyChar)
        }
    }

    override var height: Float
        get() = if (setting.isVisible()) super.height else 0f
        set(value) {}

    override fun getSubElementsHeight(): Float {
        return if (setting.isVisible()) super.getSubElementsHeight() else 0f
    }

    override fun getTotalHeight(): Float {
        return if (setting.isVisible()) super.getTotalHeight() else 0f
    }
}