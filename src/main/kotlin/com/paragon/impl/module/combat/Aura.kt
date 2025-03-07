package com.paragon.impl.module.combat

import com.paragon.Paragon
import com.paragon.impl.module.Module
import com.paragon.impl.setting.Setting
import com.paragon.impl.managers.rotation.Rotate
import com.paragon.impl.managers.rotation.Rotation
import com.paragon.impl.managers.rotation.RotationPriority
import com.paragon.impl.module.Aliases
import com.paragon.impl.module.Category
import com.paragon.util.anyNull
import com.paragon.util.calculations.Timer
import com.paragon.util.entity.EntityUtil.isEntityAllowed
import com.paragon.util.player.EntityFakePlayer
import com.paragon.util.player.InventoryUtil.getItemSlot
import com.paragon.util.player.InventoryUtil.isHoldingSword
import com.paragon.util.player.InventoryUtil.switchToSlot
import com.paragon.util.player.RotationUtil.getRotationToVec3d
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Items
import net.minecraft.network.play.client.CPacketUseEntity
import net.minecraft.util.EnumHand
import net.minecraft.util.math.Vec2f
import net.minecraft.util.math.Vec3d
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import java.util.stream.Collectors

/**
 * Basic Aura module.
 *
 * @author Surge
 */
@SideOnly(Side.CLIENT)
@Aliases(["KillAura", "Forcefield"])
object Aura : Module("Aura", Category.COMBAT, "Automatically attacks entities") {

    // How to sort the targets
    private val sort = Setting("Sort", com.paragon.impl.module.combat.Aura.Sort.DISTANCE, null, null, null) describedBy "How to sort the targets"

    // Filters
    private val players = Setting("Players", true) describedBy "Attack players"
    private val mobs = Setting("Mobs", true) describedBy "Attack mobs"
    private val passives = Setting("Passives", true) describedBy "Attack passives"

    // Main settings
    private val range = Setting("Range", 5f, 0f, 6f, 0.1f) describedBy "The range to attack"

    private val delay = Setting(
        "Delay", 700.0, 0.0, 2000.0, 1.0
    ) describedBy "The delay between attacking in milliseconds"

    private val performWhen = Setting("When", com.paragon.impl.module.combat.Aura.When.HOLDING) describedBy "When to attack"

    private val rotate = Setting("Rotate", Rotate.PACKET) describedBy "How to rotate to the target"

    private val rotateBack = Setting(
        "RotateBack", true
    ) describedBy "Rotate back to your original rotation" subOf com.paragon.impl.module.combat.Aura.rotate

    private val where = Setting("Where", com.paragon.impl.module.combat.Aura.Where.BODY) describedBy "Where to attack"
    private val packetAttack = Setting("Packet", false) describedBy "Attack with a packet"

    var lastTarget: EntityLivingBase? = null
        private set

    private val attackTimer = Timer()
    private var target: EntityLivingBase? = null

    override fun onTick() {
        if (minecraft.anyNull) {
            return
        }

        com.paragon.impl.module.combat.Aura.target = null

        // Check the delay has passed
        if (com.paragon.impl.module.combat.Aura.attackTimer.hasMSPassed(com.paragon.impl.module.combat.Aura.delay.value)) {
            // Filter entities
            var entities = minecraft.world.loadedEntityList.stream().filter {
                EntityLivingBase::class.java.isInstance(it)
            }.collect(Collectors.toList())

            // Filter entities based on settings
            entities = entities.stream().filter {
                it.getDistance(minecraft.player) <= com.paragon.impl.module.combat.Aura.range.value && it !== minecraft.player && !it.isDead && (it.isEntityAllowed(
                    com.paragon.impl.module.combat.Aura.players.value, com.paragon.impl.module.combat.Aura.mobs.value, com.paragon.impl.module.combat.Aura.passives.value
                ) || it is EntityFakePlayer) && (it !is EntityPlayer || !Paragon.INSTANCE.friendManager.isFriend(it.getName()))
            }.collect(Collectors.toList())

            // Sort entities
            entities = entities.sortedWith(Comparator.comparingDouble {
                com.paragon.impl.module.combat.Aura.sort.value!!.getSort(it as EntityLivingBase).toDouble()
            })

            // Check we have targets
            if (entities.isNotEmpty()) {
                // Get the target
                val entityLivingBase = entities[0] as EntityLivingBase
                com.paragon.impl.module.combat.Aura.target = entityLivingBase
                com.paragon.impl.module.combat.Aura.lastTarget = com.paragon.impl.module.combat.Aura.target

                // Get our old slot
                val oldSlot: Int = minecraft.player.inventory.currentItem
                when (com.paragon.impl.module.combat.Aura.performWhen.value) {
                    com.paragon.impl.module.combat.Aura.When.SILENT_SWITCH, com.paragon.impl.module.combat.Aura.When.SWITCH -> {
                        if (!isHoldingSword) {
                            val swordSlot = getItemSlot(Items.DIAMOND_SWORD)

                            if (swordSlot > -1) {
                                switchToSlot(swordSlot, false)
                            }
                            else {
                                com.paragon.impl.module.combat.Aura.lastTarget = null
                                return
                            }
                        }

                        if (!isHoldingSword) {
                            com.paragon.impl.module.combat.Aura.lastTarget = null
                            return
                        }
                    }

                    com.paragon.impl.module.combat.Aura.When.HOLDING -> if (!isHoldingSword) {
                        com.paragon.impl.module.combat.Aura.lastTarget = null
                        return
                    }
                }

                // Get our original rotation
                val originalRotation = Vec2f(minecraft.player.rotationYaw, minecraft.player.rotationPitch)

                // Get our target rotation
                val rotationVec = getRotationToVec3d(
                    Vec3d(
                        entityLivingBase.posX, entityLivingBase.posY + com.paragon.impl.module.combat.Aura.where.value.getWhere(entityLivingBase), entityLivingBase.posZ
                    )
                )
                val rotation = Rotation(rotationVec.x, rotationVec.y, rotate.value, RotationPriority.HIGH)

                // Rotate to the target
                Paragon.INSTANCE.rotationManager.addRotation(rotation)

                // Attack the target
                if (com.paragon.impl.module.combat.Aura.packetAttack.value) {
                    minecraft.player.connection.sendPacket(CPacketUseEntity(entityLivingBase, EnumHand.MAIN_HAND))
                }
                else {
                    minecraft.playerController.attackEntity(minecraft.player, entityLivingBase)
                }

                // Swing hand
                minecraft.player.swingArm(EnumHand.MAIN_HAND)

                // Reset our cooldown
                minecraft.player.resetCooldown()

                // Rotate back to the original rotation
                if (com.paragon.impl.module.combat.Aura.rotateBack.value && com.paragon.impl.module.combat.Aura.rotate.value != Rotate.NONE) {
                    val rotationBack = Rotation(
                        originalRotation.x, originalRotation.y, rotate.value, RotationPriority.NORMAL
                    )
                    Paragon.INSTANCE.rotationManager.addRotation(rotationBack)
                }

                // Switch back to the old slot
                if (oldSlot != minecraft.player.inventory.currentItem && com.paragon.impl.module.combat.Aura.performWhen.value == com.paragon.impl.module.combat.Aura.When.SILENT_SWITCH) {
                    switchToSlot(oldSlot, false)
                }
            }
            else {
                com.paragon.impl.module.combat.Aura.lastTarget = null
            }
            com.paragon.impl.module.combat.Aura.attackTimer.reset()
        }
    }

    private val isReady: Boolean
        get() {
            if (minecraft.anyNull) {
                return false
            }

            when (com.paragon.impl.module.combat.Aura.performWhen.value) {
                com.paragon.impl.module.combat.Aura.When.SILENT_SWITCH, com.paragon.impl.module.combat.Aura.When.SWITCH -> {
                    if (!isHoldingSword) {
                        val swordSlot = getItemSlot(Items.DIAMOND_SWORD)
                        return swordSlot > -1
                    }
                    if (isHoldingSword) {
                        return true
                    }
                }

                com.paragon.impl.module.combat.Aura.When.HOLDING -> if (isHoldingSword) return true
            }

            return false
        }

    override fun getData(): String {
        return if (com.paragon.impl.module.combat.Aura.target == null) "No target" else com.paragon.impl.module.combat.Aura.target!!.name
    }

    override fun isActive(): Boolean {
        return super.isActive() && com.paragon.impl.module.combat.Aura.target != null && com.paragon.impl.module.combat.Aura.isReady
    }

    @Suppress("unused")
    enum class Sort(var function: (EntityLivingBase) -> Float) {
        /**
         * Sort by distance
         */
        DISTANCE({ minecraft.player.getDistance(it) }),

        /**
         * Sort by health
         */
        HEALTH({ it.health }),

        /**
         * Sort by armour
         */
        ARMOUR({
            var totalArmourDamage = 0f
            for (itemStack in it.armorInventoryList) {
                totalArmourDamage += itemStack.itemDamage.toFloat()
            }
            totalArmourDamage
        });

        /**
         * Gets the function to sort by
         *
         * @return The function to sort by
         */
        fun getSort(entityLivingBase: EntityLivingBase) = function(entityLivingBase)
    }

    enum class When {
        /**
         * Only attack when we are holding a sword
         */
        HOLDING,

        /**
         * Switch to a sword
         */
        SWITCH,

        /**
         * Silent switch to a sword
         */
        SILENT_SWITCH
    }

    @Suppress("unused")
    enum class Where(var function: (EntityLivingBase) -> Float) {
        /**
         * Rotate to feet of target
         */
        FEET({ 0F }),

        /**
         * Rotate to body of target
         */
        BODY({ it.width / 2F }),

        /**
         * Rotate to head of target
         */
        HEAD({ it.height });

        /**
         * Gets the height to add to the rotation
         *
         * @param entityLivingBase The entity to get the height for
         * @return The height to add to the rotation
         */
        fun getWhere(entityLivingBase: EntityLivingBase) = function(entityLivingBase)

    }

}