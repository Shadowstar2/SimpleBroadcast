# SimpleBroadcast

A lightweight, modern Minecraft broadcast solution written in idiomatic **Kotlin 2.1** for **Paper** servers (1.21+) and **Velocity** proxies (3.4+). Features full Adventure MiniMessage formatting, rich target routing, optional sound notifications, PlaceholderAPI integration, configurable scheduled auto-broadcasts, smart tab completion, live Brigadier command syncing, and strict default-deny permissions with trusted user access controls.

---

## Highlights in v1.3.0

- **Configurable Scheduled Auto-Broadcasts**: Both Paper and Velocity support automated periodic broadcasts (`auto-broadcast`) with configurable interval, random or sequential message cycling, minimum player requirements, optional prefix prepending, sound playback, and target server routing (Velocity).
- **Velocity Broadcast Sound Support**: Native Adventure sound playback on Velocity proxies supporting standard Minecraft sound keys (`minecraft:entity.experience_orb.pickup`) and Bukkit enum names (`ENTITY_EXPERIENCE_ORB_PICKUP`) with graceful fallback.
- **Strict Default-Deny Permissions**: Both Paper and Velocity enforce strict default-deny access controls. Ordinary players cannot execute or tab-complete any commands unless explicitly granted permission via a permission plugin (e.g. LuckPerms), operator status, or `trusted-users`.
- **Live Brigadier Command Refresh**: Reloading the Velocity config immediately resends available commands to all online players, giving newly added `trusted-users` instant tab completion and execution rights without relogging.
- **Native `/sbv` Alias**: `/sbv` is registered directly as an alias of `/simplebroadcastvelocity`, sharing identical permission checks and tab-completion.
- **Trusted Users Access Control**: Configurable `trusted-users` list (usernames or UUIDs) enabling permission-free usage on vanilla/semi-vanilla servers and proxies without LuckPerms.
- **Unified Access Checks**: Centralized `hasAccess` helper enforcing permission, console bypass, and trusted-user verification across command execution, Brigadier permissions, and tab completion.
- **Embedded & Relocated Kotlin Runtime**: `kotlin-stdlib` is shaded and relocated (`dev.announce.simplebroadcast.libs.kotlin`) into both plugin JARs.
- **Clean Artifacts**: Dedicated standalone shaded builds for Paper (`SimpleBroadcast-Paper-1.3.0.jar`) and Velocity (`SimpleBroadcast-Velocity-1.3.0.jar`), with unshaded archives tagged as `-plain`.

---

## Modules

SimpleBroadcast is organized as a multi-module Gradle project:

- **`paper`**: Standalone Bukkit/Paper plugin with sound playback, MiniMessage formatting, scheduled auto-broadcasts, trusted user support, and optional PlaceholderAPI integration.
- **`velocity`**: Standalone Velocity proxy plugin supporting network-wide (`all`) or per-backend server broadcasts, proxy sound notifications, scheduled auto-broadcasts, live command sync, trusted user checks, and async tab completion.

---

## Paper Plugin (`paper`)

### Features
- **MiniMessage Formatting**: Complete support for Kyori Adventure's MiniMessage format (hex colors, gradients, rainbows, hover, click events).
- **PlaceholderAPI Support**: Seamless soft-dependency with PlaceholderAPI. Resolves player placeholders when sent by players and server-wide placeholders (e.g., `%server_online%`) for console broadcasts and auto-broadcasts.
- **Broadcast Sound Effect**: Plays a customizable Bukkit sound (default: `ENTITY_EXPERIENCE_ORB_PICKUP`) to all online players upon broadcasting, or leave blank to disable.
- **Scheduled Auto-Broadcasts**: Configurable repeating timer with min-player threshold, random or sequential cycling, optional prefix, and optional sound notifications.
- **Smart Tab Completion**: Auto-completes `reload` for `/simplebroadcast` and suggests common MiniMessage tags as well as online player names for `/broadcast` (restricted to permitted/trusted users).
- **Customizable Prefix**: Configurable broadcast prefix in `config.yml` supporting MiniMessage tags.
- **Trusted Users**: Allow specific players by username or UUID to execute broadcasts without a permission manager.

### Commands & Aliases
| Command | Aliases | Description | Permission |
| :--- | :--- | :--- | :--- |
| `/broadcast <message>` | `/bc`, `/bcast`, `/announce` | Broadcasts a formatted message to all online players and console. | `simplebroadcast.use` |
| `/simplebroadcast reload` | *None* | Reloads the Paper plugin configuration (`config.yml`) and restarts auto-broadcast scheduler. | `simplebroadcast.reload` |

### Permissions
| Permission | Description | Default |
| :--- | :--- | :--- |
| `simplebroadcast.use` | Allows execution and tab-completion of `/broadcast` and its aliases. | `op` |
| `simplebroadcast.reload` | Allows reloading configuration via `/simplebroadcast reload`. | `op` |

*Note: Any player listed under `trusted-users` in `config.yml` bypasses permission requirements.*

### Configuration (`plugins/SimpleBroadcast/config.yml`)
```yaml
# SimpleBroadcast config
# Supports full MiniMessage formatting: https://docs.advntr.dev/minimessage/format.html

prefix: "<bold><gradient:#3939FF:#782AD4>[Announce]</gradient></bold> "

# Sound played to all players when a broadcast is sent.
# Set to a valid Bukkit Sound name (e.g., ENTITY_EXPERIENCE_ORB_PICKUP), or leave blank ("") to disable.
sound: "ENTITY_EXPERIENCE_ORB_PICKUP"

# Optional list of player usernames or UUIDs allowed to use SimpleBroadcast commands
# Intended for servers/proxies without LuckPerms or a permission plugin (leave empty if using LuckPerms).
trusted-users: []

# Configurable scheduled auto-broadcasts
auto-broadcast:
  enabled: false
  interval-seconds: 300
  random: false
  min-players: 1
  use-prefix: true
  play-sound: false
  messages:
    - "<green>Enjoying the server? Join our Discord at <click:open_url:'https://discord.gg/example'><underlined><aqua>discord.gg/example</aqua></underlined></click>!"
    - "<gradient:#ff5555:#ffff55>Remember to vote daily</gradient> to earn rewards and support the server!"
    - "<yellow>Check out our server store for ranks, perks, and cosmetics at <click:open_url:'https://store.example.com'><gold><underlined>store.example.com</underlined></gold></click>!"
```

---

## Velocity Proxy Plugin (`velocity`)

### Features
- **Proxy-Wide & Server-Targeted Broadcasting**: Broadcast to the entire proxy network (`all`) or target a specific registered backend server (`<server>`), broadcasting to target players and the proxy console.
- **Broadcast Sound Effect**: Plays a customizable sound to targeted players using Kyori Adventure (default: `minecraft:entity.experience_orb.pickup` or Bukkit sound names), or leave blank to disable.
- **Scheduled Auto-Broadcasts**: Configurable repeating timer with target choice (`all` or server), min-player threshold, random or sequential cycling, optional prefix, and optional sound notifications.
- **Standalone Proxy Plugin**: Operates directly on the Velocity proxy without needing any database or backend sub-plugins.
- **Invalid Target Detection**: Clean error feedback detailing all currently registered target choices if an unknown target is entered.
- **MiniMessage Support**: Complete Kyori Adventure MiniMessage rendering for prefixes and broadcast messages.
- **Live Brigadier Command Sync**: Automatically resends command trees to all connected players upon reloading config.
- **Async Tab Completion**:
  - Argument 1: Suggests `all` and all registered backend server names.
  - Argument 2+: Suggests common MiniMessage tags and online proxy player names.
  - `/sbv`: Suggests `reload` when permitted.
- **Trusted Users**: Allow specific proxy players by username or UUID to execute broadcasts without a permission manager.

### Commands & Aliases
| Command | Aliases | Description | Permission |
| :--- | :--- | :--- | :--- |
| `/broadcast <all|server> <message>` | `/bc`, `/bcast`, `/announce` | Broadcasts to all proxy players or players on the specified backend server (+ console). | `simplebroadcast.use` |
| `/simplebroadcastvelocity reload` | `/sbv` | Reloads the Velocity plugin configuration (`config.yml`), resyncs commands, and restarts auto-broadcast scheduler. | `simplebroadcast.reload` |

### Permissions
| Permission | Description | Default |
| :--- | :--- | :--- |
| `simplebroadcast.use` | Allows execution and tab completion of `/broadcast` and its aliases. | `false (op / trusted-users)` |
| `simplebroadcast.reload` | Allows reloading configuration via `/simplebroadcastvelocity reload` (`/sbv`). | `op` |

*Note: Any player listed under `trusted-users` in `config.yml` bypasses permission requirements.*

### Configuration (`plugins/simplebroadcastvelocity/config.yml`)
```yaml
# SimpleBroadcast config for Velocity
# Supports full MiniMessage formatting: https://docs.advntr.dev/minimessage/format.html

prefix: "<bold><gradient:#3939FF:#782AD4>[Announce]</gradient></bold> "

# Sound played to target players when a broadcast is sent.
# Any Minecraft sound key (e.g., "minecraft:entity.experience_orb.pickup") or Bukkit sound enum (e.g., "ENTITY_EXPERIENCE_ORB_PICKUP") can be used.
# Leave blank ("") to disable sound playback.
sound: "minecraft:entity.experience_orb.pickup"

# Optional list of player usernames or UUIDs allowed to use SimpleBroadcast commands
# Intended for servers/proxies without LuckPerms or a permission plugin (leave empty if using LuckPerms).
trusted-users: []

# Configurable scheduled auto-broadcasts
auto-broadcast:
  enabled: false
  interval-seconds: 300
  random: false
  min-players: 1
  use-prefix: true
  play-sound: false
  target: "all"
  messages:
    - "<green>Enjoying the server? Join our Discord at <click:open_url:'https://discord.gg/example'><underlined><aqua>discord.gg/example</aqua></underlined></click>!"
    - "<gradient:#ff5555:#ffff55>Remember to vote daily</gradient> to earn rewards and support the server!"
    - "<yellow>Check out our server store for ranks, perks, and cosmetics at <click:open_url:'https://store.example.com'><gold><underlined>store.example.com</underlined></gold></click>!"
```

---

## Formatting Examples

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
3. Compiled standalone artifacts will be located at:
   - Paper: `paper/build/libs/SimpleBroadcast-Paper-1.3.0.jar`
   - Velocity: `velocity/build/libs/SimpleBroadcast-Velocity-1.3.0.jar`

---

## License

This project is licensed under the [MIT License](LICENSE).
