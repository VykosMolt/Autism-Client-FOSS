# Commands Subsystem (`autismclient.commands.*`)

AUTISM Client implements client-side chat commands using Mojang's **Brigadier** command parser. Phase 2 renamed all `40` classes in `autismclient.commands.impl` (formerly `commands.b`: `39` built-in `*Command` subclasses + `SlotClickCommandHelper`), Phase 3A renamed the remaining `3` core classes in `autismclient.commands` and all `8` custom Brigadier argument types in `autismclient.commands.arguments` (formerly `commands.a`), and Phase 8A consolidated both subpackages (`0/53` obfuscated remaining across the entire commands subsystem).

## Package Layout

| Package | Files | Status | Role |
|---|---:|---|---|
| `autismclient.commands` | 5 | **0 obf / 5 tot** | Core base (`Command`), source (`AutismCommandSource`), dispatcher/registry (`CommandManager`), completion helpers (`CommandSuggestions`, `RegistrySuggestions`) |
| `autismclient.commands.arguments` | 8 | **0 obf / 8 tot** | All 8 custom Brigadier `*ArgumentType` implementations (moved from `commands.a` in Phase 8A) |
| `autismclient.commands.impl` | 40 | **0 obf / 40 tot** | All 39 built-in commands + `SlotClickCommandHelper` (moved from `commands.b` in Phase 8A) |

## Core Classes (`autismclient.commands`)
- `Command` (`41` LOC) — abstract base class for all client commands (`super(name, description, aliases...)`).
- `AutismCommandSource` (`32` LOC) — Brigadier command source carrying client context and feedback helpers.
- `CommandManager` (`470` LOC) — command registry, prefix handling, and Brigadier `CommandDispatcher<AutismCommandSource>` setup.
- `CommandSuggestions` (`258` LOC) — Brigadier tab-completion providers for toggles, numeric presets, waypoints, prefixes, blocks, entities, and items.
- `RegistrySuggestions` (`204` LOC) — fuzzy item/block registry ID resolver and player-inventory item completion helper.

## Custom Argument Types (`autismclient.commands.arguments`)
| Class | Type Parameter | Parses |
|---|---|---|
| `SlotArgumentType` | `Integer` | Inventory / container / armor slot indices (`hotbar`, `gui`, `inventory`, `helmet`..`boots`) |
| `MacroNameArgumentType` | `String` | Saved macro names with tab-completions |
| `ModuleArgumentType` | `String` | Module / HUD identifiers (`all`, `hud`, module IDs) |
| `ItemOrBlockIdArgumentType` | `String` | Registry item or block IDs |
| `PacketNameArgumentType` | `String` | C2S packet class names (for `send`) |
| `ContainerItemNameArgumentType` | `String` | Visible item names inside an open container (for `click-item`) |
| `KeyBindArgumentType` | `Integer` | GLFW key names / codes (`NONE`, `KEY_*`, `GLFW_KEY_*`) |
| `XCarrySlotArgumentType` | `Integer` | XCarry slot names (`craft1`..`craft5`, `result`, armor/offhand/cursor) |

## Built-In Commands (`autismclient.commands.impl`)

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
