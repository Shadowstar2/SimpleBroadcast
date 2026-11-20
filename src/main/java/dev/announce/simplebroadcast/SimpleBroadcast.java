package dev.announce.simplebroadcast;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class SimpleBroadcast extends JavaPlugin {

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
        if (args.length == 0) {
            sender.sendMessage(miniMessage.deserialize("<red>Usage: /" + label + " <message>"));
            return true;
        }

        String prefix = getConfig().getString("prefix", "<bold><gradient:#3939FF:#782AD4>[Announce]</gradient></bold> ");
        String rawMessage = String.join(" ", args);

        // Apply PlaceholderAPI if available and sender is a player
        if (papiEnabled && sender instanceof Player player) {
            rawMessage = PlaceholderAPI.setPlaceholders(player, rawMessage);
            prefix = PlaceholderAPI.setPlaceholders(player, prefix);
        }

        Component message = miniMessage.deserialize(prefix + rawMessage);
        Bukkit.getServer().sendMessage(message);

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return List.of();
    }
}
