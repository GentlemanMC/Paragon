package com.paragon.impl.module.movement

import com.paragon.impl.module.Category
import com.paragon.impl.module.Module
import com.paragon.impl.setting.Setting
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.util.math.MathHelper
import com.paragon.util.mc
import kotlin.math.floor

/**
 * @author GentlemanMC, Cubic
 */
object CornerClip : Module("CornerClip", Category.MOVEMENT, "Phases slightly into the corner of a your surrounding to prevent crystal damage") {
    private var disableTicks = 0

    private var timeout = Setting("Timeout", 5, 1, 10, 1)
    private var disableSetting = Setting("Auto Disable", true, null, null, null)

    override fun onTick() {
        if (mc.player == null || mc.world == null) return
        if (movingByKeys()) {
            return
        }
        if (mc.world.getCollisionBoxes(mc.player, mc.player.entityBoundingBox.grow(0.01, 0.0, 0.01)).size < 2) {
            val x = roundToClosest(
                mc.player.posX,
                floor(mc.player.posX) + 0.301,
                floor(mc.player.posX) + 0.699
            )
            val y = mc.player.posY
            val z = roundToClosest(
                mc.player.posZ,
                floor(mc.player.posZ) + 0.301,
                floor(mc.player.posZ) + 0.699
            )
            mc.player.setPosition(x, y, z)
            checkDisable()
            return
        }
        if (mc.player.ticksExisted % timeout.value == 0) {
            val x1 = mc.player.posX +
                    MathHelper.clamp(
                        roundToClosest(mc.player.posX,
                            floor(mc.player.posX) + 0.241,
                            floor(mc.player.posX) + 0.759) - mc.player.posX, -0.03, 0.03
                    )
            val y1 = mc.player.posY
            val z1 = mc.player.posZ +
                    MathHelper.clamp(
                        roundToClosest(mc.player.posZ,
                            floor(mc.player.posZ) + 0.241,
                            floor(mc.player.posZ) + 0.759) - mc.player.posZ, -0.03, 0.03
                    )
            mc.player.setPosition(x1, y1, z1)
            mc.player.connection.sendPacket(CPacketPlayer.Position(mc.player.posX, mc.player.posY, mc.player.posZ, true))

            val x2 = roundToClosest(
                mc.player.posX,
                floor(mc.player.posX) + 0.23,
                floor(mc.player.posX) + 0.77
            )
            val y2 = mc.player.posY
            val z2 = roundToClosest(mc.player.posZ,
                floor(mc.player.posZ) + 0.23,
                floor(mc.player.posZ) + 0.77)

            mc.player.connection.sendPacket(CPacketPlayer.Position(x2, y2, z2, true))
            disableSetting.value
            disableTicks++
        } else {
            disableTicks = 0
            checkDisable()
        }
    }

    private fun checkDisable() {
        if (disableTicks >= 2 && disableSetting.value!!) {
            disableTicks = 0
        }
    }

    private fun movingByKeys(): Boolean {
        return mc.gameSettings.keyBindForward.isKeyDown || mc.gameSettings.keyBindBack.isKeyDown || mc.gameSettings.keyBindLeft.isKeyDown || mc.gameSettings.keyBindRight.isKeyDown
    }

        private fun roundToClosest(num: Double, low: Double, high: Double): Double {
            // lD = low difference, hD = high difference - Cubic
            val lD = num - low
            val hD = high - num
            // >= because we want to round up if hD is 0.5 - Cubic
            return if (hD >= lD) low else high
        }
    }