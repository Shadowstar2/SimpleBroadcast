package dev.announce.simplebroadcast;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SimpleBroadcast extends JavaPlugin {

    private static final List<String> COMMON_TAGS = List.of(
        "<black>", "<dark_blue>", "<dark_green>", "<dark_aqua>", "<dark_red>",
        "<dark_purple>", "<gold>", "<gray>", "<dark_gray>", "<blue>",
        "<green>", "<aqua>", "<red>", "<light_purple>", "<yellow>",
        "<white>", "<bold>", "<italic>", "<underlined>", "<strikethrough>",
        "<obfuscated>", "<reset>", "<rainbow>", "<gradient:#3939FF:#782AD4>"
    );

    private MiniMessage miniMessage;
    private boolean papiEnabled;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        miniMessage = MiniMessage.miniMessage();
        papiEnabled = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;
        if (papiEnabled) {
            getLogger().info("PlaceholderAPI found — placeholder support enabled.");
        }
        getLogger().info("SimpleBroadcast enabled.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Handle /simplebroadcast reload
        if (command.getName().equalsIgnoreCase("simplebroadcast")) {
            if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
                if (!sender.hasPermission("simplebroadcast.reload")) {
                    sender.sendMessage(miniMessage.deserialize("<red>You don't have permission to do that."));
                    return true;
                }
                reloadConfig();
                sender.sendMessage(miniMessage.deserialize("<green>SimpleBroadcast config reloaded."));
                return true;
            }
            sender.sendMessage(miniMessage.deserialize("<red>Usage: /simplebroadcast reload"));
            return true;
        }

        // Handle /broadcast
        if (!sender.hasPermission("simplebroadcast.use")) {
            sender.sendMessage(miniMessage.deserialize("<red>You don't have permission to do that."));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(miniMessage.deserialize("<red>Usage: /" + label + " <message>"));
            return true;
        }

        String prefix = getConfig().getString("prefix", "<bold><gradient:#3939FF:#782AD4>[Announce]</gradient></bold> ");
        String rawMessage = String.join(" ", args);

        // Apply PlaceholderAPI if available (pass player if sender is a player, or null for server-wide placeholders)
        if (papiEnabled) {
            Player player = (sender instanceof Player p) ? p : null;
            rawMessage = PlaceholderAPI.setPlaceholders(player, rawMessage);
            prefix = PlaceholderAPI.setPlaceholders(player, prefix);
        }

        Component message = miniMessage.deserialize(prefix + rawMessage);
        Bukkit.getServer().sendMessage(message);

        playBroadcastSound();

        return true;
    }

    private void playBroadcastSound() {
        String soundName = getConfig().getString("sound", "ENTITY_EXPERIENCE_ORB_PICKUP");
        if (soundName != null && !soundName.isBlank()) {
            try {
                Sound sound = Sound.valueOf(soundName.trim().toUpperCase(Locale.ROOT));
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
                }
            } catch (IllegalArgumentException e) {
                getLogger().warning("Invalid sound specified in config.yml: " + soundName);
            }
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (command.getName().equalsIgnoreCase("simplebroadcast")) {
            if (args.length == 1) {
                if ("reload".startsWith(args[0].toLowerCase(Locale.ROOT))) {
                    return List.of("reload");
                }
            }
            return List.of();
        }

        if (command.getName().equalsIgnoreCase("broadcast")) {
            if (!sender.hasPermission("simplebroadcast.use")) {
                return List.of();
            }

            String current = args.length > 0 ? args[args.length - 1].toLowerCase(Locale.ROOT) : "";
            List<String> completions = new ArrayList<>();

            for (String tag : COMMON_TAGS) {
                if (tag.toLowerCase(Locale.ROOT).startsWith(current) ||
                    (!current.isEmpty() && tag.toLowerCase(Locale.ROOT).substring(1).startsWith(current))) {
                    completions.add(tag);
                }
            }

            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getName().toLowerCase(Locale.ROOT).startsWith(current)) {
                    completions.add(player.getName());
                }
            }

            return completions;
        }

        return List.of();
    }
}
