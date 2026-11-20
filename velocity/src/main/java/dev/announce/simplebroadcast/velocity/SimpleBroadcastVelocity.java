package dev.announce.simplebroadcast.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import dev.announce.simplebroadcast.velocity.command.BroadcastCommand;
import dev.announce.simplebroadcast.velocity.command.SbvCommand;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.slf4j.Logger;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@Plugin(
    id = "simplebroadcastvelocity",
    name = "SimpleBroadcastVelocity",
    version = "1.1.0",
    description = "Velocity broadcast plugin with MiniMessage support.",
    authors = {"custom"}
)
public class SimpleBroadcastVelocity {

    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;
    private final MiniMessage miniMessage;

    private String prefix = "<bold><gradient:#3939FF:#782AD4>[Announce]</gradient></bold> ";

    @Inject
    public SimpleBroadcastVelocity(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
        this.miniMessage = MiniMessage.miniMessage();
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        loadConfig();

        CommandManager commandManager = server.getCommandManager();

        CommandMeta broadcastMeta = commandManager.metaBuilder("broadcast")
            .aliases("bc", "bcast", "announce")
            .plugin(this)
            .build();
        commandManager.register(broadcastMeta, new BroadcastCommand(this));

        CommandMeta sbvMeta = commandManager.metaBuilder("simplebroadcastvelocity")
            .aliases("sbv")
            .plugin(this)
            .build();
        commandManager.register(sbvMeta, new SbvCommand(this));

        logger.info("SimpleBroadcastVelocity enabled.");
    }

    public void loadConfig() {
        try {
            if (!Files.exists(dataDirectory)) {
                Files.createDirectories(dataDirectory);
            }
            Path configFile = dataDirectory.resolve("config.yml");
            if (!Files.exists(configFile)) {
                try (InputStream in = getClass().getClassLoader().getResourceAsStream("config.yml")) {
                    if (in != null) {
                        Files.copy(in, configFile);
                    } else {
                        Files.writeString(configFile, "prefix: \"<bold><gradient:#3939FF:#782AD4>[Announce]</gradient></bold> \"\n");
                    }
                }
            }

            Yaml yaml = new Yaml();
            try (InputStream in = Files.newInputStream(configFile)) {
                Map<String, Object> data = yaml.load(in);
                if (data != null && data.containsKey("prefix")) {
                    Object val = data.get("prefix");
                    this.prefix = val != null ? String.valueOf(val) : "";
                } else {
                    this.prefix = "<bold><gradient:#3939FF:#782AD4>[Announce]</gradient></bold> ";
                }
            }
        } catch (Exception e) {
            logger.error("Failed to load config.yml", e);
            this.prefix = "<bold><gradient:#3939FF:#782AD4>[Announce]</gradient></bold> ";
        }
    }

    public void reloadConfig() {
        loadConfig();
    }

    public ProxyServer getServer() {
        return server;
    }

    public Logger getLogger() {
        return logger;
    }

    public MiniMessage getMiniMessage() {
        return miniMessage;
    }

    public String getPrefix() {
        return prefix;
    }
}
