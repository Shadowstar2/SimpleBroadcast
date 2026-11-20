package dev.announce.simplebroadcast.velocity

import com.google.inject.Inject
import com.velocitypowered.api.command.CommandMeta
import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.ProxyServer
import dev.announce.simplebroadcast.velocity.command.BroadcastCommand
import dev.announce.simplebroadcast.velocity.command.SbvCommand
import net.kyori.adventure.text.minimessage.MiniMessage
import org.slf4j.Logger
import org.yaml.snakeyaml.Yaml
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.inputStream

@Plugin(
    id = "simplebroadcastvelocity",
    name = "SimpleBroadcastVelocity",
    version = "1.2.1",
    description = "Velocity broadcast plugin with MiniMessage support.",
    authors = ["custom"]
)
class SimpleBroadcastVelocity @Inject constructor(
    val server: ProxyServer,
    val logger: Logger,
    @param:DataDirectory val dataDirectory: Path
) {
    val miniMessage: MiniMessage = MiniMessage.miniMessage()
    var prefix: String = DEFAULT_PREFIX
        private set
    var trustedUsers: List<String> = emptyList()
        private set

    companion object {
        const val DEFAULT_PREFIX = "<bold><gradient:#3939FF:#782AD4>[Announce]</gradient></bold> "
    }

    @Subscribe
    fun onProxyInitialization(event: ProxyInitializeEvent) {
        loadConfig()

        val commandManager = server.commandManager

        val broadcastMeta: CommandMeta = commandManager.metaBuilder("broadcast")
            .aliases("bc", "bcast", "announce")
            .plugin(this)
            .build()
        commandManager.register(broadcastMeta, BroadcastCommand(this))

        val sbvMeta: CommandMeta = commandManager.metaBuilder("simplebroadcastvelocity")
            .aliases("sbv")
            .plugin(this)
            .build()
        commandManager.register(sbvMeta, SbvCommand(this))

        logger.info("SimpleBroadcastVelocity enabled.")
    }

    fun loadConfig() {
        val (loadedPrefix, loadedTrusted) = runCatching {
            if (!dataDirectory.exists()) {
                dataDirectory.createDirectories()
            }
            val configFile = dataDirectory.resolve("config.yml")
            if (!configFile.exists()) {
                javaClass.classLoader.getResourceAsStream("config.yml")?.use { input ->
                    Files.copy(input, configFile)
                } ?: Files.writeString(configFile, "prefix: \"$DEFAULT_PREFIX\"\ntrusted-users: []\n")
            }

            configFile.inputStream().use { input ->
                val data = Yaml().load<Map<String, Any>?>(input)
                val pref = data?.get("prefix")?.toString() ?: DEFAULT_PREFIX
                val rawTrusted = data?.get("trusted-users")
                val trusted = when (rawTrusted) {
                    is List<*> -> rawTrusted.mapNotNull { it?.toString()?.trim() }
                    else -> emptyList()
                }
                Pair(pref, trusted)
            }
        }.onFailure { e ->
            logger.error("Failed to load config.yml", e)
        }.getOrDefault(Pair(DEFAULT_PREFIX, emptyList()))

        prefix = loadedPrefix
        trustedUsers = loadedTrusted
    }

    fun reloadConfig() = loadConfig()

    fun hasAccess(source: CommandSource, permission: String): Boolean {
        if (source !is Player) return true
        if (source.hasPermission(permission)) return true
        if (trustedUsers.isEmpty()) return false
        val uuidStr = source.uniqueId.toString()
        val username = source.username
        return trustedUsers.any { entry ->
            val trimmed = entry.trim()
            trimmed.equals(uuidStr, ignoreCase = true) ||
            trimmed.replace("-", "").equals(uuidStr.replace("-", ""), ignoreCase = true) ||
            trimmed.equals(username, ignoreCase = true)
        }
    }
}
