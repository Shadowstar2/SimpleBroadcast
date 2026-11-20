package dev.announce.simplebroadcast.velocity.command;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import dev.announce.simplebroadcast.velocity.SimpleBroadcastVelocity;
import net.kyori.adventure.text.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class BroadcastCommand implements SimpleCommand {

    private static final List<String> COMMON_TAGS = List.of(
        "<black>", "<dark_blue>", "<dark_green>", "<dark_aqua>", "<dark_red>",
        "<dark_purple>", "<gold>", "<gray>", "<dark_gray>", "<blue>",
        "<green>", "<aqua>", "<red>", "<light_purple>", "<yellow>",
        "<white>", "<bold>", "<italic>", "<underlined>", "<strikethrough>",
        "<obfuscated>", "<reset>", "<rainbow>", "<gradient:#3939FF:#782AD4>"
    );

    private final SimpleBroadcastVelocity plugin;

    public BroadcastCommand(SimpleBroadcastVelocity plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();

        if (!source.hasPermission("simplebroadcast.use")) {
            source.sendMessage(plugin.getMiniMessage().deserialize("<red>You don't have permission to do that.</red>"));
            return;
        }

        String[] args = invocation.arguments();
        if (args.length < 2) {
            source.sendMessage(plugin.getMiniMessage().deserialize("<red>Usage: /" + invocation.alias() + " <all|server> <message></red>"));
            return;
        }

        String target = args[0];
        String rawMessage = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        Component message = plugin.getMiniMessage().deserialize(plugin.getPrefix() + rawMessage);

        ProxyServer server = plugin.getServer();

        if (target.equalsIgnoreCase("all")) {
            for (Player player : server.getAllPlayers()) {
                player.sendMessage(message);
            }
            server.getConsoleCommandSource().sendMessage(message);
            return;
        }

        Optional<RegisteredServer> targetServer = server.getServer(target);
        if (targetServer.isPresent()) {
            for (Player player : targetServer.get().getPlayersConnected()) {
                player.sendMessage(message);
            }
            server.getConsoleCommandSource().sendMessage(message);
            return;
        }

        // Target is invalid - show clean error listing valid targets
        List<String> validTargets = new ArrayList<>();
        validTargets.add("all");
        server.getAllServers().stream()
            .map(s -> s.getServerInfo().getName())
            .sorted(String.CASE_INSENSITIVE_ORDER)
            .forEach(validTargets::add);

        String validTargetsStr = String.join(", ", validTargets);
        source.sendMessage(plugin.getMiniMessage().deserialize(
            "<red>Invalid target '" + target + "'. Valid targets: <yellow>" + validTargetsStr + "</yellow></red>"
        ));
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("simplebroadcast.use");
    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
        CommandSource source = invocation.source();
        if (!source.hasPermission("simplebroadcast.use")) {
            return CompletableFuture.completedFuture(List.of());
        }

        String[] args = invocation.arguments();
        ProxyServer server = plugin.getServer();

        // arg 1 suggests "all" + registered server names
        if (args.length <= 1) {
            String current = args.length == 1 ? args[0].toLowerCase(Locale.ROOT) : "";
            List<String> suggestions = new ArrayList<>();

            if ("all".startsWith(current)) {
                suggestions.add("all");
            }
            for (RegisteredServer rs : server.getAllServers()) {
                String serverName = rs.getServerInfo().getName();
                if (serverName.toLowerCase(Locale.ROOT).startsWith(current)) {
                    suggestions.add(serverName);
                }
            }
            return CompletableFuture.completedFuture(suggestions);
        }

        // arg 2+ suggests common MiniMessage tags + online proxy player names
        String current = args[args.length - 1].toLowerCase(Locale.ROOT);
        List<String> suggestions = new ArrayList<>();

        for (String tag : COMMON_TAGS) {
            if (tag.toLowerCase(Locale.ROOT).startsWith(current) ||
                (!current.isEmpty() && tag.toLowerCase(Locale.ROOT).substring(1).startsWith(current))) {
                suggestions.add(tag);
            }
        }

        for (Player player : server.getAllPlayers()) {
            if (player.getUsername().toLowerCase(Locale.ROOT).startsWith(current)) {
                suggestions.add(player.getUsername());
            }
        }

        return CompletableFuture.completedFuture(suggestions);
    }
}
