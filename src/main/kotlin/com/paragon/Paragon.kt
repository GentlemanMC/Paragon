package com.paragon

import com.paragon.bus.EventBus
import com.paragon.impl.event.EventFactory
import com.paragon.impl.managers.*
import com.paragon.impl.ui.configuration.ConfigurationGUI
import com.paragon.impl.ui.configuration.GuiImplementation
import com.paragon.impl.ui.configuration.panel.PanelGUI
import com.paragon.impl.ui.configuration.retrowindows.Windows98
import com.paragon.impl.ui.console.Console
import com.paragon.impl.ui.taskbar.Taskbar
import com.paragon.util.render.font.FontUtil
import net.minecraft.client.Minecraft
import net.minecraftforge.common.ForgeVersion
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.Mod.EventHandler
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.lwjgl.opengl.Display
import java.awt.Desktop
import java.net.URI
import javax.swing.JOptionPane
import kotlin.math.log

@Mod(name = com.paragon.Paragon.Companion.modName, modid = com.paragon.Paragon.Companion.modID, version = com.paragon.Paragon.Companion.modVersion)
class Paragon {

    @EventHandler
    fun preInit(event: FMLPreInitializationEvent?) {
        if (ForgeVersion.buildVersion < 2860) {
            JOptionPane.showMessageDialog(
                null, "Forge version is too old. Paragon requires Forge to be at least build 2860.", "Outdated Forge!", JOptionPane.ERROR_MESSAGE
            )

            Desktop.getDesktop().browse(
                URI("https://files.minecraftforge.net/net/minecraftforge/forge/index_1.12.2.html")
            )

            Minecraft.getMinecraft().shutdown()

            // When trying to exit throws an exception lmao
            Display.destroy()

            return
        }

        eventParser = EventFactory()
        FontUtil.init()
    }

    @EventHandler
    fun init(event: FMLInitializationEvent?) {
        logger.info("Starting Paragon ${modVersion} initialisation")

        //  ________  ________  ________  ________  ________  ________  ________            _____      ________      ________
        // |\   __  \|\   __  \|\   __  \|\   __  \|\   ____\|\   __  \|\   ___  \         / __  \    |\   __  \    |\   __  \
        // \ \  \|\  \ \  \|\  \ \  \|\  \ \  \|\  \ \  \___|\ \  \|\  \ \  \\ \  \       |\/_|\  \   \ \  \|\  \   \ \  \|\  \
        //  \ \   ____\ \   __  \ \   _  _\ \   __  \ \  \  __\ \  \\\  \ \  \\ \  \      \|/ \ \  \   \ \  \\\  \   \ \  \\\  \
        //   \ \  \___|\ \  \ \  \ \  \\  \\ \  \ \  \ \  \|\  \ \  \\\  \ \  \\ \  \          \ \  \ __\ \  \\\  \ __\ \  \\\  \
        //    \ \__\    \ \__\ \__\ \__\\ _\\ \__\ \__\ \_______\ \_______\ \__\\ \__\          \ \__\\__\ \_______\\__\ \_______\
        //     \|__|     \|__|\|__|\|__|\|__|\|__|\|__|\|_______|\|_______|\|__| \|__|           \|__\|__|\|_______\|__|\|_______|

        println(
            "\n ________  ________  ________  ________  ________  ________  ________            _____      ________      ________     \n" + "|\\   __  \\|\\   __  \\|\\   __  \\|\\   __  \\|\\   ____\\|\\   __  \\|\\   ___  \\         / __  \\    |\\   __  \\    |\\   __  \\    \n" + "\\ \\  \\|\\  \\ \\  \\|\\  \\ \\  \\|\\  \\ \\  \\|\\  \\ \\  \\___|\\ \\  \\|\\  \\ \\  \\\\ \\  \\       |\\/_|\\  \\   \\ \\  \\|\\  \\   \\ \\  \\|\\  \\   \n" + " \\ \\   ____\\ \\   __  \\ \\   _  _\\ \\   __  \\ \\  \\  __\\ \\  \\\\\\  \\ \\  \\\\ \\  \\      \\|/ \\ \\  \\   \\ \\  \\\\\\  \\   \\ \\  \\\\\\  \\  \n" + "  \\ \\  \\___|\\ \\  \\ \\  \\ \\  \\\\  \\\\ \\  \\ \\  \\ \\  \\|\\  \\ \\  \\\\\\  \\ \\  \\\\ \\  \\          \\ \\  \\ __\\ \\  \\\\\\  \\ __\\ \\  \\\\\\  \\ \n" + "   \\ \\__\\    \\ \\__\\ \\__\\ \\__\\\\ _\\\\ \\__\\ \\__\\ \\_______\\ \\_______\\ \\__\\\\ \\__\\          \\ \\__\\\\__\\ \\_______\\\\__\\ \\_______\\\n" + "    \\|__|     \\|__|\\|__|\\|__|\\|__|\\|__|\\|__|\\|_______|\\|_______|\\|__| \\|__|           \\|__\\|__|\\|_______\\|__|\\|_______|"
        )

        storageManager = StorageManager()
        logger.info("StorageManager initialised")

        // Module /  Commands

        moduleManager = ModuleManager()
        logger.info("ModuleManager initialised")

        commandManager = CommandManager()
        logger.info("CommandManager initialised")

        pluginManager = PluginManager()
        logger.info("PluginManager initialised")

        pluginManager.onLoad()
        logger.info("Plugins loaded")

        // Misc client stuff

        altManager = AltManager()
        logger.info("AltManager initialised")

        capeManager = CapeManager()

        notificationManager = NotificationManager()
        logger.info("NotificationManager initialised")

        friendManager = FriendManager()
        logger.info("SocialManager initialised")

        // Event / Ingame stuff

        popManager = PopManager()
        logger.info("PopManager initialised")

        rotationManager = RotationManager()
        logger.info("RotationManager initialised")

        // GUIs

        taskbar = Taskbar
        logger.info("Taskbar Initialised")

        panelGUI = PanelGUI()
        logger.info("PanelGUI Initialised")

        windows98GUI = Windows98()
        logger.info("Windows98 GUI Initialised")

        console = Console("Paragon Console", 400f, 300f)
        logger.info("Console Initialised")

        configurationGUI = ConfigurationGUI()
        logger.info("Configuration GUI Initialised")

        // Load

        storageManager.loadModules("current")
        logger.info("Modules Loaded")

        storageManager.loadSocial()
        logger.info("Social Loaded")

        storageManager.loadAlts()
        logger.info("Alts Loaded")

        storageManager.loadOther()
        logger.info("Other Loaded")

        logger.info("Paragon ${com.paragon.Paragon.Companion.modVersion} Initialised Successfully")
    }

    @EventHandler
    fun postInit(event: FMLPostInitializationEvent) {
        pluginManager.onPostLoad()
    }

    companion object {
        const val modName = "Paragon"
        const val modID = "paragon"
        const val modVersion = "1.0.0"

        @JvmField
        @Mod.Instance
        var INSTANCE = Paragon()
    }

    val eventBus = EventBus()

    // Client stuff
    var logger: Logger = LogManager.getLogger("paragon")
        private set

    var pluginGui: GuiImplementation? = null

    val presenceManager = DiscordPresenceManager()

    // Managers
    lateinit var storageManager: StorageManager
        private set

    lateinit var moduleManager: ModuleManager
        private set

    lateinit var commandManager: CommandManager
        private set

    private lateinit var eventParser: EventFactory

    lateinit var popManager: PopManager
        private set

    lateinit var rotationManager: RotationManager
        private set

    lateinit var friendManager: FriendManager
        private set

    lateinit var altManager: AltManager
        private set

    lateinit var notificationManager: NotificationManager
        private set

    lateinit var capeManager: CapeManager
        private set

    lateinit var pluginManager: PluginManager
        private set

    // GUIs
    lateinit var taskbar: Taskbar
        private set

    lateinit var panelGUI: PanelGUI
        //private set

    lateinit var windows98GUI: Windows98
        private set

    lateinit var configurationGUI: ConfigurationGUI
        private set

    lateinit var console: Console
        private set

}