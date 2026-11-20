package dev.announce.simplebroadcast.velocity

import com.google.inject.Inject
import com.velocitypowered.api.command.CommandMeta
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.plugin.annotation.DataDirectory
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
    version = "1.2.0",
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
        prefix = runCatching {
            if (!dataDirectory.exists()) {
                dataDirectory.createDirectories()
            }
            val configFile = dataDirectory.resolve("config.yml")
            if (!configFile.exists()) {
                javaClass.classLoader.getResourceAsStream("config.yml")?.use { input ->
                    Files.copy(input, configFile)
                } ?: Files.writeString(configFile, "prefix: \"$DEFAULT_PREFIX\"\n")
            }

            configFile.inputStream().use { input ->
                val data = Yaml().load<Map<String, Any>?>(input)
                data?.get("prefix")?.toString() ?: DEFAULT_PREFIX
            }
        }.onFailure { e ->
            logger.error("Failed to load config.yml", e)
        }.getOrDefault(DEFAULT_PREFIX)
    }

    fun reloadConfig() = loadConfig()
}
