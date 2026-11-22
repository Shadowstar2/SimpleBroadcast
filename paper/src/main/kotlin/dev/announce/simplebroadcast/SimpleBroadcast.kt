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
import java.util.Locale

class SimpleBroadcast : JavaPlugin() {

    private val miniMessage: MiniMessage by lazy { MiniMessage.miniMessage() }
    private var papiEnabled: Boolean = false

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
        logger.info("SimpleBroadcast v${description.version} by Shadowstar2 enabled — https://github.com/Shadowstar2/SimpleBroadcast")
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
