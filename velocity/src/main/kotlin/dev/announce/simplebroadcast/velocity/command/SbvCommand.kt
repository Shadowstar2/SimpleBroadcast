package dev.announce.simplebroadcast.velocity.command

import com.velocitypowered.api.command.SimpleCommand
import com.velocitypowered.api.command.SimpleCommand.Invocation
import dev.announce.simplebroadcast.velocity.SimpleBroadcastVelocity
import java.util.Locale
import java.util.concurrent.CompletableFuture

class SbvCommand(private val plugin: SimpleBroadcastVelocity) : SimpleCommand {

    override fun execute(invocation: Invocation) {
        val source = invocation.source()
        if (!source.hasPermission("simplebroadcast.reload")) {
            source.sendMessage(plugin.miniMessage.deserialize("<red>You don't have permission to do that.</red>"))
            return
        }

        val args = invocation.arguments()
        if (args.isNotEmpty() && args[0].equals("reload", ignoreCase = true)) {
            plugin.reloadConfig()
            source.sendMessage(plugin.miniMessage.deserialize("<green>SimpleBroadcast config reloaded.</green>"))
            return
        }

        source.sendMessage(plugin.miniMessage.deserialize("<red>Usage: /${invocation.alias()} reload</red>"))
    }

    override fun hasPermission(invocation: Invocation): Boolean =
        invocation.source().hasPermission("simplebroadcast.reload")

    override fun suggestAsync(invocation: Invocation): CompletableFuture<List<String>> {
        val source = invocation.source()
        if (!source.hasPermission("simplebroadcast.reload")) {
            return CompletableFuture.completedFuture(emptyList())
        }

        val args = invocation.arguments()
        return if (args.size <= 1) {
            val current = args.firstOrNull()?.lowercase(Locale.ROOT).orEmpty()
            if ("reload".startsWith(current)) {
                CompletableFuture.completedFuture(listOf("reload"))
            } else {
                CompletableFuture.completedFuture(emptyList())
            }
        } else {
            CompletableFuture.completedFuture(emptyList())
        }
    }
}
