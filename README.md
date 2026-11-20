# SimpleBroadcast

A lightweight, modern Minecraft broadcast solution for Paper servers (1.21+) and Velocity proxies (3.4+) featuring Adventure MiniMessage formatting, rich target routing, and smart tab completion.

---

## Modules

SimpleBroadcast is organized as a multi-module project:

- **`paper`**: Standalone Bukkit/Paper plugin with sound notifications and optional PlaceholderAPI integration.
- **`velocity`**: Standalone Velocity proxy plugin allowing network-wide or per-backend server broadcasts.

---

## Paper Plugin (`paper`)

### Features
- **MiniMessage Formatting**: Full support for Kyori Adventure's MiniMessage format (hex colors, gradients, rainbows, hover, click events).
- **PlaceholderAPI Support**: Seamless soft-dependency with PlaceholderAPI. Resolves player placeholders when sent by players and server-wide placeholders (e.g., `%server_online%`) when sent from console.
- **Broadcast Sound Effect**: Plays a customizable sound (such as `ENTITY_EXPERIENCE_ORB_PICKUP`) to all online players upon broadcasting, or can be left blank to disable.
- **Smart Tab Completion**: Auto-completes `reload` for `/simplebroadcast` and suggests common MiniMessage tags as well as online player names for `/broadcast`.
- **Customizable Prefix**: Configurable broadcast prefix in `config.yml` supporting MiniMessage tags.

### Commands & Aliases
| Command | Aliases | Description | Permission |
| :--- | :--- | :--- | :--- |
| `/broadcast <message>` | `/bc`, `/bcast`, `/announce` | Broadcasts a formatted message to all online players and console. | `simplebroadcast.use` |
| `/simplebroadcast reload` | *None* | Reloads the Paper plugin configuration (`config.yml`). | `simplebroadcast.reload` |

### Permissions
| Permission | Description | Default |
| :--- | :--- | :--- |
| `simplebroadcast.use` | Allows execution and tab-completion of `/broadcast` and its aliases. | `op` |
| `simplebroadcast.reload` | Allows reloading configuration via `/simplebroadcast reload`. | `op` |

### Configuration (`plugins/SimpleBroadcast/config.yml`)
```yaml
# SimpleBroadcast config
# Supports full MiniMessage formatting: https://docs.advntr.dev/minimessage/format.html

prefix: "<bold><gradient:#3939FF:#782AD4>[Announce]</gradient></bold> "

# Sound played to all players when a broadcast is sent.
# Set to a valid Bukkit Sound name (e.g., ENTITY_EXPERIENCE_ORB_PICKUP), or leave blank ("") to disable.
sound: "ENTITY_EXPERIENCE_ORB_PICKUP"
```

---

## Velocity Proxy Plugin (`velocity`)

### Features
- **Proxy-Wide & Server-Targeted Broadcasting**: Broadcast to the entire proxy network (`all`) or target a specific registered backend server (`<server>`), both broadcasting to target players and the proxy console.
- **Standalone Proxy Plugin**: Operates directly on the Velocity proxy without needing any database or backend sub-plugins.
- **Invalid Target Detection**: Clean error feedback detailing all currently registered target choices if an unknown target is entered.
- **MiniMessage Support**: Complete Kyori Adventure MiniMessage rendering for prefixes and broadcast messages.
- **Async Tab Completion**:
  - Argument 1: Suggests `all` and all registered backend server names.
  - Argument 2+: Suggests common MiniMessage tags and online proxy player names.
  - `/sbv`: Suggests `reload` when permitted.

### Commands & Aliases
| Command | Aliases | Description | Permission |
| :--- | :--- | :--- | :--- |
| `/broadcast <all\|server> <message>` | `/bc`, `/bcast`, `/announce` | Broadcasts to all proxy players or players on the specified backend server (+ console). | `simplebroadcast.use` |
| `/simplebroadcastvelocity reload` | `/sbv` | Reloads the Velocity plugin configuration (`config.yml`). | `simplebroadcast.reload` |

### Permissions
| Permission | Description | Default |
| :--- | :--- | :--- |
| `simplebroadcast.use` | Allows execution and tab completion of `/broadcast` and its aliases. | `true` |
| `simplebroadcast.reload` | Allows reloading configuration via `/simplebroadcastvelocity reload` (`/sbv`). | `op` |

### Configuration (`plugins/simplebroadcastvelocity/config.yml`)
```yaml
# SimpleBroadcast config for Velocity
# Supports full MiniMessage formatting: https://docs.advntr.dev/minimessage/format.html

prefix: "<bold><gradient:#3939FF:#782AD4>[Announce]</gradient></bold> "
```

---

## Formatting Examples

### MiniMessage Support
Both Paper and Velocity modules support all standard tags from [Adventure MiniMessage](https://docs.advntr.dev/minimessage/format.html):

- **Colors & Decorations**:
  ```text
  /broadcast <yellow>Server restarting in <red><bold>5 minutes</bold></red>!
  /broadcast all <yellow>Proxy network maintenance in <red><bold>10 minutes</bold></red>!
  ```
- **Gradients & Rainbow**:
  ```text
  /broadcast <gradient:#ff5555:#ffff55>Welcome new players to our server!</gradient>
  /broadcast survival <rainbow>Celebration event is now live on Survival!</rainbow>
  ```
- **Clickable Links & Hovers**:
  ```text
  /broadcast all <click:open_url:'https://discord.gg/example'><hover:show_text:'<gray>Click to join our Discord server</gray>'><aqua><underlined>Join our Discord</underlined></aqua></hover></click>
  ```

---

## Building from Source

This project uses Gradle with the Kotlin DSL and Java 21.

1. Clone the repository:
   ```bash
   git clone https://github.com/Shadowstar2/SimpleBroadcast.git
   cd SimpleBroadcast
   ```
2. Build all modules:
   ```bash
   ./gradlew build
   ```
3. Compiled artifacts will be located at:
   - Paper: `paper/build/libs/SimpleBroadcast-Paper-1.1.0.jar`
   - Velocity: `velocity/build/libs/SimpleBroadcast-Velocity-1.1.0.jar`

---

## License

This project is licensed under the [MIT License](LICENSE).
