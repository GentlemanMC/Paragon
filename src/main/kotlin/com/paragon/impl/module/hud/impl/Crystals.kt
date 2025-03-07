package com.paragon.impl.module.hud.impl

import com.paragon.impl.module.hud.HUDModule
import com.paragon.impl.module.client.Colours
import com.paragon.util.render.RenderUtil.drawBorder
import com.paragon.util.render.RenderUtil.drawRect
import com.paragon.util.render.RenderUtil.renderItemStack
import com.paragon.util.toColour
import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import net.minecraft.util.math.MathHelper

object Crystals : HUDModule("Crystals", "Displays the amount of crystals in your inventory") {

    override fun render() {
        drawRect(x, y, width, height, 0x70000000.toColour())
        drawBorder(x, y, width, height, 1f, Colours.mainColour.value)
        val itemStack = ItemStack(Items.END_CRYSTAL, crystals)
        renderItemStack(itemStack, x + width - 18, y + 2, true)
    }

    override var width: Float
        get() = (MathHelper.clamp(minecraft.fontRenderer.getStringWidth(crystals.toString()), 19, minecraft.fontRenderer.getStringWidth(crystals.toString())) + 2).toFloat()
        set(width) {
            super.width = width
        }

    override var height: Float
        get() = 19f
        set(height) {
            super.height = height
        }

    val crystals: Int
        get() {
            var count = 0

            for (i in 0..35) {
                val itemStack: ItemStack = minecraft.player.inventory.getStackInSlot(i)

                if (itemStack.item === Items.END_CRYSTAL) {
                    count += itemStack.count
                }
            }

            if (minecraft.player.heldItemMainhand.item === Items.END_CRYSTAL) {
                count += minecraft.player.heldItemMainhand.count
            }

            if (minecraft.player.heldItemOffhand.item === Items.END_CRYSTAL) {
                count += minecraft.player.heldItemOffhand.count
            }

            return count
        }

}