package dev.announce.simplebroadcast

import me.clip.placeholderapi.PlaceholderAPI
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitTask
import java.util.Locale
import kotlin.random.Random

data class AutoBroadcastConfig(
    val enabled: Boolean = false,
    val intervalSeconds: Long = 300L,
    val random: Boolean = false,
    val minPlayers: Int = 1,
    val usePrefix: Boolean = true,
    val playSound: Boolean = false,
    val messages: List<String> = emptyList()
)

class SimpleBroadcast : JavaPlugin() {

    private val miniMessage: MiniMessage by lazy { MiniMessage.miniMessage() }
    private var papiEnabled: Boolean = false

    private var autoBroadcastConfig = AutoBroadcastConfig()
    private var autoBroadcastTask: BukkitTask? = null
    private var lastAutoMessageIndex: Int = -1

    companion object {
        private const val DEFAULT_PREFIX = "<bold><gradient:#3939FF:#782AD4>[Announce]</gradient></bold> "
        private val COMMON_TAGS = listOf(
            "<black>", "<dark_blue>", "<dark_green>", "<dark_aqua>", "<dark_red>",
            "<dark_purple>", "<gold>", "<gray>", "<dark_gray>", "<blue>",
            "<green>", "<aqua>", "<red>", "<light_purple>", "<yellow>",
            "<white>", "<bold>", "<italic>", "<underlined>", "<strikethrough>",
            "<obfuscated>", "<reset>", "<rainbow>", "<gradient:#3939FF:#782AD4>"
        )
    }

    override fun onEnable() {
        saveDefaultConfig()
        papiEnabled = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null
        if (papiEnabled) {
            logger.info("PlaceholderAPI found — placeholder support enabled.")
        }
        loadAutoBroadcastConfig()
        startAutoBroadcast()
        logger.info("SimpleBroadcast v${description.version} by Shadowstar2 enabled — https://github.com/Shadowstar2/SimpleBroadcast")
    }

    override fun onDisable() {
        stopAutoBroadcast()
    }

    private fun loadAutoBroadcastConfig() {
        val sec = config.getConfigurationSection("auto-broadcast")
        autoBroadcastConfig = if (sec != null) {
            AutoBroadcastConfig(
                enabled = sec.getBoolean("enabled", false),
                intervalSeconds = sec.getLong("interval-seconds", 300L),
                random = sec.getBoolean("random", false),
                minPlayers = sec.getInt("min-players", 1),
                usePrefix = sec.getBoolean("use-prefix", true),
                playSound = sec.getBoolean("play-sound", false),
                messages = sec.getStringList("messages")
            )
        } else {
            AutoBroadcastConfig()
        }
    }

    fun startAutoBroadcast() {
        stopAutoBroadcast()
        lastAutoMessageIndex = -1

        if (!autoBroadcastConfig.enabled || autoBroadcastConfig.messages.isEmpty()) {
            return
        }

        val intervalTicks = maxOf(1L, autoBroadcastConfig.intervalSeconds) * 20L
        autoBroadcastTask = Bukkit.getScheduler().runTaskTimerAsynchronously(
            this,
            Runnable { runAutoBroadcast() },
            intervalTicks,
            intervalTicks
        )
    }

    fun stopAutoBroadcast() {
        autoBroadcastTask?.cancel()
        autoBroadcastTask = null
    }

    private fun runAutoBroadcast() {
        val cfg = autoBroadcastConfig
        if (!cfg.enabled || cfg.messages.isEmpty()) return

        val onlinePlayers = Bukkit.getOnlinePlayers()
        if (onlinePlayers.size < cfg.minPlayers) return

        val messages = cfg.messages
        val nextIndex = if (messages.size <= 1) {
            0
        } else if (cfg.random) {
            var next = Random.nextInt(messages.size)
            if (next == lastAutoMessageIndex) {
                next = (next + 1 + Random.nextInt(messages.size - 1)) % messages.size
            }
            next
        } else {
            (lastAutoMessageIndex + 1) % messages.size
        }
        lastAutoMessageIndex = nextIndex

        var rawMessage = messages[nextIndex]
        var prefixToUse = if (cfg.usePrefix) {
            config.getString("prefix", DEFAULT_PREFIX) ?: DEFAULT_PREFIX
        } else {
            ""
        }

        if (papiEnabled) {
            rawMessage = PlaceholderAPI.setPlaceholders(null, rawMessage)
            if (prefixToUse.isNotEmpty()) {
                prefixToUse = PlaceholderAPI.setPlaceholders(null, prefixToUse)
            }
        }

        val message: Component = miniMessage.deserialize(prefixToUse + rawMessage)
        Bukkit.getServer().sendMessage(message)

        if (cfg.playSound) {
            Bukkit.getScheduler().runTask(this, Runnable {
                playBroadcastSound()
            })
        }
    }

    fun hasAccess(sender: CommandSender, permission: String): Boolean {
        if (sender !is Player) return true
        if (sender.hasPermission(permission)) return true
        val trusted = config.getStringList("trusted-users")
        if (trusted.isEmpty()) return false
        val uuidStr = sender.uniqueId.toString()
        val name = sender.name
        return trusted.any { entry ->
            val trimmed = entry.trim()
            trimmed.equals(uuidStr, ignoreCase = true) ||
            trimmed.replace("-", "").equals(uuidStr.replace("-", ""), ignoreCase = true) ||
            trimmed.equals(name, ignoreCase = true)
        }
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean =
        when (command.name.lowercase(Locale.ROOT)) {
            "simplebroadcast" -> handleAdminCommand(sender, args)
            "broadcast" -> handleBroadcastCommand(sender, label, args)
            else -> false
        }

    private fun handleAdminCommand(sender: CommandSender, args: Array<out String>): Boolean {
        if (!hasAccess(sender, "simplebroadcast.reload")) {
            sender.sendMessage(miniMessage.deserialize("<red>You don't have permission to do that."))
            return true
        }

        if (args.isNotEmpty() && args[0].equals("reload", ignoreCase = true)) {
            reloadConfig()
            loadAutoBroadcastConfig()
            startAutoBroadcast()
            sender.sendMessage(miniMessage.deserialize("<green>SimpleBroadcast config reloaded."))
            return true
        }
        sender.sendMessage(miniMessage.deserialize("<red>Usage: /simplebroadcast reload"))
        return true
    }

    private fun handleBroadcastCommand(sender: CommandSender, label: String, args: Array<out String>): Boolean {
        if (!hasAccess(sender, "simplebroadcast.use")) {
            sender.sendMessage(miniMessage.deserialize("<red>You don't have permission to do that."))
            return true
        }

        if (args.isEmpty()) {
            sender.sendMessage(miniMessage.deserialize("<red>Usage: /$label <message>"))
            return true
        }

        var prefix = config.getString("prefix", DEFAULT_PREFIX) ?: DEFAULT_PREFIX
        var rawMessage = args.joinToString(" ")

        if (papiEnabled) {
            val player = sender as? Player
            rawMessage = PlaceholderAPI.setPlaceholders(player, rawMessage)
            prefix = PlaceholderAPI.setPlaceholders(player, prefix)
        }

        val message: Component = miniMessage.deserialize(prefix + rawMessage)
        Bukkit.getServer().sendMessage(message)

        playBroadcastSound()
        return true
    }

    private fun playBroadcastSound() {
        val soundName = config.getString("sound", "ENTITY_EXPERIENCE_ORB_PICKUP")
            ?.takeIf { it.isNotBlank() } ?: return

        runCatching {
            val sound = Sound.valueOf(soundName.trim().uppercase(Locale.ROOT))
            for (player in Bukkit.getOnlinePlayers()) {
                player.playSound(player.location, sound, 1.0f, 1.0f)
            }
        }.onFailure {
            logger.warning("Invalid sound specified in config.yml: $soundName")
        }
    }

    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<out String>): List<String> =
        when (command.name.lowercase(Locale.ROOT)) {
            "simplebroadcast" -> {
                if (!hasAccess(sender, "simplebroadcast.reload")) {
                    emptyList()
                } else if (args.size == 1 && "reload".startsWith(args[0], ignoreCase = true)) {
                    listOf("reload")
                } else {
                    emptyList()
                }
            }
            "broadcast" -> {
                if (!hasAccess(sender, "simplebroadcast.use")) {
                    emptyList()
                } else {
                    val current = args.lastOrNull()?.lowercase(Locale.ROOT).orEmpty()
                    val tagCompletions = COMMON_TAGS.filter { tag ->
                        val lower = tag.lowercase(Locale.ROOT)
                        lower.startsWith(current) || (current.isNotEmpty() && lower.removePrefix("<").startsWith(current))
                    }
                    val playerCompletions = Bukkit.getOnlinePlayers()
                        .map { it.name }
                        .filter { it.lowercase(Locale.ROOT).startsWith(current) }

                    tagCompletions + playerCompletions
                }
            }
            else -> emptyList()
        }
}
