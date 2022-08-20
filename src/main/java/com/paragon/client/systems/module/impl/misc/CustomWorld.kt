package com.paragon.client.systems.module.impl.misc

import com.paragon.api.event.network.PacketEvent.PreReceive
import com.paragon.api.module.Category
import com.paragon.api.module.Module
import com.paragon.api.setting.Setting
import com.paragon.api.util.anyNull
import me.wolfsurge.cerauno.listener.Listener
import net.minecraft.network.play.server.SPacketTimeUpdate

object CustomWorld : Module("CustomWorld", Category.MISC, "Changes the way the world is shown client side") {

    private val customWeather = Setting(
        "CustomWeather",
        true
    ) describedBy "Set the world weather to a custom value"

    private val weather = Setting(
        "Weather",
        Weather.CLEAR
    ) describedBy "The weather to display" subOf customWeather

    private val customTime = Setting(
        "CustomTime",
        true
    ) describedBy "Set the world time to a custom value"

    private val time = Setting(
        "Time",
        1000f,
        0f,
        24000f,
        1f
    ) describedBy "The time of day" subOf customTime

    override fun onTick() {
        if (minecraft.anyNull) {
            return
        }

        minecraft.world.setRainStrength(weather.value.rainStrength.toFloat())
        minecraft.world.worldTime = time.value.toLong()
    }

    @Listener
    fun onPacketReceive(event: PreReceive) {
        if (event.packet is SPacketTimeUpdate) {
            // Stop the world from updating the time
            event.cancel()
        }
    }

    enum class Weather(val rainStrength: Int) {
        /**
         * Clear weather - no rain or thunder
         */
        CLEAR(0),

        /**
         * Rainy weather - just rain, darker sky
         */
        RAIN(1),

        /**
         * Thunder - rain + thunder
         */
        THUNDER(2);

    }

}