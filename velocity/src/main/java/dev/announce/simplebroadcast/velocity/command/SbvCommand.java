package dev.announce.simplebroadcast.velocity.command;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import dev.announce.simplebroadcast.velocity.SimpleBroadcastVelocity;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public class SbvCommand implements SimpleCommand {

    private final SimpleBroadcastVelocity plugin;

    public SbvCommand(SimpleBroadcastVelocity plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();

        if (!source.hasPermission("simplebroadcast.reload")) {
            source.sendMessage(plugin.getMiniMessage().deserialize("<red>You don't have permission to do that.</red>"));
            return;
        }

        String[] args = invocation.arguments();
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            plugin.reloadConfig();
            source.sendMessage(plugin.getMiniMessage().deserialize("<green>SimpleBroadcast config reloaded.</green>"));
            return;
        }

        source.sendMessage(plugin.getMiniMessage().deserialize("<red>Usage: /" + invocation.alias() + " reload</red>"));
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("simplebroadcast.reload");
    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
        CommandSource source = invocation.source();
        if (!source.hasPermission("simplebroadcast.reload")) {
            return CompletableFuture.completedFuture(List.of());
        }

        String[] args = invocation.arguments();
        if (args.length <= 1) {
            String current = args.length == 1 ? args[0].toLowerCase(Locale.ROOT) : "";
            if ("reload".startsWith(current)) {
                return CompletableFuture.completedFuture(List.of("reload"));
            }
        }

        return CompletableFuture.completedFuture(List.of());
    }
}
