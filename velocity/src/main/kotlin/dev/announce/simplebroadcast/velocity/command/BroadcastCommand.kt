package dev.announce.simplebroadcast.velocity.command

import com.velocitypowered.api.command.SimpleCommand
import com.velocitypowered.api.command.SimpleCommand.Invocation
import dev.announce.simplebroadcast.velocity.SimpleBroadcastVelocity
import java.util.Locale
import java.util.concurrent.CompletableFuture

class BroadcastCommand(private val plugin: SimpleBroadcastVelocity) : SimpleCommand {

    companion object {
        private val COMMON_TAGS = listOf(
            "<black>", "<dark_blue>", "<dark_green>", "<dark_aqua>", "<dark_red>",
            "<dark_purple>", "<gold>", "<gray>", "<dark_gray>", "<blue>",
            "<green>", "<aqua>", "<red>", "<light_purple>", "<yellow>",
            "<white>", "<bold>", "<italic>", "<underlined>", "<strikethrough>",
            "<obfuscated>", "<reset>", "<rainbow>", "<gradient:#3939FF:#782AD4>"
        )
    }

    override fun execute(invocation: Invocation) {
        val source = invocation.source()
        if (!plugin.hasAccess(source, "simplebroadcast.use")) {
            source.sendMessage(plugin.miniMessage.deserialize("<red>You don't have permission to do that.</red>"))
            return
        }

        val args = invocation.arguments()
        if (args.size < 2) {
            source.sendMessage(plugin.miniMessage.deserialize("<red>Usage: /${invocation.alias()} <all|server> <message></red>"))
            return
        }

        val target = args[0]
        val rawMessage = args.drop(1).joinToString(" ")
        val message = plugin.miniMessage.deserialize(plugin.prefix + rawMessage)
        val server = plugin.server

        when {
            target.equals("all", ignoreCase = true) -> {
                val players = server.allPlayers
                players.forEach { it.sendMessage(message) }
                server.consoleCommandSource.sendMessage(message)
                plugin.playBroadcastSound(players)
            }
            else -> {
                val targetServer = server.getServer(target)
                if (targetServer.isPresent) {
                    val players = targetServer.get().playersConnected
                    players.forEach { it.sendMessage(message) }
                    server.consoleCommandSource.sendMessage(message)
                    plugin.playBroadcastSound(players)
                } else {
                    val validTargets = (listOf("all") + server.allServers.map { it.serverInfo.name }.sortedWith(String.CASE_INSENSITIVE_ORDER))
                        .joinToString(", ")
                    source.sendMessage(plugin.miniMessage.deserialize(
                        "<red>Invalid target '$target'. Valid targets: <yellow>$validTargets</yellow></red>"
                    ))
                }
            }
        }
    }

    override fun hasPermission(invocation: Invocation): Boolean =
        plugin.hasAccess(invocation.source(), "simplebroadcast.use")

    override fun suggestAsync(invocation: Invocation): CompletableFuture<List<String>> {
        val source = invocation.source()
        if (!plugin.hasAccess(source, "simplebroadcast.use")) {
            return CompletableFuture.completedFuture(emptyList())
        }

        val args = invocation.arguments()
        val server = plugin.server

        if (args.size <= 1) {
            val current = args.firstOrNull()?.lowercase(Locale.ROOT).orEmpty()
            val allSuggestion = listOf("all").filter { it.startsWith(current) }
            val serverSuggestions = server.allServers
                .map { it.serverInfo.name }
                .filter { it.lowercase(Locale.ROOT).startsWith(current) }
            return CompletableFuture.completedFuture(allSuggestion + serverSuggestions)
        }

        val current = args.last().lowercase(Locale.ROOT)
        val tagSuggestions = COMMON_TAGS.filter { tag ->
            val lower = tag.lowercase(Locale.ROOT)
            lower.startsWith(current) || (current.isNotEmpty() && lower.removePrefix("<").startsWith(current))
        }
        val playerSuggestions = server.allPlayers
            .map { it.username }
            .filter { it.lowercase(Locale.ROOT).startsWith(current) }

        return CompletableFuture.completedFuture(tagSuggestions + playerSuggestions)
    }
}
