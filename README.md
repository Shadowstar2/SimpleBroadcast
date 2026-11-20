# SimpleBroadcast

A lightweight, modern Minecraft broadcast plugin for Paper servers (1.21+) featuring full Adventure MiniMessage formatting and optional PlaceholderAPI integration.

---

## Features

- **MiniMessage Formatting**: Full support for Kyori Adventure's MiniMessage format, including hex colors, gradients, rainbows, bold, italic, hover events, and click events.
- **PlaceholderAPI Support**: Seamless soft-dependency with PlaceholderAPI. Player placeholders in broadcast messages and prefix are automatically resolved when a player sends the broadcast.
- **Customizable Prefix**: Configurable broadcast prefix in `config.yml` supporting MiniMessage tags.
- **Simple & Fast**: Minimal overhead, clean Kotlin DSL Gradle build script, and zero unnecessary bloat.

---

## Commands & Aliases

| Command | Aliases | Description | Permission |
| :--- | :--- | :--- | :--- |
| `/broadcast <message>` | `/bc`, `/bcast`, `/announce` | Broadcasts a formatted message to all online players and console. | `simplebroadcast.use` |
| `/simplebroadcast reload` | *None* | Reloads the plugin configuration (`config.yml`). | `simplebroadcast.reload` |

---

## Permissions

| Permission | Description | Default |
| :--- | :--- | :--- |
| `simplebroadcast.use` | Allows execution of `/broadcast` and its aliases. | `op` |
| `simplebroadcast.reload` | Allows reloading configuration via `/simplebroadcast reload`. | `op` |

---

## Configuration

The default configuration file (`config.yml`):

```yaml
# SimpleBroadcast config
# Supports full MiniMessage formatting: https://docs.advntr.dev/minimessage/format.html

prefix: "<bold><gradient:#3939FF:#782AD4>[Announce]</gradient></bold> "
```

You can customize the prefix using any valid MiniMessage tag or gradient.

---

## Formatting Examples

### MiniMessage Support

SimpleBroadcast supports all standard tags from [Adventure MiniMessage](https://docs.advntr.dev/minimessage/format.html):

- **Colors & Decorations**:
  ```text
  /broadcast <yellow>Server restarting in <red><bold>5 minutes</bold></red>!
  ```
- **Gradients & Rainbow**:
  ```text
  /broadcast <gradient:#ff5555:#ffff55>Welcome new players to our server!</gradient>
  /broadcast <rainbow>Celebration event is now live!</rainbow>
  ```
- **Clickable Links & Hovers**:
  ```text
  /broadcast <click:open_url:'https://discord.gg/example'><hover:show_text:'<gray>Click to join our Discord server</gray>'><aqua><underlined>Join our Discord</underlined></aqua></hover></click>
  ```

### PlaceholderAPI Support

If [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) is installed on the server, placeholders are parsed when executed by players:

- Display sender's name:
  ```text
  /broadcast <yellow>%player_name% has started a community challenge!
  ```
- Display player statistics or world:
  ```text
  /broadcast <green>%player_name%</green> reached level <gold>%player_level%</gold> in <aqua>%player_world%</aqua>!
  ```

> **Note**: Console broadcasts do not have an associated player, so player-specific placeholders will remain unparsed if sent via the server console.

---

## Building from Source

This project uses Gradle with the Kotlin DSL and Java 21.

1. Clone the repository:
   ```bash
   git clone https://github.com/Shadowstar2/SimpleBroadcast.git
   cd SimpleBroadcast
   ```
2. Build the shadow JAR:
   ```bash
   ./gradlew build
   ```
3. The compiled plugin JAR will be located at:
   ```text
   build/libs/SimpleBroadcast-1.0.0.jar
   ```

---

## License

This project is licensed under the [MIT License](LICENSE).
