package com.paragon.impl.module.hud.impl

import com.paragon.impl.module.hud.HUDModule
import com.paragon.impl.module.client.Colours
import com.paragon.util.render.RenderUtil.drawBorder
import com.paragon.util.render.RenderUtil.drawRect
import com.paragon.util.render.RenderUtil.renderItemStack
import com.paragon.util.toColour
import net.minecraft.item.ItemStack

/**
 * @author Surge
 */
object Inventory : HUDModule("Inventory", "Displays the contents of your inventory") {

    override fun render() {
        drawRect(x, y, width, height - 4, 0x70000000.toColour())
        drawBorder(x, y, width, height - 4, 1f, Colours.mainColour.value)

        var x = 0f
        var y = 0f

        for (i in 9..35) {
            val stack: ItemStack = minecraft.player.inventory.getStackInSlot(i)

            renderItemStack(stack, this.x + x, this.y + y, true)

            x += 18f

            // cba for calcs
            if (i == 17 || i == 26 || i == 35) {
                x = 0f
                y += 18f
            }
        }
    }

    override var width: Float
        get() = (18 * 9).toFloat()
        set(width) {
            super.width = width
        }

    override var height: Float
        get() = (18 * 3 + 4).toFloat()
        set(height) {
            super.height = height
        }

}