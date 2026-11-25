package dev.announce.simplebroadcast.velocity

import com.google.inject.Inject
import com.velocitypowered.api.command.CommandMeta
import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.ProxyServer
import com.velocitypowered.api.scheduler.ScheduledTask
import dev.announce.simplebroadcast.velocity.command.BroadcastCommand
import dev.announce.simplebroadcast.velocity.command.SbvCommand
import net.kyori.adventure.key.InvalidKeyException
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.minimessage.MiniMessage
import org.slf4j.Logger
import org.yaml.snakeyaml.Yaml
import java.nio.file.Files
import java.nio.file.Path
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.inputStream
import kotlin.random.Random

data class AutoBroadcastConfig(
    val enabled: Boolean = false,
    val intervalSeconds: Long = 300L,
    val random: Boolean = false,
    val minPlayers: Int = 1,
    val usePrefix: Boolean = true,
    val playSound: Boolean = false,
    val target: String = "all",
    val messages: List<String> = emptyList()
)

@Plugin(
    id = "simplebroadcastvelocity",
    name = "SimpleBroadcastVelocity",
    version = "1.3.0",
    description = "Velocity broadcast plugin with MiniMessage support.",
    authors = ["Shadowstar2"],
    url = "https://github.com/Shadowstar2/SimpleBroadcast"
)
class SimpleBroadcastVelocity @Inject constructor(
    val server: ProxyServer,
    val logger: Logger,
    @param:DataDirectory val dataDirectory: Path
) {
    val miniMessage: MiniMessage = MiniMessage.miniMessage()
    var prefix: String = DEFAULT_PREFIX
        private set
    var sound: String = DEFAULT_SOUND
        private set
    var trustedUsers: List<String> = emptyList()
        private set
    var autoBroadcastConfig: AutoBroadcastConfig = AutoBroadcastConfig()
        private set

    private var autoBroadcastTask: ScheduledTask? = null
    private var lastAutoMessageIndex: Int = -1

    companion object {
        const val DEFAULT_PREFIX = "<bold><gradient:#3939FF:#782AD4>[Announce]</gradient></bold> "
        const val DEFAULT_SOUND = "minecraft:entity.experience_orb.pickup"
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

        startAutoBroadcast()

        logger.info("SimpleBroadcastVelocity v1.3.0 by Shadowstar2 enabled — https://github.com/Shadowstar2/SimpleBroadcast")
    }

    @Subscribe
    fun onProxyShutdown(event: ProxyShutdownEvent) {
        stopAutoBroadcast()
    }

    fun loadConfig() {
        val (loadedPrefix, loadedSound, loadedTrusted, loadedAuto) = runCatching {
            if (!dataDirectory.exists()) {
                dataDirectory.createDirectories()
            }
            val configFile = dataDirectory.resolve("config.yml")
            if (!configFile.exists()) {
                javaClass.classLoader.getResourceAsStream("config.yml")?.use { input ->
                    Files.copy(input, configFile)
                } ?: Files.writeString(
                    configFile,
                    "prefix: \"$DEFAULT_PREFIX\"\nsound: \"$DEFAULT_SOUND\"\ntrusted-users: []\n"
                )
            }

            configFile.inputStream().use { input ->
                val data = Yaml().load<Map<String, Any>?>(input)
                val pref = data?.get("prefix")?.toString() ?: DEFAULT_PREFIX
                val snd = data?.get("sound")?.toString() ?: DEFAULT_SOUND
                val rawTrusted = data?.get("trusted-users")
                val trusted = when (rawTrusted) {
                    is List<*> -> rawTrusted.mapNotNull { it?.toString()?.trim() }
                    else -> emptyList()
                }

                val rawAuto = data?.get("auto-broadcast") as? Map<*, *>
                val autoConfig = if (rawAuto != null) {
                    val enabled = rawAuto["enabled"] as? Boolean ?: false
                    val interval = (rawAuto["interval-seconds"] as? Number)?.toLong() ?: 300L
                    val random = rawAuto["random"] as? Boolean ?: false
                    val minPlayers = (rawAuto["min-players"] as? Number)?.toInt() ?: 1
                    val usePref = rawAuto["use-prefix"] as? Boolean ?: true
                    val playSnd = rawAuto["play-sound"] as? Boolean ?: false
                    val tgt = rawAuto["target"]?.toString()?.trim() ?: "all"
                    val msgs = when (val rawMsgs = rawAuto["messages"]) {
                        is List<*> -> rawMsgs.mapNotNull { it?.toString() }
                        else -> emptyList()
                    }
                    AutoBroadcastConfig(enabled, interval, random, minPlayers, usePref, playSnd, tgt, msgs)
                } else {
                    AutoBroadcastConfig()
                }

                Tuple4(pref, snd, trusted, autoConfig)
            }
        }.onFailure { e ->
            logger.error("Failed to load config.yml", e)
        }.getOrDefault(Tuple4(DEFAULT_PREFIX, DEFAULT_SOUND, emptyList(), AutoBroadcastConfig()))

        prefix = loadedPrefix
        sound = loadedSound
        trustedUsers = loadedTrusted
        autoBroadcastConfig = loadedAuto
    }

    fun reloadConfig() {
        loadConfig()
        refreshCommands()
        startAutoBroadcast()
    }

    fun startAutoBroadcast() {
        stopAutoBroadcast()
        lastAutoMessageIndex = -1

        if (!autoBroadcastConfig.enabled || autoBroadcastConfig.messages.isEmpty()) {
            return
        }

        val interval = maxOf(1L, autoBroadcastConfig.intervalSeconds)
        autoBroadcastTask = server.scheduler.buildTask(this, Runnable {
            runAutoBroadcast()
        }).delay(interval, TimeUnit.SECONDS)
          .repeat(interval, TimeUnit.SECONDS)
          .schedule()
    }

    fun stopAutoBroadcast() {
        autoBroadcastTask?.cancel()
        autoBroadcastTask = null
    }

    private fun runAutoBroadcast() {
        val cfg = autoBroadcastConfig
        if (!cfg.enabled || cfg.messages.isEmpty()) return

        val targetPlayers: Collection<Player> = if (cfg.target.equals("all", ignoreCase = true)) {
            server.allPlayers
        } else {
            val serverOpt = server.getServer(cfg.target)
            if (!serverOpt.isPresent) {
                logger.warn("Auto-broadcast configured target server '{}' was not found.", cfg.target)
                return
            }
            serverOpt.get().playersConnected
        }

        if (targetPlayers.size < cfg.minPlayers) {
            return
        }

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

        val rawMessage = messages[nextIndex]
        val messageText = if (cfg.usePrefix) prefix + rawMessage else rawMessage
        val component = miniMessage.deserialize(messageText)

        targetPlayers.forEach { it.sendMessage(component) }
        server.consoleCommandSource.sendMessage(component)

        if (cfg.playSound) {
            playBroadcastSound(targetPlayers)
        }
    }

    fun normalizeSoundKey(raw: String): String {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return ""

        if (trimmed.contains(":") || trimmed.contains(".")) {
            val lower = trimmed.lowercase(Locale.ROOT)
            return if (lower.contains(":")) lower else "minecraft:$lower"
        }

        val lower = trimmed.lowercase(Locale.ROOT)
        val categories = listOf("ambient_", "block_", "enchant_", "entity_", "event_", "item_", "music_", "particle_", "ui_", "weather_")
        val matchedPrefix = categories.firstOrNull { lower.startsWith(it) }

        return if (matchedPrefix != null) {
            val category = matchedPrefix.removeSuffix("_")
            val remainder = lower.removePrefix(matchedPrefix)
            val lastUnderscore = remainder.lastIndexOf('_')
            val soundPath = if (lastUnderscore != -1) {
                remainder.substring(0, lastUnderscore) + "." + remainder.substring(lastUnderscore + 1)
            } else {
                remainder
            }
            "minecraft:$category.$soundPath"
        } else {
            "minecraft:" + lower.replace('_', '.')
        }
    }

    fun playBroadcastSound(targets: Collection<Player>) {
        val rawSound = sound.takeIf { it.isNotBlank() } ?: return
        if (targets.isEmpty()) return

        val soundKey = normalizeSoundKey(rawSound)
        if (soundKey.isBlank()) return

        try {
            val key = Key.key(soundKey)
            val adventureSound = Sound.sound(key, Sound.Source.MASTER, 1.0f, 1.0f)
            targets.forEach { player ->
                player.playSound(adventureSound)
            }
        } catch (e: InvalidKeyException) {
            logger.warn("Invalid sound key specified in config.yml: '{}' (normalized to '{}')", rawSound, soundKey)
        } catch (e: Exception) {
            logger.warn("Failed to play broadcast sound '{}': {}", rawSound, e.message)
        }
    }

    fun refreshCommands() {
        server.allPlayers.forEach { player ->
            runCatching {
                val method = sequenceOf(
                    player.javaClass.methods.asSequence(),
                    player.javaClass.declaredMethods.asSequence()
                ).flatten().firstOrNull {
                    it.name in listOf("updateCommands", "resendCommands", "sendAvailableCommands") && it.parameterCount == 0
                }
                method?.let {
                    it.isAccessible = true
                    it.invoke(player)
                }
            }
        }
    }

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

    private data class Tuple4<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)
}
