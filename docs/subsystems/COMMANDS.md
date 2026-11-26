# Commands Subsystem (`autismclient.commands.*`)

AUTISM Client implements client-side chat commands using Mojang's **Brigadier** command parser. Phase 2 deobfuscation renamed all `40` classes in `autismclient.commands.b` (`39` built-in `*Command` subclasses + `SlotClickCommandHelper`) to match upstream `source-contracts.json` names (`0/40` obfuscated remaining in `commands.b`).

## Package Layout

| Current Package | Files | Status | Planned Target | Role |
|---|---:|---|---|---|
| `autismclient.commands` | 5 | 3 obf / 5 tot | `autismclient.commands` | Core base (`Command`), source (`AutismCommandSource`), dispatcher/registry (`DataUtil`, `StreamUtil`, `CachePool`) |
| `autismclient.commands.a` | 8 | 8 obf / 8 tot | `autismclient.commands.arguments` | Custom Brigadier `ArgumentType` implementations |
| `autismclient.commands.b` | 40 | **0 obf / 40 tot** | `autismclient.commands.impl` | All 39 built-in commands + `SlotClickCommandHelper` |

## Core Classes (`autismclient.commands`)
- `Command` (`41` LOC) — abstract base class for all client commands (`super(name, description, aliases...)`).
- `AutismCommandSource` (`32` LOC) — Brigadier command source carrying client context and feedback helpers.
- `DataUtil` (`470` LOC) — command registry, prefix handling, and Brigadier `CommandDispatcher<AutismCommandSource>` setup.
- `StreamUtil` (`258` LOC) — command argument suggestion / completion utilities.
- `CachePool` (`204` LOC) — command keybind store and dispatcher (`bind` / `binds`).

## Custom Argument Types (`autismclient.commands.a`)
| Class | Type Parameter | Parses |
|---|---|---|
| `BufferHelper` | `Integer` | Inventory / container / armor slot indices (`hotbar`, `gui`, `inventory`, `helmet`..`boots`) |
| `CachePool` | `String` | Command / macro / player names with completions |
| `ConfigStore` | `String` | Module / HUD identifiers (`all`, `hud`, module IDs) |
| `DataUtil` | `String` | Registry item or block IDs |
| `JsonUtil` | `String` | Quoted or greedy text tokens |
| `StateCache` | `String` | Named targets / identifiers |
| `StreamUtil` | `Integer` | GLFW key names / codes (`NONE`, `KEY_*`, `GLFW_KEY_*`) |
| `SyncManager` | `Integer` | XCarry slot names (`craft1`..`craft5`, `result`, armor/offhand/cursor) |

## Built-In Commands (`autismclient.commands.b`)

| Class | Command | Summary |
|---|---|---|
| `AiCommand` | `ai` | Control the AI assistant: objectives, provider settings, speedrun, run trace |
| `BindCommand` | `bind` | Bind a key to a command (`bind <key> <command…>` / `bind clear <key>`) |
| `BindsCommand` | `binds` | List active command keybinds |
| `ChangeSlotCommand` | `change-slot` | Select hotbar slot `1`–`9` |
| `ClearCommand` | `clear` | Clear every message from client chat |
| `ClickItemCommand` | `click-item` | Click an item in an open GUI by visible name |
| `ClickSlotCommand` | `click-slot` | Click a GUI or player slot with Item Click actions (uses `SlotClickCommandHelper`) |
| `CommandsCommand` | `commands` | List all available commands |
| `CopyPosCommand` | `copypos` | Copy current coordinates formatted for `tp` |
| `DamageCommand` | `damage` | Inflict self-damage (vanilla `/damage` when permitted) |
| `DelayCommand` | `delay` | Toggle the GUI packet delay queue |
| `DisconnectCommand` | `disconnect` | Disconnect from the current server |
| `DismountCommand` | `dismount` | Force-dismount from any vehicle |
| `DropCommand` | `drop` | Drop held item, full inventory, or a count of a matching item |
| `FakeGmCommand` | `fakegm` | Client-side fake game mode illusion (no server packet) |
| `FriendCommand` | `friend` | Manage friends/teams list (`add`, `remove`, `clear`) |
| `GamemodeCommand` | `gamemode` | Switch real game mode via the `F3+F4` packet route |
| `GhostBlockCommand` | `ghostblock` | Place, remove, and manage client-side ghost blocks |
| `GiveCommand` | `give` | Give an item to self (creative only) |
| `HClipCommand` | `hclip` | Clip horizontally through blocks with movement packets |
| `HelpCommand` | `help` | Show usage info for a command or list all commands |
| `MacroCommand` | `macro` | Run a saved macro, optionally `N` times with a delay |
| `ModulesCommand` | `modules` | List installed modules |
| `NameScrapeCommand` | `namescrape` | Scrape player names to clipboard (`[count\|deep\|pause\|resume\|stop]`) |
| `NbtCommand` | `nbt` | Inspect or copy the held item's data components (NBT) |
| `OreSimCommand` | `oresim` | Diagnose why OreSim is or is not predicting ores |
| `PluginsCommand` | `plugins` | Open the server plugin scanner (alias of `server plugins`) |
| `PrefixCommand` | `prefix` | Change the AUTISM command prefix |
| `RemoteViewCommand` | `rv` | Camera-only remote view of a loaded player's POV |
| `SayCommand` | `say` | Send a chat message |
| `SendCommand` | `send` | Send a raw C2S packet by class name with optional `field=value` args |
| `ServerCommand` | `server` | Open the server info panel or plugin scanner |
| `SyncCommand` | `sync` | Synchronize a chat message, command, or macro across LAN Sync |
| `ToggleCommand` | `toggle` | Toggle a module on or off |
| `ToolsCommand` | `tools` | Run AI `game:` / `client:` tools directly without an assistant session |
| `TpCommand` | `tp` | Ground-first HClip/VClip coordinate or waypoint teleport |
| `VClipCommand` | `vclip` | Clip vertically through blocks with movement packets |
| `WaypointsCommand` | `waypoints` | Save, list, and delete per-server waypoints (`.wp`) |
| `XCarryCommand` | `xcarry` | Stash the held item into an XCarry slot (`craft1`..`craft5`, armor, offhand, cursor) |

Helper:
- `SlotClickCommandHelper` — shared container/inventory click helper used by `ClickSlotCommand` and `ClickItemCommand`.
