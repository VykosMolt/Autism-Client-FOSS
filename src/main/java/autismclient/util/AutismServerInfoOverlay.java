package autismclient.util;

import autismclient.gui.ui.widgets.SliderRenderer;
import autismclient.gui.ui.widgets.TabStripRenderer;
import autismclient.gui.ui.widgets.UiLayoutMath;
import autismclient.gui.macro.editor.ActionEditorOverlay;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import com.mojang.logging.LogUtils;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.stream.Collectors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundCommandSuggestionsPacket;
import net.minecraft.network.protocol.game.ServerboundCommandSuggestionPacket;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;

public class AutismServerInfoOverlay extends DraggableOverlayWindow {
   private static final Minecraft MC = Minecraft.getInstance();
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final String OVERLAY_ID = "autism-serverinfo";
   private static final int flush_I = 12;
   private static final int close_I = 10;
   private static final int open_I = 3;
   private static final float init_F_2 = 3.0F;
   private int overlayX = 350;
   private int overlayY = 30;
   private int overlayWidth = 236;
   private int viewportTopOffset;
   private int overlayHeight = 246;
   private static final int get_I = 16;
   private static final int set_I = 8;
   private boolean overlayVisible = false;
   private boolean overlayCollapsed = false;
   private boolean configurationOnly = false;
   private boolean dragging = false;
   private double dragOffsetX;
   private double dragOffsetY;
   private float dragStartMouseX;
   private float dragStartMouseY;
   private int dragStartX;
   private int dragStartY;
   private boolean dragMoved = false;
   private final Font textRenderer;
   private final autismclient.gui.f.b.CharEncoder theme = new autismclient.gui.f.b.CharEncoder();
   private final autismclient.gui.ui.layout.CollapsibleWindowNode windowNode = new autismclient.gui.ui.layout.CollapsibleWindowNode("Server Info");
   private final autismclient.gui.ui.layout.LayoutTreeHost layoutHost = new autismclient.gui.ui.layout.LayoutTreeHost(this.theme, this.windowNode);
   private final AutismServerInfoOverlay.TabBarNode tabBar = new AutismServerInfoOverlay.TabBarNode();
   private final autismclient.gui.ui.widgets.TextInputWidget searchInput = new autismclient.gui.ui.widgets.TextInputWidget();
   private final AutismServerInfoOverlay.SliderNode scanDelaySlider = new AutismServerInfoOverlay.SliderNode();
   private final AutismServerInfoOverlay.ProgressBarNode progressBar = new AutismServerInfoOverlay.ProgressBarNode();
   private final autismclient.gui.ui.layout.VerticalSpacerNode listViewportSpacer = new autismclient.gui.ui.layout.VerticalSpacerNode();
   private autismclient.gui.ui.layout.ColumnLayout infoRowsColumn;
   private boolean draggingScrollbar = false;
   private int scrollbarGrabOffset = 0;
   private long lastLayoutRebuildMs = 0L;
   private static final String[] TAB_LABELS = new String[]{"Info", "Plugins"};
   private int activeTab = 0;
   private final List<AutismServerInfoOverlay.ClickHotspot> clickHotspots = new ArrayList<>();
   private volatile String dnsResult = null;
   private volatile String dnsHost = null;
   private volatile boolean dnsLookupRunning = false;
   private final Set<String> auditDnsRunning = ConcurrentHashMap.newKeySet();
   private final List<String> detectedPlugins = new ArrayList<>();
   private final Map<String, List<String>> pluginCommands = new LinkedHashMap<>();
   private final Map<String, List<String>> pluginChannels = new LinkedHashMap<>();
   private final Map<String, List<String>> pluginGuis = new LinkedHashMap<>();
   private List<AutismServerInfoOverlay.DetailRow> detailRows = List.of();
   private String detailRowsQuery = null;
   private String detailRowsSelectedKey = null;
   private int detailRowsContentWidth = -1;
   private int[] detailRowOffsets = new int[0];
   private int detailRowsHeight = 0;
   private int detailsRevision = 0;
   private int detailRowsRevision = 0;
   private int builtDetailRowsRevision = -1;
   private Map<String, AutismServerInfoOverlay.DetectedPluginEntry> detectedPluginDetails = Map.of();
   private int builtDetailsRevision = -1;
   private final autismclient.gui.ui.widgets.ScrollOffsetController scroll = new autismclient.gui.ui.widgets.ScrollOffsetController();
   private int contentHeight = 0;
   private String selectedPlugin = null;
   private int scanDelayMs = 50;
   private boolean scanDone = false;
   private boolean tick_Z = false;
   private AutismServerInfoOverlay.ActiveScanSession activeScan;
   private long nextScanGeneration = 1L;
   private final ServerScanIdAllocator scanIdAllocator = new ServerScanIdAllocator();
   private AutismServerInfoOverlay.ScanStatus scanStatus = AutismServerInfoOverlay.ScanStatus.NONE;
   private String scanIssue = "";
   private int scanTotalProbes;
   private int scanAnsweredProbes;
   private int scanRetriedProbes;
   private int scanFailedProbes;
   private final Set<String> observedCommandTokens = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
   private final Map<String, AutismServerInfoOverlay.EvidenceSource> pluginEvidence = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
   private final Map<String, AutismServerInfoOverlay.EvidenceSource> pluginCopyEvidence = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
   private final Map<String, AutismServerInfoOverlay.MatchConfidence> pluginConfidence = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
   private final Map<String, AutismServerInfoOverlay.DetectionType> pluginDetectionTypes = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
   private final Map<String, List<String>> pluginCopyCommands = new LinkedHashMap<>();
   private final Map<String, AutismServerInfoOverlay.PluginAccumulator> poll_Map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
   private final Map<String, AutismServerInfoOverlay.PluginAccumulator> legacyPluginEntries = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
   private final Map<Integer, AutismServerInfoOverlay.ProbeSpec> userProbesById = new LinkedHashMap<>();
   private final Deque<AutismServerInfoOverlay.CommandHistoryEntry> commandHistory = new ArrayDeque<>();
   private static final long OBSERVATION_FLUSH_INTERVAL_MS = 250L;
   private static final int task = 8192;
   private static final int pool = 1000;
   private static final int index = 256;
   private static final int count = 64;
   private final List<AutismServerInfoOverlay.ProbeObservation> write_List = new ArrayList<>();
   private final List<AutismServerInfoOverlay.CommandEvidence> pendingCommandEvidence = new ArrayList<>();
   private boolean observationsDirty = false;
   private long lastObservationFlushMs = 0L;
   private final Set<String> reportedFailureStages = new HashSet<>();
   private boolean completionAnnounced = false;
   private boolean layoutDirty = true;
   private boolean setupRequested = false;
   private int activeScanDelayMs = 50;
   private String requestedScanContext = null;
   private String scanAddress = null;
   private String lastSeenAddress = "";
   private String contextSignature = "";
   private String signatureCacheAddress = "";
   private String signatureCacheBrand = "";
   private String cachedContextSignature = "";
   private boolean contextSignatureDirty = true;
   private long catalogRevision = -1L;
   private static final long RESPONSE_QUIET_MS = 700L;
   private static final long MIN_SCAN_TIMEOUT_MS = 12000L;
   private static final int level = 50;
   private static final int frame = 10;
   private static final int block = 500;
   private static final long PLUGIN_SCAN_CACHE_MAX_AGE_MS = 5184000000L;
   private static final long poll_J = 450L;
   private static final long PROBE_SETTLE_WINDOW_MS = 1400L;
   private static final String UNUSED_SCAN_VERSION_TAG = "hybrid-plugin-scan-v12";
   private static final boolean TRACE_PLUGIN_SCAN = Boolean.getBoolean("autism.plugin.scan.trace");
   private static final long COMMAND_HISTORY_WINDOW_MS = 1500L;
   private static final int chunk = 256;
   private static final int table = 24;
   private static final int store = 12;
   private static final int drain = 200;
   private static final int fill = 200;
   private static final int mark = 126;
   private static final int scan = 200;
   private static final int parse = 92;
   private static final int bind = 200;
   private static final int f_a_I = 258;
   private static final int f_b_I = 200;
   private static final int f_c_I = 280;
   private static final int f_d_I = 200;
   private static final int f_e_I = 12;
   private int infoTabWidth = 200;
   private int infoTabHeight = 258;
   private int pluginsTabWidth = 200;
   private int pluginsTabHeight = 280;
   private static final String[] PROBED_NAMESPACES = new String[]{
      "essentials",
      "essentialsx",
      "worldedit",
      "worldguard",
      "luckperms",
      "vault",
      "citizens",
      "cmi",
      "cmilib",
      "multiverse-core",
      "multiverse",
      "viaversion",
      "viabackwards",
      "viarewind",
      "geysermc",
      "geyser",
      "floodgate",
      "protocollib",
      "coreprotect",
      "griefprevention",
      "shopkeepers",
      "dynmap",
      "placeholderapi",
      "skinsrestorer",
      "skript",
      "advancedanticheat",
      "vulcan",
      "grimac",
      "matrix",
      "spartan",
      "aac",
      "karhu",
      "verus",
      "nocheatplus",
      "authme",
      "authmereloaded",
      "librelogin",
      "nlogin",
      "openlogin",
      "limboauth",
      "loginsecurity",
      "fastlogin",
      "bungeeguard",
      "deluxemenus",
      "plotsquared",
      "supervanish",
      "packetevents",
      "oraxen",
      "itemsadder",
      "fawe",
      "fastasyncworldedit",
      "luckpermsbukkit",
      "essentialsgeoip",
      "essentialsprotect",
      "essentialsspawn",
      "essentialsxspawn",
      "multiverse-inventories",
      "multiverse-netherportals",
      "worldborder",
      "votifier",
      "nuVotifier",
      "votingplugin",
      "excellentcrates",
      "crazycrates",
      "goldencrates",
      "goldencrate",
      "cratereloaded",
      "magiccrates",
      "simplelootcrates",
      "cratesplus",
      "cratekeys",
      "advancedcrates",
      "phoenixcrates",
      "mysterycrates",
      "deluxecrates",
      "crazyenvoys",
      "crazyrewards",
      "jobs",
      "jobsreborn",
      "mcmmo",
      "towny",
      "factions",
      "factionsuuid",
      "lands",
      "residence",
      "claimchunk",
      "quickshop",
      "quickshop-hikari",
      "chestshop",
      "shopgui",
      "auctionhouse",
      "combatlogx",
      "litebans",
      "advancedban",
      "libertybans",
      "luckpermsgui",
      "tab",
      "tablist",
      "scoreboard",
      "animatedscoreboard",
      "ajleaderboards",
      "ajqueue",
      "spark",
      "sparkbukkit",
      "plan",
      "minimotd",
      "protocolsupport",
      "viaversion",
      "excellentenchants",
      "eco",
      "ecoenchants",
      "mythicmobs",
      "mythiclib",
      "modelengine",
      "mmoitems",
      "mmocore",
      "denizen",
      "citizenscmd",
      "sentinel",
      "npcs",
      "proantitab",
      "pat",
      "papiproxybridge",
      "groupmanager",
      "vulcanbungee",
      "grimacbukkit",
      "negativity",
      "intave",
      "polar",
      "horizon",
      "themis",
      "libreforge",
      "casinoslots",
      "slotmachine",
      "slotmachineplus",
      "roulette",
      "casino",
      "crazycasino",
      "coinflip",
      "deluxecoinflip",
      "crazycoinflip",
      "jackpot",
      "lottery",
      "lotteryplus",
      "blackjack",
      "crash",
      "plinko",
      "casebattle"
   };
   private static final String[] PLUGIN_LIST_COMMANDS = new String[]{"/plugins ", "/pl ", "/bukkit:plugins ", "/bukkit:pl "};
   private static final String[] HELP_COMMANDS = new String[]{"/help ", "/? ", "/bukkit:help ", "/minecraft:help "};
   private static final String[] VERSION_PROBE_PLUGINS = new String[]{
      "essentials",
      "essentialsx",
      "worldedit",
      "worldguard",
      "luckperms",
      "vault",
      "cmi",
      "citizens",
      "multiverse",
      "viaversion",
      "geyser",
      "floodgate",
      "protocollib",
      "coreprotect",
      "placeholderapi",
      "skinsrestorer",
      "skript",
      "vulcan",
      "grimac",
      "matrix",
      "spartan",
      "authme",
      "librelogin",
      "nlogin",
      "limboauth",
      "loginsecurity",
      "crazycrates",
      "excellentcrates",
      "goldencrates",
      "cratereloaded",
      "deluxemenus",
      "plotsquared",
      "tab",
      "spark",
      "proantitab"
   };
   private static final Set<String> SERVER_META_COMMANDS = Set.of(
      "aliases",
      "bukkit:aliases",
      "icanhasbukkit",
      "plugins",
      "plugin",
      "pl",
      "version",
      "ver",
      "about",
      "help",
      "?",
      "bukkit:plugins",
      "bukkit:pl",
      "bukkit:version",
      "bukkit:ver",
      "bukkit:help",
      "minecraft:help"
   );
   private static final String PROBE_PREFIX_ALPHABET = "abcdefghijklmnopqrstuvwxyz0123456789";
   private static final Map<String, String> PLUGIN_ALIASES = Map.ofEntries(
      Map.entry("lp", "luckperms"),
      Map.entry("we", "worldedit"),
      Map.entry("rg", "worldguard"),
      Map.entry("mv", "multiverse-core"),
      Map.entry("npc", "citizens"),
      Map.entry("papi", "placeholderapi"),
      Map.entry("cmi", "cmi"),
      Map.entry("co", "coreprotect"),
      Map.entry("grim", "grimac"),
      Map.entry("geyser", "geysermc"),
      Map.entry("floodgate", "floodgate"),
      Map.entry("viaver", "viaversion"),
      Map.entry("sr", "skinsrestorer"),
      Map.entry("authme", "authme"),
      Map.entry("authmereloaded", "authme"),
      Map.entry("librelogin", "librelogin"),
      Map.entry("nlogin", "nlogin"),
      Map.entry("openlogin", "openlogin"),
      Map.entry("limboauth", "limboauth"),
      Map.entry("loginsecurity", "loginsecurity"),
      Map.entry("fastlogin", "fastlogin"),
      Map.entry("bungeeguard", "bungeeguard"),
      Map.entry("dm", "deluxemenus"),
      Map.entry("plots", "plotsquared"),
      Map.entry("sv", "supervanish"),
      Map.entry("spawn", "essentialsx"),
      Map.entry("home", "essentialsx"),
      Map.entry("homes", "essentialsx"),
      Map.entry("warp", "essentialsx"),
      Map.entry("warps", "essentialsx"),
      Map.entry("tpa", "essentialsx"),
      Map.entry("tpahere", "essentialsx"),
      Map.entry("bal", "essentialsx"),
      Map.entry("balance", "essentialsx"),
      Map.entry("money", "essentialsx"),
      Map.entry("eco", "essentialsx"),
      Map.entry("ban", "essentialsx"),
      Map.entry("kick", "essentialsx"),
      Map.entry("mute", "essentialsx"),
      Map.entry("jail", "essentialsx"),
      Map.entry("seen", "essentialsx"),
      Map.entry("ptime", "essentialsx"),
      Map.entry("pweather", "essentialsx"),
      Map.entry("tppos", "essentialsx"),
      Map.entry("near", "essentialsx"),
      Map.entry("back", "essentialsx"),
      Map.entry("afk", "essentialsx"),
      Map.entry("msg", "essentialsx"),
      Map.entry("r", "essentialsx"),
      Map.entry("reply", "essentialsx"),
      Map.entry("mail", "essentialsx"),
      Map.entry("pay", "essentialsx"),
      Map.entry("sell", "essentialsx"),
      Map.entry("worth", "essentialsx"),
      Map.entry("kit", "essentialsx"),
      Map.entry("kits", "essentialsx"),
      Map.entry("lb", "litebans"),
      Map.entry("litebans", "litebans"),
      Map.entry("ab", "advancedban"),
      Map.entry("advancedban", "advancedban"),
      Map.entry("tab", "tab"),
      Map.entry("pat", "proantitab"),
      Map.entry("proantitab", "proantitab"),
      Map.entry("spark", "spark"),
      Map.entry("plan", "plan"),
      Map.entry("votingplugin", "votingplugin"),
      Map.entry("vote", "votingplugin"),
      Map.entry("votes", "votingplugin"),
      Map.entry("jobs", "jobsreborn"),
      Map.entry("mcmmo", "mcmmo"),
      Map.entry("towny", "towny"),
      Map.entry("f", "factions"),
      Map.entry("factions", "factions"),
      Map.entry("lands", "lands"),
      Map.entry("res", "residence"),
      Map.entry("residence", "residence"),
      Map.entry("qs", "quickshop"),
      Map.entry("quickshop", "quickshop"),
      Map.entry("auctionhouse", "auctionhouse"),
      Map.entry("crazycrates", "crazycrates"),
      Map.entry("excellentcrates", "excellentcrates"),
      Map.entry("goldencrates", "goldencrates"),
      Map.entry("cratereloaded", "cratereloaded"),
      Map.entry("magiccrates", "magiccrates"),
      Map.entry("simplelootcrates", "simplelootcrates"),
      Map.entry("cratesplus", "cratesplus"),
      Map.entry("cratekeys", "cratekeys"),
      Map.entry("casino", "casino"),
      Map.entry("casinoslots", "casinoslots"),
      Map.entry("slotmachine", "slotmachine"),
      Map.entry("coinflip", "coinflip"),
      Map.entry("jackpot", "jackpot"),
      Map.entry("lottery", "lottery"),
      Map.entry("mythicmobs", "mythicmobs"),
      Map.entry("mm", "mythicmobs"),
      Map.entry("mmoitems", "mmoitems"),
      Map.entry("denizen", "denizen"),
      Map.entry("sentinel", "sentinel"),
      Map.entry("vulcan", "vulcan"),
      Map.entry("matrix", "matrix"),
      Map.entry("karhu", "karhu"),
      Map.entry("verus", "verus"),
      Map.entry("ncp", "nocheatplus")
   );
   private static final Map<String, String> FEATURE_KEYWORDS = Map.ofEntries(
      Map.entry("shop", "Shop"),
      Map.entry("shops", "Shop"),
      Map.entry("store", "Shop"),
      Map.entry("stores", "Shop"),
      Map.entry("market", "Market"),
      Map.entry("marketplace", "Market"),
      Map.entry("ah", "Auction"),
      Map.entry("auction", "Auction"),
      Map.entry("auctions", "Auction"),
      Map.entry("crate", "Crates"),
      Map.entry("crates", "Crates"),
      Map.entry("key", "Crates"),
      Map.entry("keys", "Crates"),
      Map.entry("case", "Crates"),
      Map.entry("cases", "Crates"),
      Map.entry("box", "Crates"),
      Map.entry("boxes", "Crates"),
      Map.entry("lootbox", "Crates"),
      Map.entry("lootboxes", "Crates"),
      Map.entry("cratekey", "Crates"),
      Map.entry("cratekeys", "Crates"),
      Map.entry("votekey", "Crates"),
      Map.entry("votekeys", "Crates"),
      Map.entry("casino", "Gambling"),
      Map.entry("slot", "Gambling"),
      Map.entry("slots", "Gambling"),
      Map.entry("slotmachine", "Gambling"),
      Map.entry("roulette", "Gambling"),
      Map.entry("coinflip", "Gambling"),
      Map.entry("cf", "Gambling"),
      Map.entry("jackpot", "Gambling"),
      Map.entry("lottery", "Gambling"),
      Map.entry("lotto", "Gambling"),
      Map.entry("raffle", "Gambling"),
      Map.entry("dice", "Gambling"),
      Map.entry("blackjack", "Gambling"),
      Map.entry("poker", "Gambling"),
      Map.entry("crash", "Gambling"),
      Map.entry("plinko", "Gambling"),
      Map.entry("casebattle", "Gambling"),
      Map.entry("casebattles", "Gambling"),
      Map.entry("wheel", "Gambling"),
      Map.entry("spin", "Gambling"),
      Map.entry("login", "Auth"),
      Map.entry("register", "Auth"),
      Map.entry("reg", "Auth"),
      Map.entry("logout", "Auth"),
      Map.entry("unregister", "Auth"),
      Map.entry("changepassword", "Auth"),
      Map.entry("changepw", "Auth"),
      Map.entry("captcha", "Auth"),
      Map.entry("2fa", "Auth"),
      Map.entry("totp", "Auth"),
      Map.entry("email", "Auth"),
      Map.entry("verify", "Auth"),
      Map.entry("verification", "Auth"),
      Map.entry("premium", "Auth"),
      Map.entry("cracked", "Auth"),
      Map.entry("backpack", "Backpacks"),
      Map.entry("backpacks", "Backpacks"),
      Map.entry("bp", "Backpacks"),
      Map.entry("pv", "Player Vaults"),
      Map.entry("playervault", "Player Vaults"),
      Map.entry("playervaults", "Player Vaults"),
      Map.entry("vaults", "Player Vaults"),
      Map.entry("enderchest", "Ender Chest"),
      Map.entry("echest", "Ender Chest"),
      Map.entry("ec", "Ender Chest"),
      Map.entry("shulker", "Shulker"),
      Map.entry("shulkers", "Shulker"),
      Map.entry("minion", "Minions"),
      Map.entry("minions", "Minions"),
      Map.entry("mine", "Mines"),
      Map.entry("mines", "Mines"),
      Map.entry("kit", "Kits"),
      Map.entry("kits", "Kits")
   );
   private static final Set<String> ANTICHEAT_PLUGINS = Set.of(
      "nocheatplus",
      "aac",
      "spartan",
      "matrix",
      "vulcan",
      "grim",
      "grimac",
      "intave",
      "karhu",
      "verus",
      "polar",
      "negativity",
      "themis",
      "fairfight",
      "wraith",
      "horizon",
      "reflex",
      "antiaura",
      "guardian",
      "hac",
      "thotpatrol",
      "alice"
   );
   private static final Set<String> PLATFORM_NAMESPACES = Set.of("minecraft", "brigadier", "bukkit", "spigot", "paper", "purpur", "velocity", "bungeecord", "waterfall");
   private static final Set<String> VANILLA_COMMANDS = Set.of(
      "advancement",
      "attribute",
      "ban",
      "ban-ip",
      "banlist",
      "bossbar",
      "clear",
      "clone",
      "damage",
      "data",
      "datapack",
      "debug",
      "defaultgamemode",
      "deop",
      "difficulty",
      "effect",
      "enchant",
      "execute",
      "experience",
      "xp",
      "fill",
      "fillbiome",
      "forceload",
      "function",
      "gamemode",
      "gamerule",
      "give",
      "help",
      "item",
      "jfr",
      "kick",
      "kill",
      "list",
      "locate",
      "loot",
      "me",
      "msg",
      "op",
      "pardon",
      "pardon-ip",
      "particle",
      "perf",
      "place",
      "playsound",
      "publish",
      "random",
      "recipe",
      "reload",
      "return",
      "ride",
      "rotate",
      "save-all",
      "save-off",
      "save-on",
      "say",
      "schedule",
      "scoreboard",
      "seed",
      "setblock",
      "setidletimeout",
      "setworldspawn",
      "spawnpoint",
      "spectate",
      "spreadplayers",
      "stop",
      "stopsound",
      "summon",
      "tag",
      "team",
      "teammsg",
      "tm",
      "teleport",
      "tell",
      "tellraw",
      "tick",
      "time",
      "title",
      "tp",
      "transfer",
      "trigger",
      "w",
      "weather",
      "whitelist",
      "worldborder"
   );
   private static final int f_j_I = -46261;

   private void reportStageFailure(String stage, Throwable t) {
      if (this.reportedFailureStages.add(stage)) {
         LOGGER.warn("Server info overlay '{}' failed; further errors for this stage are suppressed", stage, t);
      }
   }

   public AutismServerInfoOverlay(Font textRenderer) {
      this.textRenderer = textRenderer;
      this.setupLayout();
   }

   public void setConfigurationOnly(boolean configurationOnly) {
      if (this.configurationOnly != configurationOnly) {
         this.configurationOnly = configurationOnly;
         if (configurationOnly) {
            this.cancelScan("main menu");
         }

         this.requestLayoutRebuild();
      }
   }

   private void setupLayout() {
      this.windowNode.setCenterTitle(false);
      this.windowNode.setTitleTone(autismclient.gui.f.b.l.load);
      this.windowNode.setHeaderButtons(true, true);
      this.windowNode.setTitleInsets(this.contentPadding() + 1, this.contentPadding() + this.k() + this.l() + this.m() + 12);
      this.windowNode.body().withGap(this.contentGap()).withPadding(autismclient.gui.ui.layout.EdgeInsets.uniform(this.contentPadding()));
      this.tabBar
         .setTabs(TAB_LABELS)
         .setActiveIndex(this.activeTab)
         .setOnSelect(this::onTabSelected);
      this.searchInput
         .setPlaceholder("Search plugins...")
         .setPreferredWidth(this.minSearchWidth())
         .setFixedHeight(this.searchHeight())
         .setOnChange(text -> {
            this.scroll.jumpTo(0, 0);
            this.requestLayoutRebuild();
         });
      this.scanDelaySlider
         .setRange(10.0F, 500.0F)
         .setStep(10.0F)
         .setValue(this.scanDelayMs)
         .setOnChange(value -> this.setScanDelayMs(Math.round(value)));
      this.progressBar.setProgress(0.0F);
   }

   public void saveState() {
      AutismSessionState shared = AutismSessionState.getInstance();
      this.saveTabSize();
      String stateAddress = this.normalizedServerAddress();
      if (stateAddress.isEmpty() && this.scanAddress != null) {
         stateAddress = this.scanAddress;
      }

      shared.setServerInfoActiveTab(this.activeTab);
      shared.setServerInfoScrollOffset(this.scroll.getOffset());
      shared.setServerInfoSelectedPlugin(this.selectedPlugin);
      shared.setServerInfoStateAddress(stateAddress);
      shared.setServerInfoScanDelayMs(this.scanDelayMs);
      shared.setServerInfoTabWidth(this.infoTabWidth);
      shared.setServerInfoTabHeight(this.infoTabHeight);
      shared.setServerInfoPluginsTabWidth(this.pluginsTabWidth);
      shared.setServerInfoPluginsTabHeight(this.pluginsTabHeight);
      this.saveScanToConfig();
      this.saveBounds();
   }

   public void restoreState() {
      AutismSessionState shared = AutismSessionState.getInstance();
      this.loadBounds();
      this.activeTab = Math.max(0, Math.min(TAB_LABELS.length - 1, shared.getServerInfoActiveTab()));
      this.scanDelayMs = Math.max(10, Math.min(500, shared.getServerInfoScanDelayMs()));
      this.infoTabWidth = Math.max(this.minInfoTabWidth(), shared.getServerInfoTabWidth());
      this.infoTabHeight = Math.max(this.minInfoTabHeight(), shared.getServerInfoTabHeight());
      this.pluginsTabWidth = Math.max(this.minPluginsTabWidth(), shared.getServerInfoPluginsTabWidth());
      this.pluginsTabHeight = Math.max(this.minPluginsTabHeight(), shared.getServerInfoPluginsTabHeight());
      this.syncServerContext();
      String stateAddress = this.normalizedServerAddress();
      if (stateAddress.isEmpty() && this.scanAddress != null) {
         stateAddress = this.scanAddress;
      }

      if (!stateAddress.isEmpty() && stateAddress.equals(shared.getServerInfoStateAddress())) {
         this.scroll.setOffsetUnclamped(shared.getServerInfoScrollOffset());
         this.selectedPlugin = shared.getServerInfoSelectedPlugin();
         if (this.selectedPlugin != null && this.selectedPlugin.isBlank()) {
            this.selectedPlugin = null;
         }

         if (this.selectedPlugin != null && !this.isDetected(this.selectedPlugin)) {
            this.selectedPlugin = null;
         }
      } else {
         this.scroll.jumpTo(0, 0);
         this.selectedPlugin = null;
      }

      if (this.activeTab == 0) {
         this.resizeForInfoTab();
      } else if (this.tick_Z) {
         this.resizeForScanningPlugins();
      } else if (!this.scanDone) {
         this.resizeForUnscannedPlugins();
      }

      this.tabBar.setActiveIndex(this.activeTab);
      this.scanDelaySlider.setValue(this.scanDelayMs);
   }

   public static boolean isSavedVisible() {
      OverlayBounds layout = AutismSessionState.getInstance().getOverlayBounds("autism-serverinfo");
      return layout != null && layout.visible;
   }

   private void onTabSelected(int tabIdx) {
      this.saveTabSize();
      this.activeTab = Math.max(0, Math.min(TAB_LABELS.length - 1, tabIdx));
      this.tabBar.setActiveIndex(this.activeTab);
      this.scroll.jumpTo(0, 0);
      this.selectedPlugin = null;
      if (this.activeTab == 0) {
         this.resizeForInfoTab();
      } else if (this.tick_Z) {
         this.resizeForScanningPlugins();
      } else if (!this.scanDone) {
         this.resizeForUnscannedPlugins();
      } else {
         this.resizeForPluginResults();
      }

      this.saveState();
   }

   private void rebuildLayout() {
      this.layoutDirty = false;
      this.windowNode.body().clearChildren();
      this.tabBar.setActiveIndex(this.activeTab);
      this.scanDelaySlider.setValue(this.scanDelayMs);
      this.progressBar.setProgress(this.scanProgress());
      if (!this.tick_Z) {
         this.windowNode.body().addChild(this.tabBar);
      }

      if (this.activeTab == 0) {
         this.buildInfoTab();
      } else if (!this.scanDone && !this.tick_Z) {
         this.buildProbeSetupSection();
      } else if (this.tick_Z) {
         this.buildScanProgressSection();
      } else {
         this.buildPluginResultsSection();
      }
   }

   private void requestLayoutRebuild() {
      this.layoutDirty = true;
   }

   private void buildInfoTab() {
      ServerData entry = MC.getCurrentServer();
      String displayedAddress = this.displayedAddress();
      String realIp = this.resolvedIp(displayedAddress);
      String software = this.guessServerSoftware(entry);
      String versionNote = this.versionNote(entry);
      String ping = entry != null ? entry.ping + " ms" : "--";
      String players = "--";
      if (MC.getConnection() != null) {
         int online = MC.getConnection().getListedOnlinePlayers().size();
         players = entry != null && entry.players != null ? online + " / " + entry.players.max() : String.valueOf(online);
      }

      String proto = entry != null ? String.valueOf(entry.protocol) : "--";
      String diff = MC.level != null ? MC.level.getDifficulty().getDisplayName().getString() : "--";
      String time = "--";
      if (MC.level != null) {
         long dayCount = MC.level.getOverworldClockTime() / 24000L;
         long timeOfDay = MC.level.getOverworldClockTime() % 24000L;
         int hours = (int)((timeOfDay / 1000L + 6L) % 24L);
         int minutes = (int)(timeOfDay % 1000L * 60L / 1000L);
         time = "Day " + dayCount + " (" + String.format("%02d:%02d", hours, minutes) + ")";
      }

      double estimatedTps = AutismSessionState.getInstance().getEstimatedTps();
      String tps = estimatedTps > 0.0 ? String.format("%.1f", Math.min(20.0, estimatedTps)) : "--";
      this.infoRowsColumn = new autismclient.gui.ui.layout.ColumnLayout();
      this.addInfoRow("IP:", displayedAddress, null, () -> this.copyToClipboard(displayedAddress, "Server address copied.", "Server address unavailable."));
      String host = this.hostFromAddress(displayedAddress);
      if (!host.isBlank() && (!host.equals(realIp) || this.dnsLookupRunning)) {
         this.addInfoRow("Real IP:", realIp, null, this::copyRealIp);
      }

      this.addInfoRow("Version:", this.reportedVersion(entry), null);
      this.addInfoRow("Real Version:", this.detectedVersion(), null);
      this.addInfoRow("Brand:", this.serverBrand(), null);
      if (!"--".equals(software)) {
         this.addInfoRow("Software:", software, null);
      }

      this.addInfoRow("Ping:", ping, null);
      this.addInfoRow("Players:", players, null);
      this.addInfoRow("Protocol:", proto, null);
      if (!"--".equals(versionNote)) {
         this.addInfoRow("Version Note:", versionNote, UiColorPalette.warningYellow());
      }

      this.addInfoRow("Difficulty:", diff, null);
      this.addInfoRow("World:", this.worldName(), null);
      this.addInfoRow("Time:", time, null);
      this.addInfoRow("TPS:", tps, null);
      List<String> detectedAcs = this.detectedAnticheats();
      if (this.tick_Z) {
         this.addInfoRow("AntiCheats:", "Scanning...", ThemePaletteManager.recolor(-48060, ThemePaletteManager.ColorRole.DANGER));
      } else if (!this.scanDone) {
         this.addInfoRow("AntiCheats:", "Probe Plugins First", ThemePaletteManager.recolor(-48060, ThemePaletteManager.ColorRole.DANGER));
      } else if (detectedAcs.isEmpty()) {
         this.addInfoRow("AntiCheats:", "None detected", null);
      } else {
         this.addInfoRow("AntiCheats:", String.join(", ", detectedAcs), ThemePaletteManager.recolor(-48060, ThemePaletteManager.ColorRole.DANGER));
      }

      this.windowNode.body().addChild(this.infoRowsColumn);
      this.infoRowsColumn = null;
      this.windowNode.body().addChild(new autismclient.gui.ui.layout.FixedSpacerNode(0.0F, 2.0F));
      this.windowNode
         .body()
         .addChild(
            new autismclient.gui.ui.layout.ButtonNode("Copy Report", autismclient.gui.ui.layout.ButtonNode.Variant.SECONDARY, this::copyServerInfo)
               .setFixedHeight(this.buttonHeight())
         );
   }

   private void addInfoRow(String label, String value, Integer valueColor) {
      this.addInfoRow(label, value, valueColor, null);
   }

   private void addInfoRow(String label, String value, Integer valueColor, Runnable onPress) {
      autismclient.gui.ui.layout.KeyValueRowNode row = new autismclient.gui.ui.layout.KeyValueRowNode(label, value)
         .setLabelWidth(this.infoLabelWidth())
         .setValueColor(valueColor)
         .setOnPress(onPress);
      if (this.infoRowsColumn != null) {
         this.infoRowsColumn.addChild(row);
      } else {
         this.windowNode.body().addChild(row);
      }
   }

   private int infoLabelWidth() {
      return this.widestLabel(
         "IP:",
         "Real IP:",
         "Version:",
         "Real Version:",
         "Brand:",
         "Software:",
         "Ping:",
         "Players:",
         "Protocol:",
         "Version Note:",
         "Difficulty:",
         "World:",
         "Time:",
         "TPS:",
         "AntiCheats:"
      );
   }

   private void buildProbeSetupSection() {
      this.windowNode.body().addChild(this.createAutoProbeButton());
      this.windowNode
         .body()
         .addChild(new autismclient.gui.ui.layout.LabelNode("Auto-Scans on join.", autismclient.gui.f.b.l.write));
      this.windowNode.body().addChild(new autismclient.gui.ui.layout.FixedSpacerNode(0.0F, 2.0F));
      this.windowNode
         .body()
         .addChild(new autismclient.gui.ui.layout.LabelNode("Start probing manually", autismclient.gui.f.b.l.write));
      this.windowNode
         .body()
         .addChild(new autismclient.gui.ui.layout.KeyValueRowNode("Delay:", this.scanDelayMs + " ms").setLabelWidth(this.delayLabelWidth()));
      this.windowNode.body().addChild(this.scanDelaySlider);
      this.windowNode.body().addChild(new autismclient.gui.ui.layout.LabelNode("Default: 50 ms", autismclient.gui.f.b.l.write));
      this.windowNode
         .body()
         .addChild(new autismclient.gui.ui.layout.LabelNode("If you get kicked increase the delay.", autismclient.gui.f.b.l.write));
      autismclient.gui.ui.layout.ButtonNode startProbe = new autismclient.gui.ui.layout.ButtonNode(
         this.configurationOnly ? "Join a Server to Probe" : "Start Probing", autismclient.gui.ui.layout.ButtonNode.Variant.SECONDARY, this::rescan
      );
      startProbe.setFixedHeight(this.buttonHeight());
      startProbe.setEnabled(!this.configurationOnly);
      this.windowNode.body().addChild(startProbe);
      if (!this.detectedPlugins.isEmpty()) {
         this.windowNode
            .body()
            .addChild(new autismclient.gui.ui.layout.ButtonNode("Back to Results", autismclient.gui.ui.layout.ButtonNode.Variant.SECONDARY, () -> {
               this.setupRequested = false;
               this.scanDone = true;
               this.tick_Z = false;
               this.resizeForPluginResults();
               this.saveState();
            }).setFixedHeight(this.buttonHeight()));
      }
   }

   private void buildScanProgressSection() {
      int progressPercent = Math.max(0, Math.min(100, Math.round(this.scanProgress() * 100.0F)));
      AutismServerInfoOverlay.ActiveScanSession session = this.activeScan;
      this.windowNode
         .body()
         .addChild(
            new autismclient.gui.ui.layout.KeyValueRowNode("State:", this.scanPhaseText()).setLabelWidth(this.progressLabelWidth()).setValueColor(this.scanPhaseColor())
         );
      this.windowNode
         .body()
         .addChild(
            new autismclient.gui.ui.layout.KeyValueRowNode("Sent:", this.sentProbeCount() + "/" + Math.max(1, this.totalProbeCount())).setLabelWidth(this.progressLabelWidth())
         );
      this.windowNode
         .body()
         .addChild(
            new autismclient.gui.ui.layout.KeyValueRowNode("Replies:", this.replyProbeCount() + "/" + Math.max(1, this.totalProbeCount())).setLabelWidth(this.progressLabelWidth())
         );
      this.windowNode
         .body()
         .addChild(
            new autismclient.gui.ui.layout.KeyValueRowNode("Retries:", Integer.toString(session == null ? 0 : session.retryCount)).setLabelWidth(this.progressLabelWidth())
         );
      this.windowNode
         .body()
         .addChild(
            new autismclient.gui.ui.layout.KeyValueRowNode("Failed:", Integer.toString(session == null ? 0 : session.failedCount)).setLabelWidth(this.progressLabelWidth())
         );
      int foundCount = this.foundCount();
      this.windowNode
         .body()
         .addChild(
            new autismclient.gui.ui.layout.KeyValueRowNode("Found:", foundCount + " plugin" + (foundCount == 1 ? "" : "s")).setLabelWidth(this.progressLabelWidth())
         );
      this.windowNode
         .body()
         .addChild(
            new autismclient.gui.ui.layout.KeyValueRowNode("Progress:", progressPercent + "%").setLabelWidth(this.progressLabelWidth()).setValueColor(this.scanPhaseColor())
         );
      this.windowNode.body().addChild(this.progressBar);
   }

   private void buildPluginResultsSection() {
      this.windowNode.body().addChild(this.createAutoProbeButton());
      this.searchInput.setPreferredWidth(Math.max(this.minSearchWidth(), this.overlayWidth - this.searchWidthInset()));
      this.windowNode.body().addChild(this.searchInput);
      autismclient.gui.ui.layout.StretchRowLayout buttons = new autismclient.gui.ui.layout.StretchRowLayout().withGap(this.buttonRowGap());
      buttons.addChild(
         new autismclient.gui.ui.layout.ButtonNode("Rescan", autismclient.gui.ui.layout.ButtonNode.Variant.SECONDARY, () -> this.resetToSetup())
            .setFixedHeight(this.buttonHeight())
      );
      buttons.addChild(
         new autismclient.gui.ui.layout.ButtonNode("Copy Plugins", autismclient.gui.ui.layout.ButtonNode.Variant.SECONDARY, this::copyPlugins)
            .setFixedHeight(this.buttonHeight())
      );
      this.windowNode.body().addChild(buttons);
      String query = this.searchInput.getValue().toLowerCase(Locale.ROOT);
      int filteredCount = 0;

      for (AutismServerInfoOverlay.DetectedPluginEntry detail : this.rebuildPluginDetails()) {
         if (detail != null && (query.isEmpty() || detail.searchText.contains(query))) {
            filteredCount++;
         }
      }

      String header = this.detectedPlugins.isEmpty() ? "No plugins detected" : "Detected: " + filteredCount + " plugin" + (filteredCount == 1 ? "" : "s");
      this.windowNode.body().addChild(new autismclient.gui.ui.layout.LabelNode(header, autismclient.gui.f.b.l.write));
      this.windowNode.body().addChild(this.listViewportSpacer);
      this.viewportTopOffset = this.measureSlotOffset(this.listViewportSpacer, this.estimateListViewportTop());
      float viewportHeight = Math.max(this.minViewportHeight(), this.overlayHeight - this.viewportTopOffset);
      this.listViewportSpacer.withHeight(viewportHeight);
   }

   private int measureSlotOffset(autismclient.gui.ui.layout.VerticalSpacerNode slot, int fallback) {
      slot.withHeight(0.0F);
      int measured = Math.round(this.layoutHost.measureHeight(this.overlayWidth));
      return measured > 0 ? measured : fallback;
   }

   private autismclient.gui.ui.layout.ButtonNode createAutoProbeButton() {
      boolean autoOn = AutismConfig.getGlobal().autoProbePlugins;
      return new autismclient.gui.ui.layout.ButtonNode(
            "Auto Probe: " + (autoOn ? "ON" : "OFF"),
            autoOn ? autismclient.gui.ui.layout.ButtonNode.Variant.SUCCESS : autismclient.gui.ui.layout.ButtonNode.Variant.SECONDARY,
            this::toggleAutoProbe
         )
         .setFixedHeight(this.buttonHeight());
   }

   private void toggleAutoProbe() {
      AutismConfig cfg = AutismConfig.getGlobal();
      cfg.autoProbePlugins = !cfg.autoProbePlugins;
      cfg.save();
      this.requestedScanContext = null;
      this.requestLayoutRebuild();
      this.saveState();
   }

   private void resetToSetup() {
      this.cancelScan("setup");
      this.scanDone = false;
      this.setupRequested = true;
      this.completionAnnounced = false;
      this.selectedPlugin = null;
      this.searchInput.setValue("");
      this.searchInput.setFocused(false);
      this.scroll.jumpTo(0, 0);
      this.activeTab = 1;
      this.resizeForUnscannedPlugins();
      this.saveState();
   }

   private String normalizedServerAddress() {
      ServerData entry = MC.getCurrentServer();
      if (entry != null && entry.ip != null && !entry.ip.isBlank()) {
         return entry.ip.trim().toLowerCase(Locale.ROOT);
      }

      if (MC.getConnection() != null && MC.getConnection().getConnection() != null) {
         SocketAddress address = MC.getConnection().getConnection().getRemoteAddress();
         if (address instanceof InetSocketAddress inet) {
            String host = inet.getHostString();
            if ((host == null || host.isBlank()) && inet.getAddress() != null) {
               host = inet.getAddress().getHostAddress();
            }

            if (host != null && !host.isBlank()) {
               return (host + ":" + inet.getPort()).trim().toLowerCase(Locale.ROOT);
            }
         } else if (address != null) {
            String raw = address.toString();
            if (raw != null && !raw.isBlank()) {
               return raw.replaceFirst("^/", "").trim().toLowerCase(Locale.ROOT);
            }
         }
      }

      return "";
   }

   private String normalizeSignature(String signature) {
      return signature == null ? "" : signature.trim().toLowerCase(Locale.ROOT);
   }

   private String computeContextSignature() {
      if (MC.getConnection() == null) {
         return "";
      }

      String currentAddress = this.normalizedServerAddress();
      String brand = MC.getConnection().serverBrand();
      String normalizedBrand = brand == null ? "" : brand.trim().toLowerCase(Locale.ROOT);
      if (!this.contextSignatureDirty && currentAddress.equals(this.signatureCacheAddress) && normalizedBrand.equals(this.signatureCacheBrand)) {
         return this.cachedContextSignature;
      }

      List<String> parts = new ArrayList<>();
      parts.add("scan=hybrid-plugin-scan-v12");
      if (brand != null && !brand.isBlank()) {
         parts.add("brand=" + normalizedBrand);
      }

      try {
         CommandDispatcher<?> dispatcher = MC.getConnection().getCommands();
         if (dispatcher != null) {
            RootCommandNode<?> root = dispatcher.getRoot();
            if (root != null && !root.getChildren().isEmpty()) {
               List<String> rootCommands = new ArrayList<>();

               for (CommandNode<?> child : root.getChildren()) {
                  String name = child.getName();
                  if (name != null && !name.isBlank()) {
                     rootCommands.add(name.trim().toLowerCase(Locale.ROOT));
                  }
               }

               rootCommands.sort(String.CASE_INSENSITIVE_ORDER);
               if (!rootCommands.isEmpty()) {
                  parts.add("cmd=" + String.join(",", rootCommands));
               }
            }
         }
      } catch (Exception var11) {
      }

      this.signatureCacheAddress = currentAddress;
      this.signatureCacheBrand = normalizedBrand;
      this.cachedContextSignature = this.normalizeSignature(String.join("|", parts));
      this.contextSignatureDirty = false;
      return this.cachedContextSignature;
   }

   private void invalidateContextSignature() {
      this.contextSignatureDirty = true;
   }

   private void setScanDelayMs(int delayMs) {
      this.scanDelayMs = Math.max(10, Math.min(500, delayMs));
      AutismSessionState.getInstance().setServerInfoScanDelayMs(this.scanDelayMs);
   }

   @Override
   public boolean isResizable() {
      return true;
   }

   private int computeWidth() {
      return this.resolveWindowWidth(this.computeMinWidth(), this.computeMinWidth());
   }

   private boolean isPluginResultsView() {
      return this.activeTab == 1 && this.scanDone && !this.tick_Z;
   }

   private int pluginResultsHeight() {
      return this.resolveWindowHeight(Math.max(this.pluginResultsMinHeight(), Math.min(this.fitInfoTabHeight(), this.pluginResultsMaxHeight())), this.getResizeMinHeight());
   }

   @Override
   protected int getResizeMinHeight() {
      return this.isPluginResultsView() && this.viewportTopOffset > 0 ? Math.max(this.getMinHeight(), this.viewportTopOffset + Math.round(this.minViewportHeight())) : this.getMinHeight();
   }

   @Override
   protected void onSizeChanged() {
      if (!this.hasCustomStoredSize()) {
         this.overlayWidth = this.computeMinWidth();
      }

      this.requestLayoutRebuild();
   }

   private void applyClampedBounds() {
      OverlayBounds clamped = this.clampBounds(new OverlayBounds(this.overlayX, this.overlayY, this.overlayWidth, this.overlayHeight, this.overlayVisible, this.overlayCollapsed));
      this.overlayX = clamped.x;
      this.overlayY = clamped.y;
      this.overlayWidth = clamped.width;
      this.overlayHeight = clamped.height;
   }

   private void resizeForUnscannedPlugins() {
      this.overlayWidth = this.computeWidth();
      this.overlayHeight = this.unscannedPluginsHeight();
      this.applyClampedBounds();
      this.requestLayoutRebuild();
   }

   private void resizeForScanningPlugins() {
      this.overlayWidth = this.computeWidth();
      this.overlayHeight = this.scanningPluginsHeight();
      this.applyClampedBounds();
      this.requestLayoutRebuild();
   }

   private void resizeForPluginResults() {
      this.overlayWidth = this.computeWidth();
      this.overlayHeight = this.pluginResultsHeight();
      this.applyClampedBounds();
      this.requestLayoutRebuild();
   }

   private void resizeForInfoTab() {
      this.overlayWidth = this.computeWidth();
      this.overlayHeight = this.fitInfoTabHeight();
      this.applyClampedBounds();
      this.requestLayoutRebuild();
   }

   private int computeMinWidth() {
      autismclient.gui.ui.layout.LayoutViewport viewport = this.layoutHost.createViewport();
      return Math.max(this.minInfoTabWidth(), Math.min(this.minTabWidth(), Math.round(viewport.width())));
   }

   private int pluginResultsMinHeight() {
      return this.fitInfoTabHeight();
   }

   private int pluginResultsMaxHeight() {
      autismclient.gui.ui.layout.LayoutViewport viewport = this.layoutHost.createViewport();
      int viewportHeight = Math.round(viewport.height());
      return Math.max(this.pluginResultsMinHeight(), viewportHeight - 28);
   }

   private int fitInfoTabHeight() {
      autismclient.gui.ui.layout.LayoutViewport viewport = this.layoutHost.createViewport();
      int measured = Math.max(this.minInfoTabHeight(), this.infoTabContentHeight());
      return Math.max(this.minInfoTabHeight(), Math.min(measured, Math.max(this.minInfoTabHeight(), Math.round(viewport.height()) - this.verticalScreenMargin())));
   }

   private int unscannedPluginsHeight() {
      autismclient.gui.ui.layout.LayoutViewport viewport = this.layoutHost.createViewport();
      int measured = Math.max(this.minUnscannedPluginsHeight(), this.measureHeightFor(1, false, false, Math.max(this.overlayWidth, this.unscannedPluginsMinWidth())));
      return Math.min(measured, Math.max(this.minUnscannedPluginsHeight(), Math.round(viewport.height()) - this.verticalScreenMargin()));
   }

   private int scanningPluginsHeight() {
      autismclient.gui.ui.layout.LayoutViewport viewport = this.layoutHost.createViewport();
      int measured = Math.max(this.minScanningPluginsHeight(), this.measureHeightFor(1, false, true, Math.max(this.overlayWidth, this.scanningPluginsMinWidth())));
      return Math.min(measured, Math.max(this.minScanningPluginsHeight(), Math.round(viewport.height()) - this.verticalScreenMargin()));
   }

   private int estimateListViewportTop() {
      int contentPadding = this.contentPadding() * 2;
      int contentGap = this.contentGap() * 5;
      int autoToggleHeight = this.buttonHeight();
      int searchHeight = this.searchHeight();
      int buttonRowHeight = this.buttonHeight();
      int headerLabelHeight = this.theme.init_I_l_I(autismclient.gui.f.b.l.write, 3);
      return this.theme.load()
         + contentPadding
         + contentGap
         + this.tabBarHeight()
         + autoToggleHeight
         + searchHeight
         + buttonRowHeight
         + headerLabelHeight;
   }

   private int infoTabContentHeight() {
      return Math.max(this.minInfoTabHeight(), this.measureHeightFor(0, this.scanDone, this.tick_Z, Math.max(this.minInfoTabWidth(), this.overlayWidth)));
   }

   private int measureHeightFor(int tab, boolean scanDone, boolean scanInProgress, int measureWidth) {
      int previousTab = this.activeTab;
      boolean previousScanDone = this.scanDone;
      boolean previousScanInProgress = this.tick_Z;
      this.activeTab = tab;
      this.scanDone = scanDone;
      this.tick_Z = scanInProgress;
      this.rebuildLayout();
      int measured = Math.round(this.layoutHost.measureHeight(measureWidth));
      this.activeTab = previousTab;
      this.scanDone = previousScanDone;
      this.tick_Z = previousScanInProgress;
      this.rebuildLayout();
      return measured;
   }

   private int infoRowsHeight() {
      int rowCount = 12;
      String displayedAddress = this.displayedAddress();
      String host = this.hostFromAddress(displayedAddress);
      String realIp = this.resolvedIp(displayedAddress);
      if (!host.isBlank() && (!host.equals(realIp) || this.dnsLookupRunning)) {
         rowCount++;
      }

      ServerData entry = MC.getCurrentServer();
      String software = this.guessServerSoftware(entry);
      if (!"--".equals(software)) {
         rowCount++;
      }

      String versionNote = this.versionNote(entry);
      if (!"--".equals(versionNote)) {
         rowCount++;
      }

      int contentHeight = this.tabBarHeight() + rowCount * 12 + 2 + this.buttonHeight();
      return this.theme.load() + this.contentPadding() * 2 + this.contentGap() * 3 + contentHeight;
   }

   private void saveTabSize() {
      if (this.activeTab == 0) {
         this.infoTabWidth = Math.max(this.minInfoTabWidth(), this.overlayWidth);
         this.infoTabHeight = Math.max(this.infoRowsHeight(), this.overlayHeight);
         this.pluginsTabWidth = Math.max(this.pluginsTabWidth, this.infoTabWidth);
      } else if (this.scanDone && !this.tick_Z) {
         this.pluginsTabWidth = Math.max(this.minPluginsTabWidth(), this.overlayWidth);
         this.pluginsTabHeight = Math.max(this.pluginResultsMinHeight(), Math.min(this.pluginResultsMaxHeight(), this.overlayHeight));
         this.infoTabWidth = Math.max(this.infoTabWidth, this.pluginsTabWidth);
      }
   }

   private void resetForServer(String address, String contextSignature) {
      this.scanDone = false;
      this.setupRequested = false;
      this.cancelScan("clear");
      this.scanStatus = AutismServerInfoOverlay.ScanStatus.NONE;
      this.scanIssue = "";
      this.scanTotalProbes = 0;
      this.scanAnsweredProbes = 0;
      this.scanRetriedProbes = 0;
      this.scanFailedProbes = 0;
      this.completionAnnounced = false;
      this.observedCommandTokens.clear();
      this.pluginEvidence.clear();
      this.pluginCopyEvidence.clear();
      this.pluginCopyCommands.clear();
      this.pluginConfidence.clear();
      this.pluginDetectionTypes.clear();
      this.poll_Map.clear();
      this.legacyPluginEntries.clear();
      this.scanAddress = address;
      this.contextSignature = this.normalizeSignature(contextSignature);
      this.detectedPlugins.clear();
      this.pluginCommands.clear();
      this.pluginChannels.clear();
      this.pluginGuis.clear();
      this.invalidateDetails();
      this.scroll.jumpTo(0, 0);
      this.selectedPlugin = null;
      this.catalogRevision = -1L;
      this.userProbesById.clear();
      this.commandHistory.clear();
      this.invalidateContextSignature();
   }

   private boolean loadCachedScan(String address, String contextSignature) {
      AutismConfig.PluginScanCacheEntry cached = AutismConfig.getGlobal().getPluginScan(address, contextSignature);
      if (cached == null) {
         cached = AutismConfig.getGlobal().getPluginScanByAddress(address);
      }

      if (cached == null) {
         return false;
      }

      this.detectedPlugins.clear();
      if (cached.plugins != null) {
         int kept = 0;

         for (String plugin : this.uniquePlugins((Collection<String>)cached.plugins)) {
            if (kept++ >= 1000) {
               break;
            }

            this.detectedPlugins.add(plugin);
         }
      }

      this.pluginCommands.clear();
      if (cached.commands != null) {
         this.pluginCommands.putAll(cached.commands);
      }

      this.pluginChannels.clear();
      if (cached.channels != null) {
         this.pluginChannels.putAll(cached.channels);
      }

      this.pluginGuis.clear();
      if (cached.guis != null) {
         this.pluginGuis.putAll(cached.guis);
      }

      this.pluginCopyCommands.clear();
      if (cached.copyCommands != null) {
         this.pluginCopyCommands.putAll(cached.copyCommands);
      }

      this.pluginCommands.keySet().retainAll(this.detectedPlugins);
      this.pluginChannels.keySet().retainAll(this.detectedPlugins);
      this.pluginGuis.keySet().retainAll(this.detectedPlugins);
      this.pluginCopyCommands.keySet().retainAll(this.detectedPlugins);
      this.scroll.jumpTo(0, 0);
      this.selectedPlugin = null;
      this.scanDone = true;
      this.setupRequested = false;
      this.cancelScan("cache-load");
      this.scanStatus = this.parseScanStatus(cached.scanStatus);
      if (this.scanStatus == AutismServerInfoOverlay.ScanStatus.NONE) {
         this.scanStatus = AutismServerInfoOverlay.ScanStatus.COMPLETE;
      }

      this.scanIssue = this.scanStatus == AutismServerInfoOverlay.ScanStatus.PARTIAL ? "Cached partial scan" : "";
      this.scanTotalProbes = Math.max(0, cached.totalProbes);
      this.scanAnsweredProbes = Math.max(0, cached.answeredProbes);
      this.scanRetriedProbes = Math.max(0, cached.retriedProbes);
      this.scanFailedProbes = Math.max(0, cached.failedProbes);
      this.observedCommandTokens.clear();
      this.pluginEvidence.clear();
      this.pluginCopyEvidence.clear();
      this.pluginConfidence.clear();
      this.pluginDetectionTypes.clear();
      this.poll_Map.clear();
      this.legacyPluginEntries.clear();
      this.userProbesById.clear();
      this.commandHistory.clear();
      this.scanAddress = address;
      this.contextSignature = this.normalizeSignature(cached.contextSignature);
      Map<String, String> cachedEvidence = cached.evidence == null ? Map.of() : cached.evidence;
      Map<String, String> cachedCopyEvidence = cached.copyEvidence == null ? Map.of() : cached.copyEvidence;
      Map<String, String> cachedConfidence = cached.confidence == null ? Map.of() : cached.confidence;

      for (String plugin : this.detectedPlugins) {
         String key = this.pluginKey(plugin);
         this.pluginEvidence.put(key, this.parseEvidenceSource(cachedEvidence.get(key)));
         AutismServerInfoOverlay.EvidenceSource copyEvidence = this.parseEvidenceSource(cachedCopyEvidence.get(key));
         if (copyEvidence == AutismServerInfoOverlay.EvidenceSource.UNKNOWN) {
            copyEvidence = this.copyEvidenceFor(plugin, this.pluginEvidence.get(key));
         }

         this.pluginCopyEvidence.put(key, copyEvidence);
         this.pluginConfidence
            .put(
               key,
               this.parseConfidence(
                  cachedConfidence.get(key), this.confidenceForEvidence(this.pluginEvidence.get(key))
               )
            );
         this.pluginDetectionTypes.put(key, this.detectionTypeFor(plugin, this.pluginEvidence.get(key), this.pluginConfidence.get(key)));
      }

      this.catalogRevision = -1L;
      this.refreshLiveScanResults();
      this.invalidateDetails();
      return true;
   }

   private void saveScanToConfig() {
      if (this.scanAddress != null && !this.scanAddress.isBlank() && this.scanDone && !this.tick_Z && !this.detectedPlugins.isEmpty()) {
         Map<String, String> evidenceSnapshot = new LinkedHashMap<>();
         Map<String, String> copyEvidenceSnapshot = new LinkedHashMap<>();

         for (Entry<String, AutismServerInfoOverlay.EvidenceSource> entry : this.pluginEvidence.entrySet()) {
            if (entry.getKey() != null && !entry.getKey().isBlank() && entry.getValue() != null) {
               evidenceSnapshot.put(entry.getKey(), entry.getValue().name());
            }
         }

         for (Entry<String, AutismServerInfoOverlay.EvidenceSource> entry : this.pluginCopyEvidence.entrySet()) {
            if (entry.getKey() != null && !entry.getKey().isBlank() && entry.getValue() != null) {
               copyEvidenceSnapshot.put(entry.getKey(), entry.getValue().name());
            }
         }

         Map<String, String> confidenceSnapshot = new LinkedHashMap<>();

         for (Entry<String, AutismServerInfoOverlay.MatchConfidence> entry : this.pluginConfidence.entrySet()) {
            if (entry.getKey() != null && !entry.getKey().isBlank() && entry.getValue() != null) {
               confidenceSnapshot.put(entry.getKey(), entry.getValue().name());
            }
         }

         ServerData current = MC.getCurrentServer();
         String serverName = current != null && current.name != null ? current.name : "";
         AutismConfig.getGlobal()
            .putPluginScan(
               this.scanAddress,
               "",
               this.detectedPlugins,
               this.pluginCommands,
               evidenceSnapshot,
               this.pluginChannels,
               this.pluginGuis,
               confidenceSnapshot,
               copyEvidenceSnapshot,
               this.pluginCopyCommands,
               this.scanStatus.name(),
               this.scanTotalProbes,
               this.scanAnsweredProbes,
               this.scanRetriedProbes,
               this.scanFailedProbes,
               serverName,
               this.scanAddress
            );
      }
   }

   private void syncServerContext() {
      String currentAddress = this.normalizedServerAddress();
      if (!currentAddress.isEmpty()) {
         if (!currentAddress.equals(this.lastSeenAddress)) {
            this.lastSeenAddress = currentAddress;
         }

         String currentContextSignature = this.computeContextSignature();
         boolean sameAddress = currentAddress.equals(this.scanAddress);
         boolean hasCurrentSignature = !currentContextSignature.isEmpty();
         boolean sameContext = sameAddress && currentContextSignature.equals(this.contextSignature);
         AutismServerInfoOverlay.ActiveScanSession session = this.activeScan;
         if (session != null && this.tick_Z) {
            if (currentAddress.equals(session.serverAddress)) {
               session.contextSignature = currentContextSignature;
               this.contextSignature = currentContextSignature;
               return;
            }

            this.cancelScan("server-changed");
         }

         if (sameAddress && this.scanDone && hasCurrentSignature && !sameContext) {
            this.contextSignature = currentContextSignature;
            this.saveScanToConfig();
         } else if (!this.setupRequested || !sameAddress || !sameContext && hasCurrentSignature) {
            if (!sameAddress || hasCurrentSignature && !sameContext) {
               this.setupRequested = false;
            }

            if ((!sameContext || !this.scanDone && !this.tick_Z)
               && (!sameAddress || hasCurrentSignature || !this.scanDone && !this.tick_Z)
               && !this.loadCachedScan(currentAddress, currentContextSignature)) {
               this.resetForServer(currentAddress, currentContextSignature);
            }
         }
      }
   }

   private String pluginKey(String plugin) {
      return PluginSignatureCatalog.canonicalName(plugin);
   }

   private int evidenceRank(AutismServerInfoOverlay.EvidenceSource evidence) {
      byte var10000;
      switch (evidence) {
         case PAYLOAD_CHANNEL:
            var10000 = 0;
            break;
         case COMMAND_GUI:
            var10000 = 4;
            break;
         case COMMAND_TREE:
            var10000 = 1;
            break;
         case USER_AUTOCOMPLETE:
            var10000 = 8;
            break;
         case SCANNER_AUTOCOMPLETE:
            var10000 = 7;
            break;
         case NAMESPACE:
            var10000 = 2;
            break;
         case ROOT_HINT:
            var10000 = 3;
            break;
         case HELP_HINT:
            var10000 = 10;
            break;
         case PLUGIN_LIST:
            var10000 = 6;
            break;
         case VERSION_HINT:
            var10000 = 5;
            break;
         case FEATURE:
            var10000 = 9;
            break;
         case UNKNOWN:
            var10000 = 11;
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   private int confidenceRank(AutismServerInfoOverlay.MatchConfidence confidence) {
      byte var10000;
      switch (confidence == null ? AutismServerInfoOverlay.MatchConfidence.UNKNOWN : confidence) {
         case EXACT:
            var10000 = 0;
            break;
         case STRONG:
            var10000 = 1;
            break;
         case FEATURE:
            var10000 = 2;
            break;
         case UNKNOWN:
            var10000 = 3;
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   private AutismServerInfoOverlay.EvidenceSource mergeEvidence(
      AutismServerInfoOverlay.EvidenceSource current, AutismServerInfoOverlay.EvidenceSource candidate
   ) {
      if (current == null) {
         return candidate == null ? AutismServerInfoOverlay.EvidenceSource.UNKNOWN : candidate;
      } else if (candidate == null) {
         return current;
      } else {
         return this.evidenceRank(candidate) < this.evidenceRank(current) ? candidate : current;
      }
   }

   private int copyEvidenceRank(AutismServerInfoOverlay.EvidenceSource evidence) {
      byte var10000;
      switch (evidence == null ? AutismServerInfoOverlay.EvidenceSource.UNKNOWN : evidence) {
         case PAYLOAD_CHANNEL:
            var10000 = 4;
            break;
         case COMMAND_GUI:
            var10000 = 5;
            break;
         case COMMAND_TREE:
            var10000 = 0;
            break;
         case USER_AUTOCOMPLETE:
         case FEATURE:
            var10000 = 6;
            break;
         case SCANNER_AUTOCOMPLETE:
         case PLUGIN_LIST:
            var10000 = 2;
            break;
         case NAMESPACE:
         case ROOT_HINT:
            var10000 = 1;
            break;
         case HELP_HINT:
         case UNKNOWN:
            var10000 = 7;
            break;
         case VERSION_HINT:
            var10000 = 3;
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   private AutismServerInfoOverlay.EvidenceSource mergeCopyEvidence(
      AutismServerInfoOverlay.EvidenceSource current, AutismServerInfoOverlay.EvidenceSource candidate
   ) {
      if (candidate == null) {
         return current == null ? AutismServerInfoOverlay.EvidenceSource.UNKNOWN : current;
      } else if (current == null) {
         return candidate;
      } else {
         return this.copyEvidenceRank(candidate) < this.copyEvidenceRank(current) ? candidate : current;
      }
   }

   private boolean isCopyCommandEvidence(AutismServerInfoOverlay.EvidenceSource evidence) {
      boolean var10000;
      switch (evidence == null ? AutismServerInfoOverlay.EvidenceSource.UNKNOWN : evidence) {
         case COMMAND_TREE:
         case NAMESPACE:
         case ROOT_HINT:
         case PLUGIN_LIST:
            var10000 = true;
            break;
         case USER_AUTOCOMPLETE:
         case SCANNER_AUTOCOMPLETE:
         case HELP_HINT:
         default:
            var10000 = false;
      }

      return var10000;
   }

   private boolean isTrustedEvidence(AutismServerInfoOverlay.EvidenceSource evidence) {
      boolean var10000;
      switch (evidence == null ? AutismServerInfoOverlay.EvidenceSource.UNKNOWN : evidence) {
         case PAYLOAD_CHANNEL:
         case COMMAND_GUI:
         case COMMAND_TREE:
         case NAMESPACE:
         case ROOT_HINT:
         case PLUGIN_LIST:
         case VERSION_HINT:
            var10000 = true;
            break;
         case USER_AUTOCOMPLETE:
         case SCANNER_AUTOCOMPLETE:
         case HELP_HINT:
         default:
            var10000 = false;
      }

      return var10000;
   }

   private AutismServerInfoOverlay.MatchConfidence mergeConfidence(
      AutismServerInfoOverlay.MatchConfidence current, AutismServerInfoOverlay.MatchConfidence candidate
   ) {
      if (current == null) {
         return candidate == null ? AutismServerInfoOverlay.MatchConfidence.UNKNOWN : candidate;
      } else if (candidate == null) {
         return current;
      } else {
         return this.confidenceRank(candidate) < this.confidenceRank(current) ? candidate : current;
      }
   }

   private AutismServerInfoOverlay.MatchConfidence confidenceForEvidence(AutismServerInfoOverlay.EvidenceSource evidence) {
      AutismServerInfoOverlay.MatchConfidence var10000;
      switch (evidence == null ? AutismServerInfoOverlay.EvidenceSource.UNKNOWN : evidence) {
         case PAYLOAD_CHANNEL:
         case NAMESPACE:
         case VERSION_HINT:
            var10000 = AutismServerInfoOverlay.MatchConfidence.EXACT;
            break;
         case COMMAND_GUI:
         case COMMAND_TREE:
         case USER_AUTOCOMPLETE:
         case SCANNER_AUTOCOMPLETE:
         case HELP_HINT:
         case PLUGIN_LIST:
         case FEATURE:
            var10000 = AutismServerInfoOverlay.MatchConfidence.FEATURE;
            break;
         case ROOT_HINT:
            var10000 = AutismServerInfoOverlay.MatchConfidence.STRONG;
            break;
         case UNKNOWN:
            var10000 = AutismServerInfoOverlay.MatchConfidence.UNKNOWN;
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   private boolean isIndirectEvidence(AutismServerInfoOverlay.EvidenceSource evidence) {
      boolean var10000;
      switch (evidence == null ? AutismServerInfoOverlay.EvidenceSource.UNKNOWN : evidence) {
         case USER_AUTOCOMPLETE:
         case SCANNER_AUTOCOMPLETE:
         case HELP_HINT:
         case FEATURE:
         case UNKNOWN:
            var10000 = true;
            break;
         case NAMESPACE:
         case ROOT_HINT:
         case PLUGIN_LIST:
         case VERSION_HINT:
         default:
            var10000 = false;
      }

      return var10000;
   }

   private boolean isFeatureOrIndirectEntry(AutismServerInfoOverlay.PluginAccumulator entry) {
      return entry == null
         ? false
         : entry.resultKind == AutismServerInfoOverlay.DetectionType.FEATURE || this.isIndirectEvidence(entry.evidence);
   }

   private boolean isDirectPluginEvidence(AutismServerInfoOverlay.PluginAccumulator entry) {
      return entry != null
         && entry.resultKind == AutismServerInfoOverlay.DetectionType.PLUGIN
         && !this.isIndirectEvidence(entry.evidence)
         && entry.evidence != AutismServerInfoOverlay.EvidenceSource.COMMAND_GUI;
   }

   private int entrySortScore(AutismServerInfoOverlay.PluginAccumulator entry) {
      if (entry == null) {
         return 2147483647;
      }

      int score = this.evidenceRank(entry.evidence) * 100 + this.confidenceRank(entry.confidence) * 10;
      if (entry.resultKind == AutismServerInfoOverlay.DetectionType.FEATURE) {
         score += 1000;
      }

      if (this.isIndirectEvidence(entry.evidence)) {
         score += 500;
      }

      if (!entry.commandBacked) {
         score += 25;
      }

      String name = entry.name == null ? "" : entry.name.trim();
      if (name.startsWith("/")) {
         score += 75;
      }

      return score;
   }

   private AutismServerInfoOverlay.EvidenceSource parseEvidenceSource(String value) {
      if (value != null && !value.isBlank()) {
         try {
            return AutismServerInfoOverlay.EvidenceSource.valueOf(value);
         } catch (IllegalArgumentException ignored) {
            return AutismServerInfoOverlay.EvidenceSource.UNKNOWN;
         }
      } else {
         return AutismServerInfoOverlay.EvidenceSource.UNKNOWN;
      }
   }

   private AutismServerInfoOverlay.MatchConfidence parseConfidence(
      String value, AutismServerInfoOverlay.MatchConfidence fallback
   ) {
      if (value != null && !value.isBlank()) {
         try {
            return AutismServerInfoOverlay.MatchConfidence.valueOf(value);
         } catch (IllegalArgumentException ignored) {
            return fallback == null ? AutismServerInfoOverlay.MatchConfidence.UNKNOWN : fallback;
         }
      } else {
         return fallback == null ? AutismServerInfoOverlay.MatchConfidence.UNKNOWN : fallback;
      }
   }

   private int confidenceColor(AutismServerInfoOverlay.MatchConfidence confidence, boolean hovered) {
      if (hovered) {
         return UiColorPalette.softWhite();
      }

      int var10000;
      switch (confidence == null ? AutismServerInfoOverlay.MatchConfidence.UNKNOWN : confidence) {
         case EXACT:
            var10000 = UiColorPalette.mint();
            break;
         case STRONG:
            var10000 = UiColorPalette.cyan();
            break;
         case FEATURE:
            var10000 = UiColorPalette.warningYellow();
            break;
         case UNKNOWN:
            var10000 = this.theme.load(autismclient.gui.f.b.l.write);
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   private String confidenceLabel(AutismServerInfoOverlay.MatchConfidence confidence) {
      String var10000;
      switch (confidence == null ? AutismServerInfoOverlay.MatchConfidence.UNKNOWN : confidence) {
         case EXACT:
            var10000 = "Exact";
            break;
         case STRONG:
            var10000 = "Strong";
            break;
         case FEATURE:
            var10000 = "Feature";
            break;
         case UNKNOWN:
            var10000 = "Unknown";
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   private Map<String, AutismServerInfoOverlay.PluginAccumulator> snapshotPluginEntries() {
      Map<String, AutismServerInfoOverlay.PluginAccumulator> entries = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

      for (String plugin : this.detectedPlugins) {
         if (plugin != null && !plugin.isBlank()) {
            List<String> existing = this.commandsFor(plugin);
            AutismServerInfoOverlay.EvidenceSource evidence = this.pluginEvidence
               .getOrDefault(
                  this.pluginKey(plugin),
                  existing != null && !existing.isEmpty() ? AutismServerInfoOverlay.EvidenceSource.ROOT_HINT : AutismServerInfoOverlay.EvidenceSource.UNKNOWN
               );
            List<String> existingChannels = this.observedChannelsFor(plugin);
            List<String> existingGuis = this.guisFor(plugin);
            AutismServerInfoOverlay.MatchConfidence confidence = this.pluginConfidence
               .getOrDefault(this.pluginKey(plugin), this.confidenceForEvidence(evidence));
            AutismServerInfoOverlay.DetectionType resultKind = this.detectionTypeFor(plugin, evidence, confidence);
            this.mergePluginEntry(
               entries, null, plugin, existing, existingChannels, existingGuis, existing != null && !existing.isEmpty(), evidence, confidence, resultKind
            );
            String key = this.pluginKey(plugin);
            AutismServerInfoOverlay.PluginAccumulator entry = entries.get(key);
            if (entry != null) {
               AutismServerInfoOverlay.EvidenceSource copyEvidence = this.pluginCopyEvidence
                  .getOrDefault(key, this.copyEvidenceFor(plugin, evidence));
               if (this.copyEvidenceRank(copyEvidence) < this.copyEvidenceRank(entry.copyEvidence)) {
                  entry.copyCommands.clear();
               }

               entry.copyEvidence = this.mergeCopyEvidence(entry.copyEvidence, copyEvidence);
               List<String> copyCommands = this.lookupPluginList(this.pluginCopyCommands, plugin);
               if (copyCommands != null) {
                  for (String command : copyCommands) {
                     if (command != null && !command.isBlank()) {
                        entry.copyCommands.add(command.trim());
                     }
                  }
               }
            }
         }
      }

      return entries;
   }

   private AutismServerInfoOverlay.ScanStatus parseScanStatus(String value) {
      if (value != null && !value.isBlank()) {
         try {
            return AutismServerInfoOverlay.ScanStatus.valueOf(value.trim().toUpperCase(Locale.ROOT));
         } catch (IllegalArgumentException ignored) {
            return AutismServerInfoOverlay.ScanStatus.NONE;
         }
      } else {
         return AutismServerInfoOverlay.ScanStatus.NONE;
      }
   }

   private boolean mergePluginEntry(
      Map<String, AutismServerInfoOverlay.PluginAccumulator> entries,
      String pluginName,
      Collection<String> commands,
      boolean commandBacked,
      AutismServerInfoOverlay.EvidenceSource evidence
   ) {
      return this.mergePluginEntry(
         entries, null, pluginName, commands, List.of(), List.of(), commandBacked, evidence, this.confidenceForEvidence(evidence)
      );
   }

   private boolean mergePluginEntry(
      Map<String, AutismServerInfoOverlay.PluginAccumulator> entries,
      String pluginName,
      Collection<String> commands,
      Collection<String> channels,
      boolean commandBacked,
      AutismServerInfoOverlay.EvidenceSource evidence
   ) {
      return this.mergePluginEntry(
         entries, null, pluginName, commands, channels, List.of(), commandBacked, evidence, this.confidenceForEvidence(evidence)
      );
   }

   private boolean mergePluginEntry(
      Map<String, AutismServerInfoOverlay.PluginAccumulator> entries,
      String explicitKey,
      String pluginName,
      Collection<String> commands,
      Collection<String> channels,
      boolean commandBacked,
      AutismServerInfoOverlay.EvidenceSource evidence
   ) {
      return this.mergePluginEntry(
         entries, explicitKey, pluginName, commands, channels, List.of(), commandBacked, evidence, this.confidenceForEvidence(evidence)
      );
   }

   private boolean mergePluginEntry(
      Map<String, AutismServerInfoOverlay.PluginAccumulator> entries,
      String explicitKey,
      String pluginName,
      Collection<String> commands,
      Collection<String> channels,
      Collection<String> guis,
      boolean commandBacked,
      AutismServerInfoOverlay.EvidenceSource evidence,
      AutismServerInfoOverlay.MatchConfidence confidence
   ) {
      return this.mergePluginEntry(
         entries, explicitKey, pluginName, commands, channels, guis, commandBacked, evidence, confidence, AutismServerInfoOverlay.DetectionType.PLUGIN
      );
   }

   private boolean mergePluginEntry(
      Map<String, AutismServerInfoOverlay.PluginAccumulator> entries,
      String explicitKey,
      String pluginName,
      Collection<String> commands,
      Collection<String> channels,
      Collection<String> guis,
      boolean commandBacked,
      AutismServerInfoOverlay.EvidenceSource evidence,
      AutismServerInfoOverlay.MatchConfidence confidence,
      AutismServerInfoOverlay.DetectionType resultKind
   ) {
      if (pluginName != null && !pluginName.isBlank()) {
         String cleanName = pluginName.trim();
         String key = explicitKey != null && !explicitKey.isBlank() ? this.pluginKey(explicitKey) : this.pluginKey(cleanName);
         if (!key.isEmpty() && (!PLATFORM_NAMESPACES.contains(key) || evidence == AutismServerInfoOverlay.EvidenceSource.PAYLOAD_CHANNEL)) {
            AutismServerInfoOverlay.PluginAccumulator entry = entries.get(key);
            if (entry == null) {
               if (entries.size() >= 1000) {
                  return false;
               }

               entry = new AutismServerInfoOverlay.PluginAccumulator();
               entries.put(key, entry);
            }

            String beforeName = entry.name;
            boolean beforeBacked = entry.commandBacked;
            AutismServerInfoOverlay.EvidenceSource beforeEvidence = entry.evidence;
            AutismServerInfoOverlay.EvidenceSource beforeCopyEvidence = entry.copyEvidence;
            AutismServerInfoOverlay.MatchConfidence beforeConfidence = entry.confidence;
            AutismServerInfoOverlay.DetectionType beforeKind = entry.resultKind;
            int beforeCommandCount = entry.commands.size();
            int beforeCopyCommandCount = entry.copyCommands.size();
            int beforeChannelCount = entry.channels.size();
            int beforeGuiCount = entry.guis.size();
            boolean candidateHasBetterCopySource = this.copyEvidenceRank(evidence) < this.copyEvidenceRank(entry.copyEvidence);
            boolean allowReadableRename = !this.isTrustedEvidence(entry.copyEvidence) || candidateHasBetterCopySource;
            if (candidateHasBetterCopySource) {
               entry.copyCommands.clear();
            }

            if (entry.name == null
               || entry.name.isBlank()
               || commandBacked && !entry.commandBacked
               || allowReadableRename && this.isBetterPluginName(cleanName, entry.name)) {
               entry.name = cleanName;
            }

            entry.resultKind = this.mergeDetectionType(entry.resultKind, resultKind);
            if (commandBacked) {
               entry.commandBacked = true;
            }

            entry.evidence = this.mergeEvidence(entry.evidence, evidence);
            entry.copyEvidence = this.mergeCopyEvidence(entry.copyEvidence, evidence);
            entry.confidence = this.mergeConfidence(entry.confidence, confidence);
            this.pluginEvidence.put(key, this.mergeEvidence(this.pluginEvidence.get(key), evidence));
            this.pluginCopyEvidence.put(key, this.mergeCopyEvidence(this.pluginCopyEvidence.get(key), evidence));
            this.pluginConfidence.put(key, this.mergeConfidence(this.pluginConfidence.get(key), confidence));
            this.pluginDetectionTypes.put(key, this.mergeDetectionType(this.pluginDetectionTypes.get(key), resultKind));
            if (commands != null) {
               for (String command : commands) {
                  if (command != null) {
                     String cleanCommand = command.trim();
                     if (!cleanCommand.isEmpty() && (entry.commands.size() < 256 || entry.commands.contains(cleanCommand))) {
                        entry.commands.add(cleanCommand);
                     }

                     if (!cleanCommand.isEmpty()
                        && this.isCopyCommandEvidence(evidence)
                        && this.copyEvidenceRank(evidence) <= this.copyEvidenceRank(entry.copyEvidence)
                        && (entry.copyCommands.size() < 256 || entry.copyCommands.contains(cleanCommand))) {
                        entry.copyCommands.add(cleanCommand);
                     }
                  }
               }
            }

            if (channels != null) {
               for (String channel : channels) {
                  if (channel != null) {
                     String cleanChannel = channel.trim();
                     if (!cleanChannel.isEmpty() && (entry.channels.size() < 64 || entry.channels.contains(cleanChannel))) {
                        entry.channels.add(cleanChannel);
                     }
                  }
               }
            }

            if (guis != null) {
               for (String gui : guis) {
                  if (gui != null) {
                     String cleanGui = gui.trim();
                     if (!cleanGui.isEmpty() && (entry.guis.size() < 64 || entry.guis.contains(cleanGui))) {
                        entry.guis.add(cleanGui);
                     }
                  }
               }
            }

            return !Objects.equals(beforeName, entry.name)
               || beforeBacked != entry.commandBacked
               || beforeEvidence != entry.evidence
               || beforeCopyEvidence != entry.copyEvidence
               || beforeConfidence != entry.confidence
               || beforeKind != entry.resultKind
               || beforeCommandCount != entry.commands.size()
               || beforeCopyCommandCount != entry.copyCommands.size()
               || beforeChannelCount != entry.channels.size()
               || beforeGuiCount != entry.guis.size();
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private boolean mergeAccumulator(
      Map<String, AutismServerInfoOverlay.PluginAccumulator> target, String explicitKey, AutismServerInfoOverlay.PluginAccumulator source
   ) {
      if (target != null && source != null && source.name != null && !source.name.isBlank()) {
         boolean changed = this.mergePluginEntry(
            target,
            explicitKey,
            source.name,
            source.commands,
            source.channels,
            source.guis,
            source.commandBacked,
            source.evidence,
            source.confidence,
            source.resultKind
         );
         String key = explicitKey != null && !explicitKey.isBlank() ? this.pluginKey(explicitKey) : this.pluginKey(source.name);
         AutismServerInfoOverlay.PluginAccumulator entry = target.get(key);
         if (entry != null) {
            AutismServerInfoOverlay.EvidenceSource beforeCopyEvidence = entry.copyEvidence;
            int beforeCopyCommandCount = entry.copyCommands.size();
            boolean betterCopySource = this.copyEvidenceRank(source.copyEvidence) < this.copyEvidenceRank(entry.copyEvidence);
            if (betterCopySource) {
               entry.copyCommands.clear();
            }

            entry.copyEvidence = this.mergeCopyEvidence(
               entry.copyEvidence, source.copyEvidence
            );
            Collection<String> sourceCopyCommands = source.copyCommands.isEmpty() && this.isCopyCommandEvidence(source.copyEvidence)
               ? source.commands
               : source.copyCommands;
            if (sourceCopyCommands != null && this.copyEvidenceRank(source.copyEvidence) <= this.copyEvidenceRank(entry.copyEvidence)) {
               for (String command : sourceCopyCommands) {
                  if (command != null && !command.isBlank()) {
                     entry.copyCommands.add(command.trim());
                  }
               }
            }

            changed |= beforeCopyEvidence != entry.copyEvidence || beforeCopyCommandCount != entry.copyCommands.size();
         }

         return changed;
      } else {
         return false;
      }
   }

   private boolean mergeAccumulators(Map<String, AutismServerInfoOverlay.PluginAccumulator> target, Map<String, AutismServerInfoOverlay.PluginAccumulator> source) {
      if (target != null && source != null && !source.isEmpty()) {
         boolean changed = false;

         for (Entry<String, AutismServerInfoOverlay.PluginAccumulator> entry : source.entrySet()) {
            changed |= this.mergeAccumulator(target, entry.getKey(), entry.getValue());
         }

         return changed;
      } else {
         return false;
      }
   }

   private boolean normalizeCommandSet(Set<String> commands) {
      if (commands != null && !commands.isEmpty()) {
         TreeSet<String> normalized = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

         for (String command : commands) {
            String token = this.normalizeCommandToken(command);
            if (!token.isBlank()) {
               normalized.add(token);
            }
         }

         if (normalized.size() == commands.size() && normalized.containsAll(commands)) {
            return false;
         }

         commands.clear();
         commands.addAll(normalized);
         return true;
      } else {
         return false;
      }
   }

   private String findOwnerKey(Map<String, AutismServerInfoOverlay.PluginAccumulator> entries, String command) {
      if (entries != null && !entries.isEmpty()) {
         String token = this.normalizeCommandToken(command);
         if (token.isBlank()) {
            return null;
         }

         String bestKey = null;
         int bestScore = 2147483647;

         for (Entry<String, AutismServerInfoOverlay.PluginAccumulator> mapEntry : new ArrayList<>(entries.entrySet())) {
            AutismServerInfoOverlay.PluginAccumulator entry = mapEntry.getValue();
            if (entry != null && entry.commands != null && entry.commands.contains(token)) {
               int score = this.entrySortScore(entry);
               if (score < bestScore) {
                  bestScore = score;
                  bestKey = mapEntry.getKey();
               }
            }
         }

         String aliasedPlugin = PLUGIN_ALIASES.get(token);
         if (aliasedPlugin != null && !aliasedPlugin.isBlank()) {
            String aliasedKey = this.pluginKey(aliasedPlugin);
            AutismServerInfoOverlay.PluginAccumulator aliasedEntry = entries.get(aliasedKey);
            if (this.isDirectPluginEvidence(aliasedEntry)) {
               int score = this.entrySortScore(aliasedEntry);
               if (bestKey == null || score <= bestScore || !this.isDirectPluginEvidence(entries.get(bestKey))) {
                  bestKey = aliasedKey;
               }
            }
         }

         return bestKey;
      } else {
         return null;
      }
   }

   private AutismServerInfoOverlay.PluginAccumulator createCommandAccumulator(String command) {
      String token = this.normalizeCommandToken(command);
      AutismServerInfoOverlay.PluginAccumulator entry = new AutismServerInfoOverlay.PluginAccumulator();
      entry.name = "/" + token;
      entry.commandBacked = true;
      entry.resultKind = AutismServerInfoOverlay.DetectionType.PLUGIN;
      entry.evidence = AutismServerInfoOverlay.EvidenceSource.SCANNER_AUTOCOMPLETE;
      entry.copyEvidence = AutismServerInfoOverlay.EvidenceSource.UNKNOWN;
      entry.confidence = AutismServerInfoOverlay.MatchConfidence.FEATURE;
      entry.commands.add(token);
      return entry;
   }

   private boolean normalizeEntries(Map<String, AutismServerInfoOverlay.PluginAccumulator> entries) {
      if (entries != null && !entries.isEmpty()) {
         boolean changed = false;

         for (AutismServerInfoOverlay.PluginAccumulator entry : entries.values()) {
            if (entry != null) {
               changed |= this.normalizeCommandSet(entry.commands);
               changed |= this.normalizeCommandSet(entry.copyCommands);
            }
         }

         TreeSet<String> allCommands = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

         for (AutismServerInfoOverlay.PluginAccumulator entry : entries.values()) {
            if (entry != null && entry.commands != null) {
               allCommands.addAll(entry.commands);
            }
         }

         Map<String, String> ownerByCommand = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

         for (String command : allCommands) {
            String ownerKey = this.findOwnerKey(entries, command);
            if (ownerKey != null && !ownerKey.isBlank()) {
               ownerByCommand.put(command, ownerKey);
            }
         }

         for (Entry<String, AutismServerInfoOverlay.PluginAccumulator> mapEntry : new ArrayList<>(entries.entrySet())) {
            String key = mapEntry.getKey();
            AutismServerInfoOverlay.PluginAccumulator entry = mapEntry.getValue();
            if (key != null && entry != null && !entry.commands.isEmpty()) {
               for (String command : new ArrayList<>(entry.commands)) {
                  String ownerKey = ownerByCommand.get(command);
                  if (ownerKey != null && !ownerKey.equals(key)) {
                     AutismServerInfoOverlay.PluginAccumulator owner = entries.get(ownerKey);
                     if (owner != null) {
                        if (owner.commands.add(command)) {
                           changed = true;
                        }

                        if (this.isCopyCommandEvidence(owner.copyEvidence) && owner.copyCommands.add(command)) {
                           changed = true;
                        }

                        if (!entry.guis.isEmpty()) {
                           int beforeGuiCount = owner.guis.size();
                           owner.guis.addAll(entry.guis);
                           changed |= beforeGuiCount != owner.guis.size();
                        }

                        entry.commands.remove(command);
                        entry.copyCommands.remove(command);
                        changed = true;
                     }
                  }
               }
            }
         }

         List<String> removeKeys = new ArrayList<>();
         Map<String, AutismServerInfoOverlay.PluginAccumulator> addEntries = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

         for (Entry<String, AutismServerInfoOverlay.PluginAccumulator> mapEntry : new ArrayList<>(entries.entrySet())) {
            String key = mapEntry.getKey();
            AutismServerInfoOverlay.PluginAccumulator entry = mapEntry.getValue();
            if (key == null || entry == null) {
               removeKeys.add(key);
            } else if (this.isFeatureOrIndirectEntry(entry)) {
               if (entry.commands.isEmpty()) {
                  removeKeys.add(key);
               } else if (entry.commands.size() == 1) {
                  String command = entry.commands.iterator().next();
                  String expectedName = "/" + command;
                  if (!expectedName.equals(entry.name)) {
                     entry.name = expectedName;
                     changed = true;
                  }

                  if (entry.resultKind != AutismServerInfoOverlay.DetectionType.PLUGIN) {
                     entry.resultKind = AutismServerInfoOverlay.DetectionType.PLUGIN;
                     changed = true;
                  }

                  if (entry.evidence != AutismServerInfoOverlay.EvidenceSource.SCANNER_AUTOCOMPLETE) {
                     entry.evidence = AutismServerInfoOverlay.EvidenceSource.SCANNER_AUTOCOMPLETE;
                     changed = true;
                  }

                  if (entry.copyEvidence != AutismServerInfoOverlay.EvidenceSource.UNKNOWN) {
                     entry.copyEvidence = AutismServerInfoOverlay.EvidenceSource.UNKNOWN;
                     entry.copyCommands.clear();
                     changed = true;
                  }

                  entry.confidence = AutismServerInfoOverlay.MatchConfidence.FEATURE;
               } else {
                  for (String command : entry.commands) {
                     String hintKey = this.pluginKey("command_" + command);
                     if (!entries.containsKey(hintKey) && !addEntries.containsKey(hintKey)) {
                        addEntries.put(hintKey, this.createCommandAccumulator(command));
                     }
                  }

                  removeKeys.add(key);
                  changed = true;
               }
            }
         }

         for (String key : removeKeys) {
            if (key != null && entries.remove(key) != null) {
               changed = true;
            }
         }

         if (!addEntries.isEmpty()) {
            entries.putAll(addEntries);
            changed = true;
         }

         return changed;
      } else {
         return false;
      }
   }

   private int countNamedEntries(Map<String, AutismServerInfoOverlay.PluginAccumulator> entries) {
      if (entries != null && !entries.isEmpty()) {
         int count = 0;

         for (AutismServerInfoOverlay.PluginAccumulator entry : entries.values()) {
            if (entry != null && entry.name != null && !entry.name.isBlank()) {
               count++;
            }
         }

         return count;
      } else {
         return 0;
      }
   }

   private void trace(AutismServerInfoOverlay.ActiveScanSession session, String message) {
      if (TRACE_PLUGIN_SCAN) {
         String generation = session == null ? "-" : Long.toString(session.generation);
         System.out.println("[AUTISM][PluginScan][gen=" + generation + "] " + message);
      }
   }

   private AutismServerInfoOverlay.DetectionType mergeDetectionType(
      AutismServerInfoOverlay.DetectionType current, AutismServerInfoOverlay.DetectionType candidate
   ) {
      if (candidate == null) {
         return current == null ? AutismServerInfoOverlay.DetectionType.PLUGIN : current;
      } else if (current == null) {
         return candidate;
      } else {
         return current != AutismServerInfoOverlay.DetectionType.PLUGIN && candidate != AutismServerInfoOverlay.DetectionType.PLUGIN
            ? AutismServerInfoOverlay.DetectionType.FEATURE
            : AutismServerInfoOverlay.DetectionType.PLUGIN;
      }
   }

   private AutismServerInfoOverlay.DetectionType detectionTypeFor(
      String plugin, AutismServerInfoOverlay.EvidenceSource evidence, AutismServerInfoOverlay.MatchConfidence confidence
   ) {
      String key = this.pluginKey(plugin);
      AutismServerInfoOverlay.DetectionType stored = this.pluginDetectionTypes.get(key);
      if (stored != null) {
         return stored;
      }

      if (confidence != AutismServerInfoOverlay.MatchConfidence.EXACT && confidence != AutismServerInfoOverlay.MatchConfidence.STRONG) {
         if (evidence != AutismServerInfoOverlay.EvidenceSource.PAYLOAD_CHANNEL
            && evidence != AutismServerInfoOverlay.EvidenceSource.VERSION_HINT
            && evidence != AutismServerInfoOverlay.EvidenceSource.NAMESPACE) {
            String lower = plugin == null ? "" : plugin.trim().toLowerCase(Locale.ROOT);

            for (String featureLabel : FEATURE_KEYWORDS.values()) {
               if (lower.equals(featureLabel.toLowerCase(Locale.ROOT))) {
                  return AutismServerInfoOverlay.DetectionType.FEATURE;
               }
            }

            return AutismServerInfoOverlay.DetectionType.PLUGIN;
         } else {
            return AutismServerInfoOverlay.DetectionType.PLUGIN;
         }
      } else {
         return AutismServerInfoOverlay.DetectionType.PLUGIN;
      }
   }

   private AutismServerInfoOverlay.EvidenceSource copyEvidenceFor(
      String plugin, AutismServerInfoOverlay.EvidenceSource evidence
   ) {
      if (evidence == AutismServerInfoOverlay.EvidenceSource.UNKNOWN || evidence == null) {
         List<String> commands = this.lookupPluginList(this.pluginCommands, plugin);
         if (commands != null && !commands.isEmpty()) {
            return AutismServerInfoOverlay.EvidenceSource.ROOT_HINT;
         }
      }

      return this.isTrustedEvidence(evidence) ? evidence : AutismServerInfoOverlay.EvidenceSource.UNKNOWN;
   }

   private List<String> lookupPluginList(Map<String, List<String>> map, String plugin) {
      if (map != null && !map.isEmpty() && plugin != null && !plugin.isBlank()) {
         List<String> direct = map.get(plugin);
         if (direct != null) {
            return direct;
         }

         String key = this.pluginKey(plugin);

         for (Entry<String, List<String>> entry : map.entrySet()) {
            if (entry.getKey() != null && this.pluginKey(entry.getKey()).equals(key)) {
               return entry.getValue();
            }
         }

         return null;
      } else {
         return null;
      }
   }

   private boolean isBetterPluginName(String candidate, String current) {
      if (candidate == null || candidate.isBlank()) {
         return false;
      }

      if (current != null && !current.isBlank()) {
         String c = candidate.trim();
         String old = current.trim();
         if (c.equalsIgnoreCase(old)) {
            return false;
         }

         String cKey = this.pluginKey(c);
         String oldKey = this.pluginKey(old);
         if (!cKey.equals(oldKey)) {
            return false;
         }

         boolean candidateHasCase = !c.equals(c.toLowerCase(Locale.ROOT));
         boolean currentHasCase = !old.equals(old.toLowerCase(Locale.ROOT));
         boolean candidateHasSpace = c.indexOf(32) >= 0;
         boolean currentHasSpace = old.indexOf(32) >= 0;
         return candidateHasCase && !currentHasCase || candidateHasSpace && !currentHasSpace;
      } else {
         return true;
      }
   }

   private void applyPluginEntries(Map<String, AutismServerInfoOverlay.PluginAccumulator> entries, boolean resetSelection) {
      Map<String, AutismServerInfoOverlay.PluginAccumulator> normalizedEntries = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
      if (entries != null) {
         normalizedEntries.putAll(entries);
      }

      this.normalizeEntries(normalizedEntries);
      this.detectedPlugins.clear();
      this.pluginCommands.clear();
      this.pluginChannels.clear();
      this.pluginGuis.clear();
      this.pluginEvidence.clear();
      this.pluginCopyEvidence.clear();
      this.pluginCopyCommands.clear();
      this.pluginConfidence.clear();
      this.pluginDetectionTypes.clear();
      List<AutismServerInfoOverlay.PluginAccumulator> sortedEntries = new ArrayList<>(normalizedEntries.values());
      sortedEntries.removeIf(entryx -> entryx == null || entryx.name == null || entryx.name.isBlank());
      sortedEntries.sort((a, b) -> String.CASE_INSENSITIVE_ORDER.compare(a.name, b.name));

      for (AutismServerInfoOverlay.PluginAccumulator entry : sortedEntries) {
         String plugin = entry.name;
         List<String> commands = new ArrayList<>(entry.commands);
         commands.removeIf(cmd -> cmd == null || cmd.isBlank());
         commands.sort(String.CASE_INSENSITIVE_ORDER);
         this.detectedPlugins.add(plugin);
         this.pluginCommands.put(plugin, commands);
         this.pluginChannels.put(plugin, this.distinctRows(entry.channels));
         List<String> guis = new ArrayList<>(entry.guis);
         guis.removeIf(gui -> gui == null || gui.isBlank());
         guis.sort(String.CASE_INSENSITIVE_ORDER);
         this.pluginGuis.put(plugin, List.copyOf(guis));
         this.pluginEvidence
            .put(
               this.pluginKey(plugin), entry.evidence == null ? AutismServerInfoOverlay.EvidenceSource.UNKNOWN : entry.evidence
            );
         this.pluginCopyEvidence
            .put(this.pluginKey(plugin), entry.copyEvidence == null ? AutismServerInfoOverlay.EvidenceSource.UNKNOWN : entry.copyEvidence);
         List<String> copyCommands = new ArrayList<>(entry.copyCommands);
         copyCommands.removeIf(cmd -> cmd == null || cmd.isBlank());
         copyCommands.sort(String.CASE_INSENSITIVE_ORDER);
         this.pluginCopyCommands.put(plugin, List.copyOf(copyCommands));
         this.pluginConfidence
            .put(
               this.pluginKey(plugin),
               entry.confidence == null
                  ? this.confidenceForEvidence(entry.evidence)
                  : entry.confidence
            );
         this.pluginDetectionTypes
            .put(
               this.pluginKey(plugin), entry.resultKind == null ? AutismServerInfoOverlay.DetectionType.PLUGIN : entry.resultKind
            );
      }

      if (resetSelection) {
         this.scroll.jumpTo(0, 0);
         this.selectedPlugin = null;
      } else if (this.selectedPlugin != null && !this.isDetected(this.selectedPlugin)) {
         this.selectedPlugin = null;
      }

      this.invalidateDetails();
   }

   private boolean isProbedNamespace(String key) {
      for (String namespace : PROBED_NAMESPACES) {
         if (namespace.equalsIgnoreCase(key)) {
            return true;
         }
      }

      return false;
   }

   private String normalizeCommandToken(String raw) {
      if (raw == null) {
         return "";
      }

      String token = raw.trim();
      if (token.isEmpty()) {
         return "";
      }

      while (token.startsWith("/")) {
         token = token.substring(1).trim();
      }

      int spaceIndex = token.indexOf(32);
      if (spaceIndex >= 0) {
         token = token.substring(0, spaceIndex).trim();
      }

      while (token.endsWith(":")) {
         token = token.substring(0, token.length() - 1).trim();
      }

      return token.toLowerCase(Locale.ROOT);
   }

   private boolean isPluginCommandToken(String token) {
      if (token != null && !token.isBlank()) {
         String clean = token.trim().toLowerCase(Locale.ROOT);
         if (clean.length() < 2) {
            return false;
         } else if (!PLATFORM_NAMESPACES.contains(clean) && !VANILLA_COMMANDS.contains(clean)) {
            return this.knownPlayerNames().contains(clean)
               ? false
               : clean.chars().allMatch(ch -> Character.isLetterOrDigit(ch) || ch == 95 || ch == 45 || ch == 46 || ch == 58);
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private boolean attributeCommand(Map<String, AutismServerInfoOverlay.PluginAccumulator> entries, String rawCommand, AutismServerInfoOverlay.EvidenceSource source) {
      String token = this.normalizeCommandToken(rawCommand);
      if (!this.isPluginCommandToken(token)) {
         return false;
      }

      this.rememberCommandToken(token);
      if (token.contains(":")) {
         String[] parts = token.split(":", 2);
         String namespace = parts[0].trim().toLowerCase(Locale.ROOT);
         String command = parts.length > 1 ? parts[1].trim().toLowerCase(Locale.ROOT) : "";
         return this.isPluginCommandToken(namespace) && !PLATFORM_NAMESPACES.contains(namespace)
            ? this.mergePluginEntry(
               entries,
               namespace,
               namespace,
               command.isBlank() ? List.of() : List.of(command),
               List.of(),
               List.of(),
               !command.isBlank(),
               AutismServerInfoOverlay.EvidenceSource.NAMESPACE,
               AutismServerInfoOverlay.MatchConfidence.EXACT
            )
            : false;
      }

      String plugin = null;
      AutismServerInfoOverlay.MatchConfidence confidence = AutismServerInfoOverlay.MatchConfidence.FEATURE;
      AutismServerInfoOverlay.EvidenceSource evidence = source == null ? AutismServerInfoOverlay.EvidenceSource.FEATURE : source;
      if (this.isProbedNamespace(token)) {
         plugin = token;
         confidence = AutismServerInfoOverlay.MatchConfidence.EXACT;
         evidence = AutismServerInfoOverlay.EvidenceSource.NAMESPACE;
      } else if (PLUGIN_ALIASES.containsKey(token)) {
         plugin = PLUGIN_ALIASES.get(token);
         confidence = AutismServerInfoOverlay.MatchConfidence.STRONG;
         evidence = AutismServerInfoOverlay.EvidenceSource.ROOT_HINT;
      }

      if (plugin != null && !plugin.isBlank()) {
         return this.mergePluginEntry(
            entries, null, plugin, List.of(token), List.of(), List.of(), true, evidence, confidence, AutismServerInfoOverlay.DetectionType.PLUGIN
         );
      }

      String ownerKey = this.findOwnerKey(entries, token);
      if (ownerKey != null) {
         AutismServerInfoOverlay.PluginAccumulator owner = entries.get(ownerKey);
         if (owner == null) {
            return false;
         }

         boolean changed = owner.commands.add(token);
         if (this.isCopyCommandEvidence(owner.copyEvidence)) {
            changed |= owner.copyCommands.add(token);
         }

         return changed;
      } else {
         return source != AutismServerInfoOverlay.EvidenceSource.USER_AUTOCOMPLETE
               && source != AutismServerInfoOverlay.EvidenceSource.COMMAND_GUI
               && source != AutismServerInfoOverlay.EvidenceSource.SCANNER_AUTOCOMPLETE
               && !FEATURE_KEYWORDS.containsKey(token)
            ? false
            : this.mergePluginEntry(
               entries,
               "command_" + token,
               "/" + token,
               List.of(token),
               List.of(),
               List.of(),
               true,
               AutismServerInfoOverlay.EvidenceSource.SCANNER_AUTOCOMPLETE,
               AutismServerInfoOverlay.MatchConfidence.FEATURE,
               AutismServerInfoOverlay.DetectionType.PLUGIN
            );
      }
   }

   private Set<String> auditActiveKnownPlayers;

   private Set<String> knownPlayerNames() {
      if (this.auditActiveKnownPlayers != null) {
         return this.auditActiveKnownPlayers;
      }

      Set<String> names = new HashSet<>();
      if (MC != null) {
         if (MC.player != null && MC.player.getGameProfile() != null) {
            auditAddPlayerName(names, MC.player.getGameProfile().name());
         }

         // Tab-list entries only: level players include NPCs whose names can match real commands.
         if (MC.getConnection() != null) {
            for (PlayerInfo player : MC.getConnection().getOnlinePlayers()) {
               if (player != null && player.getProfile() != null) {
                  auditAddPlayerName(names, player.getProfile().name());
               }
            }
         }
      }

      return names;
   }

   private static void auditAddPlayerName(Set<String> names, String raw) {
      if (raw != null && !raw.isBlank()) {
         String lower = raw.trim().toLowerCase(Locale.ROOT);
         names.add(lower);
         if (lower.startsWith(".") && lower.length() > 2) {
            names.add(lower.substring(1));
         }
      }
   }

   private static String auditSelfPlayerNameLower() {
      if (MC != null && MC.player != null && MC.player.getGameProfile() != null) {
         String name = MC.player.getGameProfile().name();
         if (name != null && !name.isBlank()) {
            return name.trim().toLowerCase(Locale.ROOT);
         }
      }
      return "";
   }

   private static boolean auditIsObservableCommandCompletion(String rawCommand) {
      if (rawCommand == null) {
         return false;
      }
      String leading = rawCommand.stripLeading();
      if (!leading.startsWith("/")) {
         return false;
      }
      int space = leading.indexOf(' ');
      if (space < 0) {
         return true;
      }
      // Version, about and help arguments name plugins, also under a namespace (/bukkit:ver, /essentials:about).
      String root = leading.substring(1, space).toLowerCase(Locale.ROOT);
      root = root.substring(root.indexOf(':') + 1);
      return root.equals("ver") || root.equals("version") || root.equals("about") || root.equals("icanhasbukkit")
         || root.equals("help") || root.equals("?");
   }

   private static boolean auditIsRootProbe(AutismServerInfoOverlay.ProbeSpec state) {
      if (state == null) {
         return false;
      }
      if (state.kind == AutismServerInfoOverlay.ProbeKind.ROOT) {
         return true;
      }
      String query = state.query == null ? "" : state.query.trim();
      return query.startsWith("/") && query.length() <= 2;
   }

   private boolean auditIsPlayerListBatch(List<String> tokens, Set<String> knownPlayers, boolean isArgCompletion, boolean rootProbe) {
      if (tokens == null || tokens.isEmpty()) {
         return false;
      }
      String selfLower = auditSelfPlayerNameLower();
      int nonNamespaced = 0;
      int playerMatches = 0;
      int knownPluginMatches = 0;
      boolean selfMatched = false;

      for (String raw : tokens) {
         if (raw == null) {
            continue;
         }
         String clean = raw.trim();
         if (clean.isEmpty() || clean.contains(":")) {
            continue;
         }
         nonNamespaced++;
         String lower = clean.toLowerCase(Locale.ROOT);
         String stripped = lower.startsWith(".") && lower.length() > 2 ? lower.substring(1) : lower;
         if (!selfLower.isEmpty() && (lower.equals(selfLower) || stripped.equals(selfLower))) {
            selfMatched = true;
         }
         if (knownPlayers.contains(lower) || knownPlayers.contains(stripped)) {
            playerMatches++;
         }
         String key = this.pluginKey(clean);
         if (!key.isEmpty() && (PLUGIN_ALIASES.containsKey(key) || this.isProbedNamespace(key))) {
            knownPluginMatches++;
         }
      }

      if (nonNamespaced == 0) {
         return false;
      }
      if (!isArgCompletion) {
         if (playerMatches >= 3 && playerMatches > knownPluginMatches) {
            return true;
         }
         if (selfMatched && playerMatches >= 2 && knownPluginMatches == 0) {
            return true;
         }
         return !rootProbe && nonNamespaced >= 120 && knownPluginMatches * 20 < nonNamespaced;
      }
      if (selfMatched && knownPluginMatches <= playerMatches) {
         return true;
      }
      if (playerMatches >= 2 && playerMatches >= knownPluginMatches) {
         return true;
      }
      if (playerMatches >= 1 && knownPluginMatches == 0 && (knownPlayers.size() <= 2 || playerMatches * 4 >= nonNamespaced)) {
         return true;
      }
      return !rootProbe && nonNamespaced >= 120 && knownPluginMatches * 20 < nonNamespaced;
   }

   private boolean isPlausiblePluginCandidate(String candidate, AutismServerInfoOverlay.ProbeKind kind) {
      if (candidate == null) {
         return false;
      } else {
         String clean = candidate.trim();
         String key = this.pluginKey(clean);
         if (key.isEmpty()) {
            return false;
         } else if (key.length() < 2) {
            return false;
         } else if (clean.contains(" ") || clean.contains("/") || clean.contains(":")) {
            return false;
         } else if (PLATFORM_NAMESPACES.contains(key)) {
            return false;
         }

         Set<String> players = this.knownPlayerNames();
         if (players.contains(key) || players.contains(clean.toLowerCase(Locale.ROOT))) {
            return false;
         } else {
            return (kind == AutismServerInfoOverlay.ProbeKind.PLUGIN_LIST || kind == AutismServerInfoOverlay.ProbeKind.VERSION)
                  && (key.equals("plugins") || key.equals("plugin") || key.equals("version") || key.equals("about"))
               ? false
               : key.chars().allMatch(ch -> Character.isLetterOrDigit(ch) || ch == 95 || ch == 45 || ch == 46);
         }
      }
   }

   private void rememberIfPluginToken(String raw) {
      String token = this.normalizeCommandToken(raw);
      if (this.isPluginCommandToken(token)) {
         this.rememberCommandToken(token);
      }
   }

   private void rememberCommandToken(String token) {
      if (this.observedCommandTokens.size() < 8192 || this.observedCommandTokens.contains(token)) {
         this.observedCommandTokens.add(token);
      }
   }

   private void applyCommandInference(Map<String, AutismServerInfoOverlay.PluginAccumulator> entries) {
      this.inferOwnersFromObservedCommands(entries);
   }

   private void inferOwnersFromObservedCommands(Map<String, AutismServerInfoOverlay.PluginAccumulator> entries) {
      if (!this.observedCommandTokens.isEmpty()) {
         Map<String, Set<String>> inferredMatches = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

         for (String command : this.observedCommandTokens) {
            String plugin = null;
            if (this.isProbedNamespace(command)) {
               plugin = command;
            } else if (PLUGIN_ALIASES.containsKey(command)) {
               plugin = PLUGIN_ALIASES.get(command);
            }

            if (plugin != null && !plugin.isBlank()) {
               inferredMatches.computeIfAbsent(plugin, unused -> new TreeSet<>(String.CASE_INSENSITIVE_ORDER)).add(command);
            }
         }

         for (Entry<String, Set<String>> entry : inferredMatches.entrySet()) {
            this.mergePluginEntry(entries, entry.getKey(), entry.getValue(), true, AutismServerInfoOverlay.EvidenceSource.ROOT_HINT);
         }
      }
   }

   private boolean attachLooseCommand(Map<String, AutismServerInfoOverlay.PluginAccumulator> entries, String rawCommand) {
      String token = this.normalizeCommandToken(rawCommand);
      if (!this.isLooseCommandCandidate(token)) {
         return false;
      }

      if (token.contains(":")) {
         return false;
      }

      String ownerKey = this.findOwnerKey(entries, token);
      if (ownerKey != null) {
         AutismServerInfoOverlay.PluginAccumulator owner = entries.get(ownerKey);
         if (owner != null && !owner.commands.contains(token)) {
            owner.commands.add(token);
            if (this.isCopyCommandEvidence(owner.copyEvidence)) {
               owner.copyCommands.add(token);
            }

            return true;
         } else {
            return false;
         }
      } else {
         return this.mergePluginEntry(
            entries,
            "command_" + token,
            "/" + token,
            List.of(token),
            List.of(),
            List.of(),
            true,
            AutismServerInfoOverlay.EvidenceSource.SCANNER_AUTOCOMPLETE,
            AutismServerInfoOverlay.MatchConfidence.FEATURE,
            AutismServerInfoOverlay.DetectionType.PLUGIN
         );
      }
   }

   private boolean isLooseCommandCandidate(String token) {
      if (!this.isPluginCommandToken(token)) {
         return false;
      } else {
         String clean = token.trim().toLowerCase(Locale.ROOT);
         if (clean.length() < 3) {
            return false;
         } else if (SERVER_META_COMMANDS.contains(clean)) {
            return false;
         } else if (!PLATFORM_NAMESPACES.contains(clean) && !VANILLA_COMMANDS.contains(clean)) {
            return FEATURE_KEYWORDS.containsKey(clean) ? false : !PLUGIN_ALIASES.containsKey(clean) && !this.isProbedNamespace(clean);
         } else {
            return false;
         }
      }
   }

   private boolean applyCatalogFingerprints(Map<String, AutismServerInfoOverlay.PluginAccumulator> entries) {
      String address = this.normalizedServerAddress();
      if (address != null && !address.isBlank()) {
         boolean changed = false;

         for (PluginSignatureCatalog.PluginFingerprint fingerprint : PluginSignatureCatalog.fingerprints(address, this.serverBrand())) {
            if (fingerprint != null && fingerprint.plugin() != null && !fingerprint.plugin().isBlank()) {
               changed |= this.applyFingerprintFeatures(entries, fingerprint);
               AutismServerInfoOverlay.MatchConfidence confidence = this.confidenceForFingerprint(fingerprint);
               changed |= this.mergePluginEntry(
                  entries,
                  fingerprint.key(),
                  fingerprint.plugin(),
                  List.of(),
                  fingerprint.channels(),
                  List.of(),
                  false,
                  AutismServerInfoOverlay.EvidenceSource.PAYLOAD_CHANNEL,
                  confidence
               );
            }
         }

         return changed;
      } else {
         return false;
      }
   }

   private AutismServerInfoOverlay.MatchConfidence confidenceForFingerprint(PluginSignatureCatalog.PluginFingerprint fingerprint) {
      String basis = fingerprint == null ? "" : fingerprint.bestSource();
      return "embedded".equalsIgnoreCase(basis) ? AutismServerInfoOverlay.MatchConfidence.STRONG : AutismServerInfoOverlay.MatchConfidence.EXACT;
   }

   private boolean applyFingerprintFeatures(Map<String, AutismServerInfoOverlay.PluginAccumulator> entries, PluginSignatureCatalog.PluginFingerprint fingerprint) {
      if (entries != null && !entries.isEmpty() && fingerprint != null) {
         Set<String> keywords = this.fingerprintKeywords(fingerprint);
         if (keywords.isEmpty()) {
            return false;
         }

         boolean changed = false;
         List<String> removeKeys = new ArrayList<>();

         for (Entry<String, AutismServerInfoOverlay.PluginAccumulator> mapEntry : new ArrayList<>(entries.entrySet())) {
            String key = mapEntry.getKey();
            AutismServerInfoOverlay.PluginAccumulator entry = mapEntry.getValue();
            if (key != null && key.startsWith("feature") && entry != null && this.entryMatchesKeywords(entry, keywords)) {
               changed |= this.mergePluginEntry(
                  entries,
                  fingerprint.key(),
                  fingerprint.plugin(),
                  entry.commands,
                  fingerprint.channels(),
                  entry.guis,
                  entry.commandBacked,
                  AutismServerInfoOverlay.EvidenceSource.PAYLOAD_CHANNEL,
                  this.confidenceForFingerprint(fingerprint)
               );
               removeKeys.add(key);
            }
         }

         for (String key : removeKeys) {
            entries.remove(key);
            this.pluginEvidence.remove(key);
            this.pluginConfidence.remove(key);
            this.pluginDetectionTypes.remove(key);
            changed = true;
         }

         return changed;
      } else {
         return false;
      }
   }

   private Set<String> fingerprintKeywords(PluginSignatureCatalog.PluginFingerprint fingerprint) {
      LinkedHashSet<String> keywords = new LinkedHashSet<>();
      this.addFeatureKeywords(keywords, fingerprint.plugin());
      this.addFeatureKeywords(keywords, fingerprint.key());
      if (fingerprint.channels() != null) {
         for (String channel : fingerprint.channels()) {
            this.addFeatureKeywords(keywords, channel);
         }
      }

      keywords.removeIf(keyword -> keyword.length() < 3);
      return keywords;
   }

   private void addFeatureKeywords(Set<String> out, String text) {
      if (out != null && text != null && !text.isBlank()) {
         String lower = text.toLowerCase(Locale.ROOT);

         for (Entry<String, String> entry : FEATURE_KEYWORDS.entrySet()) {
            String key = entry.getKey().toLowerCase(Locale.ROOT);
            String label = entry.getValue().toLowerCase(Locale.ROOT);
            if (lower.contains(key) || lower.contains(label.replace(" ", "")) || lower.contains(label)) {
               out.add(key);
               out.add(label);
            }
         }
      }
   }

   private boolean entryMatchesKeywords(AutismServerInfoOverlay.PluginAccumulator entry, Set<String> keywords) {
      if (entry != null && keywords != null && !keywords.isEmpty()) {
         List<String> evidenceText = new ArrayList<>();
         evidenceText.add(entry.name);
         evidenceText.addAll(entry.commands);
         evidenceText.addAll(entry.guis);

         for (String text : evidenceText) {
            if (text != null) {
               String lower = text.toLowerCase(Locale.ROOT);

               for (String keyword : keywords) {
                  if (keyword != null && !keyword.isBlank() && lower.contains(keyword.toLowerCase(Locale.ROOT))) {
                     return true;
                  }
               }
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private void refreshLiveScanResults() {
      if (this.tick_Z) {
         if (this.applyCatalogFingerprints(this.poll_Map)) {
            this.normalizeEntries(this.poll_Map);
            AutismServerInfoOverlay.ActiveScanSession session = this.activeScan;
            if (session != null) {
               session.lastResponseAt = System.currentTimeMillis();
            }
         }
      } else if (this.scanDone) {
         String address = this.normalizedServerAddress();
         long revision = PluginSignatureCatalog.revision(address, this.serverBrand());
         if (revision != this.catalogRevision) {
            this.catalogRevision = revision;
            if (revision > 0L) {
               Map<String, AutismServerInfoOverlay.PluginAccumulator> entries = this.snapshotPluginEntries();
               if (this.applyCatalogFingerprints(entries)) {
                  this.applyPluginEntries(entries, false);
                  if (this.scanDone) {
                     this.saveScanToConfig();
                  }
               }
            }
         }
      }
   }

   private int addProbes(
      Map<Integer, AutismServerInfoOverlay.ProbeSpec> probes,
      Set<String> seen,
      int nextId,
      AutismServerInfoOverlay.ProbeKind kind,
      String... baseCommands
   ) {
      for (String base : baseCommands) {
         if (base != null && !base.isBlank()) {
            String trimmed = base.trim();
            nextId = this.addProbe(probes, seen, nextId, trimmed, kind, null);
            nextId = this.addProbe(probes, seen, nextId, trimmed + " ", kind, null);
         }
      }

      return nextId;
   }

   private Map<Integer, AutismServerInfoOverlay.ProbeSpec> buildProbePlan() {
      Map<Integer, AutismServerInfoOverlay.ProbeSpec> probes = new LinkedHashMap<>();
      Set<String> seen = new HashSet<>();
      int nextId = this.allocateProbeId();
      nextId = this.addProbe(probes, seen, nextId, "/", AutismServerInfoOverlay.ProbeKind.ROOT, null);
      nextId = this.addProbe(probes, seen, nextId, "/ ", AutismServerInfoOverlay.ProbeKind.ROOT, null);
      nextId = this.addProbes(probes, seen, nextId, AutismServerInfoOverlay.ProbeKind.PLUGIN_LIST, "/plugins", "/pl", "/bukkit:plugins", "/bukkit:pl");
      nextId = this.addProbes(
         probes,
         seen,
         nextId,
         AutismServerInfoOverlay.ProbeKind.VERSION,
         "/ver",
         "/version",
         "/about",
         "/icanhasbukkit",
         "/bukkit:ver",
         "/bukkit:version"
      );
      nextId = this.addProbes(probes, seen, nextId, AutismServerInfoOverlay.ProbeKind.HELP, "/help", "/?", "/bukkit:help", "/minecraft:help");

      for (int i = 0; i < "abcdefghijklmnopqrstuvwxyz0123456789".length(); i++) {
         char prefix = "abcdefghijklmnopqrstuvwxyz0123456789".charAt(i);
         String rootProbe = "/" + prefix;
         nextId = this.addProbe(probes, seen, nextId, rootProbe, AutismServerInfoOverlay.ProbeKind.ROOT, String.valueOf(prefix));
         nextId = this.addProbe(probes, seen, nextId, "/help " + prefix, AutismServerInfoOverlay.ProbeKind.HELP, String.valueOf(prefix));
         nextId = this.addProbe(probes, seen, nextId, "/? " + prefix, AutismServerInfoOverlay.ProbeKind.HELP, String.valueOf(prefix));
      }

      for (String namespace : PROBED_NAMESPACES) {
         nextId = this.addProbe(probes, seen, nextId, "/" + namespace + ":", AutismServerInfoOverlay.ProbeKind.NAMESPACE, namespace);
      }

      for (String plugin : VERSION_PROBE_PLUGINS) {
         nextId = this.addProbe(probes, seen, nextId, "/version " + plugin, AutismServerInfoOverlay.ProbeKind.VERSION, plugin);
         nextId = this.addProbe(probes, seen, nextId, "/ver " + plugin, AutismServerInfoOverlay.ProbeKind.VERSION, plugin);
         nextId = this.addProbe(probes, seen, nextId, "/help " + plugin, AutismServerInfoOverlay.ProbeKind.HELP, plugin);
      }

      return probes;
   }

   private int allocateProbeId() {
      return this.scanIdAllocator.nextBase();
   }

   public boolean hasServer() {
      return MC != null && MC.getConnection() != null && !this.normalizedServerAddress().isBlank();
   }

   public boolean hasCommandTree() {
      try {
         if (MC != null && MC.getConnection() != null) {
            CommandDispatcher<?> dispatcher = MC.getConnection().getCommands();
            return dispatcher != null && dispatcher.getRoot() != null && !dispatcher.getRoot().getChildren().isEmpty();
         } else {
            return false;
         }
      } catch (Exception ignored) {
         return false;
      }
   }

   public boolean startAutoScan() {
      if (!this.hasServer()) {
         return false;
      } else {
         String address = this.normalizedServerAddress();
         String context = this.computeContextSignature();
         this.syncServerContext();
         if (this.tick_Z) {
            return true;
         } else if (context.equals(this.requestedScanContext) && this.scanDone && this.scanStatus == AutismServerInfoOverlay.ScanStatus.COMPLETE) {
            return true;
         } else {
            long cachedAt = AutismConfig.getGlobal().getPluginScanTimestampByAddress(address);
            boolean fresh = cachedAt > 0L && System.currentTimeMillis() - cachedAt < 5184000000L;
            if (fresh && this.scanDone && this.scanStatus == AutismServerInfoOverlay.ScanStatus.COMPLETE) {
               this.requestedScanContext = context;
               return true;
            } else {
               this.requestedScanContext = context;
               AutismConfig.getGlobal().removePluginScan(address);
               this.restartScan();
               this.startDefaultScan();
               return this.tick_Z;
            }
         }
      }
   }

   public void clearRequestedScanContext() {
      this.requestedScanContext = null;
   }

   public void resetForDisconnect() {
      this.saveScanToConfig();
      this.requestedScanContext = null;
      this.resetForServer(null, "");
      this.signatureCacheAddress = "";
      this.signatureCacheBrand = "";
      this.cachedContextSignature = "";
      this.lastSeenAddress = "";
      this.requestLayoutRebuild();
   }

   private void cancelScan(String reason) {
      AutismServerInfoOverlay.ActiveScanSession session = this.activeScan;
      if (session != null) {
         session.attemptTracker.finish();
         this.trace(session, "cancel reason=" + reason);
      }

      this.activeScan = null;
      this.tick_Z = false;
      this.write_List.clear();
      this.pendingCommandEvidence.clear();
      this.observationsDirty = false;
   }

   private void recordScanFailure(AutismServerInfoOverlay.ActiveScanSession session, String stage, Throwable error) {
      if (session != null && !session.attemptTracker.isFinished()) {
         session.processingFailures++;
         String detail = error == null ? "unknown" : error.getClass().getSimpleName() + ": " + error.getMessage();
         this.scanIssue = stage + " failed";
         this.trace(session, "error stage=" + stage + " detail=" + detail);
         LOGGER.warn("Plugin scan {} failed during {}", session.generation, stage, error);
      }
   }

   private void completeScan() {
      this.finishScan(null, "");
   }

   private void finishScan(AutismServerInfoOverlay.ScanStatus requestedOutcome, String issue) {
      AutismServerInfoOverlay.ActiveScanSession session = this.activeScan;
      if (session != null && session.attemptTracker.finish()) {
         this.flushObservations(true);
         Map<String, AutismServerInfoOverlay.PluginAccumulator> finalEntries = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
         boolean processingFailed = false;

         try {
            this.mergeAccumulators(finalEntries, this.legacyPluginEntries);
            this.mergeAccumulators(finalEntries, this.poll_Map);
            this.applyCatalogFingerprints(finalEntries);
            this.normalizeEntries(finalEntries);
         } catch (RuntimeException error) {
            processingFailed = true;
            session.processingFailures++;
            this.scanIssue = "Final processing failed";
            LOGGER.warn("Plugin scan {} failed during final processing", session.generation, error);
            this.trace(session, "error stage=final-processing detail=" + error.getClass().getSimpleName());
         }

         AutismServerInfoOverlay.ScanStatus outcome = requestedOutcome;
         if (outcome == null) {
            outcome = !processingFailed && !session.attemptTracker.hadFailures(session.processingFailures, session.failedCount, session.pendingProbeIds.size())
               ? AutismServerInfoOverlay.ScanStatus.COMPLETE
               : AutismServerInfoOverlay.ScanStatus.PARTIAL;
         }

         if (processingFailed && finalEntries.isEmpty()) {
            outcome = AutismServerInfoOverlay.ScanStatus.FAILED;
         }

         boolean infrastructureFailure = processingFailed || session.processingFailures > 0 || session.failedCount > 0 || !session.pendingProbeIds.isEmpty();
         boolean restorePrevious = finalEntries.isEmpty() && infrastructureFailure && !session.previousEntries.isEmpty();
         if (restorePrevious) {
            outcome = AutismServerInfoOverlay.ScanStatus.FAILED;
         }

         this.trace(
            session,
            "final roots="
               + session.rootCount
               + " suggestionReplies="
               + session.suggestionReplies
               + " suggestionEntries="
               + session.suggestionEntries
               + " sent="
               + session.sentCount
               + " answered="
               + session.replyCount
               + " retried="
               + session.retryCount
               + " failed="
               + session.failedCount
               + " legacy="
               + this.countNamedEntries(this.legacyPluginEntries)
               + " working="
               + this.countNamedEntries(this.poll_Map)
               + " final="
               + this.countNamedEntries(finalEntries)
               + " outcome="
               + outcome
         );

         try {
            if (restorePrevious) {
               this.applyPluginEntries(session.previousEntries, false);
            } else if (outcome != AutismServerInfoOverlay.ScanStatus.FAILED || !finalEntries.isEmpty()) {
               this.applyPluginEntries(finalEntries, true);
            }
         } catch (RuntimeException error) {
            outcome = AutismServerInfoOverlay.ScanStatus.FAILED;
            this.scanIssue = "Could not publish results";
            LOGGER.warn("Plugin scan {} could not publish results", session.generation, error);
            if (!session.previousEntries.isEmpty()) {
               try {
                  this.applyPluginEntries(session.previousEntries, false);
               } catch (RuntimeException restoreError) {
                  LOGGER.warn("Plugin scan {} could not restore previous results", session.generation, restoreError);
               }
            }
         }

         session.contextSignature = this.computeContextSignature();
         this.scanAddress = session.serverAddress;
         this.contextSignature = session.contextSignature;
         this.scanTotalProbes = session.totalProbes;
         this.scanAnsweredProbes = session.replyCount;
         this.scanRetriedProbes = session.retryCount;
         this.scanFailedProbes = session.failedCount;
         this.activeScan = null;
         this.tick_Z = false;
         this.scanDone = true;
         this.scanStatus = outcome;
         if (issue != null && !issue.isBlank()) {
            this.scanIssue = issue;
         } else if (outcome == AutismServerInfoOverlay.ScanStatus.COMPLETE) {
            this.scanIssue = "";
         } else if (this.scanIssue.isBlank()) {
            this.scanIssue = outcome == AutismServerInfoOverlay.ScanStatus.PARTIAL ? "Some probes did not reply" : "Scan failed";
         }

         this.observedCommandTokens.clear();
         this.poll_Map.clear();
         this.legacyPluginEntries.clear();
         this.resizeForPluginResults();
         if (outcome != AutismServerInfoOverlay.ScanStatus.FAILED || !this.detectedPlugins.isEmpty()) {
            this.saveScanToConfig();
         }

         this.announceCompletion();
         this.activeScanDelayMs = this.scanDelayMs;
      }
   }

   private void pumpProbes() {
      if (autismclient.modules.aD.read_Z()) {
         this.hardLock();
      } else {
         AutismServerInfoOverlay.ActiveScanSession session = this.activeScan;
         if (this.tick_Z && session != null && !session.attemptTracker.isFinished() && MC.getConnection() != null) {
            long now = System.currentTimeMillis();
            if (now >= session.nextProbeAt) {
               AutismServerInfoOverlay.QueuedProbe request = session.probeQueue.pollFirst();
               if (request != null) {
                  try {
                     ServerboundCommandSuggestionPacket packet = new ServerboundCommandSuggestionPacket(
                        request.id, request.spec.query
                     );
                     MC.getConnection().send(packet);
                     session.sentCount++;
                     session.nextProbeAt = now + this.activeScanDelayMs;
                  } catch (Exception error) {
                     session.nextProbeAt = now + this.activeScanDelayMs;
                     if (session.attemptTracker.canRetry(request.attempt)) {
                        session.retryCount++;
                        this.trace(session, "retry probe=" + request.id + " reason=" + error.getClass().getSimpleName());
                        LOGGER.warn("Plugin scan {} will retry probe {}", session.generation, request.id, error);
                        session.probeQueue
                           .addLast(new AutismServerInfoOverlay.QueuedProbe(request.id, request.spec, request.attempt + 1));
                     } else {
                        session.failedCount++;
                        session.pendingProbeIds.remove(request.id);
                        session.probesById.remove(request.id);
                        this.recordScanFailure(session, "probe send", error);
                     }
                  }
               }
            }
         }
      }
   }

   private int totalProbeCount() {
      AutismServerInfoOverlay.ActiveScanSession session = this.activeScan;
      return session == null ? 0 : Math.max(0, session.totalProbes);
   }

   private long expectedScanDurationMs() {
      AutismServerInfoOverlay.ActiveScanSession session = this.activeScan;
      int retryCount = session == null ? 0 : session.retryCount;
      return Math.max(this.activeScanDelayMs, (long)(this.totalProbeCount() + retryCount) * this.activeScanDelayMs);
   }

   private long finishedSendingAt() {
      AutismServerInfoOverlay.ActiveScanSession session = this.activeScan;
      if (session == null || session.startedAt <= 0L) {
         return 0L;
      }

      if (!session.probeQueue.isEmpty()) {
         return 0L;
      }

      long candidate = session.nextProbeAt - this.activeScanDelayMs;
      return Math.max(session.startedAt, candidate);
   }

   private long scanTimeoutMs() {
      return Math.max(12000L, this.expectedScanDurationMs() + 1400L + 700L);
   }

   private int lerpColor(int from, int to, float t) {
      float clamped = Math.max(0.0F, Math.min(1.0F, t));
      int a1 = from >>> 24 & 0xFF;
      int r1 = from >>> 16 & 0xFF;
      int g1 = from >>> 8 & 0xFF;
      int b1 = from & 0xFF;
      int a2 = to >>> 24 & 0xFF;
      int r2 = to >>> 16 & 0xFF;
      int g2 = to >>> 8 & 0xFF;
      int b2 = to & 0xFF;
      int a = Math.round(a1 + (a2 - a1) * clamped);
      int r = Math.round(r1 + (r2 - r1) * clamped);
      int g = Math.round(g1 + (g2 - g1) * clamped);
      int b = Math.round(b1 + (b2 - b1) * clamped);
      return a << 24 | r << 16 | g << 8 | b;
   }

   private int sentProbeCount() {
      AutismServerInfoOverlay.ActiveScanSession session = this.activeScan;
      int total = this.totalProbeCount();
      return session == null ? 0 : Math.max(0, Math.min(total, session.sentCount));
   }

   private int replyProbeCount() {
      AutismServerInfoOverlay.ActiveScanSession session = this.activeScan;
      int total = this.totalProbeCount();
      return session == null ? 0 : Math.max(0, Math.min(total, session.replyCount));
   }

   private float scanProgress() {
      if (this.scanDone) {
         return 1.0F;
      }

      int total = this.totalProbeCount();
      if (total <= 0) {
         return 0.0F;
      }

      float sentRatio = (float)this.sentProbeCount() / total;
      float answeredRatio = (float)this.replyProbeCount() / total;
      AutismServerInfoOverlay.ActiveScanSession session = this.activeScan;
      if (session != null && !session.probeQueue.isEmpty()) {
         float progress = 0.8F * sentRatio;
         progress += 0.05F * answeredRatio;
         return Math.max(0.0F, Math.min(0.85F, progress));
      }

      float progress = 0.85F;
      long finishedSendingAt = this.finishedSendingAt();
      long waitWindow = 1400L;
      if (finishedSendingAt > 0L && waitWindow > 0L) {
         long now = System.currentTimeMillis();
         float settle = Math.max(0.0F, Math.min(1.0F, (float)(now - finishedSendingAt) / (float)waitWindow));
         progress += 0.14F * settle;
      }

      return Math.max(0.0F, Math.min(0.99F, progress));
   }

   private int liveDetectedCount() {
      return (int)this.poll_Map.values().stream().filter(entry -> entry != null && entry.name != null && !entry.name.isBlank()).count();
   }

   private int foundCount() {
      return this.tick_Z ? this.liveDetectedCount() : this.detectedPlugins.size();
   }

   private String scanStatusText() {
      int foundCount = this.foundCount();
      return foundCount > 0 ? "Scanning plugins | found " + foundCount : "Scanning plugins";
   }

   private String scanPhaseText() {
      AutismServerInfoOverlay.ActiveScanSession session = this.activeScan;
      return session != null && !session.probeQueue.isEmpty() ? "Sending probes" : "Analyzing replies";
   }

   private int scanPhaseColor() {
      AutismServerInfoOverlay.ActiveScanSession session = this.activeScan;
      return session != null && !session.probeQueue.isEmpty() ? UiColorPalette.warningYellow() : UiColorPalette.cyan();
   }

   private String scanDetailText() {
      AutismServerInfoOverlay.ActiveScanSession session = this.activeScan;
      int total = Math.max(1, this.totalProbeCount());
      return session != null && !session.probeQueue.isEmpty() ? this.sentProbeCount() + "/" + total : this.replyProbeCount() + "/" + total;
   }

   private void announceCompletion() {
      if (!this.completionAnnounced) {
         this.completionAnnounced = true;
         int count = this.detectedPlugins.size();
         if (count <= 0) {
            AutismClientMessaging.sendPrefixed("Plugin probing finished: no plugins found.");
         } else if (count == 1) {
            AutismClientMessaging.sendPrefixed("Plugin probing finished: found 1 plugin.");
         } else {
            AutismClientMessaging.sendPrefixed("Plugin probing finished: found " + count + " plugins.");
         }
      }
   }

   public boolean isScanRunning() {
      return this.tick_Z;
   }

   public boolean hasPendingWork() {
      return this.overlayVisible || this.tick_Z || this.scanDone || !this.detectedPlugins.isEmpty();
   }

   public void renderProbeBanner(GuiGraphicsExtractor ctx) {
      if (this.isScanRunning() && MC != null && MC.getWindow() != null && this.textRenderer != null) {
         autismclient.gui.ui.layout.LayoutViewport viewport = this.layoutHost.createViewport();
         String status = this.scanStatusText();
         String phase = this.scanPhaseText();
         String detail = this.scanDetailText();
         int progressPercent = Math.max(0, Math.min(100, Math.round(this.scanProgress() * 100.0F)));
         String percentComponent = progressPercent + "%";
         int screenW = Math.round(viewport.width());
         int statusWidth = autismclient.gui.f.b.k.init(
            this.textRenderer,
            status,
            this.theme.init_Identifier_l(autismclient.gui.f.b.l.write),
            this.theme.load(autismclient.gui.f.b.l.write)
         );
         int phaseWidth = autismclient.gui.f.b.k.init(
            this.textRenderer,
            phase,
            this.theme.init_Identifier_l(autismclient.gui.f.b.l.read),
            this.theme.load(autismclient.gui.f.b.l.read)
         );
         int detailWidth = autismclient.gui.f.b.k.init(
            this.textRenderer, detail, this.theme.init_Identifier_l(autismclient.gui.f.b.l.write), this.scanPhaseColor()
         );
         int percentWidth = autismclient.gui.f.b.k.init(
            this.textRenderer,
            percentComponent,
            this.theme.init_Identifier_l(autismclient.gui.f.b.l.read),
            this.theme.load(autismclient.gui.f.b.l.read)
         );
         int contentW = Math.max(180, Math.max(statusWidth, Math.max(phaseWidth + 12 + detailWidth, percentWidth + 150)));
         int boxW = Math.max(1, Math.min(Math.max(1, screenW - 16), Math.max(236, contentW + 24)));
         int boxH = 42;
         int boxX = Math.max(8, (screenW - boxW) / 2);
         int boxY = 8;
         int innerX = boxX + 8;
         int barY = boxY + 17;
         int barW = boxW - 16;
         int barH = 10;
         viewport.pushTransform(ctx);

         try {
            autismclient.gui.ui.layout.LayoutContext bannerContext = new autismclient.gui.ui.layout.LayoutContext(
               ctx, this.textRenderer, viewport, this.theme, 0.0F, 0.0F, 0.0F
            );
            int border = this.theme.get();
            int headerAccent = this.theme.run();
            autismclient.gui.f.PacketUtil.init(
               ctx,
               autismclient.gui.f.NetHelper.init(boxX, boxY, boxW, boxH),
               14,
               bannerContext.applyAlpha(this.theme.poll()),
               bannerContext.applyAlpha(this.theme.pop()),
               bannerContext.applyAlpha(this.theme.poll()),
               bannerContext.applyAlpha(border),
               bannerContext.applyAlpha(headerAccent),
               1.0F
            );
            autismclient.gui.f.b.k.init(
               ctx,
               this.textRenderer,
               status,
               this.theme.init_Identifier_l(autismclient.gui.f.b.l.write),
               this.theme.load(autismclient.gui.f.b.l.write),
               innerX,
               boxY + 3,
               false
            );
            autismclient.gui.f.b.k.init(
               ctx,
               this.textRenderer,
               percentComponent,
               this.theme.init_Identifier_l(autismclient.gui.f.b.l.read),
               this.scanPhaseColor(),
               boxX + boxW - 8 - percentWidth,
               boxY + 3,
               false
            );
            autismclient.gui.ui.widgets.ProgressBarRenderer.draw(
               autismclient.gui.f.UiHelper.init(ctx, this.textRenderer, -1, -1), autismclient.gui.f.NetHelper.init(innerX, barY, barW, barH), this.scanProgress()
            );
            autismclient.gui.f.b.k.init(
               ctx,
               this.textRenderer,
               phase,
               this.theme.init_Identifier_l(autismclient.gui.f.b.l.read),
               this.theme.load(autismclient.gui.f.b.l.read),
               innerX,
               barY + 13,
               false
            );
            autismclient.gui.f.b.k.init(
               ctx,
               this.textRenderer,
               detail,
               this.theme.init_Identifier_l(autismclient.gui.f.b.l.write),
               this.scanPhaseColor(),
               boxX + boxW - 8 - detailWidth,
               barY + 13,
               false
            );
         } finally {
            viewport.popTransform(ctx);
         }
      }
   }

   private void tickActiveScanSession() {
      AutismServerInfoOverlay.ActiveScanSession session = this.activeScan;
      if (this.tick_Z && session != null && !session.attemptTracker.isFinished()) {
         try {
            this.pumpProbes();
            long now = System.currentTimeMillis();
            boolean allProbesSent = session.probeQueue.isEmpty();
            boolean allResponsesReceived = session.pendingProbeIds.isEmpty();
            long finishedSendingAt = this.finishedSendingAt();
            long settleWindow = allResponsesReceived ? 1400L : Math.max(1400L, 450L);
            boolean settledAfterSend = allProbesSent && finishedSendingAt > 0L && now - finishedSendingAt >= settleWindow;
            boolean settledAfterLastResponse = session.lastResponseAt > 0L && now - session.lastResponseAt >= 700L;
            boolean timedOut = now - session.startedAt >= this.scanTimeoutMs();
            if (allProbesSent && settledAfterSend && (!allResponsesReceived || settledAfterLastResponse) || timedOut) {
               AutismServerInfoOverlay.ScanStatus outcome = !session.attemptTracker.hadFailures(session.processingFailures, session.failedCount, session.pendingProbeIds.size())
                  ? AutismServerInfoOverlay.ScanStatus.COMPLETE
                  : AutismServerInfoOverlay.ScanStatus.PARTIAL;
               this.finishScan(outcome, timedOut && !allResponsesReceived ? "Some probes timed out" : "");
            }
         } catch (RuntimeException error) {
            this.recordScanFailure(session, "lifecycle", error);
            this.finishScan(AutismServerInfoOverlay.ScanStatus.PARTIAL, "Scan recovered from an error");
         }
      }
   }

   public void tickScan() {
      try {
         this.flushObservations(false);
         this.syncServerContext();
         this.refreshLiveScanResults();
         if (this.tick_Z) {
            this.tickActiveScanSession();
         }
      } catch (Throwable error) {
         AutismServerInfoOverlay.ActiveScanSession session = this.activeScan;
         if (session != null) {
            this.recordScanFailure(session, "background tick", error);
            this.finishScan(AutismServerInfoOverlay.ScanStatus.PARTIAL, "Scan recovered from an error");
         } else {
            LOGGER.warn("Plugin scanner background update failed", error);
         }
      }
   }

   private void ingestCommandTree(RootCommandNode<?> root) {
      Map<String, AutismServerInfoOverlay.PluginAccumulator> entries = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
      entries.putAll(this.poll_Map);
      Map<String, AutismServerInfoOverlay.PluginAccumulator> legacyEntries = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
      legacyEntries.putAll(this.legacyPluginEntries);
      this.auditActiveKnownPlayers = this.knownPlayerNames();

      try {
         for (CommandNode<?> child : root.getChildren()) {
            String name = child.getName();
            AutismServerInfoOverlay.ActiveScanSession session = this.activeScan;
            if (session != null) {
               session.rootCount++;
            }

            if (name.contains(":")) {
               String[] parts = name.split(":", 2);
               String ns = parts[0].toLowerCase();
               String cmd = parts[1];
               if (!PLATFORM_NAMESPACES.contains(ns)) {
                  this.mergePluginEntry(legacyEntries, ns, List.of(cmd), true, AutismServerInfoOverlay.EvidenceSource.COMMAND_TREE);
                  this.attributeCommand(entries, ns + ":" + cmd, AutismServerInfoOverlay.EvidenceSource.COMMAND_TREE);
                  this.rememberIfPluginToken(cmd);
               }
            } else {
               this.rememberIfPluginToken(name);
               String token = this.normalizeCommandToken(name);
               if (FEATURE_KEYWORDS.containsKey(token)) {
                  this.attributeCommand(entries, name, AutismServerInfoOverlay.EvidenceSource.COMMAND_TREE);
               }
            }
         }

         this.inferOwnersFromObservedCommands(legacyEntries);
         this.applyCommandInference(entries);
         this.mergeAccumulators(entries, legacyEntries);
         this.normalizeEntries(entries);
      } finally {
         this.auditActiveKnownPlayers = null;
      }

      this.legacyPluginEntries.clear();
      this.legacyPluginEntries.putAll(legacyEntries);
      this.poll_Map.clear();
      this.poll_Map.putAll(entries);
      AutismServerInfoOverlay.ActiveScanSession session = this.activeScan;
      this.trace(
         session,
         "command tree roots="
            + (session == null ? 0 : session.rootCount)
            + " legacy="
            + this.countNamedEntries(this.legacyPluginEntries)
            + " working="
            + this.countNamedEntries(this.poll_Map)
      );
   }

   public void clearSearch() {
      this.resetForServer(null, "");
      this.searchInput.setValue("");
      this.searchInput.setFocused(false);
      this.dnsResult = null;
      this.dnsHost = null;
      this.invalidateContextSignature();
   }

   public void hardLock() {
      this.cancelScan("hard-lock");
      this.poll_Map.clear();
      this.legacyPluginEntries.clear();
      this.userProbesById.clear();
      if (!this.detectedPlugins.isEmpty()) {
         this.scanDone = true;
      }

      this.layoutDirty = true;
   }

   private void restartScan() {
      this.cancelScan("restart");
      this.scanDone = false;
      this.setupRequested = false;
      this.completionAnnounced = false;
      this.scanStatus = AutismServerInfoOverlay.ScanStatus.NONE;
      this.scanIssue = "";
      this.observedCommandTokens.clear();
      this.poll_Map.clear();
      this.legacyPluginEntries.clear();
      this.selectedPlugin = null;
      this.searchInput.setValue("");
      this.searchInput.setFocused(false);
      this.scroll.jumpTo(0, 0);
   }

   private void rescan() {
      if (!this.configurationOnly && MC.getConnection() != null) {
         String address = this.normalizedServerAddress();
         AutismConfig.getGlobal().removePluginScan(address);
         this.restartScan();
         this.startScan(true);
         this.saveState();
      }
   }

   private int addProbe(
      Map<Integer, AutismServerInfoOverlay.ProbeSpec> probes,
      Set<String> seen,
      int nextId,
      String query,
      AutismServerInfoOverlay.ProbeKind kind,
      String hint
   ) {
      if (query != null && !query.isBlank()) {
         String key = kind.name() + "|" + query + "|" + (hint == null ? "" : hint);
         if (!seen.add(key)) {
            return nextId;
         }

         probes.put(nextId++, new AutismServerInfoOverlay.ProbeSpec(query, kind, hint));
         return nextId;
      } else {
         return nextId;
      }
   }

   private void startDefaultScan() {
      this.startScan(false);
   }

   private void startScan(boolean forceLive) {
      if (!this.configurationOnly && !autismclient.modules.aD.read_Z()) {
         if (!forceLive) {
            this.syncServerContext();
         }

         if ((forceLive || !this.scanDone) && !this.tick_Z) {
            if (MC.getConnection() == null) {
               this.scanDone = true;
               this.scanStatus = AutismServerInfoOverlay.ScanStatus.FAILED;
               this.scanIssue = "Not connected";
            } else {
               this.activeScanDelayMs = this.scanDelayMs;
               this.scanAddress = this.normalizedServerAddress();
               this.contextSignature = this.computeContextSignature();
               long startedAt = System.currentTimeMillis();
               AutismServerInfoOverlay.ActiveScanSession session = new AutismServerInfoOverlay.ActiveScanSession(
                  this.nextScanGeneration++, this.scanAddress, this.contextSignature, startedAt, this.snapshotPluginEntries()
               );
               session.nextProbeAt = startedAt + this.activeScanDelayMs;
               this.activeScan = session;
               this.activeTab = 1;
               this.tick_Z = true;
               this.scanDone = false;
               this.observedCommandTokens.clear();
               this.poll_Map.clear();
               this.legacyPluginEntries.clear();
               this.scanStatus = AutismServerInfoOverlay.ScanStatus.NONE;
               this.scanIssue = "";
               this.resizeForScanningPlugins();

               try {
                  CommandDispatcher<?> dispatcher = MC.getConnection().getCommands();
                  if (dispatcher != null) {
                     RootCommandNode<?> root = dispatcher.getRoot();
                     if (root != null && !root.getChildren().isEmpty()) {
                        this.ingestCommandTree(root);
                     }
                  }

                  this.applyCatalogFingerprints(this.poll_Map);
                  Map<Integer, AutismServerInfoOverlay.ProbeSpec> probes = this.buildProbePlan();
                  session.totalProbes = probes.size();

                  for (Entry<Integer, AutismServerInfoOverlay.ProbeSpec> probe : probes.entrySet()) {
                     session.pendingProbeIds.add(probe.getKey());
                     session.probesById.put(probe.getKey(), probe.getValue());
                     session.probeQueue.addLast(new AutismServerInfoOverlay.QueuedProbe(probe.getKey(), probe.getValue()));
                  }

                  this.trace(session, "start probes=" + session.totalProbes + " delay=" + this.activeScanDelayMs + "ms");
                  this.tickActiveScanSession();
               } catch (Exception error) {
                  this.recordScanFailure(session, "startup", error);
                  this.finishScan(AutismServerInfoOverlay.ScanStatus.PARTIAL, "Scan recovered from a startup error");
               }
            }
         }
      }
   }

   public void onOutgoingPacket(Packet<?> packet) {
      if (packet != null && !autismclient.modules.aD.init_Z() && "ServerboundCommandSuggestionPacket".equals(packet.getClass().getSimpleName())) {
         int id = this.invokeIntGetter(packet, -1, "getId", "id");
         String command = this.invokeStringGetter(packet, "getCommand", "command");
         if (id >= 0 && command != null && !command.isBlank() && auditIsObservableCommandCompletion(command) && !CommandProbeIdAllocator.isProbeId(id)) {
            MC.execute(() -> {
               try {
                  AutismServerInfoOverlay.ActiveScanSession session = this.activeScan;
                  if (session != null && session.probesById.containsKey(id)) {
                     return;
                  }

                  this.userProbesById.put(id, new AutismServerInfoOverlay.ProbeSpec(command, AutismServerInfoOverlay.ProbeKind.USER, null));

                  while (this.userProbesById.size() > 256) {
                     Integer first = this.userProbesById.keySet().iterator().next();
                     this.userProbesById.remove(first);
                  }
               } catch (Throwable t) {
                  this.reportStageFailure("suggestion-request", t);
               }
            });
         }
      }
   }

   public void onPacket(Packet<?> packet) {
      if (packet != null && !autismclient.modules.aD.init_Z()) {
         String simple = packet.getClass().getSimpleName();
         String command = null;
         if ("ServerboundChatCommandPacket".equals(simple) || "ServerboundChatCommandSignedPacket".equals(simple)) {
            command = this.invokeStringGetter(packet, "command", "getCommand");
         } else if ("ServerboundChatPacket".equals(simple)) {
            String message = this.invokeStringGetter(packet, "message", "getMessage");
            if (message != null && message.trim().startsWith("/")) {
               command = message.trim().substring(1);
            }
         }

         if (command != null && !command.isBlank()) {
            String root = this.normalizeCommandToken(command);
            if (this.isPluginCommandToken(root)) {
               String fullCommand = command;
               MC.execute(() -> {
                  try {
                     this.recordCommandHistory(root, fullCommand);
                     this.observeLiveCommand(fullCommand, AutismServerInfoOverlay.EvidenceSource.USER_AUTOCOMPLETE, null);
                  } catch (Throwable t) {
                     this.reportStageFailure("outgoing-command", t);
                  }
               });
            }
         }
      }
   }

   public void onOpenScreen(Packet<?> packet) {
      if (packet != null && !autismclient.modules.aD.init_Z() && "ClientboundOpenScreenPacket".equals(packet.getClass().getSimpleName())) {
         String title = this.componentText(this.invokeFirstGetter(packet, "getTitle", "title"));
         if (title != null && !title.isBlank()) {
            MC.execute(() -> {
               try {
                  AutismServerInfoOverlay.CommandHistoryEntry command = this.findRecentCommand(System.currentTimeMillis());
                  if (command != null) {
                     this.observeLiveCommand(command.fullCommand, AutismServerInfoOverlay.EvidenceSource.COMMAND_GUI, title);
                     return;
                  }

                  String feature = this.featureForGuiTitle(title);
                  if (feature == null || feature.isBlank()) {
                     return;
                  }

                  this.noopFeatureGuiHook(feature, title);
               } catch (Throwable t) {
                  this.reportStageFailure("open-screen", t);
               }
            });
         }
      }
   }

   private void recordCommandHistory(String root, String fullCommand) {
      if (root != null && !root.isBlank()) {
         this.commandHistory.addLast(new AutismServerInfoOverlay.CommandHistoryEntry(root, fullCommand == null ? root : fullCommand, System.currentTimeMillis()));

         while (this.commandHistory.size() > 24) {
            this.commandHistory.removeFirst();
         }
      }
   }

   private AutismServerInfoOverlay.CommandHistoryEntry findRecentCommand(long now) {
      AutismServerInfoOverlay.CommandHistoryEntry best = null;

      while (!this.commandHistory.isEmpty() && now - this.commandHistory.peekFirst().timestampMs > 1500L) {
         this.commandHistory.removeFirst();
      }

      for (AutismServerInfoOverlay.CommandHistoryEntry command : this.commandHistory) {
         if (command != null && now - command.timestampMs <= 1500L) {
            best = command;
         }
      }

      return best;
   }

   private void observeLiveCommand(String rawCommand, AutismServerInfoOverlay.EvidenceSource evidence, String guiTitle) {
      if (rawCommand != null && !rawCommand.isBlank()) {
         MC.execute(() -> {
            try {
               if (MC.getConnection() == null) {
                  return;
               }

               this.syncServerContext();
               String root = this.normalizeCommandToken(rawCommand);
               if (!this.isInCommandTree(root)) {
                  return;
               }

               this.recordCommandHistory(root, rawCommand);
               this.pendingCommandEvidence.add(new AutismServerInfoOverlay.CommandEvidence(rawCommand, evidence, guiTitle));
               this.observationsDirty = true;
               this.flushObservations(false);
            } catch (Throwable t) {
               this.reportStageFailure("live-command", t);
            }
         });
      }
   }

   private boolean isInCommandTree(String rootToken) {
      if (rootToken != null && !rootToken.isBlank()) {
         ClientPacketListener connection = MC.getConnection();
         CommandDispatcher<ClientSuggestionProvider> dispatcher = connection == null ? null : connection.getCommands();
         RootCommandNode<?> treeRoot = dispatcher == null ? null : dispatcher.getRoot();
         if (treeRoot == null) {
            return false;
         }

         if (treeRoot.getChild(rootToken) != null) {
            return true;
         }

         int colon = rootToken.indexOf(58);
         return colon >= 0 && colon + 1 < rootToken.length() && treeRoot.getChild(rootToken.substring(colon + 1)) != null;
      } else {
         return false;
      }
   }

   private void noopFeatureGuiHook(String feature, String guiTitle) {
   }

   private boolean attachGuiTitle(Map<String, AutismServerInfoOverlay.PluginAccumulator> entries, String rawCommand, String guiTitle) {
      String token = this.normalizeCommandToken(rawCommand);
      if (!token.isBlank() && guiTitle != null && !guiTitle.isBlank()) {
         String ownerKey = this.findOwnerKey(entries, token);
         if (ownerKey == null) {
            return false;
         }

         AutismServerInfoOverlay.PluginAccumulator entry = entries.get(ownerKey);
         if (entry == null) {
            return false;
         }

         int before = entry.guis.size();
         String cleanTitle = guiTitle.trim();
         if (entry.guis.size() < 64 || entry.guis.contains(cleanTitle)) {
            entry.guis.add(cleanTitle);
         }

         if (!this.isIndirectEvidence(entry.evidence)) {
            entry.evidence = this.mergeEvidence(
               entry.evidence, AutismServerInfoOverlay.EvidenceSource.COMMAND_GUI
            );
         }

         entry.confidence = this.mergeConfidence(
            entry.confidence, AutismServerInfoOverlay.MatchConfidence.FEATURE
         );
         this.pluginEvidence
            .put(ownerKey, this.mergeEvidence(this.pluginEvidence.get(ownerKey), entry.evidence));
         this.pluginConfidence
            .put(
               ownerKey,
               this.mergeConfidence(this.pluginConfidence.get(ownerKey), AutismServerInfoOverlay.MatchConfidence.FEATURE)
            );
         return before != entry.guis.size();
      } else {
         return false;
      }
   }

   private void applyScanEntries(Map<String, AutismServerInfoOverlay.PluginAccumulator> entries) {
      this.applyCatalogFingerprints(entries);
      this.normalizeEntries(entries);
      if (this.tick_Z) {
         this.poll_Map.clear();
         this.poll_Map.putAll(entries);
         AutismServerInfoOverlay.ActiveScanSession session = this.activeScan;
         if (session != null) {
            session.lastResponseAt = System.currentTimeMillis();
         }

         this.invalidateDetails();
      } else if (this.scanDone) {
         this.scanAddress = this.normalizedServerAddress();
         this.contextSignature = this.computeContextSignature();
         this.scanDone = true;
         this.tick_Z = false;
         this.applyPluginEntries(entries, false);
         this.saveScanToConfig();
      }
   }

   private String featureForGuiTitle(String title) {
      if (title != null && !title.isBlank()) {
         String clean = title.toLowerCase(Locale.ROOT).replaceAll("§.", "");

         for (Entry<String, String> entry : FEATURE_KEYWORDS.entrySet()) {
            String key = entry.getKey();
            if (key.length() >= 3 && clean.contains(key)) {
               return entry.getValue();
            }
         }

         return null;
      } else {
         return null;
      }
   }

   public void onCommandSuggestions(int id, ClientboundCommandSuggestionsPacket packet) {
      if (!CommandProbeIdAllocator.isProbeId(id)) {
         MC.execute(
            () -> {
               AutismServerInfoOverlay.ActiveScanSession session = this.activeScan;
               AutismServerInfoOverlay.ProbeSpec probe = session == null ? null : session.probesById.get(id);
               AutismServerInfoOverlay.ProbeSpec observed = this.userProbesById.remove(id);
               boolean scanProbe = probe != null || session != null && session.pendingProbeIds.contains(id);
               if (scanProbe || observed != null) {
                  long capturedGeneration = scanProbe ? session.generation : -1L;
                  boolean liveObservation = !scanProbe;
                  if (!liveObservation) {
                     String address = this.normalizedServerAddress();
                     if (!this.tick_Z || !session.matches(capturedGeneration, address)) {
                        return;
                     }
                  }

                  try {
                     List<net.minecraft.network.protocol.game.ClientboundCommandSuggestionsPacket.Entry> list = packet != null && packet.suggestions() != null
                        ? packet.suggestions()
                        : List.of();
                     if (scanProbe && session != null) {
                        if (session.pendingProbeIds.remove(id)) {
                           session.replyCount++;
                        }

                        session.probesById.remove(id);
                        session.lastResponseAt = System.currentTimeMillis();
                        session.suggestionReplies++;
                        session.suggestionEntries = session.suggestionEntries + list.size();
                     }

                     List<String> tokens = new ArrayList<>(list.size());

                     for (net.minecraft.network.protocol.game.ClientboundCommandSuggestionsPacket.Entry suggestion : list) {
                        if (suggestion != null && suggestion.text() != null) {
                           tokens.add(suggestion.text());
                        }
                     }

                     if (!tokens.isEmpty()) {
                        this.write_List
                           .add(
                              new AutismServerInfoOverlay.ProbeObservation(
                                 liveObservation ? -1L : capturedGeneration, liveObservation, probe, observed, tokens
                              )
                           );
                        this.observationsDirty = true;
                     }

                     this.flushObservations(false);
                  } catch (Throwable error) {
                     if (scanProbe && session != null && session.matches(capturedGeneration, this.normalizedServerAddress())) {
                        session.pendingProbeIds.remove(id);
                        session.probesById.remove(id);
                        session.failedCount++;
                        this.recordScanFailure(session, "suggestion reply", error);
                        this.tickActiveScanSession();
                     } else {
                        LOGGER.warn("Plugin autocomplete observation failed", error);
                     }
                  }
               }
            }
         );
      }
   }

   private void flushObservations(boolean force) {
      if (this.observationsDirty) {
         long now = System.currentTimeMillis();
         if (force || now - this.lastObservationFlushMs >= 250L) {
            this.lastObservationFlushMs = now;
            this.observationsDirty = false;

            try {
               this.close(force);
            } catch (Throwable t) {
               this.reportStageFailure("suggestion-maintenance", t);
            }
         }
      }
   }

   private void close(boolean force) {
      List<AutismServerInfoOverlay.ProbeObservation> batch = new ArrayList<>(this.write_List);
      this.write_List.clear();
      List<AutismServerInfoOverlay.CommandEvidence> liveBatch = new ArrayList<>(this.pendingCommandEvidence);
      this.pendingCommandEvidence.clear();
      if (!batch.isEmpty() || !liveBatch.isEmpty()) {
         Map<String, AutismServerInfoOverlay.PluginAccumulator> entries = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
         if (this.tick_Z) {
            entries.putAll(this.poll_Map);
         } else {
            entries.putAll(this.snapshotPluginEntries());
         }

         Map<String, AutismServerInfoOverlay.PluginAccumulator> legacyEntries = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
         legacyEntries.putAll(this.legacyPluginEntries);

         boolean scanData = false;
         Set<String> knownPlayers = this.knownPlayerNames();
         this.auditActiveKnownPlayers = knownPlayers;
         try {
            for (AutismServerInfoOverlay.CommandEvidence live : liveBatch) {
               this.attributeCommand(entries, live.command, live.evidence);
               if (live.guiTitle != null && !live.guiTitle.isBlank()) {
                  this.attachGuiTitle(entries, live.command, live.guiTitle);
               }
            }

            Iterator var12 = batch.iterator();

            while (true) {
               AutismServerInfoOverlay.ProbeObservation reply;
               while (true) {
                  if (!var12.hasNext()) {
                     this.inferOwnersFromObservedCommands(legacyEntries);
                     this.applyCommandInference(entries);
                     this.mergeAccumulators(entries, legacyEntries);
                     this.normalizeEntries(entries);
                     if (!scanData && !this.tick_Z) {
                        this.applyScanEntries(entries);
                     } else {
                        this.legacyPluginEntries.clear();
                        this.legacyPluginEntries.putAll(legacyEntries);
                        this.poll_Map.clear();
                        this.poll_Map.putAll(entries);
                        AutismServerInfoOverlay.ActiveScanSession session = this.activeScan;
                        this.trace(
                           session,
                           "suggestions batch=" + batch.size() + " legacy=" + this.countNamedEntries(this.legacyPluginEntries) + " working=" + this.countNamedEntries(this.poll_Map)
                        );
                        if (!force) {
                           this.tickActiveScanSession();
                        }
                     }

                     return;
                  }

                  reply = (AutismServerInfoOverlay.ProbeObservation)var12.next();
                  if (reply.liveObservation) {
                     break;
                  }

                  AutismServerInfoOverlay.ActiveScanSession current = this.activeScan;
                  if (this.tick_Z && current != null && current.matches(reply.generation, this.normalizedServerAddress())) {
                     scanData = true;
                     break;
                  }
               }

               boolean isArgCompletion = (reply.probe != null
                     && (reply.probe.kind == AutismServerInfoOverlay.ProbeKind.PLUGIN_LIST
                        || reply.probe.kind == AutismServerInfoOverlay.ProbeKind.VERSION
                        || reply.probe.kind == AutismServerInfoOverlay.ProbeKind.HELP
                        || (reply.probe.query != null && reply.probe.query.contains(" "))))
                  || (reply.observed != null && reply.observed.query != null && reply.observed.query.contains(" "));
               boolean rootProbe = auditIsRootProbe(reply.probe) || auditIsRootProbe(reply.observed);
               boolean playerBatch = this.auditIsPlayerListBatch(reply.tokens, knownPlayers, isArgCompletion, rootProbe);

               for (String text : reply.tokens) {
                  // Player names never contain ':', so namespaced commands survive a player-list batch.
                  if (playerBatch && !text.contains(":")) {
                     continue;
                  }

                  this.processProbeReply(entries, legacyEntries, reply.probe, reply.observed, reply.liveObservation, text);
               }
            }
         } finally {
            this.auditActiveKnownPlayers = null;
         }
      }
   }

   private void processProbeReply(
      Map<String, AutismServerInfoOverlay.PluginAccumulator> entries,
      Map<String, AutismServerInfoOverlay.PluginAccumulator> legacyEntries,
      AutismServerInfoOverlay.ProbeSpec probe,
      AutismServerInfoOverlay.ProbeSpec observed,
      boolean liveObservation,
      String text
   ) {
      String normalizedToken = this.normalizeCommandToken(text);
      if (!normalizedToken.isEmpty()) {
         this.rememberIfPluginToken(normalizedToken);
      }

      if (text.contains(":")) {
         String[] parts = text.split(":", 2);
         String ns = parts[0].toLowerCase(Locale.ROOT);
         String cmd = parts.length > 1 ? parts[1] : "";
         if (!PLATFORM_NAMESPACES.contains(ns)) {
            this.mergePluginEntry(legacyEntries, ns, List.of(cmd), true, AutismServerInfoOverlay.EvidenceSource.COMMAND_TREE);
            this.rememberIfPluginToken(cmd);
         }

         this.attributeCommand(entries, text, liveObservation ? AutismServerInfoOverlay.EvidenceSource.USER_AUTOCOMPLETE : AutismServerInfoOverlay.EvidenceSource.COMMAND_TREE);
      } else if (probe != null) {
         if (probe.kind == AutismServerInfoOverlay.ProbeKind.NAMESPACE && probe.pluginHint != null && !normalizedToken.isEmpty()) {
            this.mergePluginEntry(legacyEntries, probe.pluginHint, List.of(normalizedToken), true, AutismServerInfoOverlay.EvidenceSource.NAMESPACE);
            this.mergePluginEntry(entries, probe.pluginHint, List.of(normalizedToken), true, AutismServerInfoOverlay.EvidenceSource.NAMESPACE);
         } else if (probe.kind == AutismServerInfoOverlay.ProbeKind.PLUGIN_LIST
            || probe.kind == AutismServerInfoOverlay.ProbeKind.VERSION) {
            String pluginCandidate = text.trim();
            String pluginKey = this.pluginKey(pluginCandidate);
            if (this.isPlausiblePluginCandidate(pluginCandidate, probe.kind) && !PLUGIN_ALIASES.containsKey(pluginKey)) {
               AutismServerInfoOverlay.EvidenceSource candidateEvidence = probe.kind == AutismServerInfoOverlay.ProbeKind.PLUGIN_LIST
                  ? AutismServerInfoOverlay.EvidenceSource.PLUGIN_LIST
                  : AutismServerInfoOverlay.EvidenceSource.VERSION_HINT;
               AutismServerInfoOverlay.MatchConfidence candidateConfidence = this.confidenceForEvidence(candidateEvidence);
               this.mergePluginEntry(
                  legacyEntries,
                  null,
                  pluginCandidate,
                  List.of(),
                  List.of(),
                  List.of(),
                  false,
                  candidateEvidence,
                  candidateConfidence,
                  AutismServerInfoOverlay.DetectionType.PLUGIN
               );
               this.mergePluginEntry(
                  entries,
                  null,
                  pluginCandidate,
                  List.of(),
                  List.of(),
                  List.of(),
                  false,
                  candidateEvidence,
                  candidateConfidence,
                  AutismServerInfoOverlay.DetectionType.PLUGIN
               );
            }
         } else if (probe.kind == AutismServerInfoOverlay.ProbeKind.HELP && probe.pluginHint != null && probe.pluginHint.length() >= 2 && !normalizedToken.isEmpty()) {
            this.mergePluginEntry(legacyEntries, probe.pluginHint, List.of(normalizedToken), true, AutismServerInfoOverlay.EvidenceSource.HELP_HINT);
            this.mergePluginEntry(entries, probe.pluginHint, List.of(normalizedToken), true, AutismServerInfoOverlay.EvidenceSource.HELP_HINT);
         } else if (probe.kind == AutismServerInfoOverlay.ProbeKind.ROOT && !normalizedToken.isEmpty()) {
            this.attachLooseCommand(entries, normalizedToken);
         }
      } else if (observed != null && !normalizedToken.isEmpty()) {
         this.attributeCommand(entries, normalizedToken, AutismServerInfoOverlay.EvidenceSource.USER_AUTOCOMPLETE);
      }
   }

   public void onCommandTreeReceived() {
      this.invalidateContextSignature();
      MC.execute(() -> {
         try {
            this.onCommandTreeUpdate();
         } catch (Throwable t) {
            this.reportStageFailure("command-tree", t);
         }
      });
   }

   private void onCommandTreeUpdate() {
      String currentAddress = this.normalizedServerAddress();
      if (!currentAddress.isEmpty()) {
         String currentContextSignature = this.computeContextSignature();
         AutismServerInfoOverlay.ActiveScanSession session = this.activeScan;
         if (session != null && this.tick_Z && currentAddress.equals(session.serverAddress)) {
            session.contextSignature = currentContextSignature;
            this.contextSignature = currentContextSignature;

            try {
               CommandDispatcher<ClientSuggestionProvider> dispatcher = MC.getConnection() == null
                  ? null
                  : MC.getConnection().getCommands();
               if (dispatcher != null && dispatcher.getRoot() != null) {
                  this.ingestCommandTree(dispatcher.getRoot());
               }
            } catch (RuntimeException error) {
               this.recordScanFailure(session, "command tree update", error);
            }
         } else {
            PluginSignatureCatalog.clear();
            this.catalogRevision = -1L;
            if (currentAddress.equals(this.scanAddress) && this.scanDone && AutismConfig.getGlobal().autoProbePlugins) {
               this.restartScan();
               this.startScan(true);
            } else if (currentAddress.equals(this.scanAddress) && this.scanDone) {
               this.resetForServer(currentAddress, currentContextSignature);
            } else {
               this.syncServerContext();
            }
         }
      }
   }

   public void onSignaturesChanged() {
      this.catalogRevision = -1L;
      this.invalidateDetails();
      this.invalidateContextSignature();
   }

   public void showOverviewTab() {
      this.setVisible(true);
      this.onTabSelected(0);
   }

   public void showPluginsTab() {
      this.setVisible(true);
      this.onTabSelected(1);
   }

   public void toggleVisible() {
      this.setVisible(!this.overlayVisible);
   }

   @Override
   public void setVisible(boolean v) {
      this.overlayVisible = v;
      if (v) {
         this.windowNode.setShowBody(!this.overlayCollapsed);
         OverlayManager.getInstance().bringToFront(this);
         this.syncServerContext();
         if (this.activeTab == 0) {
            this.resizeForInfoTab();
         } else if (this.tick_Z) {
            this.resizeForScanningPlugins();
         } else if (!this.scanDone) {
            this.resizeForUnscannedPlugins();
         } else {
            this.resizeForPluginResults();
         }
      }

      this.saveState();
   }

   @Override
   public boolean isVisible() {
      return this.overlayVisible;
   }

   @Override
   public boolean isCollapsed() {
      return this.overlayCollapsed;
   }

   @Override
   public void setCollapsed(boolean c) {
      if (this.overlayCollapsed != c) {
         this.overlayCollapsed = c;
         this.windowNode.setShowBody(!this.overlayCollapsed);
         if (c) {
            this.clearFocusAndDismissMenu();
         } else if (this.activeTab == 0) {
            this.resizeForInfoTab();
         } else if (this.tick_Z) {
            this.resizeForScanningPlugins();
         } else if (!this.scanDone) {
            this.resizeForUnscannedPlugins();
         } else {
            this.resizeForPluginResults();
         }

         this.saveState();
      }
   }

   @Override
   public String getOverlayId() {
      return "autism-serverinfo";
   }

   @Override
   public boolean supportsHeaderCollapseClick() {
      return true;
   }

   @Override
   public boolean containsPoint(double mx, double my) {
      if (!this.overlayVisible) {
         return false;
      }

      autismclient.gui.ui.layout.LayoutViewport viewport = this.layoutHost.createViewport();
      float uiX = viewport.toUiX(mx);
      float uiY = viewport.toUiY(my);
      return uiX >= this.overlayX && uiX <= this.overlayX + this.overlayWidth && uiY >= this.overlayY && uiY <= this.overlayY + this.overlayHeight;
   }

   @Override
   public boolean isOverHeader(double mx, double my) {
      if (!this.overlayVisible) {
         return false;
      }

      autismclient.gui.ui.layout.LayoutViewport viewport = this.layoutHost.createViewport();
      float uiX = viewport.toUiX(mx);
      float uiY = viewport.toUiY(my);
      return this.isOverHeaderUi(uiX, uiY) && !this.isOverCloseButton(uiX, uiY);
   }

   @Override
   public boolean hasFocusedTextInput() {
      return this.layoutHost.hasFocusedInput();
   }

   @Override
   public OverlayBounds getBounds() {
      return new OverlayBounds(this.overlayX, this.overlayY, this.overlayWidth, this.overlayHeight, this.overlayVisible, this.overlayCollapsed);
   }

   @Override
   public void setBounds(OverlayBounds b) {
      if (b != null) {
         OverlayBounds clamped = this.clampBounds(b);
         this.overlayX = clamped.x;
         this.overlayY = clamped.y;
         this.overlayWidth = clamped.width;
         this.overlayHeight = clamped.height;
         this.overlayVisible = clamped.visible;
         this.overlayCollapsed = clamped.collapsed;
         this.windowNode.setShowBody(!this.overlayCollapsed);
         this.saveTabSize();
      }
   }

   @Override
   public int getMinWidth() {
      return this.computeMinWidth();
   }

   @Override
   public int getMinHeight() {
      if (this.activeTab == 1) {
         if (this.tick_Z) {
            return this.scanningPluginsHeight();
         } else {
            return !this.scanDone ? this.unscannedPluginsHeight() : this.pluginResultsMinHeight();
         }
      } else {
         return this.infoTabContentHeight();
      }
   }

   @Override
   public void render(GuiGraphicsExtractor ctx, int mx, int my, float delta) {
      if (this.overlayVisible || this.isClosing()) {
         try {
            this.renderContents(ctx, mx, my, delta);
         } catch (Throwable t) {
            this.reportStageFailure("render", t);
         }
      }
   }

   private void renderContents(GuiGraphicsExtractor ctx, int mx, int my, float delta) {
      this.syncServerContext();
      this.refreshLiveScanResults();
      this.tickActiveScanSession();
      long nowMs = System.currentTimeMillis();
      if (nowMs - this.lastLayoutRebuildMs >= 100L && (this.layoutDirty || this.tick_Z)) {
         this.clickHotspots.clear();
         this.rebuildLayout();
         this.lastLayoutRebuildMs = nowMs;
      }

      autismclient.gui.ui.layout.LayoutViewport viewport = this.layoutHost.createViewport();
      float uiMouseX = viewport.toUiX(mx);
      float uiMouseY = viewport.toUiY(my);
      boolean active = OverlayManager.getInstance().isFocused(this) || OverlayManager.getInstance().isTopmost(this);
      boolean headerHovered = this.isOverHeaderUi(uiMouseX, uiMouseY);
      autismclient.gui.ui.layout.LayoutContext metrics = new autismclient.gui.ui.layout.LayoutContext(
         ctx, this.textRenderer, viewport, this.theme, uiMouseX, uiMouseY, delta
      );
      this.windowNode.setExpanded(!this.overlayCollapsed);
      this.windowNode.setActive(active);
      this.windowNode.setHeaderHovered(headerHovered);
      int preferredHeight = Math.round(this.windowNode.measureHeight(metrics, this.overlayWidth));
      if (this.overlayCollapsed) {
         this.overlayHeight = preferredHeight;
      } else if (this.activeTab == 0) {
         this.overlayHeight = this.fitInfoTabHeight();
      } else if (this.tick_Z) {
         this.overlayHeight = this.scanningPluginsHeight();
      } else if (!this.scanDone) {
         this.overlayHeight = this.unscannedPluginsHeight();
      } else {
         this.overlayHeight = this.pluginResultsHeight();
      }

      this.overlayWidth = this.computeWidth();
      OverlayBounds clamped = this.clampBounds(new OverlayBounds(this.overlayX, this.overlayY, this.overlayWidth, this.overlayHeight, this.overlayVisible, this.overlayCollapsed));
      this.overlayX = clamped.x;
      this.overlayY = clamped.y;
      this.overlayWidth = clamped.width;
      this.overlayHeight = clamped.height;
      this.windowNode.setBounds(this.overlayX, this.overlayY, this.overlayWidth, this.overlayHeight);
      this.layoutHost.render(ctx, mx, my, delta);
      if (this.windowNode.pushBodyClip(ctx)) {
         try {
            if (this.activeTab == 1 && this.scanDone && !this.tick_Z) {
               this.clickHotspots.clear();
               this.renderDetailList(ctx, viewport, uiMouseX, uiMouseY, delta);
            }
         } finally {
            autismclient.gui.ui.UiClipStack.getInstance().pop(ctx);
         }
      }
   }

   private String reportedVersion(ServerData entry) {
      return entry != null && entry.version != null ? entry.version.getString() : "--";
   }

   private String detectedVersion() {
      String version = AutismSessionState.getInstance().getDetectedServerVersion(this.displayedAddress());
      if (version != null && !version.isBlank()) {
         return version;
      }

      String brand = this.serverBrand();
      if (brand != null && !brand.isBlank() && !"--".equals(brand)) {
         String extracted = this.softwareFromBrand(brand);
         if (extracted != null) {
            return extracted;
         }
      }

      return "--";
   }

   private String softwareFromBrand(String brand) {
      if (brand != null && !brand.isBlank()) {
         String lower = brand.toLowerCase(Locale.ROOT);
         int dashIdx = lower.indexOf(45);
         if (dashIdx >= 0 && dashIdx + 1 < brand.length()) {
            String afterDash = brand.substring(dashIdx + 1);
            int spaceIdx = afterDash.indexOf(32);
            return spaceIdx >= 0 ? afterDash.substring(0, spaceIdx) : afterDash;
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   private void renderDetailList(GuiGraphicsExtractor ctx, autismclient.gui.ui.layout.LayoutViewport viewport, float uiMouseX, float uiMouseY, float delta) {
      int x = Math.round(this.listViewportSpacer.x());
      int y = Math.round(this.listViewportSpacer.y());
      int rowW = Math.round(this.listViewportSpacer.width());
      int viewH = this.visibleViewportHeight(this.listViewportSpacer);
      int listTop = y;
      int listBottom = listTop + viewH;
      if (rowW > 0 && viewH > 0) {
         int clipLeft = x + 1;
         int clipTop = listTop + 1;
         int clipRight = x + rowW - 1;
         int clipBottom = listBottom - 1;
         int innerViewH = this.innerViewportHeight(viewH);
         if (innerViewH > 0) {
            int rowContentW = Math.max(48, clipRight - clipLeft);
            List<AutismServerInfoOverlay.DetailRow> rows = this.detailRowsFor(this.searchInput.getValue(), rowContentW);
            int estimatedContentHeight = this.totalRowsHeight(rows);
            int maxScroll = Math.max(0, estimatedContentHeight - innerViewH);
            int quantizedTarget = this.quantizeScrollOffset(this.scroll.getOffset(), this.detailRowHeight(), maxScroll);
            this.scroll.setOffset(quantizedTarget, maxScroll);
            int visualScroll = this.quantizeScrollOffset(this.scroll.tickAndClamp(delta, maxScroll), this.detailRowHeight(), maxScroll);
            this.contentHeight = estimatedContentHeight;
            autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics scrollbarMetrics = this.scrollbarMetrics(x, listTop, rowW, viewH);
            int scrolledRowContentW = Math.max(48, clipRight - clipLeft - (scrollbarMetrics.isScrollable() ? 10 : 0));
            if (scrolledRowContentW != rowContentW) {
               rowContentW = scrolledRowContentW;
               rows = this.detailRowsFor(this.searchInput.getValue(), rowContentW);
               estimatedContentHeight = this.totalRowsHeight(rows);
               maxScroll = Math.max(0, estimatedContentHeight - innerViewH);
               quantizedTarget = this.quantizeScrollOffset(this.scroll.getOffset(), this.detailRowHeight(), maxScroll);
               this.scroll.setOffset(quantizedTarget, maxScroll);
               visualScroll = this.quantizeScrollOffset(this.scroll.tickAndClamp(0.0F, maxScroll), this.detailRowHeight(), maxScroll);
               this.contentHeight = estimatedContentHeight;
               scrollbarMetrics = this.scrollbarMetrics(x, listTop, rowW, viewH);
            }

            boolean active = OverlayManager.getInstance().isFocused(this) || OverlayManager.getInstance().isTopmost(this);
            viewport.pushTransform(ctx);

            try {
               autismclient.gui.ui.widgets.ListRowRenderer.drawListFrame(ctx, x, listTop, rowW, viewH, active);
               viewport.pushClip(ctx, clipLeft, clipTop, clipRight, clipBottom);

               try {
                  int startIndex = this.firstVisibleRow(rows, visualScroll);
                  int cy = clipTop - visualScroll + this.rowOffset(startIndex);

                  for (int rowIndex = startIndex; rowIndex < rows.size(); rowIndex++) {
                     AutismServerInfoOverlay.DetailRow row = rows.get(rowIndex);
                     int rowY = cy;
                     int rowTop = rowY;
                     int rowBottom = rowY + row.height;
                     boolean rowVisible = rowBottom > clipTop && rowTop < clipBottom;
                     if (row.isHeader()) {
                        if (rowVisible) {
                           autismclient.gui.ui.widgets.PanelSurfaceHelper.drawSectionHeader(ctx, clipLeft, rowTop, clipRight - clipLeft, rowBottom - rowTop);
                           int headerTextY = UiLayoutMath.centerTextY(
                              rowY, row.height, this.theme.init_I_l(autismclient.gui.f.b.l.write), this.theme.flush()
                           );
                           autismclient.gui.f.b.k.init(
                              ctx,
                              this.textRenderer,
                              row.title,
                              this.theme.init_Identifier_l(autismclient.gui.f.b.l.write),
                              this.theme.load(autismclient.gui.f.b.l.write),
                              clipLeft + this.headerTextIndent(),
                              headerTextY,
                              false
                           );
                        }
                     } else if (row.type == AutismServerInfoOverlay.DetailRowKind.PLUGIN) {
                        boolean standaloneCommandHint = this.isStandaloneCommandHint(row);
                        boolean selected = !standaloneCommandHint && this.samePluginKey(row.plugin, this.selectedPlugin);
                        boolean hovered = rowVisible && uiMouseX >= clipLeft && uiMouseX < clipLeft + rowContentW && uiMouseY >= rowTop && uiMouseY < rowBottom;
                        if (rowVisible) {
                           autismclient.gui.ui.widgets.PanelSurfaceHelper.drawCardRow(ctx, clipLeft, rowTop, rowContentW, rowBottom - rowTop, hovered, selected);
                        }

                        int rowTextY = UiLayoutMath.centerTextY(rowY, row.height, this.theme.init_I_l(autismclient.gui.f.b.l.read), this.theme.flush());
                        if (rowVisible && !standaloneCommandHint) {
                           int arrowColor = !hovered && !selected
                              ? this.theme.load(autismclient.gui.f.b.l.write)
                              : this.theme.load(autismclient.gui.f.b.l.read);
                           autismclient.gui.f.PacketUtil.init(
                              ctx,
                              autismclient.gui.f.NetHelper.init(clipLeft + 2, rowTextY, 9, this.theme.init_I_l(autismclient.gui.f.b.l.read)),
                              selected,
                              arrowColor
                           );
                        }

                        boolean isAnticheat = row.plugin != null && ANTICHEAT_PLUGINS.contains(row.plugin.toLowerCase(Locale.ROOT));
                        int nameColor = isAnticheat
                           ? ThemePaletteManager.recolor(-43691, ThemePaletteManager.ColorRole.DANGER)
                           : this.confidenceColor(row.confidence, hovered);
                        String label = isAnticheat ? "! " + row.plugin : row.plugin;
                        int labelX = clipLeft + (standaloneCommandHint ? 5 : 13);
                        int labelMaxWidth = Math.max(40, rowContentW - 18);
                        if (row.countLabel != null && !row.countLabel.isBlank()) {
                           int cw = autismclient.gui.f.b.k.init(
                              this.textRenderer,
                              row.countLabel,
                              this.theme.init_Identifier_l(autismclient.gui.f.b.l.write),
                              this.theme.load(autismclient.gui.f.b.l.write)
                           );
                           int countX = clipLeft + rowContentW - cw - 4;
                           labelMaxWidth = Math.max(36, countX - labelX - 4);
                           int countTextY = UiLayoutMath.centerTextY(
                              rowY, row.height, this.theme.init_I_l(autismclient.gui.f.b.l.write), this.theme.flush()
                           );
                           if (rowVisible) {
                              autismclient.gui.f.b.k.init(
                                 ctx,
                                 this.textRenderer,
                                 row.countLabel,
                                 this.theme.init_Identifier_l(autismclient.gui.f.b.l.write),
                                 this.theme.load(autismclient.gui.f.b.l.write),
                                 countX,
                                 countTextY,
                                 false
                              );
                           }
                        }

                        if (rowVisible) {
                           String displayLabel = autismclient.gui.f.b.k.init(
                              this.textRenderer, label, labelMaxWidth, this.theme.init_Identifier_l(autismclient.gui.f.b.l.read), nameColor
                           );
                           autismclient.gui.f.b.k.init(
                              ctx,
                              this.textRenderer,
                              displayLabel,
                              this.theme.init_Identifier_l(autismclient.gui.f.b.l.read),
                              nameColor,
                              labelX,
                              rowTextY,
                              false
                           );
                           if (!standaloneCommandHint) {
                              String clickedPlugin = row.plugin;
                              int hitTop = Math.max(rowTop, clipTop);
                              int hitBottom = Math.min(rowBottom, clipBottom);
                              this.clickHotspots
                                 .add(new AutismServerInfoOverlay.ClickHotspot(clipLeft, hitTop, rowContentW, Math.max(1, hitBottom - hitTop), () -> {
                                    this.selectedPlugin = this.samePluginKey(clickedPlugin, this.selectedPlugin) ? null : clickedPlugin;
                                    this.invalidateDetailRows();
                                    this.saveState();
                                 }));
                           } else {
                              String hintCommand = row.plugin;
                              int hitTop = Math.max(rowTop, clipTop);
                              int hitBottom = Math.min(rowBottom, clipBottom);
                              this.clickHotspots
                                 .add(new AutismServerInfoOverlay.ClickHotspot(clipLeft, hitTop, rowContentW, Math.max(1, hitBottom - hitTop), () -> {
                                    this.clearInputFocus();
                                    this.clickHotspots.clear();
                                    String fullCmd = hintCommand.startsWith("/") ? hintCommand : "/" + hintCommand;
                                    OverlayManager.getInstance().setHidden(this, true);
                                    MC.gui.setScreen(new ChatScreen(fullCmd, false));
                                 }));
                           }
                        }
                     } else {
                        boolean commandRow = row.type == AutismServerInfoOverlay.DetailRowKind.COMMAND;
                        boolean channelRow = row.type == AutismServerInfoOverlay.DetailRowKind.CHANNEL
                           && row.actionCommand != null
                           && !row.actionCommand.isBlank();
                        boolean clickableRow = commandRow || channelRow;
                        boolean hovered = clickableRow
                           && rowVisible
                           && uiMouseX >= clipLeft + 9
                           && uiMouseX < clipLeft + rowContentW - 6
                           && uiMouseY >= rowTop
                           && uiMouseY < rowBottom;
                        if (rowVisible) {
                           int detailColor = this.detailRowColor(row);
                           autismclient.gui.ui.widgets.PanelSurfaceHelper.drawCardRow(ctx, clipLeft + 5, rowTop, rowContentW - 9, rowBottom - rowTop, hovered, false);
                           autismclient.gui.ui.widgets.PanelSurfaceHelper.drawAccentBar(ctx, clipLeft + 5, rowTop, 1, rowBottom - rowTop, detailColor);
                           int textColor = hovered ? this.theme.load(autismclient.gui.f.b.l.read) : detailColor;
                           String detailLabel = autismclient.gui.f.b.k.init(
                              this.textRenderer,
                              row.label,
                              Math.max(20, rowContentW - 20),
                              this.theme.init_Identifier_l(autismclient.gui.f.b.l.write),
                              textColor
                           );
                           int detailTextY = UiLayoutMath.centerTextY(
                              rowY, row.height, this.theme.init_I_l(autismclient.gui.f.b.l.write), this.theme.flush()
                           );
                           autismclient.gui.f.b.k.init(
                              ctx,
                              this.textRenderer,
                              detailLabel,
                              this.theme.init_Identifier_l(autismclient.gui.f.b.l.write),
                              textColor,
                              clipLeft + 11,
                              detailTextY,
                              false
                           );
                        }

                        if (rowVisible && clickableRow && row.actionCommand != null && !row.actionCommand.isBlank()) {
                           String actionValue = row.actionCommand;
                           int hitTop = Math.max(rowTop, clipTop);
                           int hitBottom = Math.min(rowBottom, clipBottom);
                           this.clickHotspots
                              .add(
                                 new AutismServerInfoOverlay.ClickHotspot(
                                    clipLeft + 9, hitTop, Math.max(1, rowContentW - 15), Math.max(1, hitBottom - hitTop), () -> {
                                       this.clearInputFocus();
                                       this.clickHotspots.clear();
                                       if (commandRow) {
                                          String fullCmd = actionValue.startsWith("/") ? actionValue : "/" + actionValue;
                                          OverlayManager.getInstance().setHidden(this, true);
                                          MC.gui.setScreen(new ChatScreen(fullCmd, false));
                                       } else {
                                          this.openPayloadEditor(actionValue);
                                       }
                                    }
                                 )
                              );
                        }
                     }

                     cy += row.height;
                     if (cy > clipBottom + this.detailRowHeight()) {
                        break;
                     }
                  }
               } finally {
                  viewport.popClip(ctx);
               }

               autismclient.gui.ui.widgets.ScrollbarMetricsHelper.render(ctx, scrollbarMetrics, scrollbarMetrics.containsTrack(uiMouseX, uiMouseY), this.draggingScrollbar);
            } finally {
               viewport.popTransform(ctx);
            }
         }
      }
   }

   private int detailRowColor(AutismServerInfoOverlay.DetailRow row) {
      if (row == null) {
         return this.theme.load(autismclient.gui.f.b.l.write);
      }

      int var10000;
      switch (row.type) {
         case HEADER:
         case PLUGIN:
            var10000 = this.theme.load(autismclient.gui.f.b.l.write);
            break;
         case COMMAND:
            var10000 = -1716016;
            break;
         case GUI:
            var10000 = UiColorPalette.warningYellow();
            break;
         case CHANNEL:
            var10000 = this.channelDetailColor(row.label == null ? null : row.label.replaceFirst("^ch\\s+", ""));
            break;
         case WHY:
            var10000 = this.confidenceColor(row.confidence, false);
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   private boolean isStandaloneCommandHint(AutismServerInfoOverlay.DetailRow row) {
      return row != null
         && row.type == AutismServerInfoOverlay.DetailRowKind.PLUGIN
         && row.evidence == AutismServerInfoOverlay.EvidenceSource.SCANNER_AUTOCOMPLETE
         && row.plugin != null
         && row.plugin.startsWith("/");
   }

   private autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics scrollbarMetrics(int x, int y, int width, int height) {
      int innerHeight = this.innerViewportHeight(height);
      int maxScroll = Math.max(0, this.contentHeight - innerHeight);
      return autismclient.gui.ui.widgets.ScrollbarMetricsHelper.compute(
         this.contentHeight,
         Math.max(1, innerHeight),
         x + width - 5,
         y + 1,
         3,
         Math.max(1, innerHeight),
         this.quantizeScrollOffset(this.scroll.tickAndClamp(0.0F, maxScroll), this.detailRowHeight(), maxScroll)
      );
   }

   private String countSummary(List<String> commands, List<String> guis, List<String> channels) {
      int commandCount = commands == null ? 0 : commands.size();
      int guiCount = guis == null ? 0 : guis.size();
      int observedChannelCount = 0;
      int probableChannelCount = 0;
      boolean noChannelObserved = false;
      if (channels != null) {
         for (String channel : channels) {
            if (channel != null && !channel.isBlank()) {
               if (this.isNoChannel(channel)) {
                  noChannelObserved = true;
               } else if (this.isUnobservedChannel(channel)) {
                  probableChannelCount++;
               } else {
                  observedChannelCount++;
               }
            }
         }
      }

      int channelCount = observedChannelCount + probableChannelCount;
      String channelLabel;
      if (observedChannelCount > 0) {
         channelLabel = observedChannelCount + " ch";
      } else if (probableChannelCount > 0) {
         channelLabel = probableChannelCount + "? ch";
      } else if (noChannelObserved) {
         channelLabel = "no ch";
      } else {
         channelLabel = "0 ch";
      }

      List<String> parts = new ArrayList<>();
      if (commandCount > 0) {
         parts.add(commandCount + "c");
      }

      if (guiCount > 0) {
         parts.add(guiCount + "g");
      }

      if (channelCount > 0 || noChannelObserved) {
         parts.add(channelLabel);
      }

      return !parts.isEmpty() ? String.join("/", parts) : channelLabel;
   }

   private String countSummary(List<String> commands, List<String> channels) {
      return this.countSummary(commands, List.of(), channels);
   }

   private List<String> channelsForPlugin(String plugin) {
      List<String> observed = this.observedChannelsFor(plugin);
      if (observed != null && !observed.isEmpty()) {
         return observed;
      }

      List<String> probableNamespaces = PluginSignatureCatalog.namespacesFor(plugin);
      if (!probableNamespaces.isEmpty()) {
         List<String> rows = new ArrayList<>();

         for (String namespace : probableNamespaces) {
            if (namespace != null && !namespace.isBlank()) {
               rows.add("? " + namespace.trim().toLowerCase(Locale.ROOT) + ":*");
            }
         }

         rows = this.distinctRows(rows);
         if (!rows.isEmpty()) {
            return rows;
         }
      }

      return List.of("no channel");
   }

   private int channelDetailColor(String detail) {
      if (detail == null) {
         return this.theme.load(autismclient.gui.f.b.l.write);
      } else {
         String lower = detail.toLowerCase(Locale.ROOT);
         if (this.isNoChannel(lower)) {
            return -7371387;
         } else if (this.isUnobservedChannel(lower)) {
            return -7371387;
         } else if (lower.contains("[registered")) {
            return UiColorPalette.mint();
         } else {
            return lower.contains("[live") ? UiColorPalette.cyan() : UiColorPalette.cyan();
         }
      }
   }

   private String channelLabel(String detail) {
      if (detail == null || detail.isBlank()) {
         return "--";
      } else if (this.isNoChannel(detail)) {
         return "no channel";
      } else {
         String base = this.channelBase(detail);
         if (base.isBlank()) {
            return "--";
         } else {
            return this.isUnobservedChannel(detail) ? "? " + base : base;
         }
      }
   }

   private List<String> distinctRows(Collection<String> source) {
      if (source != null && !source.isEmpty()) {
         Map<String, String> unique = new LinkedHashMap<>();

         for (String row : source) {
            if (row != null && !row.isBlank()) {
               String clean = row.trim();
               String key = this.channelKey(clean);
               if (!key.isBlank()) {
                  String existing = unique.get(key);
                  if (existing == null || this.channelRank(clean) > this.channelRank(existing)) {
                     unique.put(key, clean);
                  }
               }
            }
         }

         List<String> out = new ArrayList<>(unique.values());
         out.sort((a, b) -> String.CASE_INSENSITIVE_ORDER.compare(this.channelLabel(a), this.channelLabel(b)));
         return out;
      } else {
         return List.of();
      }
   }

   private String channelKey(String detail) {
      return this.isNoChannel(detail) ? "no-channel" : this.channelBase(detail).toLowerCase(Locale.ROOT);
   }

   private String channelBase(String detail) {
      if (detail == null) {
         return "";
      }

      String text = detail.trim();
      if (text.regionMatches(true, 0, "ch ", 0, 3)) {
         text = text.substring(3).trim();
      }

      if (text.startsWith("?")) {
         text = text.substring(1).trim();
      }

      int bracket = text.indexOf(" [");
      if (bracket >= 0) {
         text = text.substring(0, bracket).trim();
      }

      return text;
   }

   private String channelFromDetail(String detail) {
      if (detail != null && !this.isNoChannel(detail)) {
         String base = this.channelBase(detail);
         String channel = this.normalizeChannel(base);
         return channel.isBlank() ? null : channel;
      } else {
         return null;
      }
   }

   private String normalizeChannel(String rawChannel) {
      if (rawChannel == null) {
         return "";
      }

      String channel = rawChannel.trim();
      if (channel.isBlank()) {
         return "";
      }

      String legacy = channel.replace(" ", "");
      if ("REGISTER".equalsIgnoreCase(legacy) || "minecraft:register".equalsIgnoreCase(legacy)) {
         return "minecraft:register";
      }

      if ("UNREGISTER".equalsIgnoreCase(legacy) || "minecraft:unregister".equalsIgnoreCase(legacy)) {
         return "minecraft:unregister";
      }

      if (!"BungeeCord".equalsIgnoreCase(legacy) && !"bungeecord".equalsIgnoreCase(legacy)) {
         int wildcard = channel.indexOf(42);
         if (wildcard >= 0) {
            channel = channel.substring(0, wildcard);

            while (channel.endsWith(":") || channel.endsWith("/") || channel.endsWith(".")) {
               channel = channel.substring(0, channel.length() - 1);
            }

            if (channel.isBlank()) {
               return "";
            }

            if (!channel.contains(":")) {
               channel = channel + ":main";
            } else if (channel.endsWith(":")) {
               channel = channel + "main";
            }
         }

         channel = channel.toLowerCase(Locale.ROOT).replace('\\', '/').replaceAll("[^a-z0-9_./:-]", "_");
         int colon = channel.indexOf(58);
         if (colon < 0) {
            channel = channel + ":main";
         } else if (colon == 0) {
            channel = "minecraft" + channel;
         } else if (colon == channel.length() - 1) {
            channel = channel + "main";
         }

         try {
            AutismPayloadSupport.parseChannelId(channel);
            return channel;
         } catch (IllegalArgumentException ignored) {
            return "";
         }
      } else {
         return "bungeecord:main";
      }
   }

   private void openPayloadEditor(String channel) {
      String target = channel == null ? "" : channel.trim();
      if (!target.isBlank()) {
         ActionEditorOverlay existing = ActionEditorOverlay.getExisting();
         if (existing == null || !existing.init_Z_Str(target)) {
            autismclient.util.macro.PayloadAction action = new autismclient.util.macro.PayloadAction();
            action.channel = target;
            action.payloadDirection = "C2S";
            action.sourceDirection = "C2S";
            action.payloadPhase = "PLAY";
            action.sourceProtocol = "play";
            action.payloadProvenance = "serverInfoChannel";
            ActionEditorOverlay editor = ActionEditorOverlay.getInstance();
            editor.openPayload(action);
            OverlayManager.getInstance().bringToFront(editor);
         }
      }
   }

   private boolean isNoChannel(String detail) {
      if (detail == null) {
         return false;
      }

      String lower = detail.trim().toLowerCase(Locale.ROOT);
      return lower.equals("no channel") || lower.startsWith("no client payload");
   }

   private boolean isUnobservedChannel(String detail) {
      if (detail == null) {
         return false;
      }

      String lower = detail.trim().toLowerCase(Locale.ROOT);
      return lower.startsWith("? ") || lower.contains("not observed") || lower.contains("[probable");
   }

   private int channelRank(String detail) {
      if (detail == null) {
         return 0;
      } else {
         String lower = detail.toLowerCase(Locale.ROOT);
         if (lower.contains("[live")) {
            return 4;
         } else if (lower.contains("[registered")) {
            return 3;
         } else if (lower.contains("[embedded")) {
            return 2;
         } else if (this.isUnobservedChannel(lower)) {
            return 1;
         } else {
            return this.isNoChannel(lower) ? 0 : 3;
         }
      }
   }

   private String serverBrand() {
      if (MC.getConnection() == null) {
         return "--";
      }

      String brand = MC.getConnection().serverBrand();
      return brand != null && !brand.isBlank() ? brand : "--";
   }

   private boolean isOverHeaderUi(float uiMouseX, float uiMouseY) {
      return uiMouseX >= this.overlayX && uiMouseX < this.overlayX + this.overlayWidth && uiMouseY >= this.overlayY && uiMouseY < this.overlayY + this.theme.load();
   }

   private boolean isOverCloseButton(float uiMouseX, float uiMouseY) {
      return autismclient.gui.ui.widgets.WindowHeaderHelper.overClose(
         autismclient.gui.f.NetHelper.init(this.overlayX, this.overlayY, this.overlayWidth, Math.max(this.theme.load(), this.overlayHeight)),
         this.theme.load(),
         uiMouseX,
         uiMouseY
      );
   }

   private int contentPadding() {
      return 4;
   }

   private int contentGap() {
      return 2;
   }

   private int searchHeight() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(13);
   }

   private int buttonHeight() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(13);
   }

   private int buttonRowGap() {
      return 2;
   }

   private int tabBarHeight() {
      return Math.max(autismclient.gui.ui.UiDensityMetrics.scaledHeight(13), this.theme.update());
   }

   private int widestLabel(String... labels) {
      int widest = 0;

      for (String label : labels) {
         widest = Math.max(
            widest,
            autismclient.gui.f.b.k.init(
               this.textRenderer,
               label,
               this.theme.init_Identifier_l(autismclient.gui.f.b.l.write),
               this.theme.load(autismclient.gui.f.b.l.write)
            )
         );
      }

      return widest + 10;
   }

   private int delayLabelWidth() {
      return this.widestLabel("Delay:");
   }

   private int progressLabelWidth() {
      return this.widestLabel("State:", "Sent:", "Replies:", "Retries:", "Failed:", "Found:", "Progress:");
   }

   private int statusLabelWidth() {
      return this.widestLabel("Server:", "Alert:", "Error:");
   }

   private int unusedScaledHeight14() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(14);
   }

   private int unusedScaledHeight16() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(16);
   }

   private int minSearchWidth() {
      return 128;
   }

   private int searchWidthInset() {
      return 28;
   }

   private float minViewportHeight() {
      return this.detailRowHeight() * 4 + 2;
   }

   private int minTabWidth() {
      return 204;
   }

   private int unscannedPluginsMinWidth() {
      return this.minTabWidth();
   }

   private int scanningPluginsMinWidth() {
      return this.minTabWidth();
   }

   private int minInfoTabWidth() {
      return this.minTabWidth();
   }

   private int minInfoTabHeight() {
      return this.theme.load() + this.contentPadding() * 2 + this.tabBarHeight() + this.contentGap() * 2 + 48 + this.buttonHeight();
   }

   private int minPluginsTabWidth() {
      return this.minTabWidth();
   }

   private int minPluginsTabHeight() {
      return this.minInfoTabHeight();
   }

   private int minUnscannedPluginsHeight() {
      return this.minInfoTabHeight();
   }

   private int minScanningPluginsHeight() {
      return this.theme.load() + this.contentPadding() * 2 + 48;
   }

   private int verticalScreenMargin() {
      return 24;
   }

   private int headerRowHeight() {
      return this.detailRowHeight();
   }

   private int baseRowHeight() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(12);
   }

   private int commandRowHeight() {
      return this.detailRowHeight();
   }

   private int unusedZeroMetric() {
      return 0;
   }

   private int k() {
      return 12;
   }

   private int l() {
      return 10;
   }

   private int m() {
      return 3;
   }

   private int headerTextIndent() {
      return 3;
   }

   private int detailRowHeight() {
      return this.baseRowHeight();
   }

   private int visibleViewportHeight(autismclient.gui.ui.layout.VerticalSpacerNode slot) {
      int bodyBottom = this.overlayY + this.overlayHeight - this.contentPadding();
      int rawViewH = Math.max(0, Math.min(Math.round(slot.height()), bodyBottom - Math.round(slot.y())));
      return this.clampViewportHeight(rawViewH);
   }

   private int clampViewportHeight(int rawHeight) {
      return rawHeight <= 0 ? 0 : Math.min(rawHeight, this.innerViewportHeight(rawHeight) + 2);
   }

   private int innerViewportHeight(int viewHeight) {
      return Math.max(0, this.alignHeightToStep(Math.max(0, viewHeight - 2), this.detailRowHeight()));
   }

   private OverlayBounds clampBounds(OverlayBounds bounds) {
      autismclient.gui.ui.layout.LayoutViewport viewport = this.layoutHost.createViewport();
      int viewportWidth = Math.round(viewport.width());
      int viewportHeight = Math.round(viewport.height());
      int margin = 4;
      int availableWidth = Math.max(1, viewportWidth - margin * 2);
      int availableHeight = Math.max(this.theme.load(), viewportHeight - margin * 2);
      int minWidth = Math.min(this.getMinWidth(), availableWidth);
      int width = Math.max(minWidth, Math.min(bounds.width, availableWidth));
      int minHeight = bounds.collapsed ? this.theme.load() : Math.min(this.getMinHeight(), availableHeight);
      int height = Math.max(minHeight, Math.min(bounds.height, availableHeight));
      int renderedHeight = bounds.collapsed ? this.theme.load() : height;
      int x = Math.max(margin, Math.min(bounds.x, Math.max(margin, viewportWidth - margin - width)));
      int y = Math.max(margin, Math.min(bounds.y, Math.max(margin, viewportHeight - margin - renderedHeight)));
      return new OverlayBounds(x, y, width, height, bounds.visible, bounds.collapsed);
   }

   private String guessServerSoftware(ServerData entry) {
      LinkedHashSet<String> guesses = new LinkedHashSet<>();
      String brand = this.serverBrand().toLowerCase(Locale.ROOT);
      if (brand.contains("purpur")) {
         guesses.add("Purpur");
      } else if (brand.contains("pufferfish")) {
         guesses.add("Pufferfish");
      } else if (brand.contains("folia")) {
         guesses.add("Folia");
      } else if (brand.contains("paper")) {
         guesses.add("Paper");
      } else if (brand.contains("spigot")) {
         guesses.add("Spigot");
      } else if (brand.contains("craftbukkit") || brand.contains("bukkit")) {
         guesses.add("Bukkit");
      } else if (brand.contains("waterfall")) {
         guesses.add("Waterfall");
      } else if (brand.contains("velocity")) {
         guesses.add("Velocity");
      } else if (brand.contains("bungeecord") || brand.contains("bungee")) {
         guesses.add("Bungee");
      }

      if (this.detectedPlugins
         .stream()
         .anyMatch(plugin -> "viaversion".equalsIgnoreCase(plugin) || "viabackwards".equalsIgnoreCase(plugin) || "viarewind".equalsIgnoreCase(plugin))) {
         guesses.add("ViaVersion");
      }

      if (this.detectedPlugins.stream().anyMatch(plugin -> "geysermc".equalsIgnoreCase(plugin) || "floodgate".equalsIgnoreCase(plugin))) {
         guesses.add("Bedrock Bridge");
      }

      return guesses.isEmpty() ? "--" : String.join(" + ", guesses);
   }

   private String versionNote(ServerData entry) {
      String brand = this.serverBrand();
      String reportedVersion = this.reportedVersion(entry);
      String brandLower = brand.toLowerCase(Locale.ROOT);
      String versionLower = reportedVersion.toLowerCase(Locale.ROOT);
      if (entry != null && entry.protocol == 0) {
         return "Ping Spoof";
      } else if ("--".equals(brand)
         || !versionLower.contains("paper")
            && !versionLower.contains("spigot")
            && !versionLower.contains("purpur")
            && !versionLower.contains("velocity")
            && !versionLower.contains("bungee")
         || (!brandLower.contains("paper") || versionLower.contains("paper"))
            && (!brandLower.contains("spigot") || versionLower.contains("spigot"))
            && (!brandLower.contains("purpur") || versionLower.contains("purpur"))
            && (!brandLower.contains("velocity") || versionLower.contains("velocity"))
            && (!brandLower.contains("bungee") || versionLower.contains("bungee"))) {
         return this.detectedPlugins
               .stream()
               .anyMatch(
                  plugin -> "viaversion".equalsIgnoreCase(plugin)
                     || "viabackwards".equalsIgnoreCase(plugin)
                     || "viarewind".equalsIgnoreCase(plugin)
                     || "protocolsupport".equalsIgnoreCase(plugin)
               )
            ? "Protocol Bridge"
            : "--";
      } else {
         return "Brand Mismatch";
      }
   }

   private String displayedAddress() {
      ServerData entry = MC.getCurrentServer();
      if (entry != null && entry.ip != null && !entry.ip.isBlank()) {
         return entry.ip.trim();
      }

      if (MC.getConnection() != null && MC.getConnection().getConnection() != null) {
         SocketAddress address = MC.getConnection().getConnection().getRemoteAddress();
         if (address instanceof InetSocketAddress inet) {
            String host = inet.getHostString();
            if ((host == null || host.isBlank()) && inet.getAddress() != null) {
               host = inet.getAddress().getHostAddress();
            }

            if (host != null && !host.isBlank()) {
               return host + ":" + inet.getPort();
            }
         } else if (address != null) {
            String raw = address.toString();
            if (raw != null && !raw.isBlank()) {
               return raw.replaceFirst("^/", "").trim();
            }
         }
      }

      return "--";
   }

   private String hostFromAddress(String rawAddress) {
      if (rawAddress != null && !rawAddress.isBlank() && !"--".equals(rawAddress)) {
         String trimmed = rawAddress.trim();
         if (trimmed.startsWith("[") && trimmed.contains("]")) {
            return trimmed.substring(1, trimmed.indexOf(93));
         }

         int colonCount = 0;

         for (int i = 0; i < trimmed.length(); i++) {
            if (trimmed.charAt(i) == ':') {
               colonCount++;
            }
         }

         return colonCount == 1 && trimmed.contains(":") ? trimmed.substring(0, trimmed.lastIndexOf(58)) : trimmed;
      } else {
         return "";
      }
   }

   private String socketIp() {
      if (MC.getConnection() != null && MC.getConnection().getConnection() != null) {
         if (MC.getConnection().getConnection().getRemoteAddress() instanceof InetSocketAddress inet && inet.getAddress() != null) {
            String hostAddress = inet.getAddress().getHostAddress();
            return hostAddress == null ? "" : hostAddress.trim();
         } else {
            return "";
         }
      } else {
         return "";
      }
   }

   private Integer parsePort(String rawAddress) {
      if (rawAddress != null && !rawAddress.isBlank() && !"--".equals(rawAddress)) {
         String trimmed = rawAddress.trim();
         if (trimmed.startsWith("[") && trimmed.contains("]")) {
            int closing = trimmed.indexOf(93);
            if (closing >= 0 && closing + 1 < trimmed.length() && trimmed.charAt(closing + 1) == ':') {
               try {
                  return Integer.parseInt(trimmed.substring(closing + 2));
               } catch (NumberFormatException ignored) {
                  return null;
               }
            } else {
               return null;
            }
         } else {
            int firstColon = trimmed.indexOf(58);
            int lastColon = trimmed.lastIndexOf(58);
            if (firstColon >= 0 && firstColon == lastColon) {
               try {
                  return Integer.parseInt(trimmed.substring(lastColon + 1));
               } catch (NumberFormatException ignored) {
                  return null;
               }
            } else {
               return null;
            }
         }
      } else {
         return null;
      }
   }

   private String withPort(String host, Integer port) {
      if (host != null && !host.isBlank() && port != null) {
         return host.indexOf(58) >= 0 && !host.startsWith("[") && !host.endsWith("]") ? "[" + host + "]:" + port : host + ":" + port;
      } else {
         return host;
      }
   }

   private Object invokeFirstGetter(Object target, String... names) {
      if (target != null && names != null) {
         for (String name : names) {
            if (name != null && !name.isBlank()) {
               try {
                  Method method = target.getClass().getMethod(name);
                  method.setAccessible(true);
                  return method.invoke(target);
               } catch (ReflectiveOperationException | RuntimeException var8) {
               }
            }
         }

         return null;
      } else {
         return null;
      }
   }

   private String invokeStringGetter(Object target, String... names) {
      Object value = this.invokeFirstGetter(target, names);
      if (value == null) {
         return "";
      } else {
         return value instanceof Component component ? component.getString() : String.valueOf(value);
      }
   }

   private int invokeIntGetter(Object target, int fallback, String... names) {
      Object value = this.invokeFirstGetter(target, names);
      if (value instanceof Number number) {
         return number.intValue();
      } else {
         if (value != null) {
            try {
               return Integer.parseInt(String.valueOf(value).trim());
            } catch (NumberFormatException var7) {
            }
         }

         return fallback;
      }
   }

   private String componentText(Object value) {
      if (value instanceof Component component) {
         return component.getString();
      } else {
         return value == null ? "" : String.valueOf(value);
      }
   }

   private void copyToClipboard(String value, String successMessage, String unavailableMessage) {
      String trimmed = value == null ? "" : value.trim();
      if (!trimmed.isEmpty() && !"--".equals(trimmed) && !"Resolving...".equalsIgnoreCase(trimmed) && !"Failed".equalsIgnoreCase(trimmed)) {
         MC.keyboardHandler.setClipboard(trimmed);
         bi.init(successMessage);
      } else {
         bi.write(unavailableMessage);
      }
   }

   private void copyRealIp() {
      String displayedAddress = this.displayedAddress();
      String realIp = this.resolvedIp(displayedAddress);
      String resolvedWithPort = this.withPort(realIp, this.parsePort(displayedAddress));
      this.copyToClipboard(resolvedWithPort, "Real IP copied.", "Real IP unavailable.");
   }

   private void resolveDnsAsync(String rawAddress) {
      String host = this.hostFromAddress(rawAddress);
      if (!host.isEmpty()) {
         if (!host.equals(this.dnsHost)) {
            this.dnsResult = null;
            this.dnsHost = host;
            this.dnsLookupRunning = this.auditDnsRunning.contains(host);
         }

         if (this.dnsResult == null && this.auditDnsRunning.add(host)) {
            this.dnsLookupRunning = true;
            Thread t = new Thread(() -> {
               String result;
               try {
                  result = InetAddress.getByName(host).getHostAddress();
               } catch (Exception e) {
                  result = "Failed";
               }

               // A lookup for a host the overlay no longer shows must not overwrite the current one.
               if (host.equals(this.dnsHost)) {
                  this.dnsResult = result;
                  this.dnsLookupRunning = false;
               }

               this.auditDnsRunning.remove(host);
            }, "Autism-DNS");
            t.setDaemon(true);
            t.start();
         }
      }
   }

   private String resolvedIp(String rawAddress) {
      this.resolveDnsAsync(rawAddress);
      String socketIp = this.socketIp();
      if (!socketIp.isBlank()) {
         return socketIp;
      } else if (this.dnsResult != null) {
         return this.dnsResult;
      } else {
         return this.dnsLookupRunning ? "Resolving..." : "--";
      }
   }

   private List<String> detectedAnticheats() {
      return this.detectedPlugins
         .stream()
         .filter(Objects::nonNull)
         .filter(p -> ANTICHEAT_PLUGINS.contains(p.toLowerCase(Locale.ROOT)))
         .sorted(String.CASE_INSENSITIVE_ORDER)
         .collect(Collectors.toList());
   }

   private String worldName() {
      if (MC.level == null) {
         return "--";
      }

      Identifier worldId = MC.level.dimension().identifier();
      if (worldId == null) {
         return "--";
      }

      String namespace = worldId.getNamespace();
      String path = worldId.getPath();
      if ("minecraft".equals(namespace)) {
         String var10000;
         switch (path) {
            case "overworld":
               var10000 = "Overworld";
               break;
            case "the_nether":
               var10000 = "The Nether";
               break;
            case "the_end":
               var10000 = "The End";
               break;
            default:
               var10000 = path;
         }

         return var10000;
      } else {
         return namespace + ":" + path;
      }
   }

   private List<AutismServerInfoOverlay.DetailRow> buildDetailRows(List<AutismServerInfoOverlay.DetectedPluginEntry> filteredPlugins, String query) {
      String normalizedQuery = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
      boolean searchActive = !normalizedQuery.isBlank();
      Map<String, List<AutismServerInfoOverlay.DetailRow>> grouped = new LinkedHashMap<>();

      for (String title : List.of(
         "Exact Plugins",
         "Strong Matches",
         "Command Tree",
         "Command Root Hints",
         "Plugin Command Hints",
         "Version Hints",
         "Payload Channels",
         "Command Hints",
         "Other"
      )) {
         grouped.put(title, new ArrayList<>());
      }

      for (AutismServerInfoOverlay.DetectedPluginEntry detail : filteredPlugins) {
         if (detail != null && !detail.displayName.isBlank()) {
            String plugin = detail.displayName;
            String groupTitle = this.groupTitle(
               detail.resultKind, detail.confidence, detail.evidence
            );
            boolean standaloneCommandHint = this.isStandaloneCommandHintEntry(detail);
            grouped.computeIfAbsent(groupTitle, unused -> new ArrayList<>())
               .add(
                  AutismServerInfoOverlay.DetailRow.pluginRow(
                     plugin,
                     detail.resultKind,
                     detail.evidence,
                     detail.confidence,
                     this.baseRowHeight(),
                     standaloneCommandHint ? "" : detail.countLabel,
                     detail.searchText
                  )
               );
            boolean selected = this.samePluginKey(plugin, this.selectedPlugin);
            boolean showDetails = !standaloneCommandHint && (selected || searchActive);
            if (showDetails) {
               for (String command : detail.commands) {
                  if (!searchActive || this.matchesQuery(normalizedQuery, plugin, "/" + command, detail.sourceLabel)) {
                     grouped.get(groupTitle)
                        .add(
                           AutismServerInfoOverlay.DetailRow.detailRow(
                              AutismServerInfoOverlay.DetailRowKind.COMMAND,
                              plugin,
                              "cmd /" + command,
                              "/" + command,
                              detail.evidence,
                              detail.confidence,
                              this.commandRowHeight(),
                              this.buildSearchText(plugin, command, detail.sourceLabel)
                           )
                        );
                  }
               }

               for (String gui : detail.guis) {
                  if (!searchActive || this.matchesQuery(normalizedQuery, plugin, gui, detail.sourceLabel)) {
                     grouped.get(groupTitle)
                        .add(
                           AutismServerInfoOverlay.DetailRow.detailRow(
                              AutismServerInfoOverlay.DetailRowKind.GUI,
                              plugin,
                              "gui " + gui,
                              null,
                              detail.evidence,
                              detail.confidence,
                              this.commandRowHeight(),
                              this.buildSearchText(plugin, gui, detail.sourceLabel)
                           )
                        );
                  }
               }

               for (String channel : detail.channels.isEmpty() ? this.channelsForPlugin(plugin) : detail.channels) {
                  String label = this.channelLabel(channel);
                  if (!searchActive || this.matchesQuery(normalizedQuery, plugin, label, detail.sourceLabel)) {
                     grouped.get(groupTitle)
                        .add(
                           AutismServerInfoOverlay.DetailRow.detailRow(
                              AutismServerInfoOverlay.DetailRowKind.CHANNEL,
                              plugin,
                              "ch " + label,
                              this.channelFromDetail(channel),
                              detail.evidence,
                              detail.confidence,
                              this.commandRowHeight(),
                              this.buildSearchText(plugin, label, detail.sourceLabel)
                           )
                        );
                  }
               }
            }
         }
      }

      List<AutismServerInfoOverlay.DetailRow> rows = new ArrayList<>();

      for (Entry<String, List<AutismServerInfoOverlay.DetailRow>> group : grouped.entrySet()) {
         if (!group.getValue().isEmpty()) {
            group.getValue()
               .sort(
                  (a, b) -> {
                     int byPlugin = String.CASE_INSENSITIVE_ORDER.compare(a.plugin == null ? "" : a.plugin, b.plugin == null ? "" : b.plugin);
                     return byPlugin != 0
                        ? byPlugin
                        : Integer.compare(
                           this.rowKindRank(a.type), this.rowKindRank(b.type)
                        );
                  }
               );
            rows.add(AutismServerInfoOverlay.DetailRow.header(group.getKey(), this.headerRowHeight()));
            rows.addAll(group.getValue());
         }
      }

      return rows;
   }

   private int rowKindRank(AutismServerInfoOverlay.DetailRowKind type) {
      byte var10000;
      switch (type == null ? AutismServerInfoOverlay.DetailRowKind.WHY : type) {
         case HEADER:
            var10000 = 0;
            break;
         case PLUGIN:
            var10000 = 1;
            break;
         case COMMAND:
            var10000 = 2;
            break;
         case GUI:
            var10000 = 3;
            break;
         case CHANNEL:
            var10000 = 4;
            break;
         case WHY:
            var10000 = 5;
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   private boolean isStandaloneCommandHintEntry(AutismServerInfoOverlay.DetectedPluginEntry detail) {
      if (detail != null && detail.evidence == AutismServerInfoOverlay.EvidenceSource.SCANNER_AUTOCOMPLETE && detail.commands.size() == 1) {
         String command = detail.commands.get(0);
         return command != null && detail.displayName.equalsIgnoreCase("/" + command);
      } else {
         return false;
      }
   }

   private boolean matchesQuery(String query, String plugin, String detail, String sourceLabel) {
      return query != null && !query.isBlank() ? this.buildSearchText(plugin, detail, sourceLabel).contains(query) : true;
   }

   private String groupTitle(
      AutismServerInfoOverlay.DetectionType resultKind, AutismServerInfoOverlay.MatchConfidence confidence, AutismServerInfoOverlay.EvidenceSource evidence
   ) {
      if (evidence == AutismServerInfoOverlay.EvidenceSource.SCANNER_AUTOCOMPLETE) {
         return "Command Hints";
      }

      if (evidence == AutismServerInfoOverlay.EvidenceSource.COMMAND_TREE) {
         return "Command Tree";
      }

      if (evidence == AutismServerInfoOverlay.EvidenceSource.ROOT_HINT) {
         return "Command Root Hints";
      }

      if (evidence == AutismServerInfoOverlay.EvidenceSource.PLUGIN_LIST) {
         return "Plugin Command Hints";
      }

      if (evidence == AutismServerInfoOverlay.EvidenceSource.VERSION_HINT) {
         return "Version Hints";
      }

      if (evidence == AutismServerInfoOverlay.EvidenceSource.PAYLOAD_CHANNEL) {
         return "Payload Channels";
      }

      if (resultKind != AutismServerInfoOverlay.DetectionType.FEATURE && !this.isIndirectEvidence(evidence)) {
         String var10000;
         switch (confidence == null ? AutismServerInfoOverlay.MatchConfidence.UNKNOWN : confidence) {
            case EXACT:
               var10000 = "Exact Plugins";
               break;
            case STRONG:
               var10000 = "Strong Matches";
               break;
            case FEATURE:
               var10000 = "Other";
               break;
            case UNKNOWN:
               var10000 = "Other";
               break;
            default:
               throw new MatchException(null, null);
         }

         return var10000;
      } else {
         return "Command Hints";
      }
   }

   private String evidenceLabel(
      AutismServerInfoOverlay.DetectionType resultKind, AutismServerInfoOverlay.EvidenceSource evidence, AutismServerInfoOverlay.MatchConfidence confidence
   ) {
      if (resultKind == AutismServerInfoOverlay.DetectionType.FEATURE) {
         return "feature command";
      }

      String var10000;
      switch (evidence == null ? AutismServerInfoOverlay.EvidenceSource.UNKNOWN : evidence) {
         case PAYLOAD_CHANNEL:
            var10000 = "payload channel";
            break;
         case COMMAND_GUI:
            var10000 = "command and gui";
            break;
         case COMMAND_TREE:
            var10000 = "command tree";
            break;
         case USER_AUTOCOMPLETE:
            var10000 = "user autocomplete";
            break;
         case SCANNER_AUTOCOMPLETE:
            var10000 = "command hint";
            break;
         case NAMESPACE:
            var10000 = "namespace";
            break;
         case ROOT_HINT:
            var10000 = "root hint";
            break;
         case HELP_HINT:
            var10000 = "help hint";
            break;
         case PLUGIN_LIST:
            var10000 = "plugin command hint";
            break;
         case VERSION_HINT:
            var10000 = "version hint";
            break;
         case FEATURE:
            var10000 = "feature command";
            break;
         case UNKNOWN:
            var10000 = confidence == AutismServerInfoOverlay.MatchConfidence.UNKNOWN ? "unknown" : "weak hint";
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   private List<String> groupOrder() {
      return List.of("Command Tree", "Command Root Hint", "Plugin Command Hint", "Version Command Hint", "Payload Channel");
   }

   private String groupTitleFor(AutismServerInfoOverlay.DetectedPluginEntry detail) {
      if (detail == null) {
         return "Other";
      }

      AutismServerInfoOverlay.EvidenceSource evidence = detail.copyEvidence == null
         ? AutismServerInfoOverlay.EvidenceSource.UNKNOWN
         : detail.copyEvidence;
      String var10000;
      switch (evidence) {
         case PAYLOAD_CHANNEL:
            var10000 = "Payload Channel";
            break;
         case COMMAND_GUI:
         case USER_AUTOCOMPLETE:
         case FEATURE:
            var10000 = "Other";
            break;
         case COMMAND_TREE:
            var10000 = "Command Tree";
            break;
         case SCANNER_AUTOCOMPLETE:
         case HELP_HINT:
         case UNKNOWN:
            var10000 = "Other";
            break;
         case NAMESPACE:
         case ROOT_HINT:
            var10000 = "Command Root Hint";
            break;
         case PLUGIN_LIST:
            var10000 = "Plugin Command Hint";
            break;
         case VERSION_HINT:
            var10000 = "Version Command Hint";
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   private int groupOrderIndex(String title) {
      List<String> order = this.groupOrder();
      int index = order.indexOf(title);
      return index < 0 ? order.size() : index;
   }

   private String detailSearchText(
      String plugin, List<String> commands, List<String> guis, List<String> channels, String sourceLabel, AutismServerInfoOverlay.MatchConfidence confidence
   ) {
      StringBuilder sb = new StringBuilder();
      sb.append(plugin == null ? "" : plugin)
         .append(' ')
         .append(this.confidenceLabel(confidence))
         .append(' ')
         .append(sourceLabel == null ? "" : sourceLabel);

      for (String command : commands) {
         sb.append(' ').append(command);
      }

      for (String gui : guis) {
         sb.append(' ').append(gui);
      }

      for (String channel : channels) {
         sb.append(' ').append(this.channelLabel(channel));
      }

      return sb.toString().toLowerCase(Locale.ROOT);
   }

   private String buildSearchText(String... parts) {
      StringBuilder sb = new StringBuilder();
      if (parts != null) {
         for (String part : parts) {
            if (part != null && !part.isBlank()) {
               if (!sb.isEmpty()) {
                  sb.append(' ');
               }

               sb.append(part);
            }
         }
      }

      return sb.toString().toLowerCase(Locale.ROOT);
   }

   private List<String> uniquePlugins(Collection<String> plugins) {
      if (plugins != null && !plugins.isEmpty()) {
         Map<String, String> unique = new LinkedHashMap<>();

         for (String plugin : plugins) {
            if (plugin != null && !plugin.isBlank()) {
               String clean = plugin.trim();
               String key = this.pluginKey(clean);
               if (!key.isBlank()) {
                  String existing = unique.get(key);
                  if (existing == null || this.isBetterPluginName(clean, existing)) {
                     unique.put(key, clean);
                  }
               }
            }
         }

         List<String> out = new ArrayList<>(unique.values());
         out.sort(String.CASE_INSENSITIVE_ORDER);
         return out;
      } else {
         return List.of();
      }
   }

   private boolean samePluginKey(String a, String b) {
      return a != null && b != null ? this.pluginKey(a).equals(this.pluginKey(b)) : false;
   }

   private boolean isDetected(String plugin) {
      if (plugin != null && !plugin.isBlank()) {
         String key = this.pluginKey(plugin);

         for (String detected : this.detectedPlugins) {
            if (detected != null && this.pluginKey(detected).equals(key)) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private Collection<AutismServerInfoOverlay.DetectedPluginEntry> rebuildPluginDetails() {
      if (this.builtDetailsRevision == this.detailsRevision) {
         return this.detectedPluginDetails.values();
      }

      Map<String, AutismServerInfoOverlay.NamespaceSummary> builders = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

      for (String plugin : this.uniquePlugins((Collection<String>)this.detectedPlugins)) {
         String key = this.pluginKey(plugin);
         if (!key.isBlank()) {
            AutismServerInfoOverlay.NamespaceSummary builder = builders.computeIfAbsent(key, unused -> new AutismServerInfoOverlay.NamespaceSummary());
            if (builder.name == null || builder.name.isBlank() || this.isBetterPluginName(plugin, builder.name)) {
               builder.name = plugin.trim();
            }
         }
      }

      this.mergeCommands(builders, this.pluginCommands);
      this.mergeCopyCommands(builders, this.pluginCopyCommands);
      this.mergeGuis(builders, this.pluginGuis);
      this.mergeNamespaces(builders, this.pluginChannels);
      Map<String, AutismServerInfoOverlay.DetectedPluginEntry> details = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

      for (Entry<String, AutismServerInfoOverlay.NamespaceSummary> entry : builders.entrySet()) {
         String key = entry.getKey();
         AutismServerInfoOverlay.NamespaceSummary builder = entry.getValue();
         if (builder != null) {
            String displayName = builder.name != null && !builder.name.isBlank() ? builder.name.trim() : key;
            List<String> commands = List.copyOf(builder.commands);
            List<String> copyCommands = builder.copyCommandSet.isEmpty() ? commands : List.copyOf(builder.copyCommandSet);
            List<String> guis = List.copyOf(builder.guis);
            List<String> channels = this.distinctRows(builder.channels);
            AutismServerInfoOverlay.EvidenceSource evidence = this.pluginEvidence
               .getOrDefault(key, commands.isEmpty() ? AutismServerInfoOverlay.EvidenceSource.UNKNOWN : AutismServerInfoOverlay.EvidenceSource.ROOT_HINT);
            AutismServerInfoOverlay.EvidenceSource copyEvidence = this.pluginCopyEvidence
               .getOrDefault(key, this.copyEvidenceFor(displayName, evidence));
            AutismServerInfoOverlay.MatchConfidence confidence = this.pluginConfidence.getOrDefault(key, this.confidenceForEvidence(evidence));
            AutismServerInfoOverlay.DetectionType resultKind = this.detectionTypeFor(displayName, evidence, confidence);
            String sourceLabel = this.evidenceLabel(resultKind, evidence, confidence);
            String countLabel = this.countSummary(commands, guis, channels);
            String searchText = this.detailSearchText(displayName, commands, guis, channels, sourceLabel, confidence);
            details.put(
               key,
               new AutismServerInfoOverlay.DetectedPluginEntry(
                  displayName, key, commands, guis, channels, resultKind, evidence, copyEvidence, confidence, sourceLabel, countLabel, copyCommands, searchText
               )
            );
         }
      }

      this.detectedPluginDetails = Collections.unmodifiableMap(new TreeMap<>(details));
      this.builtDetailsRevision = this.detailsRevision;
      return this.detectedPluginDetails.values();
   }

   private AutismServerInfoOverlay.DetectedPluginEntry detailFor(String plugin) {
      if (plugin != null && !plugin.isBlank()) {
         this.rebuildPluginDetails();
         return this.detectedPluginDetails.get(this.pluginKey(plugin));
      } else {
         return null;
      }
   }

   private void mergeCommands(Map<String, AutismServerInfoOverlay.NamespaceSummary> builders, Map<String, List<String>> source) {
      if (source != null && !source.isEmpty()) {
         for (Entry<String, List<String>> entry : source.entrySet()) {
            String key = this.pluginKey(entry.getKey());
            if (!key.isBlank()) {
               AutismServerInfoOverlay.NamespaceSummary builder = builders.computeIfAbsent(key, unused -> new AutismServerInfoOverlay.NamespaceSummary());
               if ((builder.name == null || builder.name.isBlank()) && entry.getKey() != null) {
                  builder.name = entry.getKey().trim();
               }

               if (entry.getValue() != null) {
                  for (String command : entry.getValue()) {
                     if (command != null && !command.isBlank()) {
                        builder.commands.add(command.trim());
                     }
                  }
               }
            }
         }
      }
   }

   private void mergeCopyCommands(Map<String, AutismServerInfoOverlay.NamespaceSummary> builders, Map<String, List<String>> source) {
      if (source != null && !source.isEmpty()) {
         for (Entry<String, List<String>> entry : source.entrySet()) {
            String key = this.pluginKey(entry.getKey());
            if (!key.isBlank()) {
               AutismServerInfoOverlay.NamespaceSummary builder = builders.computeIfAbsent(key, unused -> new AutismServerInfoOverlay.NamespaceSummary());
               if ((builder.name == null || builder.name.isBlank()) && entry.getKey() != null) {
                  builder.name = entry.getKey().trim();
               }

               if (entry.getValue() != null) {
                  for (String command : entry.getValue()) {
                     if (command != null && !command.isBlank()) {
                        builder.copyCommandSet.add(command.trim());
                     }
                  }
               }
            }
         }
      }
   }

   private void mergeGuis(Map<String, AutismServerInfoOverlay.NamespaceSummary> builders, Map<String, List<String>> source) {
      if (source != null && !source.isEmpty()) {
         for (Entry<String, List<String>> entry : source.entrySet()) {
            String key = this.pluginKey(entry.getKey());
            if (!key.isBlank()) {
               AutismServerInfoOverlay.NamespaceSummary builder = builders.computeIfAbsent(key, unused -> new AutismServerInfoOverlay.NamespaceSummary());
               if ((builder.name == null || builder.name.isBlank()) && entry.getKey() != null) {
                  builder.name = entry.getKey().trim();
               }

               if (entry.getValue() != null) {
                  for (String gui : entry.getValue()) {
                     if (gui != null && !gui.isBlank()) {
                        builder.guis.add(gui.trim());
                     }
                  }
               }
            }
         }
      }
   }

   private void mergeNamespaces(Map<String, AutismServerInfoOverlay.NamespaceSummary> builders, Map<String, List<String>> source) {
      if (source != null && !source.isEmpty()) {
         for (Entry<String, List<String>> entry : source.entrySet()) {
            String key = this.pluginKey(entry.getKey());
            if (!key.isBlank()) {
               AutismServerInfoOverlay.NamespaceSummary builder = builders.computeIfAbsent(key, unused -> new AutismServerInfoOverlay.NamespaceSummary());
               if ((builder.name == null || builder.name.isBlank()) && entry.getKey() != null) {
                  builder.name = entry.getKey().trim();
               }

               if (entry.getValue() != null) {
                  builder.channels.addAll(entry.getValue());
               }
            }
         }
      }
   }

   private List<String> commandsFor(String plugin) {
      AutismServerInfoOverlay.DetectedPluginEntry detail = this.detailFor(plugin);
      return detail == null ? List.of() : detail.commands;
   }

   private List<String> guisFor(String plugin) {
      AutismServerInfoOverlay.DetectedPluginEntry detail = this.detailFor(plugin);
      return detail == null ? List.of() : detail.guis;
   }

   private List<String> observedChannelsFor(String plugin) {
      AutismServerInfoOverlay.DetectedPluginEntry detail = this.detailFor(plugin);
      return detail == null ? List.of() : detail.channels;
   }

   private List<AutismServerInfoOverlay.DetailRow> detailRowsFor(String queryText, int rowContentWidth) {
      String query = queryText == null ? "" : queryText.trim().toLowerCase(Locale.ROOT);
      String selectedKey = this.selectedPlugin == null ? "" : this.pluginKey(this.selectedPlugin);
      if (this.builtDetailRowsRevision == this.detailRowsRevision && Objects.equals(this.detailRowsQuery, query) && Objects.equals(this.detailRowsSelectedKey, selectedKey) && this.detailRowsContentWidth == rowContentWidth) {
         return this.detailRows;
      }

      List<AutismServerInfoOverlay.DetectedPluginEntry> filtered = new ArrayList<>();

      for (AutismServerInfoOverlay.DetectedPluginEntry detail : this.rebuildPluginDetails()) {
         if (detail != null && (query.isEmpty() || detail.searchText.contains(query))) {
            filtered.add(detail);
         }
      }

      this.setDetailRows(this.buildDetailRows(filtered, query));
      this.detailRowsQuery = query;
      this.detailRowsSelectedKey = selectedKey;
      this.detailRowsContentWidth = rowContentWidth;
      this.builtDetailRowsRevision = this.detailRowsRevision;
      return this.detailRows;
   }

   private boolean pluginMatchesQuery(String plugin, String query) {
      if (plugin != null && query != null && !query.isBlank()) {
         AutismServerInfoOverlay.DetectedPluginEntry detail = this.detailFor(plugin);
         return detail != null && detail.searchText.contains(query);
      } else {
         return false;
      }
   }

   private void invalidateDetails() {
      this.detailsRevision++;
      this.detailRowsRevision++;
      this.builtDetailRowsRevision = -1;
      this.detailRowsQuery = null;
      this.detailRowsSelectedKey = null;
      this.detailRowsContentWidth = -1;
      this.detailRows = List.of();
      this.detailRowOffsets = new int[0];
      this.detailRowsHeight = 0;
      this.builtDetailsRevision = -1;
      this.detectedPluginDetails = Map.of();
      this.requestLayoutRebuild();
   }

   private void invalidateDetailRows() {
      this.detailRowsRevision++;
      this.builtDetailRowsRevision = -1;
      this.detailRowsQuery = null;
      this.detailRowsSelectedKey = null;
      this.detailRowsContentWidth = -1;
      this.detailRows = List.of();
      this.detailRowOffsets = new int[0];
      this.detailRowsHeight = 0;
      this.requestLayoutRebuild();
   }

   private int totalRowsHeight(List<AutismServerInfoOverlay.DetailRow> rows) {
      if (rows == this.detailRows) {
         return this.detailRowsHeight;
      }

      int total = 0;

      for (AutismServerInfoOverlay.DetailRow row : rows) {
         if (row != null) {
            total += row.height;
         }
      }

      return total;
   }

   private void setDetailRows(List<AutismServerInfoOverlay.DetailRow> rows) {
      List<AutismServerInfoOverlay.DetailRow> safeRows = rows == null ? List.of() : List.copyOf(rows);
      int[] offsets = new int[safeRows.size()];
      int cursor = 0;

      for (int i = 0; i < safeRows.size(); i++) {
         offsets[i] = cursor;
         AutismServerInfoOverlay.DetailRow row = safeRows.get(i);
         cursor += row == null ? 0 : row.height;
      }

      this.detailRows = safeRows;
      this.detailRowOffsets = offsets;
      this.detailRowsHeight = cursor;
   }

   private int rowOffset(int index) {
      return index >= 0 && index < this.detailRowOffsets.length ? this.detailRowOffsets[index] : 0;
   }

   private int firstVisibleRow(List<AutismServerInfoOverlay.DetailRow> rows, int scrollOffset) {
      if (rows != null && !rows.isEmpty() && this.detailRowOffsets.length != 0 && scrollOffset > 0) {
         int idx = Arrays.binarySearch(this.detailRowOffsets, scrollOffset);
         if (idx < 0) {
            idx = Math.max(0, -idx - 2);
         }

         while (idx > 0) {
            AutismServerInfoOverlay.DetailRow row = rows.get(idx);
            int rowBottom = this.detailRowOffsets[idx] + (row == null ? 0 : row.height);
            if (rowBottom <= scrollOffset) {
               break;
            }

            idx--;
         }

         while (idx < rows.size()) {
            AutismServerInfoOverlay.DetailRow row = rows.get(idx);
            int rowBottom = this.detailRowOffsets[idx] + (row == null ? 0 : row.height);
            if (rowBottom > scrollOffset) {
               break;
            }

            idx++;
         }

         return Math.max(0, Math.min(idx, rows.size() - 1));
      } else {
         return 0;
      }
   }

   @Override
   public boolean mouseClicked(double mx, double my, int button) {
      if (!this.overlayVisible) {
         return false;
      }

      try {
         return this.handleMouseClicked(mx, my, button);
      } catch (Throwable t) {
         this.reportStageFailure("mouseClicked", t);
         return false;
      }
   }

   private boolean handleMouseClicked(double mx, double my, int button) {
      autismclient.gui.ui.layout.LayoutViewport viewport = this.layoutHost.createViewport();
      float uiMouseX = viewport.toUiX(mx);
      float uiMouseY = viewport.toUiY(my);
      if (this.isOverCloseButton(uiMouseX, uiMouseY)) {
         this.setVisible(false);
         this.dragging = false;
         this.draggingScrollbar = false;
         this.dragMoved = false;
         return true;
      }

      if (button == 0 && this.isOverHeaderUi(uiMouseX, uiMouseY)) {
         this.dragging = true;
         this.dragMoved = false;
         this.dragOffsetX = uiMouseX - this.overlayX;
         this.dragOffsetY = uiMouseY - this.overlayY;
         this.dragStartMouseX = uiMouseX;
         this.dragStartMouseY = uiMouseY;
         this.dragStartX = this.overlayX;
         this.dragStartY = this.overlayY;
         return true;
      }

      if (this.overlayCollapsed) {
         return false;
      }

      if (button != 0) {
         return this.containsPoint(mx, my);
      }

      if (this.activeTab == 1 && this.scanDone && !this.tick_Z) {
         autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics scrollbarMetrics = this.scrollbarMetrics(
            Math.round(this.listViewportSpacer.x()),
            Math.round(this.listViewportSpacer.y()),
            Math.round(this.listViewportSpacer.width()),
            this.visibleViewportHeight(this.listViewportSpacer)
         );
         if (scrollbarMetrics.isScrollable() && scrollbarMetrics.containsTrack(uiMouseX, uiMouseY)) {
            this.draggingScrollbar = true;
            this.scrollbarGrabOffset = Math.max(0, Math.round(uiMouseY) - scrollbarMetrics.thumbY());
            this.scroll.dragToMouseYStepped(scrollbarMetrics, Math.round(uiMouseY), this.scrollbarGrabOffset, this.detailRowHeight());
            this.saveState();
            return true;
         }
      }

      if (this.activeTab == 1 && this.scanDone && !this.tick_Z) {
         for (AutismServerInfoOverlay.ClickHotspot region : this.clickHotspots) {
            if (region.contains(uiMouseX, uiMouseY)) {
               this.layoutHost.clearFocus();
               region.action.run();
               return true;
            }
         }
      }

      if (this.layoutHost.mouseClicked(mx, my, button)) {
         return true;
      }

      this.layoutHost.clearFocus();
      return this.containsPoint(mx, my);
   }

   @Override
   public boolean mouseReleased(double mx, double my, int button) {
      if (button == 0 && this.draggingScrollbar) {
         this.draggingScrollbar = false;
         this.saveState();
         return true;
      } else if (this.dragging) {
         this.dragging = false;
         this.saveState();
         return true;
      } else {
         return this.layoutHost.mouseReleased(mx, my, button);
      }
   }

   @Override
   public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
      if (this.draggingScrollbar && button == 0) {
         autismclient.gui.ui.layout.LayoutViewport viewport = this.layoutHost.createViewport();
         float uiMouseY = viewport.toUiY(my);
         autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics scrollbarMetrics = this.scrollbarMetrics(
            Math.round(this.listViewportSpacer.x()),
            Math.round(this.listViewportSpacer.y()),
            Math.round(this.listViewportSpacer.width()),
            this.visibleViewportHeight(this.listViewportSpacer)
         );
         this.scroll.dragToMouseYStepped(scrollbarMetrics, Math.round(uiMouseY), this.scrollbarGrabOffset, this.detailRowHeight());
         return true;
      }

      if (!this.dragging) {
         return this.layoutHost.mouseDragged(mx, my, button, dx, dy);
      }

      autismclient.gui.ui.layout.LayoutViewport viewport = this.layoutHost.createViewport();
      float uiMouseX = viewport.toUiX(mx);
      float uiMouseY = viewport.toUiY(my);
      OverlayBounds clamped = this.clampBounds(
         new OverlayBounds(Math.round(uiMouseX - (float)this.dragOffsetX), Math.round(uiMouseY - (float)this.dragOffsetY), this.overlayWidth, this.overlayHeight, this.overlayVisible, this.overlayCollapsed)
      );
      this.overlayX = clamped.x;
      this.overlayY = clamped.y;
      this.dragMoved = this.dragMoved
         || Math.abs(uiMouseX - this.dragStartMouseX) >= 3.0F
         || Math.abs(uiMouseY - this.dragStartMouseY) >= 3.0F
         || this.overlayX != this.dragStartX
         || this.overlayY != this.dragStartY;
      return true;
   }

   @Override
   public boolean mouseScrolled(double mx, double my, double amount) {
      if (this.overlayVisible && !this.overlayCollapsed && this.containsPoint(mx, my)) {
         try {
            return this.scrollDetails(mx, my, amount);
         } catch (Throwable t) {
            this.reportStageFailure("mouseScrolled", t);
            return false;
         }
      } else {
         return false;
      }
   }

   private boolean scrollDetails(double mx, double my, double amount) {
      if (this.activeTab == 1 && this.scanDone && !this.tick_Z) {
         int viewH = this.visibleViewportHeight(this.listViewportSpacer);
         int maxScroll = Math.max(0, this.contentHeight - this.innerViewportHeight(viewH));
         this.scroll.scrollByWheel(amount, this.detailRowHeight(), maxScroll);
         return true;
      } else {
         return this.layoutHost.mouseScrolled(mx, my, amount);
      }
   }

   @Override
   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      try {
         return this.overlayVisible && !this.overlayCollapsed && this.layoutHost.keyPressed(keyCode, scanCode, modifiers);
      } catch (Throwable t) {
         this.reportStageFailure("keyPressed", t);
         return false;
      }
   }

   @Override
   public boolean charTyped(char chr, int modifiers) {
      try {
         return this.overlayVisible && !this.overlayCollapsed && this.layoutHost.charTyped(chr, modifiers);
      } catch (Throwable t) {
         this.reportStageFailure("charTyped", t);
         return false;
      }
   }

   private void copyServerInfo() {
      StringBuilder sb = new StringBuilder();
      ServerData entry = MC.getCurrentServer();
      String ip = this.displayedAddress();
      String realIp = this.resolvedIp(ip);
      List<String> detectedAcs = this.detectedAnticheats();
      String software = this.guessServerSoftware(entry);
      String versionNote = this.versionNote(entry);
      sb.append("=== Server Info ===\n");
      sb.append("IP:         ").append(ip).append("\n");
      if (!realIp.equals("--")) {
         sb.append("Real IP:    ").append(realIp).append("\n");
      }

      sb.append("Version:    ").append(this.reportedVersion(entry)).append("\n");
      sb.append("RealVersion: ").append(this.detectedVersion()).append("\n");
      sb.append("Brand:      ").append(this.serverBrand()).append("\n");
      if (!"--".equals(software)) {
         sb.append("Software:   ").append(software).append("\n");
      }

      if (MC.getConnection() != null) {
         sb.append("Players:    ").append(MC.getConnection().getListedOnlinePlayers().size());
         if (entry != null && entry.players != null) {
            sb.append(" / ").append(entry.players.max());
         }

         sb.append("\n");
      } else {
         sb.append("Players:    --\n");
      }

      sb.append("Protocol:   ").append(entry != null ? entry.protocol : "--").append("\n");
      if (!"--".equals(versionNote)) {
         sb.append("VersionNote: ").append(versionNote).append("\n");
      }

      sb.append("Difficulty: ").append(MC.level != null ? MC.level.getDifficulty().getDisplayName().getString() : "--").append("\n");
      sb.append("World:      ").append(this.worldName()).append("\n");
      if (MC.level != null) {
         long dayCount = MC.level.getOverworldClockTime() / 24000L;
         long timeOfDay = MC.level.getOverworldClockTime() % 24000L;
         int hours = (int)((timeOfDay / 1000L + 6L) % 24L);
         int minutes = (int)(timeOfDay % 1000L * 60L / 1000L);
         sb.append("Time:       Day ").append(dayCount).append(" (").append(String.format("%02d:%02d", hours, minutes)).append(")\n");
      } else {
         sb.append("Time:       --\n");
      }

      if (this.tick_Z) {
         sb.append("AntiCheats: Scanning...\n");
      } else if (!this.scanDone) {
         sb.append("AntiCheats: Probe Plugins First\n");
      } else if (detectedAcs.isEmpty()) {
         sb.append("AntiCheats: None detected\n");
      } else {
         sb.append("AntiCheats: ").append(String.join(", ", detectedAcs)).append("\n");
      }

      MC.keyboardHandler.setClipboard(sb.toString());
      bi.init("Server info copied.");
   }

   private void copyPlugins() {
      if (this.detectedPlugins.isEmpty()) {
         bi.write("No plugins detected.");
      } else {
         Map<String, List<AutismServerInfoOverlay.DetectedPluginEntry>> grouped = new TreeMap<>((a, b) -> {
            int rank = Integer.compare(this.groupOrderIndex(a), this.groupOrderIndex(b));
            return rank != 0 ? rank : String.CASE_INSENSITIVE_ORDER.compare(a, b);
         });
         int copyCount = 0;

         for (String plugin : this.uniquePlugins((Collection<String>)this.detectedPlugins)) {
            if (plugin != null && !plugin.isBlank()) {
               AutismServerInfoOverlay.DetectedPluginEntry detail = this.detailFor(plugin);
               if (detail != null
                  && detail.copyEvidence != AutismServerInfoOverlay.EvidenceSource.HELP_HINT
                  && detail.copyEvidence != AutismServerInfoOverlay.EvidenceSource.UNKNOWN) {
                  String uiGroupTitle = this.groupTitle(
                     detail.resultKind, detail.confidence, detail.evidence
                  );
                  if (!"Command Hints".equals(uiGroupTitle)
                     && !"Other".equals(uiGroupTitle)
                     && (
                        detail.resultKind != AutismServerInfoOverlay.DetectionType.FEATURE
                           || detail.copyEvidence == AutismServerInfoOverlay.EvidenceSource.COMMAND_GUI
                     )) {
                     grouped.computeIfAbsent(this.groupTitleFor(detail), unused -> new ArrayList<>()).add(detail);
                     copyCount++;
                  }
               }
            }
         }

         if (copyCount <= 0) {
            bi.write("No non-hint plugins to copy.");
         } else {
            StringBuilder sb = new StringBuilder();
            sb.append("Plugins (").append(copyCount).append("):\n");

            for (Entry<String, List<AutismServerInfoOverlay.DetectedPluginEntry>> group : grouped.entrySet()) {
               List<AutismServerInfoOverlay.DetectedPluginEntry> details = group.getValue();
               if (details != null && !details.isEmpty()) {
                  details.sort(Comparator.comparing(detailx -> detailx.displayName, String.CASE_INSENSITIVE_ORDER));
                  sb.append("\n[").append(group.getKey()).append("]\n");

                  for (AutismServerInfoOverlay.DetectedPluginEntry detail : details) {
                     boolean ac = ANTICHEAT_PLUGINS.contains(detail.key.toLowerCase(Locale.ROOT));
                     String groupTitle = this.groupTitleFor(detail);
                     sb.append(ac ? "[AC] " : "- ").append(detail.displayName);
                     if (!detail.copyCommands.isEmpty()
                        && !"Plugin Command Hint".equals(groupTitle)
                        && !"Version Command Hint".equals(groupTitle)
                        && !"Payload Channel".equals(groupTitle)) {
                        sb.append(" (").append(detail.copyCommands.size()).append(" cmds)");
                     }

                     sb.append("\n");
                  }
               }
            }

            MC.keyboardHandler.setClipboard(sb.toString());
            bi.init("Plugin list copied.");
         }
      }
   }

   private static final class ProbeObservation {
      final long generation;
      final boolean liveObservation;
      final AutismServerInfoOverlay.ProbeSpec probe;
      final AutismServerInfoOverlay.ProbeSpec observed;
      final List<String> tokens;

      ProbeObservation(
         long generation,
         boolean liveObservation,
         AutismServerInfoOverlay.ProbeSpec probe,
         AutismServerInfoOverlay.ProbeSpec observed,
         List<String> tokens
      ) {
         this.generation = generation;
         this.liveObservation = liveObservation;
         this.probe = probe;
         this.observed = observed;
         this.tokens = tokens;
      }
   }

   private enum ScanStatus {
      NONE,
      COMPLETE,
      PARTIAL,
      FAILED;
   }

   private static final class CommandEvidence {
      final String command;
      final AutismServerInfoOverlay.EvidenceSource evidence;
      final String guiTitle;

      CommandEvidence(String command, AutismServerInfoOverlay.EvidenceSource evidence, String guiTitle) {
         this.command = command;
         this.evidence = evidence;
         this.guiTitle = guiTitle;
      }
   }

   private enum MatchConfidence {
      EXACT,
      STRONG,
      FEATURE,
      UNKNOWN;
   }

   private static class ClickHotspot {
      final int x;
      final int y;
      final int width;
      final int height;
      final Runnable action;

      ClickHotspot(int x, int y, int width, int height, Runnable action) {
         this.x = x;
         this.y = y;
         this.width = width;
         this.height = height;
         this.action = action;
      }

      boolean contains(double mx, double my) {
         return mx >= this.x && mx < this.x + this.width && my >= this.y && my < this.y + this.height;
      }
   }

   private enum DetectionType {
      PLUGIN,
      FEATURE;
   }

   private enum DetailRowKind {
      HEADER,
      PLUGIN,
      COMMAND,
      GUI,
      CHANNEL,
      WHY;
   }

   private static final class ProgressBarNode extends autismclient.gui.ui.layout.LayoutNode {
      private float progress = 0.0F;

      ProgressBarNode() {
         this.nodeHeight = 12.0F;
      }

      AutismServerInfoOverlay.ProgressBarNode setProgress(float progress) {
         this.progress = Math.max(0.0F, Math.min(1.0F, progress));
         return this;
      }

      @Override
      public float measureHeight(autismclient.gui.ui.layout.LayoutContext context, float availableWidth) {
         return 12.0F;
      }

      @Override
      public void render(autismclient.gui.ui.layout.LayoutContext context) {
         if (this.nodeVisible) {
            autismclient.gui.ui.widgets.ProgressBarRenderer.draw(
               autismclient.gui.f.UiHelper.init(
                  context.graphics(), context.font(), Math.round(context.mouseX()), Math.round(context.mouseY())
               ),
               autismclient.gui.f.NetHelper.init(Math.round(this.nodeX), Math.round(this.nodeY), Math.round(this.nodeWidth), Math.round(this.nodeHeight)),
               this.progress
            );
         }
      }
   }

   private enum EvidenceSource {
      PAYLOAD_CHANNEL,
      COMMAND_GUI,
      COMMAND_TREE,
      USER_AUTOCOMPLETE,
      SCANNER_AUTOCOMPLETE,
      NAMESPACE,
      ROOT_HINT,
      HELP_HINT,
      PLUGIN_LIST,
      VERSION_HINT,
      FEATURE,
      UNKNOWN;
   }

   private static final class NamespaceSummary {
      String name;
      final Set<String> commands = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
      final Set<String> copyCommandSet = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
      final Set<String> guis = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
      final List<String> channels = new ArrayList<>();
   }

   private static final class ActiveScanSession {
      final long generation;
      final String serverAddress;
      final ServerScanAttemptTracker attemptTracker;
      String contextSignature;
      final long startedAt;
      final Map<String, AutismServerInfoOverlay.PluginAccumulator> previousEntries;
      long lastResponseAt;
      long nextProbeAt;
      final Set<Integer> pendingProbeIds = new HashSet<>();
      final Map<Integer, AutismServerInfoOverlay.ProbeSpec> probesById = new HashMap<>();
      final Deque<AutismServerInfoOverlay.QueuedProbe> probeQueue = new ArrayDeque<>();
      int totalProbes;
      int sentCount;
      int replyCount;
      int retryCount;
      int failedCount;
      int processingFailures;
      int rootCount;
      int suggestionReplies;
      int suggestionEntries;

      ActiveScanSession(
         long generation, String serverAddress, String contextSignature, long startedAt, Map<String, AutismServerInfoOverlay.PluginAccumulator> previousEntries
      ) {
         this.generation = generation;
         this.serverAddress = serverAddress == null ? "" : serverAddress;
         this.attemptTracker = new ServerScanAttemptTracker(generation, this.serverAddress);
         this.contextSignature = contextSignature == null ? "" : contextSignature;
         this.startedAt = startedAt;
         this.previousEntries = previousEntries == null ? Map.of() : Collections.unmodifiableMap(new TreeMap<>(previousEntries));
         this.lastResponseAt = startedAt;
         this.nextProbeAt = startedAt;
      }

      boolean matches(long expectedGeneration, String address) {
         return this.attemptTracker.init(expectedGeneration, address);
      }
   }

   private static final class ProbeSpec {
      final String query;
      final AutismServerInfoOverlay.ProbeKind kind;
      final String pluginHint;

      ProbeSpec(String query, AutismServerInfoOverlay.ProbeKind kind, String hint) {
         this.query = query;
         this.kind = kind;
         this.pluginHint = hint;
      }
   }

   private static final class TabBarNode extends autismclient.gui.ui.layout.LayoutNode {
      private static final int init_I = 2;
      private String[] tabs = new String[0];
      private int activeIndex = 0;
      private IntConsumer onSelect;

      AutismServerInfoOverlay.TabBarNode setTabs(String... tabs) {
         this.tabs = tabs == null ? new String[0] : (String[])tabs.clone();
         this.setActiveIndex(this.activeIndex);
         this.markLayoutDirty();
         return this;
      }

      AutismServerInfoOverlay.TabBarNode setActiveIndex(int activeIndex) {
         this.activeIndex = Math.max(0, Math.min(Math.max(0, this.tabs.length - 1), activeIndex));
         return this;
      }

      AutismServerInfoOverlay.TabBarNode setOnSelect(IntConsumer onSelect) {
         this.onSelect = onSelect;
         return this;
      }

      @Override
      public float measureHeight(autismclient.gui.ui.layout.LayoutContext context, float availableWidth) {
         return Math.max(autismclient.gui.ui.UiDensityMetrics.scaledHeight(13), context.theme().update());
      }

      @Override
      public void render(autismclient.gui.ui.layout.LayoutContext context) {
         if (this.nodeVisible && this.tabs.length != 0) {
            autismclient.gui.f.SyncManager uiContext = autismclient.gui.f.UiHelper.init(
               context.graphics(), context.font(), Math.round(context.mouseX()), Math.round(context.mouseY())
            );

            for (int i = 0; i < this.tabs.length; i++) {
               autismclient.gui.f.NetHelper bounds = this.tabBounds(i);
               boolean active = i == this.activeIndex;
               TabStripRenderer.drawTab(uiContext, bounds, this.tabs[i], active);
            }
         }
      }

      @Override
      public boolean mouseClicked(autismclient.gui.ui.layout.LayoutContext context, float mouseX, float mouseY, int button) {
         if (this.nodeVisible && this.nodeEnabled && button == 0 && this.tabs.length != 0) {
            for (int i = 0; i < this.tabs.length; i++) {
               if (this.tabBounds(i).init_Z(Math.round(mouseX), Math.round(mouseY))) {
                  if (i != this.activeIndex) {
                     this.setActiveIndex(i);
                     if (this.onSelect != null) {
                        this.onSelect.accept(i);
                     }
                  }

                  return true;
               }
            }

            return false;
         } else {
            return false;
         }
      }

      private autismclient.gui.f.NetHelper tabBounds(int index) {
         return TabStripRenderer.tabBounds(
            autismclient.gui.f.NetHelper.init(Math.round(this.nodeX), Math.round(this.nodeY), Math.round(this.nodeWidth), Math.round(this.nodeHeight)),
            this.tabs.length,
            index,
            2
         );
      }
   }

   private static final class DetailRow {
      final AutismServerInfoOverlay.DetailRowKind type;
      final String title;
      final String plugin;
      final String label;
      final String actionCommand;
      final AutismServerInfoOverlay.DetectionType resultKind;
      final AutismServerInfoOverlay.EvidenceSource evidence;
      final AutismServerInfoOverlay.MatchConfidence confidence;
      final int height;
      final String countLabel;
      final String searchText;

      private DetailRow(
         AutismServerInfoOverlay.DetailRowKind type,
         String title,
         String plugin,
         String label,
         String actionCommand,
         AutismServerInfoOverlay.DetectionType resultKind,
         AutismServerInfoOverlay.EvidenceSource evidence,
         AutismServerInfoOverlay.MatchConfidence confidence,
         int height,
         String countLabel,
         String searchText
      ) {
         this.type = type;
         this.title = title;
         this.plugin = plugin;
         this.label = label;
         this.actionCommand = actionCommand;
         this.resultKind = resultKind == null ? AutismServerInfoOverlay.DetectionType.PLUGIN : resultKind;
         this.evidence = evidence == null ? AutismServerInfoOverlay.EvidenceSource.UNKNOWN : evidence;
         this.confidence = confidence == null ? AutismServerInfoOverlay.MatchConfidence.UNKNOWN : confidence;
         this.height = Math.max(1, height);
         this.countLabel = countLabel == null ? "" : countLabel;
         this.searchText = searchText == null ? "" : searchText;
      }

      static AutismServerInfoOverlay.DetailRow header(String title, int height) {
         return new AutismServerInfoOverlay.DetailRow(
            AutismServerInfoOverlay.DetailRowKind.HEADER,
            title,
            null,
            title,
            null,
            AutismServerInfoOverlay.DetectionType.PLUGIN,
            AutismServerInfoOverlay.EvidenceSource.UNKNOWN,
            AutismServerInfoOverlay.MatchConfidence.UNKNOWN,
            height,
            "",
            lowerOrEmpty(title)
         );
      }

      static AutismServerInfoOverlay.DetailRow pluginRow(
         String plugin,
         AutismServerInfoOverlay.DetectionType resultKind,
         AutismServerInfoOverlay.EvidenceSource evidence,
         AutismServerInfoOverlay.MatchConfidence confidence,
         int height,
         String countLabel,
         String searchText
      ) {
         return new AutismServerInfoOverlay.DetailRow(
            AutismServerInfoOverlay.DetailRowKind.PLUGIN, null, plugin, plugin, null, resultKind, evidence, confidence, height, countLabel, searchText
         );
      }

      static AutismServerInfoOverlay.DetailRow detailRow(
         AutismServerInfoOverlay.DetailRowKind type,
         String plugin,
         String label,
         String actionCommand,
         AutismServerInfoOverlay.EvidenceSource evidence,
         AutismServerInfoOverlay.MatchConfidence confidence,
         int height,
         String searchText
      ) {
         return new AutismServerInfoOverlay.DetailRow(
            type, null, plugin, label, actionCommand, AutismServerInfoOverlay.DetectionType.PLUGIN, evidence, confidence, height, "", searchText
         );
      }

      boolean isHeader() {
         return this.type == AutismServerInfoOverlay.DetailRowKind.HEADER;
      }

      private static String lowerOrEmpty(String text) {
         return text == null ? "" : text.toLowerCase(Locale.ROOT);
      }
   }

   private record CommandHistoryEntry(String root, String fullCommand, long timestampMs) {
   }

   private enum ProbeKind {
      ROOT,
      HELP,
      PLUGIN_LIST,
      VERSION,
      NAMESPACE,
      USER;
   }

   private static final class SliderNode extends autismclient.gui.ui.layout.LayoutNode {
      private float min = 0.0F;
      private float max = 1.0F;
      private float value = 0.0F;
      private float step = 0.0F;
      private boolean dragging = false;
      private Consumer<Float> onChange;

      SliderNode() {
         this.nodeHeight = 12.0F;
      }

      AutismServerInfoOverlay.SliderNode setRange(float min, float max) {
         this.min = min;
         this.max = Math.max(min, max);
         return this.setValue(this.value);
      }

      AutismServerInfoOverlay.SliderNode setStep(float step) {
         this.step = Math.max(0.0F, step);
         return this.setValue(this.value);
      }

      AutismServerInfoOverlay.SliderNode setValue(float value) {
         float snapped = this.snapValue(this.clampValue(value));
         if (Math.abs(this.value - snapped) > 1.0E-4F) {
            this.value = snapped;
            if (this.onChange != null) {
               this.onChange.accept(this.value);
            }
         }

         return this;
      }

      AutismServerInfoOverlay.SliderNode setOnChange(Consumer<Float> onChange) {
         this.onChange = onChange;
         return this;
      }

      @Override
      public float measureHeight(autismclient.gui.ui.layout.LayoutContext context, float availableWidth) {
         return 12.0F;
      }

      @Override
      public void render(autismclient.gui.ui.layout.LayoutContext context) {
         if (this.nodeVisible) {
            autismclient.gui.f.NetHelper bounds = autismclient.gui.f.NetHelper.init(
               Math.round(this.nodeX), Math.round(this.nodeY), Math.round(this.nodeWidth), Math.round(this.nodeHeight)
            );
            autismclient.gui.f.SyncManager uiContext = autismclient.gui.f.UiHelper.init(
               context.graphics(), context.font(), Math.round(context.mouseX()), Math.round(context.mouseY())
            );
            SliderRenderer.draw(uiContext, bounds, SliderRenderer.ratio(this.value, this.min, this.max), this.containsPoint(context.mouseX(), context.mouseY()) || this.dragging);
         }
      }

      @Override
      public boolean mouseClicked(autismclient.gui.ui.layout.LayoutContext context, float mouseX, float mouseY, int button) {
         if (button == 0 && this.containsPoint(mouseX, mouseY)) {
            this.dragging = true;
            this.setFromMouse(mouseX);
            return true;
         } else {
            return false;
         }
      }

      @Override
      public boolean mouseReleased(autismclient.gui.ui.layout.LayoutContext context, float mouseX, float mouseY, int button) {
         if (button == 0 && this.dragging) {
            this.dragging = false;
            return true;
         } else {
            return false;
         }
      }

      @Override
      public boolean mouseDragged(autismclient.gui.ui.layout.LayoutContext context, float mouseX, float mouseY, int button, float deltaX, float deltaY) {
         if (this.dragging && button == 0) {
            this.setFromMouse(mouseX);
            return true;
         } else {
            return false;
         }
      }

      private void setFromMouse(float mouseX) {
         this.setValue(
            (float)SliderRenderer.valueAt(mouseX, Math.round(this.nodeX), Math.round(this.nodeWidth), this.min, this.max, this.step <= 0.0F ? 9.999999747378752E-5 : this.step)
         );
      }

      private float clampValue(float value) {
         return Math.max(this.min, Math.min(this.max, value));
      }

      private float snapValue(float value) {
         return this.step <= 0.0F ? value : this.min + Math.round((value - this.min) / this.step) * this.step;
      }
   }

   private static final class DetectedPluginEntry {
      final String displayName;
      final String key;
      final List<String> commands;
      final List<String> guis;
      final List<String> channels;
      final AutismServerInfoOverlay.DetectionType resultKind;
      final AutismServerInfoOverlay.EvidenceSource evidence;
      final AutismServerInfoOverlay.EvidenceSource copyEvidence;
      final AutismServerInfoOverlay.MatchConfidence confidence;
      final String sourceLabel;
      final String countLabel;
      final List<String> copyCommands;
      final String searchText;

      DetectedPluginEntry(
         String displayName,
         String key,
         List<String> commands,
         List<String> guis,
         List<String> channels,
         AutismServerInfoOverlay.DetectionType resultKind,
         AutismServerInfoOverlay.EvidenceSource evidence,
         AutismServerInfoOverlay.EvidenceSource copyEvidence,
         AutismServerInfoOverlay.MatchConfidence confidence,
         String sourceLabel,
         String countLabel,
         List<String> copyCommands,
         String searchText
      ) {
         this.displayName = displayName == null ? "" : displayName;
         this.key = key == null ? "" : key;
         this.commands = commands == null ? List.of() : commands;
         this.guis = guis == null ? List.of() : guis;
         this.channels = channels == null ? List.of() : channels;
         this.resultKind = resultKind == null ? AutismServerInfoOverlay.DetectionType.PLUGIN : resultKind;
         this.evidence = evidence == null ? AutismServerInfoOverlay.EvidenceSource.UNKNOWN : evidence;
         this.copyEvidence = copyEvidence == null ? AutismServerInfoOverlay.EvidenceSource.UNKNOWN : copyEvidence;
         this.confidence = confidence == null ? AutismServerInfoOverlay.MatchConfidence.UNKNOWN : confidence;
         this.sourceLabel = sourceLabel == null ? "" : sourceLabel;
         this.countLabel = countLabel == null ? "" : countLabel;
         this.copyCommands = copyCommands == null ? List.of() : copyCommands;
         this.searchText = searchText == null ? "" : searchText;
      }
   }

   private static final class PluginAccumulator {
      String name;
      boolean commandBacked;
      AutismServerInfoOverlay.DetectionType resultKind;
      AutismServerInfoOverlay.EvidenceSource evidence = AutismServerInfoOverlay.EvidenceSource.UNKNOWN;
      AutismServerInfoOverlay.EvidenceSource copyEvidence = AutismServerInfoOverlay.EvidenceSource.UNKNOWN;
      AutismServerInfoOverlay.MatchConfidence confidence = AutismServerInfoOverlay.MatchConfidence.FEATURE;
      final Set<String> commands = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
      final Set<String> copyCommands = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
      final Set<String> channels = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
      final Set<String> guis = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
   }

   private static final class QueuedProbe {
      final int id;
      final AutismServerInfoOverlay.ProbeSpec spec;
      final int attempt;

      QueuedProbe(int id, AutismServerInfoOverlay.ProbeSpec spec) {
         this(id, spec, 0);
      }

      QueuedProbe(int id, AutismServerInfoOverlay.ProbeSpec spec, int attempt) {
         this.id = id;
         this.spec = spec;
         this.attempt = attempt;
      }
   }
}
