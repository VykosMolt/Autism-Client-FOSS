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
   private static final Minecraft init_Minecraft = Minecraft.getInstance();
   private static final Logger init_Logger = LogUtils.getLogger();
   private static final String init_Str_2 = "autism-serverinfo";
   private static final int flush_I = 12;
   private static final int close_I = 10;
   private static final int open_I = 3;
   private static final float init_F_2 = 3.0F;
   private int poll_I = 350;
   private int push_I = 30;
   private int pop_I = 236;
   private int tick_I;
   private int run_I = 246;
   private static final int get_I = 16;
   private static final int set_I = 8;
   private boolean write_Z = false;
   private boolean update_Z = false;
   private boolean sync_Z = false;
   private boolean flush_Z = false;
   private double init_D;
   private double load_D;
   private float load_F;
   private float read_F;
   private int put_I;
   private int add_I;
   private boolean close_Z = false;
   private final Font init_Font;
   private final autismclient.gui.f.b.CharEncoder init_CharEncoder = new autismclient.gui.f.b.CharEncoder();
   private final autismclient.gui.ui.layout.CollapsibleWindowNode init_CharEncoder_2 = new autismclient.gui.ui.layout.CollapsibleWindowNode("Server Info");
   private final autismclient.gui.ui.layout.LayoutTreeHost init_TextFormat = new autismclient.gui.ui.layout.LayoutTreeHost(this.init_CharEncoder, this.init_CharEncoder_2);
   private final AutismServerInfoOverlay.TabBarNode init_AutismServerInfoOv_4 = new AutismServerInfoOverlay.TabBarNode();
   private final autismclient.gui.ui.widgets.TextInputWidget init_BitSetUtil = new autismclient.gui.ui.widgets.TextInputWidget();
   private final AutismServerInfoOverlay.SliderNode init_AutismServerInfoOv_5 = new AutismServerInfoOverlay.SliderNode();
   private final AutismServerInfoOverlay.ProgressBarNode init_AutismServerInfoOv_2 = new AutismServerInfoOverlay.ProgressBarNode();
   private final autismclient.gui.ui.layout.VerticalSpacerNode init_BitSetUtil_2 = new autismclient.gui.ui.layout.VerticalSpacerNode();
   private autismclient.gui.ui.layout.ColumnLayout init_PacketUtil;
   private boolean open_Z = false;
   private int clear_I = 0;
   private long init_J = 0L;
   private static final String[] init_StrArr = new String[]{"Info", "Plugins"};
   private int start = 0;
   private final List<AutismServerInfoOverlay.ClickHotspot> init_List = new ArrayList<>();
   private volatile String load_Str = null;
   private volatile String read_Str = null;
   private volatile boolean push_Z = false;
   private final Set<String> auditDnsRunning = ConcurrentHashMap.newKeySet();
   private final List<String> load_List = new ArrayList<>();
   private final Map<String, List<String>> init_Map = new LinkedHashMap<>();
   private final Map<String, List<String>> load_Map = new LinkedHashMap<>();
   private final Map<String, List<String>> read_Map = new LinkedHashMap<>();
   private List<AutismServerInfoOverlay.DetailRow> read_List = List.of();
   private String write_Str = null;
   private String update_Str = null;
   private int stop = -1;
   private int[] init_IArr = new int[0];
   private int next = 0;
   private int prev = 0;
   private int valid = 0;
   private int size = -1;
   private Map<String, AutismServerInfoOverlay.DetectedPluginEntry> write_Map = Map.of();
   private int data = -1;
   private final autismclient.gui.ui.widgets.ScrollOffsetController init_CacheEntry = new autismclient.gui.ui.widgets.ScrollOffsetController();
   private int value = 0;
   private String sync_Str = null;
   private int state = 50;
   private boolean pop_Z = false;
   private boolean tick_Z = false;
   private AutismServerInfoOverlay.ActiveScanSession init_AutismServerInfoOv_3;
   private long read_J = 1L;
   private final ServerScanIdAllocator init_bR = new ServerScanIdAllocator();
   private AutismServerInfoOverlay.ScanStatus init_AutismServerInfoOv = AutismServerInfoOverlay.ScanStatus.NONE;
   private String close_Str = "";
   private int queue;
   private int buffer;
   private int entry;
   private int node;
   private final Set<String> load_Set = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
   private final Map<String, AutismServerInfoOverlay.EvidenceSource> update_Map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
   private final Map<String, AutismServerInfoOverlay.EvidenceSource> sync_Map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
   private final Map<String, AutismServerInfoOverlay.MatchConfidence> flush_Map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
   private final Map<String, AutismServerInfoOverlay.DetectionType> close_Map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
   private final Map<String, List<String>> open_Map = new LinkedHashMap<>();
   private final Map<String, AutismServerInfoOverlay.PluginAccumulator> poll_Map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
   private final Map<String, AutismServerInfoOverlay.PluginAccumulator> push_Map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
   private final Map<Integer, AutismServerInfoOverlay.ProbeSpec> pop_Map = new LinkedHashMap<>();
   private final Deque<AutismServerInfoOverlay.CommandHistoryEntry> init_Deque = new ArrayDeque<>();
   private static final long write_J = 250L;
   private static final int task = 8192;
   private static final int pool = 1000;
   private static final int index = 256;
   private static final int count = 64;
   private final List<AutismServerInfoOverlay.ProbeObservation> write_List = new ArrayList<>();
   private final List<AutismServerInfoOverlay.CommandEvidence> update_List = new ArrayList<>();
   private boolean run_Z = false;
   private long update_J = 0L;
   private final Set<String> read_Set = new HashSet<>();
   private boolean get_Z = false;
   private boolean set_Z = true;
   private boolean put_Z = false;
   private int flags = 50;
   private String open_Str = null;
   private String poll_Str = null;
   private String push_Str = "";
   private String pop_Str = "";
   private String tick_Str = "";
   private String run_Str = "";
   private String get_Str = "";
   private boolean add_Z = true;
   private long sync_J = -1L;
   private static final long flush_J = 700L;
   private static final long close_J = 12000L;
   private static final int level = 50;
   private static final int frame = 10;
   private static final int block = 500;
   private static final long open_J = 5184000000L;
   private static final long poll_J = 450L;
   private static final long push_J = 1400L;
   private static final String set_Str = "hybrid-plugin-scan-v12";
   private static final boolean clear_Z = Boolean.getBoolean("autism.plugin.scan.trace");
   private static final long pop_J = 1500L;
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
   private int f_f_I = 200;
   private int f_g_I = 258;
   private int f_h_I = 200;
   private int f_i_I = 280;
   private static final String[] load_StrArr = new String[]{
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
   private static final String[] read_StrArr = new String[]{"/plugins ", "/pl ", "/bukkit:plugins ", "/bukkit:pl "};
   private static final String[] update_StrArr = new String[]{"/help ", "/? ", "/bukkit:help ", "/minecraft:help "};
   private static final String[] sync_StrArr = new String[]{
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
   private static final Set<String> write_Set = Set.of(
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
   private static final String put_Str = "abcdefghijklmnopqrstuvwxyz0123456789";
   private static final Map<String, String> tick_Map = Map.ofEntries(
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
   private static final Map<String, String> run_Map = Map.ofEntries(
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
   private static final Set<String> update_Set = Set.of(
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
   private static final Set<String> sync_Set = Set.of("minecraft", "brigadier", "bukkit", "spigot", "paper", "purpur", "velocity", "bungeecord", "waterfall");
   private static final Set<String> flush_Set = Set.of(
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

   private void init_V_Str_Throwable(String stage, Throwable t) {
      if (this.read_Set.add(stage)) {
         init_Logger.warn("Server info overlay '{}' failed; further errors for this stage are suppressed", stage, t);
      }
   }

   public AutismServerInfoOverlay(Font textRenderer) {
      this.init_Font = textRenderer;
      this.stop_V_7();
   }

   public void load(boolean configurationOnly) {
      if (this.sync_Z != configurationOnly) {
         this.sync_Z = configurationOnly;
         if (configurationOnly) {
            this.read_V_3("main menu");
         }

         this.prev_V_7();
      }
   }

   private void stop_V_7() {
      this.init_CharEncoder_2.flush(false);
      this.init_CharEncoder_2.init(autismclient.gui.f.b.l.load);
      this.init_CharEncoder_2.init_CharEncoder_Z_Z(true, true);
      this.init_CharEncoder_2.init_CharEncoder_I_I(this.task_I_2() + 1, this.task_I_2() + this.k() + this.l() + this.m() + 12);
      this.init_CharEncoder_2.init_PacketUtil().init(this.pool_I_2()).init(autismclient.gui.ui.layout.EdgeInsets.init(this.task_I_2()));
      this.init_AutismServerInfoOv_4
         .init_AutismServerInfoOv_StrArr(init_StrArr)
         .init_AutismServerInfoOv_I(this.start)
         .init_AutismServerInfoOv_IntConsumer(this::init_V_I_9);
      this.init_BitSetUtil
         .load_BitSetUtil_Str("Search plugins...")
         .read_BitSetUtil_F(this.drain_I_2())
         .read_BitSetUtil_I(this.index_I_2())
         .init_BitSetUtil_Consumer(text -> {
            this.init_CacheEntry.jumpTo(0, 0);
            this.prev_V_7();
         });
      this.init_AutismServerInfoOv_5
         .init_AutismServerInfoOv(10.0F, 500.0F)
         .init_AutismServerInfoOv(10.0F)
         .load_AutismServerInfoOv(this.state)
         .init(value -> this.load_V_2(Math.round(value)));
      this.init_AutismServerInfoOv_2.init(0.0F);
   }

   public void write_V_24() {
      AutismSessionState shared = AutismSessionState.getInstance();
      this.flags_V_2();
      String stateAddress = this.load_Str_11();
      if (stateAddress.isEmpty() && this.poll_Str != null) {
         stateAddress = this.poll_Str;
      }

      shared.setServerInfoActiveTab(this.start);
      shared.setServerInfoScrollOffset(this.init_CacheEntry.getOffset());
      shared.setServerInfoSelectedPlugin(this.sync_Str);
      shared.setServerInfoStateAddress(stateAddress);
      shared.setServerInfoScanDelayMs(this.state);
      shared.setServerInfoTabWidth(this.f_f_I);
      shared.setServerInfoTabHeight(this.f_g_I);
      shared.setServerInfoPluginsTabWidth(this.f_h_I);
      shared.setServerInfoPluginsTabHeight(this.f_i_I);
      this.level_V_2();
      this.a__V();
   }

   public void update_V_21() {
      AutismSessionState shared = AutismSessionState.getInstance();
      this.b__V();
      this.start = Math.max(0, Math.min(init_StrArr.length - 1, shared.getServerInfoActiveTab()));
      this.state = Math.max(10, Math.min(500, shared.getServerInfoScanDelayMs()));
      this.f_f_I = Math.max(this.bind_I_2(), shared.getServerInfoTabWidth());
      this.f_g_I = Math.max(this.a_I_2(), shared.getServerInfoTabHeight());
      this.f_h_I = Math.max(this.b_I_2(), shared.getServerInfoPluginsTabWidth());
      this.f_i_I = Math.max(this.c_I_2(), shared.getServerInfoPluginsTabHeight());
      this.frame_V_2();
      String stateAddress = this.load_Str_11();
      if (stateAddress.isEmpty() && this.poll_Str != null) {
         stateAddress = this.poll_Str;
      }

      if (!stateAddress.isEmpty() && stateAddress.equals(shared.getServerInfoStateAddress())) {
         this.init_CacheEntry.setOffsetUnclamped(shared.getServerInfoScrollOffset());
         this.sync_Str = shared.getServerInfoSelectedPlugin();
         if (this.sync_Str != null && this.sync_Str.isBlank()) {
            this.sync_Str = null;
         }

         if (this.sync_Str != null && !this.flush_Z_2(this.sync_Str)) {
            this.sync_Str = null;
         }
      } else {
         this.init_CacheEntry.jumpTo(0, 0);
         this.sync_Str = null;
      }

      if (this.start == 0) {
         this.count_V_2();
      } else if (this.tick_Z) {
         this.task_V_2();
      } else if (!this.pop_Z) {
         this.node_V_2();
      }

      this.init_AutismServerInfoOv_4.init_AutismServerInfoOv_I(this.start);
      this.init_AutismServerInfoOv_5.load_AutismServerInfoOv(this.state);
   }

   public static boolean poll_Z_16() {
      OverlayBounds layout = AutismSessionState.getInstance().getOverlayBounds("autism-serverinfo");
      return layout != null && layout.visible;
   }

   private void init_V_I_9(int tabIdx) {
      this.flags_V_2();
      this.start = Math.max(0, Math.min(init_StrArr.length - 1, tabIdx));
      this.init_AutismServerInfoOv_4.init_AutismServerInfoOv_I(this.start);
      this.init_CacheEntry.jumpTo(0, 0);
      this.sync_Str = null;
      if (this.start == 0) {
         this.count_V_2();
      } else if (this.tick_Z) {
         this.task_V_2();
      } else if (!this.pop_Z) {
         this.node_V_2();
      } else {
         this.pool_V_2();
      }

      this.write_V_24();
   }

   private void next_V_7() {
      this.set_Z = false;
      this.init_CharEncoder_2.init_PacketUtil().init_V_2();
      this.init_AutismServerInfoOv_4.init_AutismServerInfoOv_I(this.start);
      this.init_AutismServerInfoOv_5.load_AutismServerInfoOv(this.state);
      this.init_AutismServerInfoOv_2.init(this.init_F_2());
      if (!this.tick_Z) {
         this.init_CharEncoder_2.init_PacketUtil().init_TableIndex_TableIndex(this.init_AutismServerInfoOv_4);
      }

      if (this.start == 0) {
         this.valid_V_4();
      } else if (!this.pop_Z && !this.tick_Z) {
         this.size_V_3();
      } else if (this.tick_Z) {
         this.data_V_3();
      } else {
         this.value_V_2();
      }
   }

   private void prev_V_7() {
      this.set_Z = true;
   }

   private void valid_V_4() {
      ServerData entry = init_Minecraft.getCurrentServer();
      String displayedAddress = this.open_Str_2();
      String realIp = this.pop(displayedAddress);
      String software = this.load(entry);
      String versionNote = this.read(entry);
      String ping = entry != null ? entry.ping + " ms" : "--";
      String players = "--";
      if (init_Minecraft.getConnection() != null) {
         int online = init_Minecraft.getConnection().getListedOnlinePlayers().size();
         players = entry != null && entry.players != null ? online + " / " + entry.players.max() : String.valueOf(online);
      }

      String proto = entry != null ? String.valueOf(entry.protocol) : "--";
      String diff = init_Minecraft.level != null ? init_Minecraft.level.getDifficulty().getDisplayName().getString() : "--";
      String time = "--";
      if (init_Minecraft.level != null) {
         long dayCount = init_Minecraft.level.getOverworldClockTime() / 24000L;
         long timeOfDay = init_Minecraft.level.getOverworldClockTime() % 24000L;
         int hours = (int)((timeOfDay / 1000L + 6L) % 24L);
         int minutes = (int)(timeOfDay % 1000L * 60L / 1000L);
         time = "Day " + dayCount + " (" + String.format("%02d:%02d", hours, minutes) + ")";
      }

      double estimatedTps = AutismSessionState.getInstance().getEstimatedTps();
      String tps = estimatedTps > 0.0 ? String.format("%.1f", Math.min(20.0, estimatedTps)) : "--";
      this.init_PacketUtil = new autismclient.gui.ui.layout.ColumnLayout();
      this.init_V_Str_2("IP:", displayedAddress, null, () -> this.init_V_Str_10(displayedAddress, "Server address copied.", "Server address unavailable."));
      String host = this.push(displayedAddress);
      if (!host.isBlank() && (!host.equals(realIp) || this.push_Z)) {
         this.init_V_Str_2("Real IP:", realIp, null, this::bind_V_2);
      }

      this.init_V_Str_9("Version:", this.init_Str_ServerData(entry), null);
      this.init_V_Str_9("Real Version:", this.flush_Str_2(), null);
      this.init_V_Str_9("Brand:", this.close_Str_2(), null);
      if (!"--".equals(software)) {
         this.init_V_Str_9("Software:", software, null);
      }

      this.init_V_Str_9("Ping:", ping, null);
      this.init_V_Str_9("Players:", players, null);
      this.init_V_Str_9("Protocol:", proto, null);
      if (!"--".equals(versionNote)) {
         this.init_V_Str_9("Version Note:", versionNote, UiColorPalette.warningYellow());
      }

      this.init_V_Str_9("Difficulty:", diff, null);
      this.init_V_Str_9("World:", this.push_Str_2(), null);
      this.init_V_Str_9("Time:", time, null);
      this.init_V_Str_9("TPS:", tps, null);
      List<String> detectedAcs = this.init_List_13();
      if (this.tick_Z) {
         this.init_V_Str_9("AntiCheats:", "Scanning...", ThemePaletteManager.recolor(-48060, ThemePaletteManager.ColorRole.DANGER));
      } else if (!this.pop_Z) {
         this.init_V_Str_9("AntiCheats:", "Probe Plugins First", ThemePaletteManager.recolor(-48060, ThemePaletteManager.ColorRole.DANGER));
      } else if (detectedAcs.isEmpty()) {
         this.init_V_Str_9("AntiCheats:", "None detected", null);
      } else {
         this.init_V_Str_9("AntiCheats:", String.join(", ", detectedAcs), ThemePaletteManager.recolor(-48060, ThemePaletteManager.ColorRole.DANGER));
      }

      this.init_CharEncoder_2.init_PacketUtil().init_TableIndex_TableIndex(this.init_PacketUtil);
      this.init_PacketUtil = null;
      this.init_CharEncoder_2.init_PacketUtil().init_TableIndex_TableIndex(new autismclient.gui.ui.layout.FixedSpacerNode(0.0F, 2.0F));
      this.init_CharEncoder_2
         .init_PacketUtil()
         .init_TableIndex_TableIndex(
            new autismclient.gui.ui.layout.ButtonNode("Copy Report", autismclient.gui.ui.layout.ButtonNode.Variant.SECONDARY, this::d_V_2)
               .load_ByteReader_I(this.count_I_2())
         );
   }

   private void init_V_Str_9(String label, String value, Integer valueColor) {
      this.init_V_Str_2(label, value, valueColor, null);
   }

   private void init_V_Str_2(String label, String value, Integer valueColor, Runnable onPress) {
      autismclient.gui.ui.layout.KeyValueRowNode row = new autismclient.gui.ui.layout.KeyValueRowNode(label, value)
         .init_TaskQueue_F(this.push_I_24())
         .init_TaskQueue_Integer(valueColor)
         .init_TaskQueue_Runnable(onPress);
      if (this.init_PacketUtil != null) {
         this.init_PacketUtil.init_TableIndex_TableIndex(row);
      } else {
         this.init_CharEncoder_2.init_PacketUtil().init_TableIndex_TableIndex(row);
      }
   }

   private int push_I_24() {
      return this.init_I_StrArr_2(
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

   private void size_V_3() {
      this.init_CharEncoder_2.init_PacketUtil().init_TableIndex_TableIndex(this.init_ByteReader());
      this.init_CharEncoder_2
         .init_PacketUtil()
         .init_TableIndex_TableIndex(new autismclient.gui.ui.layout.LabelNode("Auto-Scans on join.", autismclient.gui.f.b.l.write));
      this.init_CharEncoder_2.init_PacketUtil().init_TableIndex_TableIndex(new autismclient.gui.ui.layout.FixedSpacerNode(0.0F, 2.0F));
      this.init_CharEncoder_2
         .init_PacketUtil()
         .init_TableIndex_TableIndex(new autismclient.gui.ui.layout.LabelNode("Start probing manually", autismclient.gui.f.b.l.write));
      this.init_CharEncoder_2
         .init_PacketUtil()
         .init_TableIndex_TableIndex(new autismclient.gui.ui.layout.KeyValueRowNode("Delay:", this.state + " ms").init_TaskQueue_F(this.frame_I_2()));
      this.init_CharEncoder_2.init_PacketUtil().init_TableIndex_TableIndex(this.init_AutismServerInfoOv_5);
      this.init_CharEncoder_2.init_PacketUtil().init_TableIndex_TableIndex(new autismclient.gui.ui.layout.LabelNode("Default: 50 ms", autismclient.gui.f.b.l.write));
      this.init_CharEncoder_2
         .init_PacketUtil()
         .init_TableIndex_TableIndex(new autismclient.gui.ui.layout.LabelNode("If you get kicked increase the delay.", autismclient.gui.f.b.l.write));
      autismclient.gui.ui.layout.ButtonNode startProbe = new autismclient.gui.ui.layout.ButtonNode(
         this.sync_Z ? "Join a Server to Probe" : "Start Probing", autismclient.gui.ui.layout.ButtonNode.Variant.SECONDARY, this::mark_V_2
      );
      startProbe.load_ByteReader_I(this.count_I_2());
      startProbe.read_TableIndex_Z(!this.sync_Z);
      this.init_CharEncoder_2.init_PacketUtil().init_TableIndex_TableIndex(startProbe);
      if (!this.load_List.isEmpty()) {
         this.init_CharEncoder_2
            .init_PacketUtil()
            .init_TableIndex_TableIndex(new autismclient.gui.ui.layout.ButtonNode("Back to Results", autismclient.gui.ui.layout.ButtonNode.Variant.SECONDARY, () -> {
               this.put_Z = false;
               this.pop_Z = true;
               this.tick_Z = false;
               this.pool_V_2();
               this.write_V_24();
            }).load_ByteReader_I(this.count_I_2()));
      }
   }

   private void data_V_3() {
      int progressPercent = Math.max(0, Math.min(100, Math.round(this.init_F_2() * 100.0F)));
      AutismServerInfoOverlay.ActiveScanSession session = this.init_AutismServerInfoOv_3;
      this.init_CharEncoder_2
         .init_PacketUtil()
         .init_TableIndex_TableIndex(
            new autismclient.gui.ui.layout.KeyValueRowNode("State:", this.update_Str_3()).init_TaskQueue_F(this.block_I_2()).init_TaskQueue_Integer(this.node_I_2())
         );
      this.init_CharEncoder_2
         .init_PacketUtil()
         .init_TableIndex_TableIndex(
            new autismclient.gui.ui.layout.KeyValueRowNode("Sent:", this.state_I_2() + "/" + Math.max(1, this.cache_I_2())).init_TaskQueue_F(this.block_I_2())
         );
      this.init_CharEncoder_2
         .init_PacketUtil()
         .init_TableIndex_TableIndex(
            new autismclient.gui.ui.layout.KeyValueRowNode("Replies:", this.queue_I_2() + "/" + Math.max(1, this.cache_I_2())).init_TaskQueue_F(this.block_I_2())
         );
      this.init_CharEncoder_2
         .init_PacketUtil()
         .init_TableIndex_TableIndex(
            new autismclient.gui.ui.layout.KeyValueRowNode("Retries:", Integer.toString(session == null ? 0 : session.write_I)).init_TaskQueue_F(this.block_I_2())
         );
      this.init_CharEncoder_2
         .init_PacketUtil()
         .init_TableIndex_TableIndex(
            new autismclient.gui.ui.layout.KeyValueRowNode("Failed:", Integer.toString(session == null ? 0 : session.update)).init_TaskQueue_F(this.block_I_2())
         );
      int foundCount = this.entry_I_2();
      this.init_CharEncoder_2
         .init_PacketUtil()
         .init_TableIndex_TableIndex(
            new autismclient.gui.ui.layout.KeyValueRowNode("Found:", foundCount + " plugin" + (foundCount == 1 ? "" : "s")).init_TaskQueue_F(this.block_I_2())
         );
      this.init_CharEncoder_2
         .init_PacketUtil()
         .init_TableIndex_TableIndex(
            new autismclient.gui.ui.layout.KeyValueRowNode("Progress:", progressPercent + "%").init_TaskQueue_F(this.block_I_2()).init_TaskQueue_Integer(this.node_I_2())
         );
      this.init_CharEncoder_2.init_PacketUtil().init_TableIndex_TableIndex(this.init_AutismServerInfoOv_2);
   }

   private void value_V_2() {
      this.init_CharEncoder_2.init_PacketUtil().init_TableIndex_TableIndex(this.init_ByteReader());
      this.init_BitSetUtil.read_BitSetUtil_F(Math.max(this.drain_I_2(), this.pop_I - this.fill_I_2()));
      this.init_CharEncoder_2.init_PacketUtil().init_TableIndex_TableIndex(this.init_BitSetUtil);
      autismclient.gui.ui.layout.StretchRowLayout buttons = new autismclient.gui.ui.layout.StretchRowLayout().init(this.flags_I_2());
      buttons.init_TableIndex_TableIndex(
         new autismclient.gui.ui.layout.ButtonNode("Rescan", autismclient.gui.ui.layout.ButtonNode.Variant.SECONDARY, () -> this.queue_V_2())
            .load_ByteReader_I(this.count_I_2())
      );
      buttons.init_TableIndex_TableIndex(
         new autismclient.gui.ui.layout.ButtonNode("Copy Plugins", autismclient.gui.ui.layout.ButtonNode.Variant.SECONDARY, this::e_V_2)
            .load_ByteReader_I(this.count_I_2())
      );
      this.init_CharEncoder_2.init_PacketUtil().init_TableIndex_TableIndex(buttons);
      String query = this.init_BitSetUtil.init_Str().toLowerCase(Locale.ROOT);
      int filteredCount = 0;

      for (AutismServerInfoOverlay.DetectedPluginEntry detail : this.init_Collection()) {
         if (detail != null && (query.isEmpty() || detail.update.contains(query))) {
            filteredCount++;
         }
      }

      String header = this.load_List.isEmpty() ? "No plugins detected" : "Detected: " + filteredCount + " plugin" + (filteredCount == 1 ? "" : "s");
      this.init_CharEncoder_2.init_PacketUtil().init_TableIndex_TableIndex(new autismclient.gui.ui.layout.LabelNode(header, autismclient.gui.f.b.l.write));
      this.init_CharEncoder_2.init_PacketUtil().init_TableIndex_TableIndex(this.init_BitSetUtil_2);
      this.tick_I = this.init_I_BitSetUtil_I(this.init_BitSetUtil_2, this.valid_I_4());
      float viewportHeight = Math.max(this.load_F(), this.run_I - this.tick_I);
      this.init_BitSetUtil_2.init(viewportHeight);
   }

   private int init_I_BitSetUtil_I(autismclient.gui.ui.layout.VerticalSpacerNode slot, int fallback) {
      slot.init(0.0F);
      int measured = Math.round(this.init_TextFormat.init(this.pop_I));
      return measured > 0 ? measured : fallback;
   }

   private autismclient.gui.ui.layout.ButtonNode init_ByteReader() {
      boolean autoOn = AutismConfig.getGlobal().autoProbePlugins;
      return new autismclient.gui.ui.layout.ButtonNode(
            "Auto Probe: " + (autoOn ? "ON" : "OFF"),
            autoOn ? autismclient.gui.ui.layout.ButtonNode.Variant.SUCCESS : autismclient.gui.ui.layout.ButtonNode.Variant.SECONDARY,
            this::state_V_2
         )
         .load_ByteReader_I(this.count_I_2());
   }

   private void state_V_2() {
      AutismConfig cfg = AutismConfig.getGlobal();
      cfg.autoProbePlugins = !cfg.autoProbePlugins;
      cfg.save();
      this.open_Str = null;
      this.prev_V_7();
      this.write_V_24();
   }

   private void queue_V_2() {
      this.read_V_3("setup");
      this.pop_Z = false;
      this.put_Z = true;
      this.get_Z = false;
      this.sync_Str = null;
      this.init_BitSetUtil.init_BitSetUtil_Str("");
      this.init_BitSetUtil.init_V_Z(false);
      this.init_CacheEntry.jumpTo(0, 0);
      this.start = 1;
      this.node_V_2();
      this.write_V_24();
   }

   private String load_Str_11() {
      ServerData entry = init_Minecraft.getCurrentServer();
      if (entry != null && entry.ip != null && !entry.ip.isBlank()) {
         return entry.ip.trim().toLowerCase(Locale.ROOT);
      }

      if (init_Minecraft.getConnection() != null && init_Minecraft.getConnection().getConnection() != null) {
         SocketAddress address = init_Minecraft.getConnection().getConnection().getRemoteAddress();
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

   private String init_Str_Str_6(String signature) {
      return signature == null ? "" : signature.trim().toLowerCase(Locale.ROOT);
   }

   private String read_Str_8() {
      if (init_Minecraft.getConnection() == null) {
         return "";
      }

      String currentAddress = this.load_Str_11();
      String brand = init_Minecraft.getConnection().serverBrand();
      String normalizedBrand = brand == null ? "" : brand.trim().toLowerCase(Locale.ROOT);
      if (!this.add_Z && currentAddress.equals(this.tick_Str) && normalizedBrand.equals(this.run_Str)) {
         return this.get_Str;
      }

      List<String> parts = new ArrayList<>();
      parts.add("scan=hybrid-plugin-scan-v12");
      if (brand != null && !brand.isBlank()) {
         parts.add("brand=" + normalizedBrand);
      }

      try {
         CommandDispatcher<?> dispatcher = init_Minecraft.getConnection().getCommands();
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

      this.tick_Str = currentAddress;
      this.run_Str = normalizedBrand;
      this.get_Str = this.init_Str_Str_6(String.join("|", parts));
      this.add_Z = false;
      return this.get_Str;
   }

   private void buffer_V_2() {
      this.add_Z = true;
   }

   private void load_V_2(int delayMs) {
      this.state = Math.max(10, Math.min(500, delayMs));
      AutismSessionState.getInstance().setServerInfoScanDelayMs(this.state);
   }

   @Override
   public boolean read_Z() {
      return true;
   }

   private int pop_I_24() {
      return this.init_I_I_I(this.get_I_20(), this.get_I_20());
   }

   private boolean clear_Z_4() {
      return this.start == 1 && this.pop_Z && !this.tick_Z;
   }

   private int tick_I_22() {
      return this.load_I_I_I(Math.max(this.set_I_20(), Math.min(this.stop_I_7(), this.put_I_8())), this.a__I());
   }

   @Override
   protected int a__I() {
      return this.clear_Z_4() && this.tick_I > 0 ? Math.max(this.load_I(), this.tick_I + Math.round(this.load_F())) : this.load_I();
   }

   @Override
   protected void c_() {
      if (!this.set_Z()) {
         this.pop_I = this.get_I_20();
      }

      this.prev_V_7();
   }

   private void entry_V_2() {
      OverlayBounds clamped = this.init_cH_cH_5(new OverlayBounds(this.poll_I, this.push_I, this.pop_I, this.run_I, this.write_Z, this.update_Z));
      this.poll_I = clamped.x;
      this.push_I = clamped.y;
      this.pop_I = clamped.width;
      this.run_I = clamped.height;
   }

   private void node_V_2() {
      this.pop_I = this.pop_I_24();
      this.run_I = this.next_I_7();
      this.entry_V_2();
      this.prev_V_7();
   }

   private void task_V_2() {
      this.pop_I = this.pop_I_24();
      this.run_I = this.prev_I_6();
      this.entry_V_2();
      this.prev_V_7();
   }

   private void pool_V_2() {
      this.pop_I = this.pop_I_24();
      this.run_I = this.tick_I_22();
      this.entry_V_2();
      this.prev_V_7();
   }

   private void count_V_2() {
      this.pop_I = this.pop_I_24();
      this.run_I = this.stop_I_7();
      this.entry_V_2();
      this.prev_V_7();
   }

   private int get_I_20() {
      autismclient.gui.ui.layout.LayoutViewport viewport = this.init_TextFormat.init_RingBuffer();
      return Math.max(this.bind_I_2(), Math.min(this.mark_I_2(), Math.round(viewport.width())));
   }

   private int set_I_20() {
      return this.stop_I_7();
   }

   private int put_I_8() {
      autismclient.gui.ui.layout.LayoutViewport viewport = this.init_TextFormat.init_RingBuffer();
      int viewportHeight = Math.round(viewport.height());
      return Math.max(this.set_I_20(), viewportHeight - 28);
   }

   private int stop_I_7() {
      autismclient.gui.ui.layout.LayoutViewport viewport = this.init_TextFormat.init_RingBuffer();
      int measured = Math.max(this.a_I_2(), this.size_I_3());
      return Math.max(this.a_I_2(), Math.min(measured, Math.max(this.a_I_2(), Math.round(viewport.height()) - this.f())));
   }

   private int next_I_7() {
      autismclient.gui.ui.layout.LayoutViewport viewport = this.init_TextFormat.init_RingBuffer();
      int measured = Math.max(this.d_I_2(), this.init_I_I_3(1, false, false, Math.max(this.pop_I, this.scan_I_2())));
      return Math.min(measured, Math.max(this.d_I_2(), Math.round(viewport.height()) - this.f()));
   }

   private int prev_I_6() {
      autismclient.gui.ui.layout.LayoutViewport viewport = this.init_TextFormat.init_RingBuffer();
      int measured = Math.max(this.e_I_2(), this.init_I_I_3(1, false, true, Math.max(this.pop_I, this.parse_I_2())));
      return Math.min(measured, Math.max(this.e_I_2(), Math.round(viewport.height()) - this.f()));
   }

   private int valid_I_4() {
      int contentPadding = this.task_I_2() * 2;
      int contentGap = this.pool_I_2() * 5;
      int autoToggleHeight = this.count_I_2();
      int searchHeight = this.index_I_2();
      int buttonRowHeight = this.count_I_2();
      int headerLabelHeight = this.init_CharEncoder.init_I_l_I(autismclient.gui.f.b.l.write, 3);
      return this.init_CharEncoder.load()
         + contentPadding
         + contentGap
         + this.level_I_2()
         + autoToggleHeight
         + searchHeight
         + buttonRowHeight
         + headerLabelHeight;
   }

   private int size_I_3() {
      return Math.max(this.a_I_2(), this.init_I_I_3(0, this.pop_Z, this.tick_Z, Math.max(this.bind_I_2(), this.pop_I)));
   }

   private int init_I_I_3(int tab, boolean scanDone, boolean scanInProgress, int measureWidth) {
      int previousTab = this.start;
      boolean previousScanDone = this.pop_Z;
      boolean previousScanInProgress = this.tick_Z;
      this.start = tab;
      this.pop_Z = scanDone;
      this.tick_Z = scanInProgress;
      this.next_V_7();
      int measured = Math.round(this.init_TextFormat.init(measureWidth));
      this.start = previousTab;
      this.pop_Z = previousScanDone;
      this.tick_Z = previousScanInProgress;
      this.next_V_7();
      return measured;
   }

   private int data_I_3() {
      int rowCount = 12;
      String displayedAddress = this.open_Str_2();
      String host = this.push(displayedAddress);
      String realIp = this.pop(displayedAddress);
      if (!host.isBlank() && (!host.equals(realIp) || this.push_Z)) {
         rowCount++;
      }

      ServerData entry = init_Minecraft.getCurrentServer();
      String software = this.load(entry);
      if (!"--".equals(software)) {
         rowCount++;
      }

      String versionNote = this.read(entry);
      if (!"--".equals(versionNote)) {
         rowCount++;
      }

      int contentHeight = this.level_I_2() + rowCount * 12 + 2 + this.count_I_2();
      return this.init_CharEncoder.load() + this.task_I_2() * 2 + this.pool_I_2() * 3 + contentHeight;
   }

   private void flags_V_2() {
      if (this.start == 0) {
         this.f_f_I = Math.max(this.bind_I_2(), this.pop_I);
         this.f_g_I = Math.max(this.data_I_3(), this.run_I);
         this.f_h_I = Math.max(this.f_h_I, this.f_f_I);
      } else if (this.pop_Z && !this.tick_Z) {
         this.f_h_I = Math.max(this.b_I_2(), this.pop_I);
         this.f_i_I = Math.max(this.set_I_20(), Math.min(this.put_I_8(), this.run_I));
         this.f_f_I = Math.max(this.f_f_I, this.f_h_I);
      }
   }

   private void init_V_Str_Str_3(String address, String contextSignature) {
      this.pop_Z = false;
      this.put_Z = false;
      this.read_V_3("clear");
      this.init_AutismServerInfoOv = AutismServerInfoOverlay.ScanStatus.NONE;
      this.close_Str = "";
      this.queue = 0;
      this.buffer = 0;
      this.entry = 0;
      this.node = 0;
      this.get_Z = false;
      this.load_Set.clear();
      this.update_Map.clear();
      this.sync_Map.clear();
      this.open_Map.clear();
      this.flush_Map.clear();
      this.close_Map.clear();
      this.poll_Map.clear();
      this.push_Map.clear();
      this.poll_Str = address;
      this.pop_Str = this.init_Str_Str_6(contextSignature);
      this.load_List.clear();
      this.init_Map.clear();
      this.load_Map.clear();
      this.read_Map.clear();
      this.a_V_2();
      this.init_CacheEntry.jumpTo(0, 0);
      this.sync_Str = null;
      this.sync_J = -1L;
      this.pop_Map.clear();
      this.init_Deque.clear();
      this.buffer_V_2();
   }

   private boolean init_Z_Str_Str_3(String address, String contextSignature) {
      AutismConfig.PluginScanCacheEntry cached = AutismConfig.getGlobal().getPluginScan(address, contextSignature);
      if (cached == null) {
         cached = AutismConfig.getGlobal().getPluginScanByAddress(address);
      }

      if (cached == null) {
         return false;
      }

      this.load_List.clear();
      if (cached.plugins != null) {
         int kept = 0;

         for (String plugin : this.load((Collection<String>)cached.plugins)) {
            if (kept++ >= 1000) {
               break;
            }

            this.load_List.add(plugin);
         }
      }

      this.init_Map.clear();
      if (cached.commands != null) {
         this.init_Map.putAll(cached.commands);
      }

      this.load_Map.clear();
      if (cached.channels != null) {
         this.load_Map.putAll(cached.channels);
      }

      this.read_Map.clear();
      if (cached.guis != null) {
         this.read_Map.putAll(cached.guis);
      }

      this.open_Map.clear();
      if (cached.copyCommands != null) {
         this.open_Map.putAll(cached.copyCommands);
      }

      this.init_Map.keySet().retainAll(this.load_List);
      this.load_Map.keySet().retainAll(this.load_List);
      this.read_Map.keySet().retainAll(this.load_List);
      this.open_Map.keySet().retainAll(this.load_List);
      this.init_CacheEntry.jumpTo(0, 0);
      this.sync_Str = null;
      this.pop_Z = true;
      this.put_Z = false;
      this.read_V_3("cache-load");
      this.init_AutismServerInfoOv = this.init_AutismServerInfoOv_Str(cached.scanStatus);
      if (this.init_AutismServerInfoOv == AutismServerInfoOverlay.ScanStatus.NONE) {
         this.init_AutismServerInfoOv = AutismServerInfoOverlay.ScanStatus.COMPLETE;
      }

      this.close_Str = this.init_AutismServerInfoOv == AutismServerInfoOverlay.ScanStatus.PARTIAL ? "Cached partial scan" : "";
      this.queue = Math.max(0, cached.totalProbes);
      this.buffer = Math.max(0, cached.answeredProbes);
      this.entry = Math.max(0, cached.retriedProbes);
      this.node = Math.max(0, cached.failedProbes);
      this.load_Set.clear();
      this.update_Map.clear();
      this.sync_Map.clear();
      this.flush_Map.clear();
      this.close_Map.clear();
      this.poll_Map.clear();
      this.push_Map.clear();
      this.pop_Map.clear();
      this.init_Deque.clear();
      this.poll_Str = address;
      this.pop_Str = this.init_Str_Str_6(cached.contextSignature);
      Map<String, String> cachedEvidence = cached.evidence == null ? Map.of() : cached.evidence;
      Map<String, String> cachedCopyEvidence = cached.copyEvidence == null ? Map.of() : cached.copyEvidence;
      Map<String, String> cachedConfidence = cached.confidence == null ? Map.of() : cached.confidence;

      for (String plugin : this.load_List) {
         String key = this.load_Str_2(plugin);
         this.update_Map.put(key, this.init_AutismServerInfoOv_Str_2(cachedEvidence.get(key)));
         AutismServerInfoOverlay.EvidenceSource copyEvidence = this.init_AutismServerInfoOv_Str_2(cachedCopyEvidence.get(key));
         if (copyEvidence == AutismServerInfoOverlay.EvidenceSource.UNKNOWN) {
            copyEvidence = this.init_AutismServerInfoOv_Str_AutismServerInfoOv_2(plugin, this.update_Map.get(key));
         }

         this.sync_Map.put(key, copyEvidence);
         this.flush_Map
            .put(
               key,
               this.init_AutismServerInfoOv_Str_AutismServerInfoOv(
                  cachedConfidence.get(key), this.init_AutismServerInfoOv_AutismServerInfoOv(this.update_Map.get(key))
               )
            );
         this.close_Map.put(key, this.init_AutismServerInfoOv_Str(plugin, this.update_Map.get(key), this.flush_Map.get(key)));
      }

      this.sync_J = -1L;
      this.block_V_2();
      this.a_V_2();
      return true;
   }

   private void level_V_2() {
      if (this.poll_Str != null && !this.poll_Str.isBlank() && this.pop_Z && !this.tick_Z && !this.load_List.isEmpty()) {
         Map<String, String> evidenceSnapshot = new LinkedHashMap<>();
         Map<String, String> copyEvidenceSnapshot = new LinkedHashMap<>();

         for (Entry<String, AutismServerInfoOverlay.EvidenceSource> entry : this.update_Map.entrySet()) {
            if (entry.getKey() != null && !entry.getKey().isBlank() && entry.getValue() != null) {
               evidenceSnapshot.put(entry.getKey(), entry.getValue().name());
            }
         }

         for (Entry<String, AutismServerInfoOverlay.EvidenceSource> entry : this.sync_Map.entrySet()) {
            if (entry.getKey() != null && !entry.getKey().isBlank() && entry.getValue() != null) {
               copyEvidenceSnapshot.put(entry.getKey(), entry.getValue().name());
            }
         }

         Map<String, String> confidenceSnapshot = new LinkedHashMap<>();

         for (Entry<String, AutismServerInfoOverlay.MatchConfidence> entry : this.flush_Map.entrySet()) {
            if (entry.getKey() != null && !entry.getKey().isBlank() && entry.getValue() != null) {
               confidenceSnapshot.put(entry.getKey(), entry.getValue().name());
            }
         }

         ServerData current = init_Minecraft.getCurrentServer();
         String serverName = current != null && current.name != null ? current.name : "";
         AutismConfig.getGlobal()
            .putPluginScan(
               this.poll_Str,
               "",
               this.load_List,
               this.init_Map,
               evidenceSnapshot,
               this.load_Map,
               this.read_Map,
               confidenceSnapshot,
               copyEvidenceSnapshot,
               this.open_Map,
               this.init_AutismServerInfoOv.name(),
               this.queue,
               this.buffer,
               this.entry,
               this.node,
               serverName,
               this.poll_Str
            );
      }
   }

   private void frame_V_2() {
      String currentAddress = this.load_Str_11();
      if (!currentAddress.isEmpty()) {
         if (!currentAddress.equals(this.push_Str)) {
            this.push_Str = currentAddress;
         }

         String currentContextSignature = this.read_Str_8();
         boolean sameAddress = currentAddress.equals(this.poll_Str);
         boolean hasCurrentSignature = !currentContextSignature.isEmpty();
         boolean sameContext = sameAddress && currentContextSignature.equals(this.pop_Str);
         AutismServerInfoOverlay.ActiveScanSession session = this.init_AutismServerInfoOv_3;
         if (session != null && this.tick_Z) {
            if (currentAddress.equals(session.init_Str)) {
               session.load_Str = currentContextSignature;
               this.pop_Str = currentContextSignature;
               return;
            }

            this.read_V_3("server-changed");
         }

         if (sameAddress && this.pop_Z && hasCurrentSignature && !sameContext) {
            this.pop_Str = currentContextSignature;
            this.level_V_2();
         } else if (!this.put_Z || !sameAddress || !sameContext && hasCurrentSignature) {
            if (!sameAddress || hasCurrentSignature && !sameContext) {
               this.put_Z = false;
            }

            if ((!sameContext || !this.pop_Z && !this.tick_Z)
               && (!sameAddress || hasCurrentSignature || !this.pop_Z && !this.tick_Z)
               && !this.init_Z_Str_Str_3(currentAddress, currentContextSignature)) {
               this.init_V_Str_Str_3(currentAddress, currentContextSignature);
            }
         }
      }
   }

   private String load_Str_2(String plugin) {
      return PluginSignatureCatalog.init_Str(plugin);
   }

   private int init_I_AutismServerInfoOv_3(AutismServerInfoOverlay.EvidenceSource evidence) {
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

   private int init_I_AutismServerInfoOv(AutismServerInfoOverlay.MatchConfidence confidence) {
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

   private AutismServerInfoOverlay.EvidenceSource init_AutismServerInfoOv_AutismServerInfoOv_AutismServerInfoOv_3(
      AutismServerInfoOverlay.EvidenceSource current, AutismServerInfoOverlay.EvidenceSource candidate
   ) {
      if (current == null) {
         return candidate == null ? AutismServerInfoOverlay.EvidenceSource.UNKNOWN : candidate;
      } else if (candidate == null) {
         return current;
      } else {
         return this.init_I_AutismServerInfoOv_3(candidate) < this.init_I_AutismServerInfoOv_3(current) ? candidate : current;
      }
   }

   private int load_I(AutismServerInfoOverlay.EvidenceSource evidence) {
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

   private AutismServerInfoOverlay.EvidenceSource load_AutismServerInfoOv_AutismServerInfoOv_AutismServerInfoOv(
      AutismServerInfoOverlay.EvidenceSource current, AutismServerInfoOverlay.EvidenceSource candidate
   ) {
      if (candidate == null) {
         return current == null ? AutismServerInfoOverlay.EvidenceSource.UNKNOWN : current;
      } else if (current == null) {
         return candidate;
      } else {
         return this.load_I(candidate) < this.load_I(current) ? candidate : current;
      }
   }

   private boolean init_Z_AutismServerInfoOv(AutismServerInfoOverlay.EvidenceSource evidence) {
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

   private boolean load_Z(AutismServerInfoOverlay.EvidenceSource evidence) {
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

   private AutismServerInfoOverlay.MatchConfidence init_AutismServerInfoOv_AutismServerInfoOv_AutismServerInfoOv(
      AutismServerInfoOverlay.MatchConfidence current, AutismServerInfoOverlay.MatchConfidence candidate
   ) {
      if (current == null) {
         return candidate == null ? AutismServerInfoOverlay.MatchConfidence.UNKNOWN : candidate;
      } else if (candidate == null) {
         return current;
      } else {
         return this.init_I_AutismServerInfoOv(candidate) < this.init_I_AutismServerInfoOv(current) ? candidate : current;
      }
   }

   private AutismServerInfoOverlay.MatchConfidence init_AutismServerInfoOv_AutismServerInfoOv(AutismServerInfoOverlay.EvidenceSource evidence) {
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

   private boolean read(AutismServerInfoOverlay.EvidenceSource evidence) {
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

   private boolean init_Z_AutismServerInfoOv_4(AutismServerInfoOverlay.PluginAccumulator entry) {
      return entry == null
         ? false
         : entry.init_AutismServerInfoOv_2 == AutismServerInfoOverlay.DetectionType.FEATURE || this.read(entry.init_AutismServerInfoOv_3);
   }

   private boolean load(AutismServerInfoOverlay.PluginAccumulator entry) {
      return entry != null
         && entry.init_AutismServerInfoOv_2 == AutismServerInfoOverlay.DetectionType.PLUGIN
         && !this.read(entry.init_AutismServerInfoOv_3)
         && entry.init_AutismServerInfoOv_3 != AutismServerInfoOverlay.EvidenceSource.COMMAND_GUI;
   }

   private int init_I_AutismServerInfoOv_5(AutismServerInfoOverlay.PluginAccumulator entry) {
      if (entry == null) {
         return 2147483647;
      }

      int score = this.init_I_AutismServerInfoOv_3(entry.init_AutismServerInfoOv_3) * 100 + this.init_I_AutismServerInfoOv(entry.init_AutismServerInfoOv) * 10;
      if (entry.init_AutismServerInfoOv_2 == AutismServerInfoOverlay.DetectionType.FEATURE) {
         score += 1000;
      }

      if (this.read(entry.init_AutismServerInfoOv_3)) {
         score += 500;
      }

      if (!entry.init_Z) {
         score += 25;
      }

      String name = entry.init_Str == null ? "" : entry.init_Str.trim();
      if (name.startsWith("/")) {
         score += 75;
      }

      return score;
   }

   private AutismServerInfoOverlay.EvidenceSource init_AutismServerInfoOv_Str_2(String value) {
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

   private AutismServerInfoOverlay.MatchConfidence init_AutismServerInfoOv_Str_AutismServerInfoOv(
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

   private int init_I_AutismServerInfoOv_Z(AutismServerInfoOverlay.MatchConfidence confidence, boolean hovered) {
      if (hovered) {
         return UiColorPalette.softWhite();
      }

      int var10000;
      switch (confidence == null ? AutismServerInfoOverlay.MatchConfidence.UNKNOWN : confidence) {
         case EXACT:
            var10000 = UiColorPalette.teal();
            break;
         case STRONG:
            var10000 = UiColorPalette.cyan();
            break;
         case FEATURE:
            var10000 = UiColorPalette.warningYellow();
            break;
         case UNKNOWN:
            var10000 = this.init_CharEncoder.load(autismclient.gui.f.b.l.write);
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   private String init_Str_AutismServerInfoOv(AutismServerInfoOverlay.MatchConfidence confidence) {
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

   private Map<String, AutismServerInfoOverlay.PluginAccumulator> init_Map_3() {
      Map<String, AutismServerInfoOverlay.PluginAccumulator> entries = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

      for (String plugin : this.load_List) {
         if (plugin != null && !plugin.isBlank()) {
            List<String> existing = this.load_List(plugin);
            AutismServerInfoOverlay.EvidenceSource evidence = this.update_Map
               .getOrDefault(
                  this.load_Str_2(plugin),
                  existing != null && !existing.isEmpty() ? AutismServerInfoOverlay.EvidenceSource.ROOT_HINT : AutismServerInfoOverlay.EvidenceSource.UNKNOWN
               );
            List<String> existingChannels = this.write_List_2(plugin);
            List<String> existingGuis = this.read_List_2(plugin);
            AutismServerInfoOverlay.MatchConfidence confidence = this.flush_Map
               .getOrDefault(this.load_Str_2(plugin), this.init_AutismServerInfoOv_AutismServerInfoOv(evidence));
            AutismServerInfoOverlay.DetectionType resultKind = this.init_AutismServerInfoOv_Str(plugin, evidence, confidence);
            this.init_Z_Map(
               entries, null, plugin, existing, existingChannels, existingGuis, existing != null && !existing.isEmpty(), evidence, confidence, resultKind
            );
            String key = this.load_Str_2(plugin);
            AutismServerInfoOverlay.PluginAccumulator entry = entries.get(key);
            if (entry != null) {
               AutismServerInfoOverlay.EvidenceSource copyEvidence = this.sync_Map
                  .getOrDefault(key, this.init_AutismServerInfoOv_Str_AutismServerInfoOv_2(plugin, evidence));
               if (this.load_I(copyEvidence) < this.load_I(entry.load_AutismServerInfoOv)) {
                  entry.load_Set.clear();
               }

               entry.load_AutismServerInfoOv = this.load_AutismServerInfoOv_AutismServerInfoOv_AutismServerInfoOv(entry.load_AutismServerInfoOv, copyEvidence);
               List<String> copyCommands = this.init_List_Map_Str(this.open_Map, plugin);
               if (copyCommands != null) {
                  for (String command : copyCommands) {
                     if (command != null && !command.isBlank()) {
                        entry.load_Set.add(command.trim());
                     }
                  }
               }
            }
         }
      }

      return entries;
   }

   private AutismServerInfoOverlay.ScanStatus init_AutismServerInfoOv_Str(String value) {
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

   private boolean init_Z_Map(
      Map<String, AutismServerInfoOverlay.PluginAccumulator> entries,
      String pluginName,
      Collection<String> commands,
      boolean commandBacked,
      AutismServerInfoOverlay.EvidenceSource evidence
   ) {
      return this.init_Z_Map(
         entries, null, pluginName, commands, List.of(), List.of(), commandBacked, evidence, this.init_AutismServerInfoOv_AutismServerInfoOv(evidence)
      );
   }

   private boolean init_Z_Map(
      Map<String, AutismServerInfoOverlay.PluginAccumulator> entries,
      String pluginName,
      Collection<String> commands,
      Collection<String> channels,
      boolean commandBacked,
      AutismServerInfoOverlay.EvidenceSource evidence
   ) {
      return this.init_Z_Map(
         entries, null, pluginName, commands, channels, List.of(), commandBacked, evidence, this.init_AutismServerInfoOv_AutismServerInfoOv(evidence)
      );
   }

   private boolean init_Z_Map(
      Map<String, AutismServerInfoOverlay.PluginAccumulator> entries,
      String explicitKey,
      String pluginName,
      Collection<String> commands,
      Collection<String> channels,
      boolean commandBacked,
      AutismServerInfoOverlay.EvidenceSource evidence
   ) {
      return this.init_Z_Map(
         entries, explicitKey, pluginName, commands, channels, List.of(), commandBacked, evidence, this.init_AutismServerInfoOv_AutismServerInfoOv(evidence)
      );
   }

   private boolean init_Z_Map(
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
      return this.init_Z_Map(
         entries, explicitKey, pluginName, commands, channels, guis, commandBacked, evidence, confidence, AutismServerInfoOverlay.DetectionType.PLUGIN
      );
   }

   private boolean init_Z_Map(
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
         String key = explicitKey != null && !explicitKey.isBlank() ? this.load_Str_2(explicitKey) : this.load_Str_2(cleanName);
         if (!key.isEmpty() && (!sync_Set.contains(key) || evidence == AutismServerInfoOverlay.EvidenceSource.PAYLOAD_CHANNEL)) {
            AutismServerInfoOverlay.PluginAccumulator entry = entries.get(key);
            if (entry == null) {
               if (entries.size() >= 1000) {
                  return false;
               }

               entry = new AutismServerInfoOverlay.PluginAccumulator();
               entries.put(key, entry);
            }

            String beforeName = entry.init_Str;
            boolean beforeBacked = entry.init_Z;
            AutismServerInfoOverlay.EvidenceSource beforeEvidence = entry.init_AutismServerInfoOv_3;
            AutismServerInfoOverlay.EvidenceSource beforeCopyEvidence = entry.load_AutismServerInfoOv;
            AutismServerInfoOverlay.MatchConfidence beforeConfidence = entry.init_AutismServerInfoOv;
            AutismServerInfoOverlay.DetectionType beforeKind = entry.init_AutismServerInfoOv_2;
            int beforeCommandCount = entry.init_Set.size();
            int beforeCopyCommandCount = entry.load_Set.size();
            int beforeChannelCount = entry.read.size();
            int beforeGuiCount = entry.write.size();
            boolean candidateHasBetterCopySource = this.load_I(evidence) < this.load_I(entry.load_AutismServerInfoOv);
            boolean allowReadableRename = !this.load_Z(entry.load_AutismServerInfoOv) || candidateHasBetterCopySource;
            if (candidateHasBetterCopySource) {
               entry.load_Set.clear();
            }

            if (entry.init_Str == null
               || entry.init_Str.isBlank()
               || commandBacked && !entry.init_Z
               || allowReadableRename && this.load_Z_Str_Str_2(cleanName, entry.init_Str)) {
               entry.init_Str = cleanName;
            }

            entry.init_AutismServerInfoOv_2 = this.init_AutismServerInfoOv_AutismServerInfoOv_AutismServerInfoOv_2(entry.init_AutismServerInfoOv_2, resultKind);
            if (commandBacked) {
               entry.init_Z = true;
            }

            entry.init_AutismServerInfoOv_3 = this.init_AutismServerInfoOv_AutismServerInfoOv_AutismServerInfoOv_3(entry.init_AutismServerInfoOv_3, evidence);
            entry.load_AutismServerInfoOv = this.load_AutismServerInfoOv_AutismServerInfoOv_AutismServerInfoOv(entry.load_AutismServerInfoOv, evidence);
            entry.init_AutismServerInfoOv = this.init_AutismServerInfoOv_AutismServerInfoOv_AutismServerInfoOv(entry.init_AutismServerInfoOv, confidence);
            this.update_Map.put(key, this.init_AutismServerInfoOv_AutismServerInfoOv_AutismServerInfoOv_3(this.update_Map.get(key), evidence));
            this.sync_Map.put(key, this.load_AutismServerInfoOv_AutismServerInfoOv_AutismServerInfoOv(this.sync_Map.get(key), evidence));
            this.flush_Map.put(key, this.init_AutismServerInfoOv_AutismServerInfoOv_AutismServerInfoOv(this.flush_Map.get(key), confidence));
            this.close_Map.put(key, this.init_AutismServerInfoOv_AutismServerInfoOv_AutismServerInfoOv_2(this.close_Map.get(key), resultKind));
            if (commands != null) {
               for (String command : commands) {
                  if (command != null) {
                     String cleanCommand = command.trim();
                     if (!cleanCommand.isEmpty() && (entry.init_Set.size() < 256 || entry.init_Set.contains(cleanCommand))) {
                        entry.init_Set.add(cleanCommand);
                     }

                     if (!cleanCommand.isEmpty()
                        && this.init_Z_AutismServerInfoOv(evidence)
                        && this.load_I(evidence) <= this.load_I(entry.load_AutismServerInfoOv)
                        && (entry.load_Set.size() < 256 || entry.load_Set.contains(cleanCommand))) {
                        entry.load_Set.add(cleanCommand);
                     }
                  }
               }
            }

            if (channels != null) {
               for (String channel : channels) {
                  if (channel != null) {
                     String cleanChannel = channel.trim();
                     if (!cleanChannel.isEmpty() && (entry.read.size() < 64 || entry.read.contains(cleanChannel))) {
                        entry.read.add(cleanChannel);
                     }
                  }
               }
            }

            if (guis != null) {
               for (String gui : guis) {
                  if (gui != null) {
                     String cleanGui = gui.trim();
                     if (!cleanGui.isEmpty() && (entry.write.size() < 64 || entry.write.contains(cleanGui))) {
                        entry.write.add(cleanGui);
                     }
                  }
               }
            }

            return !Objects.equals(beforeName, entry.init_Str)
               || beforeBacked != entry.init_Z
               || beforeEvidence != entry.init_AutismServerInfoOv_3
               || beforeCopyEvidence != entry.load_AutismServerInfoOv
               || beforeConfidence != entry.init_AutismServerInfoOv
               || beforeKind != entry.init_AutismServerInfoOv_2
               || beforeCommandCount != entry.init_Set.size()
               || beforeCopyCommandCount != entry.load_Set.size()
               || beforeChannelCount != entry.read.size()
               || beforeGuiCount != entry.write.size();
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private boolean init_Z_Map_2(
      Map<String, AutismServerInfoOverlay.PluginAccumulator> target, String explicitKey, AutismServerInfoOverlay.PluginAccumulator source
   ) {
      if (target != null && source != null && source.init_Str != null && !source.init_Str.isBlank()) {
         boolean changed = this.init_Z_Map(
            target,
            explicitKey,
            source.init_Str,
            source.init_Set,
            source.read,
            source.write,
            source.init_Z,
            source.init_AutismServerInfoOv_3,
            source.init_AutismServerInfoOv,
            source.init_AutismServerInfoOv_2
         );
         String key = explicitKey != null && !explicitKey.isBlank() ? this.load_Str_2(explicitKey) : this.load_Str_2(source.init_Str);
         AutismServerInfoOverlay.PluginAccumulator entry = target.get(key);
         if (entry != null) {
            AutismServerInfoOverlay.EvidenceSource beforeCopyEvidence = entry.load_AutismServerInfoOv;
            int beforeCopyCommandCount = entry.load_Set.size();
            boolean betterCopySource = this.load_I(source.load_AutismServerInfoOv) < this.load_I(entry.load_AutismServerInfoOv);
            if (betterCopySource) {
               entry.load_Set.clear();
            }

            entry.load_AutismServerInfoOv = this.load_AutismServerInfoOv_AutismServerInfoOv_AutismServerInfoOv(
               entry.load_AutismServerInfoOv, source.load_AutismServerInfoOv
            );
            Collection<String> sourceCopyCommands = source.load_Set.isEmpty() && this.init_Z_AutismServerInfoOv(source.load_AutismServerInfoOv)
               ? source.init_Set
               : source.load_Set;
            if (sourceCopyCommands != null && this.load_I(source.load_AutismServerInfoOv) <= this.load_I(entry.load_AutismServerInfoOv)) {
               for (String command : sourceCopyCommands) {
                  if (command != null && !command.isBlank()) {
                     entry.load_Set.add(command.trim());
                  }
               }
            }

            changed |= beforeCopyEvidence != entry.load_AutismServerInfoOv || beforeCopyCommandCount != entry.load_Set.size();
         }

         return changed;
      } else {
         return false;
      }
   }

   private boolean init_Z_Map_Map(Map<String, AutismServerInfoOverlay.PluginAccumulator> target, Map<String, AutismServerInfoOverlay.PluginAccumulator> source) {
      if (target != null && source != null && !source.isEmpty()) {
         boolean changed = false;

         for (Entry<String, AutismServerInfoOverlay.PluginAccumulator> entry : source.entrySet()) {
            changed |= this.init_Z_Map_2(target, entry.getKey(), entry.getValue());
         }

         return changed;
      } else {
         return false;
      }
   }

   private boolean init_Z_Set(Set<String> commands) {
      if (commands != null && !commands.isEmpty()) {
         TreeSet<String> normalized = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

         for (String command : commands) {
            String token = this.read_Str_3(command);
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

   private String init_Str_Map_Str(Map<String, AutismServerInfoOverlay.PluginAccumulator> entries, String command) {
      if (entries != null && !entries.isEmpty()) {
         String token = this.read_Str_3(command);
         if (token.isBlank()) {
            return null;
         }

         String bestKey = null;
         int bestScore = 2147483647;

         for (Entry<String, AutismServerInfoOverlay.PluginAccumulator> mapEntry : new ArrayList<>(entries.entrySet())) {
            AutismServerInfoOverlay.PluginAccumulator entry = mapEntry.getValue();
            if (entry != null && entry.init_Set != null && entry.init_Set.contains(token)) {
               int score = this.init_I_AutismServerInfoOv_5(entry);
               if (score < bestScore) {
                  bestScore = score;
                  bestKey = mapEntry.getKey();
               }
            }
         }

         String aliasedPlugin = tick_Map.get(token);
         if (aliasedPlugin != null && !aliasedPlugin.isBlank()) {
            String aliasedKey = this.load_Str_2(aliasedPlugin);
            AutismServerInfoOverlay.PluginAccumulator aliasedEntry = entries.get(aliasedKey);
            if (this.load(aliasedEntry)) {
               int score = this.init_I_AutismServerInfoOv_5(aliasedEntry);
               if (bestKey == null || score <= bestScore || !this.load(entries.get(bestKey))) {
                  bestKey = aliasedKey;
               }
            }
         }

         return bestKey;
      } else {
         return null;
      }
   }

   private AutismServerInfoOverlay.PluginAccumulator init_AutismServerInfoOv_Str_4(String command) {
      String token = this.read_Str_3(command);
      AutismServerInfoOverlay.PluginAccumulator entry = new AutismServerInfoOverlay.PluginAccumulator();
      entry.init_Str = "/" + token;
      entry.init_Z = true;
      entry.init_AutismServerInfoOv_2 = AutismServerInfoOverlay.DetectionType.PLUGIN;
      entry.init_AutismServerInfoOv_3 = AutismServerInfoOverlay.EvidenceSource.SCANNER_AUTOCOMPLETE;
      entry.load_AutismServerInfoOv = AutismServerInfoOverlay.EvidenceSource.UNKNOWN;
      entry.init_AutismServerInfoOv = AutismServerInfoOverlay.MatchConfidence.FEATURE;
      entry.init_Set.add(token);
      return entry;
   }

   private boolean init_Z_Map(Map<String, AutismServerInfoOverlay.PluginAccumulator> entries) {
      if (entries != null && !entries.isEmpty()) {
         boolean changed = false;

         for (AutismServerInfoOverlay.PluginAccumulator entry : entries.values()) {
            if (entry != null) {
               changed |= this.init_Z_Set(entry.init_Set);
               changed |= this.init_Z_Set(entry.load_Set);
            }
         }

         TreeSet<String> allCommands = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

         for (AutismServerInfoOverlay.PluginAccumulator entry : entries.values()) {
            if (entry != null && entry.init_Set != null) {
               allCommands.addAll(entry.init_Set);
            }
         }

         Map<String, String> ownerByCommand = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

         for (String command : allCommands) {
            String ownerKey = this.init_Str_Map_Str(entries, command);
            if (ownerKey != null && !ownerKey.isBlank()) {
               ownerByCommand.put(command, ownerKey);
            }
         }

         for (Entry<String, AutismServerInfoOverlay.PluginAccumulator> mapEntry : new ArrayList<>(entries.entrySet())) {
            String key = mapEntry.getKey();
            AutismServerInfoOverlay.PluginAccumulator entry = mapEntry.getValue();
            if (key != null && entry != null && !entry.init_Set.isEmpty()) {
               for (String command : new ArrayList<>(entry.init_Set)) {
                  String ownerKey = ownerByCommand.get(command);
                  if (ownerKey != null && !ownerKey.equals(key)) {
                     AutismServerInfoOverlay.PluginAccumulator owner = entries.get(ownerKey);
                     if (owner != null) {
                        if (owner.init_Set.add(command)) {
                           changed = true;
                        }

                        if (this.init_Z_AutismServerInfoOv(owner.load_AutismServerInfoOv) && owner.load_Set.add(command)) {
                           changed = true;
                        }

                        if (!entry.write.isEmpty()) {
                           int beforeGuiCount = owner.write.size();
                           owner.write.addAll(entry.write);
                           changed |= beforeGuiCount != owner.write.size();
                        }

                        entry.init_Set.remove(command);
                        entry.load_Set.remove(command);
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
            } else if (this.init_Z_AutismServerInfoOv_4(entry)) {
               if (entry.init_Set.isEmpty()) {
                  removeKeys.add(key);
               } else if (entry.init_Set.size() == 1) {
                  String command = entry.init_Set.iterator().next();
                  String expectedName = "/" + command;
                  if (!expectedName.equals(entry.init_Str)) {
                     entry.init_Str = expectedName;
                     changed = true;
                  }

                  if (entry.init_AutismServerInfoOv_2 != AutismServerInfoOverlay.DetectionType.PLUGIN) {
                     entry.init_AutismServerInfoOv_2 = AutismServerInfoOverlay.DetectionType.PLUGIN;
                     changed = true;
                  }

                  if (entry.init_AutismServerInfoOv_3 != AutismServerInfoOverlay.EvidenceSource.SCANNER_AUTOCOMPLETE) {
                     entry.init_AutismServerInfoOv_3 = AutismServerInfoOverlay.EvidenceSource.SCANNER_AUTOCOMPLETE;
                     changed = true;
                  }

                  if (entry.load_AutismServerInfoOv != AutismServerInfoOverlay.EvidenceSource.UNKNOWN) {
                     entry.load_AutismServerInfoOv = AutismServerInfoOverlay.EvidenceSource.UNKNOWN;
                     entry.load_Set.clear();
                     changed = true;
                  }

                  entry.init_AutismServerInfoOv = AutismServerInfoOverlay.MatchConfidence.FEATURE;
               } else {
                  for (String command : entry.init_Set) {
                     String hintKey = this.load_Str_2("command_" + command);
                     if (!entries.containsKey(hintKey) && !addEntries.containsKey(hintKey)) {
                        addEntries.put(hintKey, this.init_AutismServerInfoOv_Str_4(command));
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

   private int init_I_Map(Map<String, AutismServerInfoOverlay.PluginAccumulator> entries) {
      if (entries != null && !entries.isEmpty()) {
         int count = 0;

         for (AutismServerInfoOverlay.PluginAccumulator entry : entries.values()) {
            if (entry != null && entry.init_Str != null && !entry.init_Str.isBlank()) {
               count++;
            }
         }

         return count;
      } else {
         return 0;
      }
   }

   private void init_V_AutismServerInfoOv_Str_2(AutismServerInfoOverlay.ActiveScanSession session, String message) {
      if (clear_Z) {
         String generation = session == null ? "-" : Long.toString(session.init_J);
         System.out.println("[AUTISM][PluginScan][gen=" + generation + "] " + message);
      }
   }

   private AutismServerInfoOverlay.DetectionType init_AutismServerInfoOv_AutismServerInfoOv_AutismServerInfoOv_2(
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

   private AutismServerInfoOverlay.DetectionType init_AutismServerInfoOv_Str(
      String plugin, AutismServerInfoOverlay.EvidenceSource evidence, AutismServerInfoOverlay.MatchConfidence confidence
   ) {
      String key = this.load_Str_2(plugin);
      AutismServerInfoOverlay.DetectionType stored = this.close_Map.get(key);
      if (stored != null) {
         return stored;
      }

      if (confidence != AutismServerInfoOverlay.MatchConfidence.EXACT && confidence != AutismServerInfoOverlay.MatchConfidence.STRONG) {
         if (evidence != AutismServerInfoOverlay.EvidenceSource.PAYLOAD_CHANNEL
            && evidence != AutismServerInfoOverlay.EvidenceSource.VERSION_HINT
            && evidence != AutismServerInfoOverlay.EvidenceSource.NAMESPACE) {
            String lower = plugin == null ? "" : plugin.trim().toLowerCase(Locale.ROOT);

            for (String featureLabel : run_Map.values()) {
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

   private AutismServerInfoOverlay.EvidenceSource init_AutismServerInfoOv_Str_AutismServerInfoOv_2(
      String plugin, AutismServerInfoOverlay.EvidenceSource evidence
   ) {
      if (evidence == AutismServerInfoOverlay.EvidenceSource.UNKNOWN || evidence == null) {
         List<String> commands = this.init_List_Map_Str(this.init_Map, plugin);
         if (commands != null && !commands.isEmpty()) {
            return AutismServerInfoOverlay.EvidenceSource.ROOT_HINT;
         }
      }

      return this.load_Z(evidence) ? evidence : AutismServerInfoOverlay.EvidenceSource.UNKNOWN;
   }

   private List<String> init_List_Map_Str(Map<String, List<String>> map, String plugin) {
      if (map != null && !map.isEmpty() && plugin != null && !plugin.isBlank()) {
         List<String> direct = map.get(plugin);
         if (direct != null) {
            return direct;
         }

         String key = this.load_Str_2(plugin);

         for (Entry<String, List<String>> entry : map.entrySet()) {
            if (entry.getKey() != null && this.load_Str_2(entry.getKey()).equals(key)) {
               return entry.getValue();
            }
         }

         return null;
      } else {
         return null;
      }
   }

   private boolean load_Z_Str_Str_2(String candidate, String current) {
      if (candidate == null || candidate.isBlank()) {
         return false;
      }

      if (current != null && !current.isBlank()) {
         String c = candidate.trim();
         String old = current.trim();
         if (c.equalsIgnoreCase(old)) {
            return false;
         }

         String cKey = this.load_Str_2(c);
         String oldKey = this.load_Str_2(old);
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

   private void init_V_Map_Z(Map<String, AutismServerInfoOverlay.PluginAccumulator> entries, boolean resetSelection) {
      Map<String, AutismServerInfoOverlay.PluginAccumulator> normalizedEntries = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
      if (entries != null) {
         normalizedEntries.putAll(entries);
      }

      this.init_Z_Map(normalizedEntries);
      this.load_List.clear();
      this.init_Map.clear();
      this.load_Map.clear();
      this.read_Map.clear();
      this.update_Map.clear();
      this.sync_Map.clear();
      this.open_Map.clear();
      this.flush_Map.clear();
      this.close_Map.clear();
      List<AutismServerInfoOverlay.PluginAccumulator> sortedEntries = new ArrayList<>(normalizedEntries.values());
      sortedEntries.removeIf(entryx -> entryx == null || entryx.init_Str == null || entryx.init_Str.isBlank());
      sortedEntries.sort((a, b) -> String.CASE_INSENSITIVE_ORDER.compare(a.init_Str, b.init_Str));

      for (AutismServerInfoOverlay.PluginAccumulator entry : sortedEntries) {
         String plugin = entry.init_Str;
         List<String> commands = new ArrayList<>(entry.init_Set);
         commands.removeIf(cmd -> cmd == null || cmd.isBlank());
         commands.sort(String.CASE_INSENSITIVE_ORDER);
         this.load_List.add(plugin);
         this.init_Map.put(plugin, commands);
         this.load_Map.put(plugin, this.init_List_Collection(entry.read));
         List<String> guis = new ArrayList<>(entry.write);
         guis.removeIf(gui -> gui == null || gui.isBlank());
         guis.sort(String.CASE_INSENSITIVE_ORDER);
         this.read_Map.put(plugin, List.copyOf(guis));
         this.update_Map
            .put(
               this.load_Str_2(plugin), entry.init_AutismServerInfoOv_3 == null ? AutismServerInfoOverlay.EvidenceSource.UNKNOWN : entry.init_AutismServerInfoOv_3
            );
         this.sync_Map
            .put(this.load_Str_2(plugin), entry.load_AutismServerInfoOv == null ? AutismServerInfoOverlay.EvidenceSource.UNKNOWN : entry.load_AutismServerInfoOv);
         List<String> copyCommands = new ArrayList<>(entry.load_Set);
         copyCommands.removeIf(cmd -> cmd == null || cmd.isBlank());
         copyCommands.sort(String.CASE_INSENSITIVE_ORDER);
         this.open_Map.put(plugin, List.copyOf(copyCommands));
         this.flush_Map
            .put(
               this.load_Str_2(plugin),
               entry.init_AutismServerInfoOv == null
                  ? this.init_AutismServerInfoOv_AutismServerInfoOv(entry.init_AutismServerInfoOv_3)
                  : entry.init_AutismServerInfoOv
            );
         this.close_Map
            .put(
               this.load_Str_2(plugin), entry.init_AutismServerInfoOv_2 == null ? AutismServerInfoOverlay.DetectionType.PLUGIN : entry.init_AutismServerInfoOv_2
            );
      }

      if (resetSelection) {
         this.init_CacheEntry.jumpTo(0, 0);
         this.sync_Str = null;
      } else if (this.sync_Str != null && !this.flush_Z_2(this.sync_Str)) {
         this.sync_Str = null;
      }

      this.a_V_2();
   }

   private boolean init_Z_Str_6(String key) {
      for (String namespace : load_StrArr) {
         if (namespace.equalsIgnoreCase(key)) {
            return true;
         }
      }

      return false;
   }

   private String read_Str_3(String raw) {
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

   private boolean load_Z_3(String token) {
      if (token != null && !token.isBlank()) {
         String clean = token.trim().toLowerCase(Locale.ROOT);
         if (clean.length() < 2) {
            return false;
         } else if (!sync_Set.contains(clean) && !flush_Set.contains(clean)) {
            return this.init_Set_3().contains(clean)
               ? false
               : clean.chars().allMatch(ch -> Character.isLetterOrDigit(ch) || ch == 95 || ch == 45 || ch == 46 || ch == 58);
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private boolean init_Z_Map(Map<String, AutismServerInfoOverlay.PluginAccumulator> entries, String rawCommand, AutismServerInfoOverlay.EvidenceSource source) {
      String token = this.read_Str_3(rawCommand);
      if (!this.load_Z_3(token)) {
         return false;
      }

      this.load_V_3(token);
      if (token.contains(":")) {
         String[] parts = token.split(":", 2);
         String namespace = parts[0].trim().toLowerCase(Locale.ROOT);
         String command = parts.length > 1 ? parts[1].trim().toLowerCase(Locale.ROOT) : "";
         return this.load_Z_3(namespace) && !sync_Set.contains(namespace)
            ? this.init_Z_Map(
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
      if (this.init_Z_Str_6(token)) {
         plugin = token;
         confidence = AutismServerInfoOverlay.MatchConfidence.EXACT;
         evidence = AutismServerInfoOverlay.EvidenceSource.NAMESPACE;
      } else if (tick_Map.containsKey(token)) {
         plugin = tick_Map.get(token);
         confidence = AutismServerInfoOverlay.MatchConfidence.STRONG;
         evidence = AutismServerInfoOverlay.EvidenceSource.ROOT_HINT;
      }

      if (plugin != null && !plugin.isBlank()) {
         return this.init_Z_Map(
            entries, null, plugin, List.of(token), List.of(), List.of(), true, evidence, confidence, AutismServerInfoOverlay.DetectionType.PLUGIN
         );
      }

      String ownerKey = this.init_Str_Map_Str(entries, token);
      if (ownerKey != null) {
         AutismServerInfoOverlay.PluginAccumulator owner = entries.get(ownerKey);
         if (owner == null) {
            return false;
         }

         boolean changed = owner.init_Set.add(token);
         if (this.init_Z_AutismServerInfoOv(owner.load_AutismServerInfoOv)) {
            changed |= owner.load_Set.add(token);
         }

         return changed;
      } else {
         return source != AutismServerInfoOverlay.EvidenceSource.USER_AUTOCOMPLETE
               && source != AutismServerInfoOverlay.EvidenceSource.COMMAND_GUI
               && source != AutismServerInfoOverlay.EvidenceSource.SCANNER_AUTOCOMPLETE
               && !run_Map.containsKey(token)
            ? false
            : this.init_Z_Map(
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

   private Set<String> init_Set_3() {
      if (this.auditActiveKnownPlayers != null) {
         return this.auditActiveKnownPlayers;
      }

      Set<String> names = new HashSet<>();
      if (init_Minecraft != null) {
         if (init_Minecraft.player != null && init_Minecraft.player.getGameProfile() != null) {
            auditAddPlayerName(names, init_Minecraft.player.getGameProfile().name());
         }

         // Tab-list entries only: level players include NPCs whose names can match real commands.
         if (init_Minecraft.getConnection() != null) {
            for (PlayerInfo player : init_Minecraft.getConnection().getOnlinePlayers()) {
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
      if (init_Minecraft != null && init_Minecraft.player != null && init_Minecraft.player.getGameProfile() != null) {
         String name = init_Minecraft.player.getGameProfile().name();
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
      if (state.init_AutismServerInfoOv == AutismServerInfoOverlay.ProbeKind.ROOT) {
         return true;
      }
      String query = state.init_Str == null ? "" : state.init_Str.trim();
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
         String key = this.load_Str_2(clean);
         if (!key.isEmpty() && (tick_Map.containsKey(key) || this.init_Z_Str_6(key))) {
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

   private boolean init_Z_Str_AutismServerInfoOv(String candidate, AutismServerInfoOverlay.ProbeKind kind) {
      if (candidate == null) {
         return false;
      } else {
         String clean = candidate.trim();
         String key = this.load_Str_2(clean);
         if (key.isEmpty()) {
            return false;
         } else if (key.length() < 2) {
            return false;
         } else if (clean.contains(" ") || clean.contains("/") || clean.contains(":")) {
            return false;
         } else if (sync_Set.contains(key)) {
            return false;
         }

         Set<String> players = this.init_Set_3();
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

   private void init_V_Str_11(String raw) {
      String token = this.read_Str_3(raw);
      if (this.load_Z_3(token)) {
         this.load_V_3(token);
      }
   }

   private void load_V_3(String token) {
      if (this.load_Set.size() < 8192 || this.load_Set.contains(token)) {
         this.load_Set.add(token);
      }
   }

   private void init_V_Map(Map<String, AutismServerInfoOverlay.PluginAccumulator> entries) {
      this.load_V(entries);
   }

   private void load_V(Map<String, AutismServerInfoOverlay.PluginAccumulator> entries) {
      if (!this.load_Set.isEmpty()) {
         Map<String, Set<String>> inferredMatches = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

         for (String command : this.load_Set) {
            String plugin = null;
            if (this.init_Z_Str_6(command)) {
               plugin = command;
            } else if (tick_Map.containsKey(command)) {
               plugin = tick_Map.get(command);
            }

            if (plugin != null && !plugin.isBlank()) {
               inferredMatches.computeIfAbsent(plugin, unused -> new TreeSet<>(String.CASE_INSENSITIVE_ORDER)).add(command);
            }
         }

         for (Entry<String, Set<String>> entry : inferredMatches.entrySet()) {
            this.init_Z_Map(entries, entry.getKey(), entry.getValue(), true, AutismServerInfoOverlay.EvidenceSource.ROOT_HINT);
         }
      }
   }

   private boolean init_Z_Map_Str(Map<String, AutismServerInfoOverlay.PluginAccumulator> entries, String rawCommand) {
      String token = this.read_Str_3(rawCommand);
      if (!this.read_Z_2(token)) {
         return false;
      }

      if (token.contains(":")) {
         return false;
      }

      String ownerKey = this.init_Str_Map_Str(entries, token);
      if (ownerKey != null) {
         AutismServerInfoOverlay.PluginAccumulator owner = entries.get(ownerKey);
         if (owner != null && !owner.init_Set.contains(token)) {
            owner.init_Set.add(token);
            if (this.init_Z_AutismServerInfoOv(owner.load_AutismServerInfoOv)) {
               owner.load_Set.add(token);
            }

            return true;
         } else {
            return false;
         }
      } else {
         return this.init_Z_Map(
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

   private boolean read_Z_2(String token) {
      if (!this.load_Z_3(token)) {
         return false;
      } else {
         String clean = token.trim().toLowerCase(Locale.ROOT);
         if (clean.length() < 3) {
            return false;
         } else if (write_Set.contains(clean)) {
            return false;
         } else if (!sync_Set.contains(clean) && !flush_Set.contains(clean)) {
            return run_Map.containsKey(clean) ? false : !tick_Map.containsKey(clean) && !this.init_Z_Str_6(clean);
         } else {
            return false;
         }
      }
   }

   private boolean load_Z(Map<String, AutismServerInfoOverlay.PluginAccumulator> entries) {
      String address = this.load_Str_11();
      if (address != null && !address.isBlank()) {
         boolean changed = false;

         for (PluginSignatureCatalog.Inner_CachePool fingerprint : PluginSignatureCatalog.init_List(address, this.close_Str_2())) {
            if (fingerprint != null && fingerprint.init_Str() != null && !fingerprint.init_Str().isBlank()) {
               changed |= this.init_Z_Map_bQ_CachePool(entries, fingerprint);
               AutismServerInfoOverlay.MatchConfidence confidence = this.init_AutismServerInfoOv_bQ_CachePool(fingerprint);
               changed |= this.init_Z_Map(
                  entries,
                  fingerprint.load(),
                  fingerprint.init_Str(),
                  List.of(),
                  fingerprint.init_List(),
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

   private AutismServerInfoOverlay.MatchConfidence init_AutismServerInfoOv_bQ_CachePool(PluginSignatureCatalog.Inner_CachePool fingerprint) {
      String basis = fingerprint == null ? "" : fingerprint.read();
      return "embedded".equalsIgnoreCase(basis) ? AutismServerInfoOverlay.MatchConfidence.STRONG : AutismServerInfoOverlay.MatchConfidence.EXACT;
   }

   private boolean init_Z_Map_bQ_CachePool(Map<String, AutismServerInfoOverlay.PluginAccumulator> entries, PluginSignatureCatalog.Inner_CachePool fingerprint) {
      if (entries != null && !entries.isEmpty() && fingerprint != null) {
         Set<String> keywords = this.init_Set_bQ_CachePool(fingerprint);
         if (keywords.isEmpty()) {
            return false;
         }

         boolean changed = false;
         List<String> removeKeys = new ArrayList<>();

         for (Entry<String, AutismServerInfoOverlay.PluginAccumulator> mapEntry : new ArrayList<>(entries.entrySet())) {
            String key = mapEntry.getKey();
            AutismServerInfoOverlay.PluginAccumulator entry = mapEntry.getValue();
            if (key != null && key.startsWith("feature") && entry != null && this.init_Z_AutismServerInfoOv_Set(entry, keywords)) {
               changed |= this.init_Z_Map(
                  entries,
                  fingerprint.load(),
                  fingerprint.init_Str(),
                  entry.init_Set,
                  fingerprint.init_List(),
                  entry.write,
                  entry.init_Z,
                  AutismServerInfoOverlay.EvidenceSource.PAYLOAD_CHANNEL,
                  this.init_AutismServerInfoOv_bQ_CachePool(fingerprint)
               );
               removeKeys.add(key);
            }
         }

         for (String key : removeKeys) {
            entries.remove(key);
            this.update_Map.remove(key);
            this.flush_Map.remove(key);
            this.close_Map.remove(key);
            changed = true;
         }

         return changed;
      } else {
         return false;
      }
   }

   private Set<String> init_Set_bQ_CachePool(PluginSignatureCatalog.Inner_CachePool fingerprint) {
      LinkedHashSet<String> keywords = new LinkedHashSet<>();
      this.init_V_Set_Str_3(keywords, fingerprint.init_Str());
      this.init_V_Set_Str_3(keywords, fingerprint.load());
      if (fingerprint.init_List() != null) {
         for (String channel : fingerprint.init_List()) {
            this.init_V_Set_Str_3(keywords, channel);
         }
      }

      keywords.removeIf(keyword -> keyword.length() < 3);
      return keywords;
   }

   private void init_V_Set_Str_3(Set<String> out, String text) {
      if (out != null && text != null && !text.isBlank()) {
         String lower = text.toLowerCase(Locale.ROOT);

         for (Entry<String, String> entry : run_Map.entrySet()) {
            String key = entry.getKey().toLowerCase(Locale.ROOT);
            String label = entry.getValue().toLowerCase(Locale.ROOT);
            if (lower.contains(key) || lower.contains(label.replace(" ", "")) || lower.contains(label)) {
               out.add(key);
               out.add(label);
            }
         }
      }
   }

   private boolean init_Z_AutismServerInfoOv_Set(AutismServerInfoOverlay.PluginAccumulator entry, Set<String> keywords) {
      if (entry != null && keywords != null && !keywords.isEmpty()) {
         List<String> evidenceText = new ArrayList<>();
         evidenceText.add(entry.init_Str);
         evidenceText.addAll(entry.init_Set);
         evidenceText.addAll(entry.write);

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

   private void block_V_2() {
      if (this.tick_Z) {
         if (this.load_Z(this.poll_Map)) {
            this.init_Z_Map(this.poll_Map);
            AutismServerInfoOverlay.ActiveScanSession session = this.init_AutismServerInfoOv_3;
            if (session != null) {
               session.read_J = System.currentTimeMillis();
            }
         }
      } else if (this.pop_Z) {
         String address = this.load_Str_11();
         long revision = PluginSignatureCatalog.init_J(address, this.close_Str_2());
         if (revision != this.sync_J) {
            this.sync_J = revision;
            if (revision > 0L) {
               Map<String, AutismServerInfoOverlay.PluginAccumulator> entries = this.init_Map_3();
               if (this.load_Z(entries)) {
                  this.init_V_Map_Z(entries, false);
                  if (this.pop_Z) {
                     this.level_V_2();
                  }
               }
            }
         }
      }
   }

   private int init_I_Map(
      Map<Integer, AutismServerInfoOverlay.ProbeSpec> probes,
      Set<String> seen,
      int nextId,
      AutismServerInfoOverlay.ProbeKind kind,
      String... baseCommands
   ) {
      for (String base : baseCommands) {
         if (base != null && !base.isBlank()) {
            String trimmed = base.trim();
            nextId = this.init_I_Map(probes, seen, nextId, trimmed, kind, null);
            nextId = this.init_I_Map(probes, seen, nextId, trimmed + " ", kind, null);
         }
      }

      return nextId;
   }

   private Map<Integer, AutismServerInfoOverlay.ProbeSpec> load_Map() {
      Map<Integer, AutismServerInfoOverlay.ProbeSpec> probes = new LinkedHashMap<>();
      Set<String> seen = new HashSet<>();
      int nextId = this.value_I_2();
      nextId = this.init_I_Map(probes, seen, nextId, "/", AutismServerInfoOverlay.ProbeKind.ROOT, null);
      nextId = this.init_I_Map(probes, seen, nextId, "/ ", AutismServerInfoOverlay.ProbeKind.ROOT, null);
      nextId = this.init_I_Map(probes, seen, nextId, AutismServerInfoOverlay.ProbeKind.PLUGIN_LIST, "/plugins", "/pl", "/bukkit:plugins", "/bukkit:pl");
      nextId = this.init_I_Map(
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
      nextId = this.init_I_Map(probes, seen, nextId, AutismServerInfoOverlay.ProbeKind.HELP, "/help", "/?", "/bukkit:help", "/minecraft:help");

      for (int i = 0; i < "abcdefghijklmnopqrstuvwxyz0123456789".length(); i++) {
         char prefix = "abcdefghijklmnopqrstuvwxyz0123456789".charAt(i);
         String rootProbe = "/" + prefix;
         nextId = this.init_I_Map(probes, seen, nextId, rootProbe, AutismServerInfoOverlay.ProbeKind.ROOT, String.valueOf(prefix));
         nextId = this.init_I_Map(probes, seen, nextId, "/help " + prefix, AutismServerInfoOverlay.ProbeKind.HELP, String.valueOf(prefix));
         nextId = this.init_I_Map(probes, seen, nextId, "/? " + prefix, AutismServerInfoOverlay.ProbeKind.HELP, String.valueOf(prefix));
      }

      for (String namespace : load_StrArr) {
         nextId = this.init_I_Map(probes, seen, nextId, "/" + namespace + ":", AutismServerInfoOverlay.ProbeKind.NAMESPACE, namespace);
      }

      for (String plugin : sync_StrArr) {
         nextId = this.init_I_Map(probes, seen, nextId, "/version " + plugin, AutismServerInfoOverlay.ProbeKind.VERSION, plugin);
         nextId = this.init_I_Map(probes, seen, nextId, "/ver " + plugin, AutismServerInfoOverlay.ProbeKind.VERSION, plugin);
         nextId = this.init_I_Map(probes, seen, nextId, "/help " + plugin, AutismServerInfoOverlay.ProbeKind.HELP, plugin);
      }

      return probes;
   }

   private int value_I_2() {
      return this.init_bR.init();
   }

   public boolean push_Z_12() {
      return init_Minecraft != null && init_Minecraft.getConnection() != null && !this.load_Str_11().isBlank();
   }

   public boolean pop_Z_7() {
      try {
         if (init_Minecraft != null && init_Minecraft.getConnection() != null) {
            CommandDispatcher<?> dispatcher = init_Minecraft.getConnection().getCommands();
            return dispatcher != null && dispatcher.getRoot() != null && !dispatcher.getRoot().getChildren().isEmpty();
         } else {
            return false;
         }
      } catch (Exception ignored) {
         return false;
      }
   }

   public boolean tick_Z_6() {
      if (!this.push_Z_12()) {
         return false;
      } else {
         String address = this.load_Str_11();
         String context = this.read_Str_8();
         this.frame_V_2();
         if (this.tick_Z) {
            return true;
         } else if (context.equals(this.open_Str) && this.pop_Z && this.init_AutismServerInfoOv == AutismServerInfoOverlay.ScanStatus.COMPLETE) {
            return true;
         } else {
            long cachedAt = AutismConfig.getGlobal().getPluginScanTimestampByAddress(address);
            boolean fresh = cachedAt > 0L && System.currentTimeMillis() - cachedAt < 5184000000L;
            if (fresh && this.pop_Z && this.init_AutismServerInfoOv == AutismServerInfoOverlay.ScanStatus.COMPLETE) {
               this.open_Str = context;
               return true;
            } else {
               this.open_Str = context;
               AutismConfig.getGlobal().removePluginScan(address);
               this.fill_V_2();
               this.scan_V_2();
               return this.tick_Z;
            }
         }
      }
   }

   public void sync_V_18() {
      this.open_Str = null;
   }

   public void flush_V_17() {
      this.level_V_2();
      this.open_Str = null;
      this.init_V_Str_Str_3(null, "");
      this.tick_Str = "";
      this.run_Str = "";
      this.get_Str = "";
      this.push_Str = "";
      this.prev_V_7();
   }

   private void read_V_3(String reason) {
      AutismServerInfoOverlay.ActiveScanSession session = this.init_AutismServerInfoOv_3;
      if (session != null) {
         session.init_bS.init();
         this.init_V_AutismServerInfoOv_Str_2(session, "cancel reason=" + reason);
      }

      this.init_AutismServerInfoOv_3 = null;
      this.tick_Z = false;
      this.write_List.clear();
      this.update_List.clear();
      this.run_Z = false;
   }

   private void init_V_AutismServerInfoOv(AutismServerInfoOverlay.ActiveScanSession session, String stage, Throwable error) {
      if (session != null && !session.init_bS.load()) {
         session.sync++;
         String detail = error == null ? "unknown" : error.getClass().getSimpleName() + ": " + error.getMessage();
         this.close_Str = stage + " failed";
         this.init_V_AutismServerInfoOv_Str_2(session, "error stage=" + stage + " detail=" + detail);
         init_Logger.warn("Plugin scan {} failed during {}", session.init_J, stage, error);
      }
   }

   private void chunk_V_2() {
      this.init_V_AutismServerInfoOv_Str(null, "");
   }

   private void init_V_AutismServerInfoOv_Str(AutismServerInfoOverlay.ScanStatus requestedOutcome, String issue) {
      AutismServerInfoOverlay.ActiveScanSession session = this.init_AutismServerInfoOv_3;
      if (session != null && session.init_bS.init()) {
         this.flush(true);
         Map<String, AutismServerInfoOverlay.PluginAccumulator> finalEntries = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
         boolean processingFailed = false;

         try {
            this.init_Z_Map_Map(finalEntries, this.push_Map);
            this.init_Z_Map_Map(finalEntries, this.poll_Map);
            this.load_Z(finalEntries);
            this.init_Z_Map(finalEntries);
         } catch (RuntimeException error) {
            processingFailed = true;
            session.sync++;
            this.close_Str = "Final processing failed";
            init_Logger.warn("Plugin scan {} failed during final processing", session.init_J, error);
            this.init_V_AutismServerInfoOv_Str_2(session, "error stage=final-processing detail=" + error.getClass().getSimpleName());
         }

         AutismServerInfoOverlay.ScanStatus outcome = requestedOutcome;
         if (outcome == null) {
            outcome = !processingFailed && !session.init_bS.init(session.sync, session.update, session.init_Set.size())
               ? AutismServerInfoOverlay.ScanStatus.COMPLETE
               : AutismServerInfoOverlay.ScanStatus.PARTIAL;
         }

         if (processingFailed && finalEntries.isEmpty()) {
            outcome = AutismServerInfoOverlay.ScanStatus.FAILED;
         }

         boolean infrastructureFailure = processingFailed || session.sync > 0 || session.update > 0 || !session.init_Set.isEmpty();
         boolean restorePrevious = finalEntries.isEmpty() && infrastructureFailure && !session.init_Map.isEmpty();
         if (restorePrevious) {
            outcome = AutismServerInfoOverlay.ScanStatus.FAILED;
         }

         this.init_V_AutismServerInfoOv_Str_2(
            session,
            "final roots="
               + session.flush
               + " suggestionReplies="
               + session.close
               + " suggestionEntries="
               + session.open
               + " sent="
               + session.load_I
               + " answered="
               + session.read_I
               + " retried="
               + session.write_I
               + " failed="
               + session.update
               + " legacy="
               + this.init_I_Map(this.push_Map)
               + " working="
               + this.init_I_Map(this.poll_Map)
               + " final="
               + this.init_I_Map(finalEntries)
               + " outcome="
               + outcome
         );

         try {
            if (restorePrevious) {
               this.init_V_Map_Z(session.init_Map, false);
            } else if (outcome != AutismServerInfoOverlay.ScanStatus.FAILED || !finalEntries.isEmpty()) {
               this.init_V_Map_Z(finalEntries, true);
            }
         } catch (RuntimeException error) {
            outcome = AutismServerInfoOverlay.ScanStatus.FAILED;
            this.close_Str = "Could not publish results";
            init_Logger.warn("Plugin scan {} could not publish results", session.init_J, error);
            if (!session.init_Map.isEmpty()) {
               try {
                  this.init_V_Map_Z(session.init_Map, false);
               } catch (RuntimeException restoreError) {
                  init_Logger.warn("Plugin scan {} could not restore previous results", session.init_J, restoreError);
               }
            }
         }

         session.load_Str = this.read_Str_8();
         this.poll_Str = session.init_Str;
         this.pop_Str = session.load_Str;
         this.queue = session.init_I;
         this.buffer = session.read_I;
         this.entry = session.write_I;
         this.node = session.update;
         this.init_AutismServerInfoOv_3 = null;
         this.tick_Z = false;
         this.pop_Z = true;
         this.init_AutismServerInfoOv = outcome;
         if (issue != null && !issue.isBlank()) {
            this.close_Str = issue;
         } else if (outcome == AutismServerInfoOverlay.ScanStatus.COMPLETE) {
            this.close_Str = "";
         } else if (this.close_Str.isBlank()) {
            this.close_Str = outcome == AutismServerInfoOverlay.ScanStatus.PARTIAL ? "Some probes did not reply" : "Scan failed";
         }

         this.load_Set.clear();
         this.poll_Map.clear();
         this.push_Map.clear();
         this.pool_V_2();
         if (outcome != AutismServerInfoOverlay.ScanStatus.FAILED || !this.load_List.isEmpty()) {
            this.level_V_2();
         }

         this.store_V_2();
         this.flags = this.state;
      }
   }

   private void table_V_2() {
      if (autismclient.modules.aD.read_Z()) {
         this.poll_V_9();
      } else {
         AutismServerInfoOverlay.ActiveScanSession session = this.init_AutismServerInfoOv_3;
         if (this.tick_Z && session != null && !session.init_bS.load() && init_Minecraft.getConnection() != null) {
            long now = System.currentTimeMillis();
            if (now >= session.write_J) {
               AutismServerInfoOverlay.QueuedProbe request = session.init_Deque.pollFirst();
               if (request != null) {
                  try {
                     ServerboundCommandSuggestionPacket packet = new ServerboundCommandSuggestionPacket(
                        request.init_I, request.init_AutismServerInfoOv.init_Str
                     );
                     init_Minecraft.getConnection().send(packet);
                     session.load_I++;
                     session.write_J = now + this.flags;
                  } catch (Exception error) {
                     session.write_J = now + this.flags;
                     if (session.init_bS.init(request.load)) {
                        session.write_I++;
                        this.init_V_AutismServerInfoOv_Str_2(session, "retry probe=" + request.init_I + " reason=" + error.getClass().getSimpleName());
                        init_Logger.warn("Plugin scan {} will retry probe {}", session.init_J, request.init_I, error);
                        session.init_Deque
                           .addLast(new AutismServerInfoOverlay.QueuedProbe(request.init_I, request.init_AutismServerInfoOv, request.load + 1));
                     } else {
                        session.update++;
                        session.init_Set.remove(request.init_I);
                        session.load_Map.remove(request.init_I);
                        this.init_V_AutismServerInfoOv(session, "probe send", error);
                     }
                  }
               }
            }
         }
      }
   }

   private int cache_I_2() {
      AutismServerInfoOverlay.ActiveScanSession session = this.init_AutismServerInfoOv_3;
      return session == null ? 0 : Math.max(0, session.init_I);
   }

   private long init_J_2() {
      AutismServerInfoOverlay.ActiveScanSession session = this.init_AutismServerInfoOv_3;
      int retryCount = session == null ? 0 : session.write_I;
      return Math.max(this.flags, (long)(this.cache_I_2() + retryCount) * this.flags);
   }

   private long load_J() {
      AutismServerInfoOverlay.ActiveScanSession session = this.init_AutismServerInfoOv_3;
      if (session == null || session.load_J <= 0L) {
         return 0L;
      }

      if (!session.init_Deque.isEmpty()) {
         return 0L;
      }

      long candidate = session.write_J - this.flags;
      return Math.max(session.load_J, candidate);
   }

   private long read_J() {
      return Math.max(12000L, this.init_J_2() + 1400L + 700L);
   }

   private int init_I_I_2(int from, int to, float t) {
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

   private int state_I_2() {
      AutismServerInfoOverlay.ActiveScanSession session = this.init_AutismServerInfoOv_3;
      int total = this.cache_I_2();
      return session == null ? 0 : Math.max(0, Math.min(total, session.load_I));
   }

   private int queue_I_2() {
      AutismServerInfoOverlay.ActiveScanSession session = this.init_AutismServerInfoOv_3;
      int total = this.cache_I_2();
      return session == null ? 0 : Math.max(0, Math.min(total, session.read_I));
   }

   private float init_F_2() {
      if (this.pop_Z) {
         return 1.0F;
      }

      int total = this.cache_I_2();
      if (total <= 0) {
         return 0.0F;
      }

      float sentRatio = (float)this.state_I_2() / total;
      float answeredRatio = (float)this.queue_I_2() / total;
      AutismServerInfoOverlay.ActiveScanSession session = this.init_AutismServerInfoOv_3;
      if (session != null && !session.init_Deque.isEmpty()) {
         float progress = 0.8F * sentRatio;
         progress += 0.05F * answeredRatio;
         return Math.max(0.0F, Math.min(0.85F, progress));
      }

      float progress = 0.85F;
      long finishedSendingAt = this.load_J();
      long waitWindow = 1400L;
      if (finishedSendingAt > 0L && waitWindow > 0L) {
         long now = System.currentTimeMillis();
         float settle = Math.max(0.0F, Math.min(1.0F, (float)(now - finishedSendingAt) / (float)waitWindow));
         progress += 0.14F * settle;
      }

      return Math.max(0.0F, Math.min(0.99F, progress));
   }

   private int buffer_I_2() {
      return (int)this.poll_Map.values().stream().filter(entry -> entry != null && entry.init_Str != null && !entry.init_Str.isBlank()).count();
   }

   private int entry_I_2() {
      return this.tick_Z ? this.buffer_I_2() : this.load_List.size();
   }

   private String write_Str_5() {
      int foundCount = this.entry_I_2();
      return foundCount > 0 ? "Scanning plugins | found " + foundCount : "Scanning plugins";
   }

   private String update_Str_3() {
      AutismServerInfoOverlay.ActiveScanSession session = this.init_AutismServerInfoOv_3;
      return session != null && !session.init_Deque.isEmpty() ? "Sending probes" : "Analyzing replies";
   }

   private int node_I_2() {
      AutismServerInfoOverlay.ActiveScanSession session = this.init_AutismServerInfoOv_3;
      return session != null && !session.init_Deque.isEmpty() ? UiColorPalette.warningYellow() : UiColorPalette.cyan();
   }

   private String sync_Str_2() {
      AutismServerInfoOverlay.ActiveScanSession session = this.init_AutismServerInfoOv_3;
      int total = Math.max(1, this.cache_I_2());
      return session != null && !session.init_Deque.isEmpty() ? this.state_I_2() + "/" + total : this.queue_I_2() + "/" + total;
   }

   private void store_V_2() {
      if (!this.get_Z) {
         this.get_Z = true;
         int count = this.load_List.size();
         if (count <= 0) {
            AutismClientMessaging.sendPrefixed("Plugin probing finished: no plugins found.");
         } else if (count == 1) {
            AutismClientMessaging.sendPrefixed("Plugin probing finished: found 1 plugin.");
         } else {
            AutismClientMessaging.sendPrefixed("Plugin probing finished: found " + count + " plugins.");
         }
      }
   }

   public boolean run_Z_5() {
      return this.tick_Z;
   }

   public boolean put_Z_4() {
      return this.write_Z || this.tick_Z || this.pop_Z || !this.load_List.isEmpty();
   }

   public void init_V_GuiGraphicsExtract(GuiGraphicsExtractor ctx) {
      if (this.run_Z_5() && init_Minecraft != null && init_Minecraft.getWindow() != null && this.init_Font != null) {
         autismclient.gui.ui.layout.LayoutViewport viewport = this.init_TextFormat.init_RingBuffer();
         String status = this.write_Str_5();
         String phase = this.update_Str_3();
         String detail = this.sync_Str_2();
         int progressPercent = Math.max(0, Math.min(100, Math.round(this.init_F_2() * 100.0F)));
         String percentComponent = progressPercent + "%";
         int screenW = Math.round(viewport.width());
         int statusWidth = autismclient.gui.f.b.k.init(
            this.init_Font,
            status,
            this.init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.write),
            this.init_CharEncoder.load(autismclient.gui.f.b.l.write)
         );
         int phaseWidth = autismclient.gui.f.b.k.init(
            this.init_Font,
            phase,
            this.init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.read),
            this.init_CharEncoder.load(autismclient.gui.f.b.l.read)
         );
         int detailWidth = autismclient.gui.f.b.k.init(
            this.init_Font, detail, this.init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.write), this.node_I_2()
         );
         int percentWidth = autismclient.gui.f.b.k.init(
            this.init_Font,
            percentComponent,
            this.init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.read),
            this.init_CharEncoder.load(autismclient.gui.f.b.l.read)
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
               ctx, this.init_Font, viewport, this.init_CharEncoder, 0.0F, 0.0F, 0.0F
            );
            int border = this.init_CharEncoder.get();
            int headerAccent = this.init_CharEncoder.run();
            autismclient.gui.f.PacketUtil.init(
               ctx,
               autismclient.gui.f.NetHelper.init(boxX, boxY, boxW, boxH),
               14,
               bannerContext.applyAlpha(this.init_CharEncoder.poll()),
               bannerContext.applyAlpha(this.init_CharEncoder.pop()),
               bannerContext.applyAlpha(this.init_CharEncoder.poll()),
               bannerContext.applyAlpha(border),
               bannerContext.applyAlpha(headerAccent),
               1.0F
            );
            autismclient.gui.f.b.k.init(
               ctx,
               this.init_Font,
               status,
               this.init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.write),
               this.init_CharEncoder.load(autismclient.gui.f.b.l.write),
               innerX,
               boxY + 3,
               false
            );
            autismclient.gui.f.b.k.init(
               ctx,
               this.init_Font,
               percentComponent,
               this.init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.read),
               this.node_I_2(),
               boxX + boxW - 8 - percentWidth,
               boxY + 3,
               false
            );
            autismclient.gui.ui.widgets.ProgressBarRenderer.init(
               autismclient.gui.f.UiHelper.init(ctx, this.init_Font, -1, -1), autismclient.gui.f.NetHelper.init(innerX, barY, barW, barH), this.init_F_2()
            );
            autismclient.gui.f.b.k.init(
               ctx,
               this.init_Font,
               phase,
               this.init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.read),
               this.init_CharEncoder.load(autismclient.gui.f.b.l.read),
               innerX,
               barY + 13,
               false
            );
            autismclient.gui.f.b.k.init(
               ctx,
               this.init_Font,
               detail,
               this.init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.write),
               this.node_I_2(),
               boxX + boxW - 8 - detailWidth,
               barY + 13,
               false
            );
         } finally {
            viewport.popTransform(ctx);
         }
      }
   }

   private void drain_V_2() {
      AutismServerInfoOverlay.ActiveScanSession session = this.init_AutismServerInfoOv_3;
      if (this.tick_Z && session != null && !session.init_bS.load()) {
         try {
            this.table_V_2();
            long now = System.currentTimeMillis();
            boolean allProbesSent = session.init_Deque.isEmpty();
            boolean allResponsesReceived = session.init_Set.isEmpty();
            long finishedSendingAt = this.load_J();
            long settleWindow = allResponsesReceived ? 1400L : Math.max(1400L, 450L);
            boolean settledAfterSend = allProbesSent && finishedSendingAt > 0L && now - finishedSendingAt >= settleWindow;
            boolean settledAfterLastResponse = session.read_J > 0L && now - session.read_J >= 700L;
            boolean timedOut = now - session.load_J >= this.read_J();
            if (allProbesSent && settledAfterSend && (!allResponsesReceived || settledAfterLastResponse) || timedOut) {
               AutismServerInfoOverlay.ScanStatus outcome = !session.init_bS.init(session.sync, session.update, session.init_Set.size())
                  ? AutismServerInfoOverlay.ScanStatus.COMPLETE
                  : AutismServerInfoOverlay.ScanStatus.PARTIAL;
               this.init_V_AutismServerInfoOv_Str(outcome, timedOut && !allResponsesReceived ? "Some probes timed out" : "");
            }
         } catch (RuntimeException error) {
            this.init_V_AutismServerInfoOv(session, "lifecycle", error);
            this.init_V_AutismServerInfoOv_Str(AutismServerInfoOverlay.ScanStatus.PARTIAL, "Scan recovered from an error");
         }
      }
   }

   public void close_V_15() {
      try {
         this.flush(false);
         this.frame_V_2();
         this.block_V_2();
         if (this.tick_Z) {
            this.drain_V_2();
         }
      } catch (Throwable error) {
         AutismServerInfoOverlay.ActiveScanSession session = this.init_AutismServerInfoOv_3;
         if (session != null) {
            this.init_V_AutismServerInfoOv(session, "background tick", error);
            this.init_V_AutismServerInfoOv_Str(AutismServerInfoOverlay.ScanStatus.PARTIAL, "Scan recovered from an error");
         } else {
            init_Logger.warn("Plugin scanner background update failed", error);
         }
      }
   }

   private void init_V_RootCommandNode(RootCommandNode<?> root) {
      Map<String, AutismServerInfoOverlay.PluginAccumulator> entries = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
      entries.putAll(this.poll_Map);
      Map<String, AutismServerInfoOverlay.PluginAccumulator> legacyEntries = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
      legacyEntries.putAll(this.push_Map);
      this.auditActiveKnownPlayers = this.init_Set_3();

      try {
         for (CommandNode<?> child : root.getChildren()) {
            String name = child.getName();
            AutismServerInfoOverlay.ActiveScanSession session = this.init_AutismServerInfoOv_3;
            if (session != null) {
               session.flush++;
            }

            if (name.contains(":")) {
               String[] parts = name.split(":", 2);
               String ns = parts[0].toLowerCase();
               String cmd = parts[1];
               if (!sync_Set.contains(ns)) {
                  this.init_Z_Map(legacyEntries, ns, List.of(cmd), true, AutismServerInfoOverlay.EvidenceSource.COMMAND_TREE);
                  this.init_Z_Map(entries, ns + ":" + cmd, AutismServerInfoOverlay.EvidenceSource.COMMAND_TREE);
                  this.init_V_Str_11(cmd);
               }
            } else {
               this.init_V_Str_11(name);
               String token = this.read_Str_3(name);
               if (run_Map.containsKey(token)) {
                  this.init_Z_Map(entries, name, AutismServerInfoOverlay.EvidenceSource.COMMAND_TREE);
               }
            }
         }

         this.load_V(legacyEntries);
         this.init_V_Map(entries);
         this.init_Z_Map_Map(entries, legacyEntries);
         this.init_Z_Map(entries);
      } finally {
         this.auditActiveKnownPlayers = null;
      }

      this.push_Map.clear();
      this.push_Map.putAll(legacyEntries);
      this.poll_Map.clear();
      this.poll_Map.putAll(entries);
      AutismServerInfoOverlay.ActiveScanSession session = this.init_AutismServerInfoOv_3;
      this.init_V_AutismServerInfoOv_Str_2(
         session,
         "command tree roots="
            + (session == null ? 0 : session.flush)
            + " legacy="
            + this.init_I_Map(this.push_Map)
            + " working="
            + this.init_I_Map(this.poll_Map)
      );
   }

   public void open_V_12() {
      this.init_V_Str_Str_3(null, "");
      this.init_BitSetUtil.init_BitSetUtil_Str("");
      this.init_BitSetUtil.init_V_Z(false);
      this.load_Str = null;
      this.read_Str = null;
      this.buffer_V_2();
   }

   public void poll_V_9() {
      this.read_V_3("hard-lock");
      this.poll_Map.clear();
      this.push_Map.clear();
      this.pop_Map.clear();
      if (!this.load_List.isEmpty()) {
         this.pop_Z = true;
      }

      this.set_Z = true;
   }

   private void fill_V_2() {
      this.read_V_3("restart");
      this.pop_Z = false;
      this.put_Z = false;
      this.get_Z = false;
      this.init_AutismServerInfoOv = AutismServerInfoOverlay.ScanStatus.NONE;
      this.close_Str = "";
      this.load_Set.clear();
      this.poll_Map.clear();
      this.push_Map.clear();
      this.sync_Str = null;
      this.init_BitSetUtil.init_BitSetUtil_Str("");
      this.init_BitSetUtil.init_V_Z(false);
      this.init_CacheEntry.jumpTo(0, 0);
   }

   private void mark_V_2() {
      if (!this.sync_Z && init_Minecraft.getConnection() != null) {
         String address = this.load_Str_11();
         AutismConfig.getGlobal().removePluginScan(address);
         this.fill_V_2();
         this.read(true);
         this.write_V_24();
      }
   }

   private int init_I_Map(
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

   private void scan_V_2() {
      this.read(false);
   }

   private void read(boolean forceLive) {
      if (!this.sync_Z && !autismclient.modules.aD.read_Z()) {
         if (!forceLive) {
            this.frame_V_2();
         }

         if ((forceLive || !this.pop_Z) && !this.tick_Z) {
            if (init_Minecraft.getConnection() == null) {
               this.pop_Z = true;
               this.init_AutismServerInfoOv = AutismServerInfoOverlay.ScanStatus.FAILED;
               this.close_Str = "Not connected";
            } else {
               this.flags = this.state;
               this.poll_Str = this.load_Str_11();
               this.pop_Str = this.read_Str_8();
               long startedAt = System.currentTimeMillis();
               AutismServerInfoOverlay.ActiveScanSession session = new AutismServerInfoOverlay.ActiveScanSession(
                  this.read_J++, this.poll_Str, this.pop_Str, startedAt, this.init_Map_3()
               );
               session.write_J = startedAt + this.flags;
               this.init_AutismServerInfoOv_3 = session;
               this.start = 1;
               this.tick_Z = true;
               this.pop_Z = false;
               this.load_Set.clear();
               this.poll_Map.clear();
               this.push_Map.clear();
               this.init_AutismServerInfoOv = AutismServerInfoOverlay.ScanStatus.NONE;
               this.close_Str = "";
               this.task_V_2();

               try {
                  CommandDispatcher<?> dispatcher = init_Minecraft.getConnection().getCommands();
                  if (dispatcher != null) {
                     RootCommandNode<?> root = dispatcher.getRoot();
                     if (root != null && !root.getChildren().isEmpty()) {
                        this.init_V_RootCommandNode(root);
                     }
                  }

                  this.load_Z(this.poll_Map);
                  Map<Integer, AutismServerInfoOverlay.ProbeSpec> probes = this.load_Map();
                  session.init_I = probes.size();

                  for (Entry<Integer, AutismServerInfoOverlay.ProbeSpec> probe : probes.entrySet()) {
                     session.init_Set.add(probe.getKey());
                     session.load_Map.put(probe.getKey(), probe.getValue());
                     session.init_Deque.addLast(new AutismServerInfoOverlay.QueuedProbe(probe.getKey(), probe.getValue()));
                  }

                  this.init_V_AutismServerInfoOv_Str_2(session, "start probes=" + session.init_I + " delay=" + this.flags + "ms");
                  this.drain_V_2();
               } catch (Exception error) {
                  this.init_V_AutismServerInfoOv(session, "startup", error);
                  this.init_V_AutismServerInfoOv_Str(AutismServerInfoOverlay.ScanStatus.PARTIAL, "Scan recovered from a startup error");
               }
            }
         }
      }
   }

   public void init_V_Packet(Packet<?> packet) {
      if (packet != null && !autismclient.modules.aD.init_Z() && "ServerboundCommandSuggestionPacket".equals(packet.getClass().getSimpleName())) {
         int id = this.init_I_Obj(packet, -1, "getId", "id");
         String command = this.init_Str_Obj_StrArr(packet, "getCommand", "command");
         if (id >= 0 && command != null && !command.isBlank() && auditIsObservableCommandCompletion(command) && !CommandProbeIdAllocator.init(id)) {
            init_Minecraft.execute(() -> {
               try {
                  AutismServerInfoOverlay.ActiveScanSession session = this.init_AutismServerInfoOv_3;
                  if (session != null && session.load_Map.containsKey(id)) {
                     return;
                  }

                  this.pop_Map.put(id, new AutismServerInfoOverlay.ProbeSpec(command, AutismServerInfoOverlay.ProbeKind.USER, null));

                  while (this.pop_Map.size() > 256) {
                     Integer first = this.pop_Map.keySet().iterator().next();
                     this.pop_Map.remove(first);
                  }
               } catch (Throwable t) {
                  this.init_V_Str_Throwable("suggestion-request", t);
               }
            });
         }
      }
   }

   public void load(Packet<?> packet) {
      if (packet != null && !autismclient.modules.aD.init_Z()) {
         String simple = packet.getClass().getSimpleName();
         String command = null;
         if ("ServerboundChatCommandPacket".equals(simple) || "ServerboundChatCommandSignedPacket".equals(simple)) {
            command = this.init_Str_Obj_StrArr(packet, "command", "getCommand");
         } else if ("ServerboundChatPacket".equals(simple)) {
            String message = this.init_Str_Obj_StrArr(packet, "message", "getMessage");
            if (message != null && message.trim().startsWith("/")) {
               command = message.trim().substring(1);
            }
         }

         if (command != null && !command.isBlank()) {
            String root = this.read_Str_3(command);
            if (this.load_Z_3(root)) {
               String fullCommand = command;
               init_Minecraft.execute(() -> {
                  try {
                     this.load_V_Str_Str_2(root, fullCommand);
                     this.init_V_Str_8(fullCommand, AutismServerInfoOverlay.EvidenceSource.USER_AUTOCOMPLETE, null);
                  } catch (Throwable t) {
                     this.init_V_Str_Throwable("outgoing-command", t);
                  }
               });
            }
         }
      }
   }

   public void read(Packet<?> packet) {
      if (packet != null && !autismclient.modules.aD.init_Z() && "ClientboundOpenScreenPacket".equals(packet.getClass().getSimpleName())) {
         String title = this.init_Str_Obj(this.init_Obj_Obj_StrArr(packet, "getTitle", "title"));
         if (title != null && !title.isBlank()) {
            init_Minecraft.execute(() -> {
               try {
                  AutismServerInfoOverlay.CommandHistoryEntry command = this.init_AutismServerInfoOv_J(System.currentTimeMillis());
                  if (command != null) {
                     this.init_V_Str_8(command.load, AutismServerInfoOverlay.EvidenceSource.COMMAND_GUI, title);
                     return;
                  }

                  String feature = this.write_Str_2(title);
                  if (feature == null || feature.isBlank()) {
                     return;
                  }

                  this.read_V_Str_Str_2(feature, title);
               } catch (Throwable t) {
                  this.init_V_Str_Throwable("open-screen", t);
               }
            });
         }
      }
   }

   private void load_V_Str_Str_2(String root, String fullCommand) {
      if (root != null && !root.isBlank()) {
         this.init_Deque.addLast(new AutismServerInfoOverlay.CommandHistoryEntry(root, fullCommand == null ? root : fullCommand, System.currentTimeMillis()));

         while (this.init_Deque.size() > 24) {
            this.init_Deque.removeFirst();
         }
      }
   }

   private AutismServerInfoOverlay.CommandHistoryEntry init_AutismServerInfoOv_J(long now) {
      AutismServerInfoOverlay.CommandHistoryEntry best = null;

      while (!this.init_Deque.isEmpty() && now - this.init_Deque.peekFirst().init_J > 1500L) {
         this.init_Deque.removeFirst();
      }

      for (AutismServerInfoOverlay.CommandHistoryEntry command : this.init_Deque) {
         if (command != null && now - command.init_J <= 1500L) {
            best = command;
         }
      }

      return best;
   }

   private void init_V_Str_8(String rawCommand, AutismServerInfoOverlay.EvidenceSource evidence, String guiTitle) {
      if (rawCommand != null && !rawCommand.isBlank()) {
         init_Minecraft.execute(() -> {
            try {
               if (init_Minecraft.getConnection() == null) {
                  return;
               }

               this.frame_V_2();
               String root = this.read_Str_3(rawCommand);
               if (!this.write_Z_2(root)) {
                  return;
               }

               this.load_V_Str_Str_2(root, rawCommand);
               this.update_List.add(new AutismServerInfoOverlay.CommandEvidence(rawCommand, evidence, guiTitle));
               this.run_Z = true;
               this.flush(false);
            } catch (Throwable t) {
               this.init_V_Str_Throwable("live-command", t);
            }
         });
      }
   }

   private boolean write_Z_2(String rootToken) {
      if (rootToken != null && !rootToken.isBlank()) {
         ClientPacketListener connection = init_Minecraft.getConnection();
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

   private void read_V_Str_Str_2(String feature, String guiTitle) {
   }

   private boolean init_Z_Map_3(Map<String, AutismServerInfoOverlay.PluginAccumulator> entries, String rawCommand, String guiTitle) {
      String token = this.read_Str_3(rawCommand);
      if (!token.isBlank() && guiTitle != null && !guiTitle.isBlank()) {
         String ownerKey = this.init_Str_Map_Str(entries, token);
         if (ownerKey == null) {
            return false;
         }

         AutismServerInfoOverlay.PluginAccumulator entry = entries.get(ownerKey);
         if (entry == null) {
            return false;
         }

         int before = entry.write.size();
         String cleanTitle = guiTitle.trim();
         if (entry.write.size() < 64 || entry.write.contains(cleanTitle)) {
            entry.write.add(cleanTitle);
         }

         if (!this.read(entry.init_AutismServerInfoOv_3)) {
            entry.init_AutismServerInfoOv_3 = this.init_AutismServerInfoOv_AutismServerInfoOv_AutismServerInfoOv_3(
               entry.init_AutismServerInfoOv_3, AutismServerInfoOverlay.EvidenceSource.COMMAND_GUI
            );
         }

         entry.init_AutismServerInfoOv = this.init_AutismServerInfoOv_AutismServerInfoOv_AutismServerInfoOv(
            entry.init_AutismServerInfoOv, AutismServerInfoOverlay.MatchConfidence.FEATURE
         );
         this.update_Map
            .put(ownerKey, this.init_AutismServerInfoOv_AutismServerInfoOv_AutismServerInfoOv_3(this.update_Map.get(ownerKey), entry.init_AutismServerInfoOv_3));
         this.flush_Map
            .put(
               ownerKey,
               this.init_AutismServerInfoOv_AutismServerInfoOv_AutismServerInfoOv(this.flush_Map.get(ownerKey), AutismServerInfoOverlay.MatchConfidence.FEATURE)
            );
         return before != entry.write.size();
      } else {
         return false;
      }
   }

   private void read(Map<String, AutismServerInfoOverlay.PluginAccumulator> entries) {
      this.load_Z(entries);
      this.init_Z_Map(entries);
      if (this.tick_Z) {
         this.poll_Map.clear();
         this.poll_Map.putAll(entries);
         AutismServerInfoOverlay.ActiveScanSession session = this.init_AutismServerInfoOv_3;
         if (session != null) {
            session.read_J = System.currentTimeMillis();
         }

         this.a_V_2();
      } else if (this.pop_Z) {
         this.poll_Str = this.load_Str_11();
         this.pop_Str = this.read_Str_8();
         this.pop_Z = true;
         this.tick_Z = false;
         this.init_V_Map_Z(entries, false);
         this.level_V_2();
      }
   }

   private String write_Str_2(String title) {
      if (title != null && !title.isBlank()) {
         String clean = title.toLowerCase(Locale.ROOT).replaceAll("§.", "");

         for (Entry<String, String> entry : run_Map.entrySet()) {
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

   public void init_V_I_ClientboundCommand(int id, ClientboundCommandSuggestionsPacket packet) {
      if (!CommandProbeIdAllocator.init(id)) {
         init_Minecraft.execute(
            () -> {
               AutismServerInfoOverlay.ActiveScanSession session = this.init_AutismServerInfoOv_3;
               AutismServerInfoOverlay.ProbeSpec probe = session == null ? null : session.load_Map.get(id);
               AutismServerInfoOverlay.ProbeSpec observed = this.pop_Map.remove(id);
               boolean scanProbe = probe != null || session != null && session.init_Set.contains(id);
               if (scanProbe || observed != null) {
                  long capturedGeneration = scanProbe ? session.init_J : -1L;
                  boolean liveObservation = !scanProbe;
                  if (!liveObservation) {
                     String address = this.load_Str_11();
                     if (!this.tick_Z || !session.init(capturedGeneration, address)) {
                        return;
                     }
                  }

                  try {
                     List<net.minecraft.network.protocol.game.ClientboundCommandSuggestionsPacket.Entry> list = packet != null && packet.suggestions() != null
                        ? packet.suggestions()
                        : List.of();
                     if (scanProbe && session != null) {
                        if (session.init_Set.remove(id)) {
                           session.read_I++;
                        }

                        session.load_Map.remove(id);
                        session.read_J = System.currentTimeMillis();
                        session.close++;
                        session.open = session.open + list.size();
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
                        this.run_Z = true;
                     }

                     this.flush(false);
                  } catch (Throwable error) {
                     if (scanProbe && session != null && session.init(capturedGeneration, this.load_Str_11())) {
                        session.init_Set.remove(id);
                        session.load_Map.remove(id);
                        session.update++;
                        this.init_V_AutismServerInfoOv(session, "suggestion reply", error);
                        this.drain_V_2();
                     } else {
                        init_Logger.warn("Plugin autocomplete observation failed", error);
                     }
                  }
               }
            }
         );
      }
   }

   private void flush(boolean force) {
      if (this.run_Z) {
         long now = System.currentTimeMillis();
         if (force || now - this.update_J >= 250L) {
            this.update_J = now;
            this.run_Z = false;

            try {
               this.close(force);
            } catch (Throwable t) {
               this.init_V_Str_Throwable("suggestion-maintenance", t);
            }
         }
      }
   }

   private void close(boolean force) {
      List<AutismServerInfoOverlay.ProbeObservation> batch = new ArrayList<>(this.write_List);
      this.write_List.clear();
      List<AutismServerInfoOverlay.CommandEvidence> liveBatch = new ArrayList<>(this.update_List);
      this.update_List.clear();
      if (!batch.isEmpty() || !liveBatch.isEmpty()) {
         Map<String, AutismServerInfoOverlay.PluginAccumulator> entries = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
         if (this.tick_Z) {
            entries.putAll(this.poll_Map);
         } else {
            entries.putAll(this.init_Map_3());
         }

         Map<String, AutismServerInfoOverlay.PluginAccumulator> legacyEntries = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
         legacyEntries.putAll(this.push_Map);

         boolean scanData = false;
         Set<String> knownPlayers = this.init_Set_3();
         this.auditActiveKnownPlayers = knownPlayers;
         try {
            for (AutismServerInfoOverlay.CommandEvidence live : liveBatch) {
               this.init_Z_Map(entries, live.init_Str, live.init_AutismServerInfoOv);
               if (live.load != null && !live.load.isBlank()) {
                  this.init_Z_Map_3(entries, live.init_Str, live.load);
               }
            }

            Iterator var12 = batch.iterator();

            while (true) {
               AutismServerInfoOverlay.ProbeObservation reply;
               while (true) {
                  if (!var12.hasNext()) {
                     this.load_V(legacyEntries);
                     this.init_V_Map(entries);
                     this.init_Z_Map_Map(entries, legacyEntries);
                     this.init_Z_Map(entries);
                     if (!scanData && !this.tick_Z) {
                        this.read(entries);
                     } else {
                        this.push_Map.clear();
                        this.push_Map.putAll(legacyEntries);
                        this.poll_Map.clear();
                        this.poll_Map.putAll(entries);
                        AutismServerInfoOverlay.ActiveScanSession session = this.init_AutismServerInfoOv_3;
                        this.init_V_AutismServerInfoOv_Str_2(
                           session,
                           "suggestions batch=" + batch.size() + " legacy=" + this.init_I_Map(this.push_Map) + " working=" + this.init_I_Map(this.poll_Map)
                        );
                        if (!force) {
                           this.drain_V_2();
                        }
                     }

                     return;
                  }

                  reply = (AutismServerInfoOverlay.ProbeObservation)var12.next();
                  if (reply.init_Z) {
                     break;
                  }

                  AutismServerInfoOverlay.ActiveScanSession current = this.init_AutismServerInfoOv_3;
                  if (this.tick_Z && current != null && current.init(reply.init_J, this.load_Str_11())) {
                     scanData = true;
                     break;
                  }
               }

               boolean isArgCompletion = (reply.init_AutismServerInfoOv != null
                     && (reply.init_AutismServerInfoOv.init_AutismServerInfoOv == AutismServerInfoOverlay.ProbeKind.PLUGIN_LIST
                        || reply.init_AutismServerInfoOv.init_AutismServerInfoOv == AutismServerInfoOverlay.ProbeKind.VERSION
                        || reply.init_AutismServerInfoOv.init_AutismServerInfoOv == AutismServerInfoOverlay.ProbeKind.HELP
                        || (reply.init_AutismServerInfoOv.init_Str != null && reply.init_AutismServerInfoOv.init_Str.contains(" "))))
                  || (reply.load != null && reply.load.init_Str != null && reply.load.init_Str.contains(" "));
               boolean rootProbe = auditIsRootProbe(reply.init_AutismServerInfoOv) || auditIsRootProbe(reply.load);
               boolean playerBatch = this.auditIsPlayerListBatch(reply.init_List, knownPlayers, isArgCompletion, rootProbe);

               for (String text : reply.init_List) {
                  // Player names never contain ':', so namespaced commands survive a player-list batch.
                  if (playerBatch && !text.contains(":")) {
                     continue;
                  }

                  this.init_V_Map(entries, legacyEntries, reply.init_AutismServerInfoOv, reply.load, reply.init_Z, text);
               }
            }
         } finally {
            this.auditActiveKnownPlayers = null;
         }
      }
   }

   private void init_V_Map(
      Map<String, AutismServerInfoOverlay.PluginAccumulator> entries,
      Map<String, AutismServerInfoOverlay.PluginAccumulator> legacyEntries,
      AutismServerInfoOverlay.ProbeSpec probe,
      AutismServerInfoOverlay.ProbeSpec observed,
      boolean liveObservation,
      String text
   ) {
      String normalizedToken = this.read_Str_3(text);
      if (!normalizedToken.isEmpty()) {
         this.init_V_Str_11(normalizedToken);
      }

      if (text.contains(":")) {
         String[] parts = text.split(":", 2);
         String ns = parts[0].toLowerCase(Locale.ROOT);
         String cmd = parts.length > 1 ? parts[1] : "";
         if (!sync_Set.contains(ns)) {
            this.init_Z_Map(legacyEntries, ns, List.of(cmd), true, AutismServerInfoOverlay.EvidenceSource.COMMAND_TREE);
            this.init_V_Str_11(cmd);
         }

         this.init_Z_Map(entries, text, liveObservation ? AutismServerInfoOverlay.EvidenceSource.USER_AUTOCOMPLETE : AutismServerInfoOverlay.EvidenceSource.COMMAND_TREE);
      } else if (probe != null) {
         if (probe.init_AutismServerInfoOv == AutismServerInfoOverlay.ProbeKind.NAMESPACE && probe.load != null && !normalizedToken.isEmpty()) {
            this.init_Z_Map(legacyEntries, probe.load, List.of(normalizedToken), true, AutismServerInfoOverlay.EvidenceSource.NAMESPACE);
            this.init_Z_Map(entries, probe.load, List.of(normalizedToken), true, AutismServerInfoOverlay.EvidenceSource.NAMESPACE);
         } else if (probe.init_AutismServerInfoOv == AutismServerInfoOverlay.ProbeKind.PLUGIN_LIST
            || probe.init_AutismServerInfoOv == AutismServerInfoOverlay.ProbeKind.VERSION) {
            String pluginCandidate = text.trim();
            String pluginKey = this.load_Str_2(pluginCandidate);
            if (this.init_Z_Str_AutismServerInfoOv(pluginCandidate, probe.init_AutismServerInfoOv) && !tick_Map.containsKey(pluginKey)) {
               AutismServerInfoOverlay.EvidenceSource candidateEvidence = probe.init_AutismServerInfoOv == AutismServerInfoOverlay.ProbeKind.PLUGIN_LIST
                  ? AutismServerInfoOverlay.EvidenceSource.PLUGIN_LIST
                  : AutismServerInfoOverlay.EvidenceSource.VERSION_HINT;
               AutismServerInfoOverlay.MatchConfidence candidateConfidence = this.init_AutismServerInfoOv_AutismServerInfoOv(candidateEvidence);
               this.init_Z_Map(
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
               this.init_Z_Map(
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
         } else if (probe.init_AutismServerInfoOv == AutismServerInfoOverlay.ProbeKind.HELP && probe.load != null && probe.load.length() >= 2 && !normalizedToken.isEmpty()) {
            this.init_Z_Map(legacyEntries, probe.load, List.of(normalizedToken), true, AutismServerInfoOverlay.EvidenceSource.HELP_HINT);
            this.init_Z_Map(entries, probe.load, List.of(normalizedToken), true, AutismServerInfoOverlay.EvidenceSource.HELP_HINT);
         } else if (probe.init_AutismServerInfoOv == AutismServerInfoOverlay.ProbeKind.ROOT && !normalizedToken.isEmpty()) {
            this.init_Z_Map_Str(entries, normalizedToken);
         }
      } else if (observed != null && !normalizedToken.isEmpty()) {
         this.init_Z_Map(entries, normalizedToken, AutismServerInfoOverlay.EvidenceSource.USER_AUTOCOMPLETE);
      }
   }

   public void set_V_9() {
      this.buffer_V_2();
      init_Minecraft.execute(() -> {
         try {
            this.parse_V_2();
         } catch (Throwable t) {
            this.init_V_Str_Throwable("command-tree", t);
         }
      });
   }

   private void parse_V_2() {
      String currentAddress = this.load_Str_11();
      if (!currentAddress.isEmpty()) {
         String currentContextSignature = this.read_Str_8();
         AutismServerInfoOverlay.ActiveScanSession session = this.init_AutismServerInfoOv_3;
         if (session != null && this.tick_Z && currentAddress.equals(session.init_Str)) {
            session.load_Str = currentContextSignature;
            this.pop_Str = currentContextSignature;

            try {
               CommandDispatcher<ClientSuggestionProvider> dispatcher = init_Minecraft.getConnection() == null
                  ? null
                  : init_Minecraft.getConnection().getCommands();
               if (dispatcher != null && dispatcher.getRoot() != null) {
                  this.init_V_RootCommandNode(dispatcher.getRoot());
               }
            } catch (RuntimeException error) {
               this.init_V_AutismServerInfoOv(session, "command tree update", error);
            }
         } else {
            PluginSignatureCatalog.init();
            this.sync_J = -1L;
            if (currentAddress.equals(this.poll_Str) && this.pop_Z && AutismConfig.getGlobal().autoProbePlugins) {
               this.fill_V_2();
               this.read(true);
            } else if (currentAddress.equals(this.poll_Str) && this.pop_Z) {
               this.init_V_Str_Str_3(currentAddress, currentContextSignature);
            } else {
               this.frame_V_2();
            }
         }
      }
   }

   public void put_V_8() {
      this.sync_J = -1L;
      this.a_V_2();
      this.buffer_V_2();
   }

   public void add_V_9() {
      this.init_V_Z(true);
      this.init_V_I_9(0);
   }

   public void clear_V_8() {
      this.init_V_Z(true);
      this.init_V_I_9(1);
   }

   public void reset_V_7() {
      this.init_V_Z(!this.write_Z);
   }

   @Override
   public void init_V_Z(boolean v) {
      this.write_Z = v;
      if (v) {
         this.init_CharEncoder_2.write(!this.update_Z);
         OverlayManager.getInstance().bringToFront(this);
         this.frame_V_2();
         if (this.start == 0) {
            this.count_V_2();
         } else if (this.tick_Z) {
            this.task_V_2();
         } else if (!this.pop_Z) {
            this.node_V_2();
         } else {
            this.pool_V_2();
         }
      }

      this.write_V_24();
   }

   @Override
   public boolean sync_Z() {
      return this.write_Z;
   }

   @Override
   public boolean flush_Z() {
      return this.update_Z;
   }

   @Override
   public void write_V_Z(boolean c) {
      if (this.update_Z != c) {
         this.update_Z = c;
         this.init_CharEncoder_2.write(!this.update_Z);
         if (c) {
            this.get_V();
         } else if (this.start == 0) {
            this.count_V_2();
         } else if (this.tick_Z) {
            this.task_V_2();
         } else if (!this.pop_Z) {
            this.node_V_2();
         } else {
            this.pool_V_2();
         }

         this.write_V_24();
      }
   }

   @Override
   public String init_Str() {
      return "autism-serverinfo";
   }

   @Override
   public boolean load_Z() {
      return true;
   }

   @Override
   public boolean init_Z_D_D(double mx, double my) {
      if (!this.write_Z) {
         return false;
      }

      autismclient.gui.ui.layout.LayoutViewport viewport = this.init_TextFormat.init_RingBuffer();
      float uiX = viewport.toUiX(mx);
      float uiY = viewport.toUiY(my);
      return uiX >= this.poll_I && uiX <= this.poll_I + this.pop_I && uiY >= this.push_I && uiY <= this.push_I + this.run_I;
   }

   @Override
   public boolean load_Z_D_D(double mx, double my) {
      if (!this.write_Z) {
         return false;
      }

      autismclient.gui.ui.layout.LayoutViewport viewport = this.init_TextFormat.init_RingBuffer();
      float uiX = viewport.toUiX(mx);
      float uiY = viewport.toUiY(my);
      return this.init_Z_F_F_6(uiX, uiY) && !this.load_Z_F_F_5(uiX, uiY);
   }

   @Override
   public boolean write_Z() {
      return this.init_TextFormat.init_Z();
   }

   @Override
   public OverlayBounds init_cH() {
      return new OverlayBounds(this.poll_I, this.push_I, this.pop_I, this.run_I, this.write_Z, this.update_Z);
   }

   @Override
   public void init_V_cH(OverlayBounds b) {
      if (b != null) {
         OverlayBounds clamped = this.init_cH_cH_5(b);
         this.poll_I = clamped.x;
         this.push_I = clamped.y;
         this.pop_I = clamped.width;
         this.run_I = clamped.height;
         this.write_Z = clamped.visible;
         this.update_Z = clamped.collapsed;
         this.init_CharEncoder_2.write(!this.update_Z);
         this.flags_V_2();
      }
   }

   @Override
   public int init_I() {
      return this.get_I_20();
   }

   @Override
   public int load_I() {
      if (this.start == 1) {
         if (this.tick_Z) {
            return this.prev_I_6();
         } else {
            return !this.pop_Z ? this.next_I_7() : this.set_I_20();
         }
      } else {
         return this.size_I_3();
      }
   }

   @Override
   public void init_V_GuiGraphicsExtract_15(GuiGraphicsExtractor ctx, int mx, int my, float delta) {
      if (this.write_Z || this.a__Z()) {
         try {
            this.load_V_GuiGraphicsExtract(ctx, mx, my, delta);
         } catch (Throwable t) {
            this.init_V_Str_Throwable("render", t);
         }
      }
   }

   private void load_V_GuiGraphicsExtract(GuiGraphicsExtractor ctx, int mx, int my, float delta) {
      this.frame_V_2();
      this.block_V_2();
      this.drain_V_2();
      long nowMs = System.currentTimeMillis();
      if (nowMs - this.init_J >= 100L && (this.set_Z || this.tick_Z)) {
         this.init_List.clear();
         this.next_V_7();
         this.init_J = nowMs;
      }

      autismclient.gui.ui.layout.LayoutViewport viewport = this.init_TextFormat.init_RingBuffer();
      float uiMouseX = viewport.toUiX(mx);
      float uiMouseY = viewport.toUiY(my);
      boolean active = OverlayManager.getInstance().isFocused(this) || OverlayManager.getInstance().isTopmost(this);
      boolean headerHovered = this.init_Z_F_F_6(uiMouseX, uiMouseY);
      autismclient.gui.ui.layout.LayoutContext metrics = new autismclient.gui.ui.layout.LayoutContext(
         ctx, this.init_Font, viewport, this.init_CharEncoder, uiMouseX, uiMouseY, delta
      );
      this.init_CharEncoder_2.load_CharEncoder(!this.update_Z);
      this.init_CharEncoder_2.update(active);
      this.init_CharEncoder_2.sync(headerHovered);
      int preferredHeight = Math.round(this.init_CharEncoder_2.init_F_SyncManager_F(metrics, this.pop_I));
      if (this.update_Z) {
         this.run_I = preferredHeight;
      } else if (this.start == 0) {
         this.run_I = this.stop_I_7();
      } else if (this.tick_Z) {
         this.run_I = this.prev_I_6();
      } else if (!this.pop_Z) {
         this.run_I = this.next_I_7();
      } else {
         this.run_I = this.tick_I_22();
      }

      this.pop_I = this.pop_I_24();
      OverlayBounds clamped = this.init_cH_cH_5(new OverlayBounds(this.poll_I, this.push_I, this.pop_I, this.run_I, this.write_Z, this.update_Z));
      this.poll_I = clamped.x;
      this.push_I = clamped.y;
      this.pop_I = clamped.width;
      this.run_I = clamped.height;
      this.init_CharEncoder_2.init_TableIndex_F(this.poll_I, this.push_I, this.pop_I, this.run_I);
      this.init_TextFormat.init(ctx, mx, my, delta);
      if (this.init_CharEncoder_2.init(ctx)) {
         try {
            if (this.start == 1 && this.pop_Z && !this.tick_Z) {
               this.init_List.clear();
               this.init_V_GuiGraphicsExtract_11(ctx, viewport, uiMouseX, uiMouseY, delta);
            }
         } finally {
            autismclient.gui.ui.UiClipStack.getInstance().pop(ctx);
         }
      }
   }

   private String init_Str_ServerData(ServerData entry) {
      return entry != null && entry.version != null ? entry.version.getString() : "--";
   }

   private String flush_Str_2() {
      String version = AutismSessionState.getInstance().getDetectedServerVersion(this.open_Str_2());
      if (version != null && !version.isBlank()) {
         return version;
      }

      String brand = this.close_Str_2();
      if (brand != null && !brand.isBlank() && !"--".equals(brand)) {
         String extracted = this.update_Str_2(brand);
         if (extracted != null) {
            return extracted;
         }
      }

      return "--";
   }

   private String update_Str_2(String brand) {
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

   private void init_V_GuiGraphicsExtract_11(GuiGraphicsExtractor ctx, autismclient.gui.ui.layout.LayoutViewport viewport, float uiMouseX, float uiMouseY, float delta) {
      int x = Math.round(this.init_BitSetUtil_2.load_F());
      int y = Math.round(this.init_BitSetUtil_2.read_F());
      int rowW = Math.round(this.init_BitSetUtil_2.write_F());
      int viewH = this.init_I_BitSetUtil(this.init_BitSetUtil_2);
      int listTop = y;
      int listBottom = listTop + viewH;
      if (rowW > 0 && viewH > 0) {
         int clipLeft = x + 1;
         int clipTop = listTop + 1;
         int clipRight = x + rowW - 1;
         int clipBottom = listBottom - 1;
         int innerViewH = this.load_I_2(viewH);
         if (innerViewH > 0) {
            int rowContentW = Math.max(48, clipRight - clipLeft);
            List<AutismServerInfoOverlay.DetailRow> rows = this.init_List_Str_I(this.init_BitSetUtil.init_Str(), rowContentW);
            int estimatedContentHeight = this.load(rows);
            int maxScroll = Math.max(0, estimatedContentHeight - innerViewH);
            int quantizedTarget = this.quantizeScrollOffset(this.init_CacheEntry.getOffset(), this.o(), maxScroll);
            this.init_CacheEntry.setOffset(quantizedTarget, maxScroll);
            int visualScroll = this.quantizeScrollOffset(this.init_CacheEntry.tickAndClamp(delta, maxScroll), this.o(), maxScroll);
            this.value = estimatedContentHeight;
            autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics scrollbarMetrics = this.load_PoolEntry_DataUtil_I(x, listTop, rowW, viewH);
            int scrolledRowContentW = Math.max(48, clipRight - clipLeft - (scrollbarMetrics.isScrollable() ? 10 : 0));
            if (scrolledRowContentW != rowContentW) {
               rowContentW = scrolledRowContentW;
               rows = this.init_List_Str_I(this.init_BitSetUtil.init_Str(), rowContentW);
               estimatedContentHeight = this.load(rows);
               maxScroll = Math.max(0, estimatedContentHeight - innerViewH);
               quantizedTarget = this.quantizeScrollOffset(this.init_CacheEntry.getOffset(), this.o(), maxScroll);
               this.init_CacheEntry.setOffset(quantizedTarget, maxScroll);
               visualScroll = this.quantizeScrollOffset(this.init_CacheEntry.tickAndClamp(0.0F, maxScroll), this.o(), maxScroll);
               this.value = estimatedContentHeight;
               scrollbarMetrics = this.load_PoolEntry_DataUtil_I(x, listTop, rowW, viewH);
            }

            boolean active = OverlayManager.getInstance().isFocused(this) || OverlayManager.getInstance().isTopmost(this);
            viewport.pushTransform(ctx);

            try {
               autismclient.gui.ui.widgets.ListRowRenderer.drawListFrame(ctx, x, listTop, rowW, viewH, active);
               viewport.pushClip(ctx, clipLeft, clipTop, clipRight, clipBottom);

               try {
                  int startIndex = this.read_I_List_I(rows, visualScroll);
                  int cy = clipTop - visualScroll + this.read(startIndex);

                  for (int rowIndex = startIndex; rowIndex < rows.size(); rowIndex++) {
                     AutismServerInfoOverlay.DetailRow row = rows.get(rowIndex);
                     int rowY = cy;
                     int rowTop = rowY;
                     int rowBottom = rowY + row.init_I;
                     boolean rowVisible = rowBottom > clipTop && rowTop < clipBottom;
                     if (row.init()) {
                        if (rowVisible) {
                           autismclient.gui.ui.widgets.PanelSurfaceHelper.drawSectionHeader(ctx, clipLeft, rowTop, clipRight - clipLeft, rowBottom - rowTop);
                           int headerTextY = UiLayoutMath.centerTextY(
                              rowY, row.init_I, this.init_CharEncoder.init_I_l(autismclient.gui.f.b.l.write), this.init_CharEncoder.flush()
                           );
                           autismclient.gui.f.b.k.init(
                              ctx,
                              this.init_Font,
                              row.init_Str,
                              this.init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.write),
                              this.init_CharEncoder.load(autismclient.gui.f.b.l.write),
                              clipLeft + this.n(),
                              headerTextY,
                              false
                           );
                        }
                     } else if (row.init_AutismServerInfoOv_3 == AutismServerInfoOverlay.DetailRowKind.PLUGIN) {
                        boolean standaloneCommandHint = this.init_Z_AutismServerInfoOv_2(row);
                        boolean selected = !standaloneCommandHint && this.read_Z_Str_Str(row.load, this.sync_Str);
                        boolean hovered = rowVisible && uiMouseX >= clipLeft && uiMouseX < clipLeft + rowContentW && uiMouseY >= rowTop && uiMouseY < rowBottom;
                        if (rowVisible) {
                           autismclient.gui.ui.widgets.PanelSurfaceHelper.drawCardRow(ctx, clipLeft, rowTop, rowContentW, rowBottom - rowTop, hovered, selected);
                        }

                        int rowTextY = UiLayoutMath.centerTextY(rowY, row.init_I, this.init_CharEncoder.init_I_l(autismclient.gui.f.b.l.read), this.init_CharEncoder.flush());
                        if (rowVisible && !standaloneCommandHint) {
                           int arrowColor = !hovered && !selected
                              ? this.init_CharEncoder.load(autismclient.gui.f.b.l.write)
                              : this.init_CharEncoder.load(autismclient.gui.f.b.l.read);
                           autismclient.gui.f.PacketUtil.init(
                              ctx,
                              autismclient.gui.f.NetHelper.init(clipLeft + 2, rowTextY, 9, this.init_CharEncoder.init_I_l(autismclient.gui.f.b.l.read)),
                              selected,
                              arrowColor
                           );
                        }

                        boolean isAnticheat = row.load != null && update_Set.contains(row.load.toLowerCase(Locale.ROOT));
                        int nameColor = isAnticheat
                           ? ThemePaletteManager.recolor(-43691, ThemePaletteManager.ColorRole.DANGER)
                           : this.init_I_AutismServerInfoOv_Z(row.init_AutismServerInfoOv, hovered);
                        String label = isAnticheat ? "! " + row.load : row.load;
                        int labelX = clipLeft + (standaloneCommandHint ? 5 : 13);
                        int labelMaxWidth = Math.max(40, rowContentW - 18);
                        if (row.update != null && !row.update.isBlank()) {
                           int cw = autismclient.gui.f.b.k.init(
                              this.init_Font,
                              row.update,
                              this.init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.write),
                              this.init_CharEncoder.load(autismclient.gui.f.b.l.write)
                           );
                           int countX = clipLeft + rowContentW - cw - 4;
                           labelMaxWidth = Math.max(36, countX - labelX - 4);
                           int countTextY = UiLayoutMath.centerTextY(
                              rowY, row.init_I, this.init_CharEncoder.init_I_l(autismclient.gui.f.b.l.write), this.init_CharEncoder.flush()
                           );
                           if (rowVisible) {
                              autismclient.gui.f.b.k.init(
                                 ctx,
                                 this.init_Font,
                                 row.update,
                                 this.init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.write),
                                 this.init_CharEncoder.load(autismclient.gui.f.b.l.write),
                                 countX,
                                 countTextY,
                                 false
                              );
                           }
                        }

                        if (rowVisible) {
                           String displayLabel = autismclient.gui.f.b.k.init(
                              this.init_Font, label, labelMaxWidth, this.init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.read), nameColor
                           );
                           autismclient.gui.f.b.k.init(
                              ctx,
                              this.init_Font,
                              displayLabel,
                              this.init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.read),
                              nameColor,
                              labelX,
                              rowTextY,
                              false
                           );
                           if (!standaloneCommandHint) {
                              String clickedPlugin = row.load;
                              int hitTop = Math.max(rowTop, clipTop);
                              int hitBottom = Math.min(rowBottom, clipBottom);
                              this.init_List
                                 .add(new AutismServerInfoOverlay.ClickHotspot(clipLeft, hitTop, rowContentW, Math.max(1, hitBottom - hitTop), () -> {
                                    this.sync_Str = this.read_Z_Str_Str(clickedPlugin, this.sync_Str) ? null : clickedPlugin;
                                    this.b_V_2();
                                    this.write_V_24();
                                 }));
                           } else {
                              String hintCommand = row.load;
                              int hitTop = Math.max(rowTop, clipTop);
                              int hitBottom = Math.min(rowBottom, clipBottom);
                              this.init_List
                                 .add(new AutismServerInfoOverlay.ClickHotspot(clipLeft, hitTop, rowContentW, Math.max(1, hitBottom - hitTop), () -> {
                                    this.push_V();
                                    this.init_List.clear();
                                    String fullCmd = hintCommand.startsWith("/") ? hintCommand : "/" + hintCommand;
                                    OverlayManager.getInstance().setHidden(this, true);
                                    init_Minecraft.gui.setScreen(new ChatScreen(fullCmd, false));
                                 }));
                           }
                        }
                     } else {
                        boolean commandRow = row.init_AutismServerInfoOv_3 == AutismServerInfoOverlay.DetailRowKind.COMMAND;
                        boolean channelRow = row.init_AutismServerInfoOv_3 == AutismServerInfoOverlay.DetailRowKind.CHANNEL
                           && row.write != null
                           && !row.write.isBlank();
                        boolean clickableRow = commandRow || channelRow;
                        boolean hovered = clickableRow
                           && rowVisible
                           && uiMouseX >= clipLeft + 9
                           && uiMouseX < clipLeft + rowContentW - 6
                           && uiMouseY >= rowTop
                           && uiMouseY < rowBottom;
                        if (rowVisible) {
                           int detailColor = this.init_I_AutismServerInfoOv_4(row);
                           autismclient.gui.ui.widgets.PanelSurfaceHelper.drawCardRow(ctx, clipLeft + 5, rowTop, rowContentW - 9, rowBottom - rowTop, hovered, false);
                           autismclient.gui.ui.widgets.PanelSurfaceHelper.drawAccentBar(ctx, clipLeft + 5, rowTop, 1, rowBottom - rowTop, detailColor);
                           int textColor = hovered ? this.init_CharEncoder.load(autismclient.gui.f.b.l.read) : detailColor;
                           String detailLabel = autismclient.gui.f.b.k.init(
                              this.init_Font,
                              row.read,
                              Math.max(20, rowContentW - 20),
                              this.init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.write),
                              textColor
                           );
                           int detailTextY = UiLayoutMath.centerTextY(
                              rowY, row.init_I, this.init_CharEncoder.init_I_l(autismclient.gui.f.b.l.write), this.init_CharEncoder.flush()
                           );
                           autismclient.gui.f.b.k.init(
                              ctx,
                              this.init_Font,
                              detailLabel,
                              this.init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.write),
                              textColor,
                              clipLeft + 11,
                              detailTextY,
                              false
                           );
                        }

                        if (rowVisible && clickableRow && row.write != null && !row.write.isBlank()) {
                           String actionValue = row.write;
                           int hitTop = Math.max(rowTop, clipTop);
                           int hitBottom = Math.min(rowBottom, clipBottom);
                           this.init_List
                              .add(
                                 new AutismServerInfoOverlay.ClickHotspot(
                                    clipLeft + 9, hitTop, Math.max(1, rowContentW - 15), Math.max(1, hitBottom - hitTop), () -> {
                                       this.push_V();
                                       this.init_List.clear();
                                       if (commandRow) {
                                          String fullCmd = actionValue.startsWith("/") ? actionValue : "/" + actionValue;
                                          OverlayManager.getInstance().setHidden(this, true);
                                          init_Minecraft.gui.setScreen(new ChatScreen(fullCmd, false));
                                       } else {
                                          this.write_V_3(actionValue);
                                       }
                                    }
                                 )
                              );
                        }
                     }

                     cy += row.init_I;
                     if (cy > clipBottom + this.o()) {
                        break;
                     }
                  }
               } finally {
                  viewport.popClip(ctx);
               }

               autismclient.gui.ui.widgets.ScrollbarMetricsHelper.render(ctx, scrollbarMetrics, scrollbarMetrics.containsTrack(uiMouseX, uiMouseY), this.open_Z);
            } finally {
               viewport.popTransform(ctx);
            }
         }
      }
   }

   private int init_I_AutismServerInfoOv_4(AutismServerInfoOverlay.DetailRow row) {
      if (row == null) {
         return this.init_CharEncoder.load(autismclient.gui.f.b.l.write);
      }

      int var10000;
      switch (row.init_AutismServerInfoOv_3) {
         case HEADER:
         case PLUGIN:
            var10000 = this.init_CharEncoder.load(autismclient.gui.f.b.l.write);
            break;
         case COMMAND:
            var10000 = -1716016;
            break;
         case GUI:
            var10000 = UiColorPalette.warningYellow();
            break;
         case CHANNEL:
            var10000 = this.read_I_4(row.read == null ? null : row.read.replaceFirst("^ch\\s+", ""));
            break;
         case WHY:
            var10000 = this.init_I_AutismServerInfoOv_Z(row.init_AutismServerInfoOv, false);
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   private boolean init_Z_AutismServerInfoOv_2(AutismServerInfoOverlay.DetailRow row) {
      return row != null
         && row.init_AutismServerInfoOv_3 == AutismServerInfoOverlay.DetailRowKind.PLUGIN
         && row.init_AutismServerInfoOv_4 == AutismServerInfoOverlay.EvidenceSource.SCANNER_AUTOCOMPLETE
         && row.load != null
         && row.load.startsWith("/");
   }

   private autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics load_PoolEntry_DataUtil_I(int x, int y, int width, int height) {
      int innerHeight = this.load_I_2(height);
      int maxScroll = Math.max(0, this.value - innerHeight);
      return autismclient.gui.ui.widgets.ScrollbarMetricsHelper.compute(
         this.value,
         Math.max(1, innerHeight),
         x + width - 5,
         y + 1,
         3,
         Math.max(1, innerHeight),
         this.quantizeScrollOffset(this.init_CacheEntry.tickAndClamp(0.0F, maxScroll), this.o(), maxScroll)
      );
   }

   private String init_Str_List(List<String> commands, List<String> guis, List<String> channels) {
      int commandCount = commands == null ? 0 : commands.size();
      int guiCount = guis == null ? 0 : guis.size();
      int observedChannelCount = 0;
      int probableChannelCount = 0;
      boolean noChannelObserved = false;
      if (channels != null) {
         for (String channel : channels) {
            if (channel != null && !channel.isBlank()) {
               if (this.update_Z_2(channel)) {
                  noChannelObserved = true;
               } else if (this.sync_Z_2(channel)) {
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

   private String init_Str_List_List(List<String> commands, List<String> channels) {
      return this.init_Str_List(commands, List.of(), channels);
   }

   private List<String> init_List_Str_3(String plugin) {
      List<String> observed = this.write_List_2(plugin);
      if (observed != null && !observed.isEmpty()) {
         return observed;
      }

      List<String> probableNamespaces = PluginSignatureCatalog.init_List(plugin);
      if (!probableNamespaces.isEmpty()) {
         List<String> rows = new ArrayList<>();

         for (String namespace : probableNamespaces) {
            if (namespace != null && !namespace.isBlank()) {
               rows.add("? " + namespace.trim().toLowerCase(Locale.ROOT) + ":*");
            }
         }

         rows = this.init_List_Collection(rows);
         if (!rows.isEmpty()) {
            return rows;
         }
      }

      return List.of("no channel");
   }

   private int read_I_4(String detail) {
      if (detail == null) {
         return this.init_CharEncoder.load(autismclient.gui.f.b.l.write);
      } else {
         String lower = detail.toLowerCase(Locale.ROOT);
         if (this.update_Z_2(lower)) {
            return -7371387;
         } else if (this.sync_Z_2(lower)) {
            return -7371387;
         } else if (lower.contains("[registered")) {
            return UiColorPalette.teal();
         } else {
            return lower.contains("[live") ? UiColorPalette.cyan() : UiColorPalette.cyan();
         }
      }
   }

   private String sync_Str_2(String detail) {
      if (detail == null || detail.isBlank()) {
         return "--";
      } else if (this.update_Z_2(detail)) {
         return "no channel";
      } else {
         String base = this.close(detail);
         if (base.isBlank()) {
            return "--";
         } else {
            return this.sync_Z_2(detail) ? "? " + base : base;
         }
      }
   }

   private List<String> init_List_Collection(Collection<String> source) {
      if (source != null && !source.isEmpty()) {
         Map<String, String> unique = new LinkedHashMap<>();

         for (String row : source) {
            if (row != null && !row.isBlank()) {
               String clean = row.trim();
               String key = this.flush_Str_2(clean);
               if (!key.isBlank()) {
                  String existing = unique.get(key);
                  if (existing == null || this.write_I_3(clean) > this.write_I_3(existing)) {
                     unique.put(key, clean);
                  }
               }
            }
         }

         List<String> out = new ArrayList<>(unique.values());
         out.sort((a, b) -> String.CASE_INSENSITIVE_ORDER.compare(this.sync_Str_2(a), this.sync_Str_2(b)));
         return out;
      } else {
         return List.of();
      }
   }

   private String flush_Str_2(String detail) {
      return this.update_Z_2(detail) ? "no-channel" : this.close(detail).toLowerCase(Locale.ROOT);
   }

   private String close(String detail) {
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

   private String open(String detail) {
      if (detail != null && !this.update_Z_2(detail)) {
         String base = this.close(detail);
         String channel = this.poll(base);
         return channel.isBlank() ? null : channel;
      } else {
         return null;
      }
   }

   private String poll(String rawChannel) {
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
            AutismPayloadSupport.init_Identifier(channel);
            return channel;
         } catch (IllegalArgumentException ignored) {
            return "";
         }
      } else {
         return "bungeecord:main";
      }
   }

   private void write_V_3(String channel) {
      String target = channel == null ? "" : channel.trim();
      if (!target.isBlank()) {
         ActionEditorOverlay existing = ActionEditorOverlay.load_ActionEditorOverla();
         if (existing == null || !existing.init_Z_Str(target)) {
            autismclient.util.macro.PayloadAction action = new autismclient.util.macro.PayloadAction();
            action.channel = target;
            action.payloadDirection = "C2S";
            action.sourceDirection = "C2S";
            action.payloadPhase = "PLAY";
            action.sourceProtocol = "play";
            action.payloadProvenance = "serverInfoChannel";
            ActionEditorOverlay editor = ActionEditorOverlay.init_ActionEditorOverla_2();
            editor.init_V_H(action);
            OverlayManager.getInstance().bringToFront(editor);
         }
      }
   }

   private boolean update_Z_2(String detail) {
      if (detail == null) {
         return false;
      }

      String lower = detail.trim().toLowerCase(Locale.ROOT);
      return lower.equals("no channel") || lower.startsWith("no client payload");
   }

   private boolean sync_Z_2(String detail) {
      if (detail == null) {
         return false;
      }

      String lower = detail.trim().toLowerCase(Locale.ROOT);
      return lower.startsWith("? ") || lower.contains("not observed") || lower.contains("[probable");
   }

   private int write_I_3(String detail) {
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
         } else if (this.sync_Z_2(lower)) {
            return 1;
         } else {
            return this.update_Z_2(lower) ? 0 : 3;
         }
      }
   }

   private String close_Str_2() {
      if (init_Minecraft.getConnection() == null) {
         return "--";
      }

      String brand = init_Minecraft.getConnection().serverBrand();
      return brand != null && !brand.isBlank() ? brand : "--";
   }

   private boolean init_Z_F_F_6(float uiMouseX, float uiMouseY) {
      return uiMouseX >= this.poll_I && uiMouseX < this.poll_I + this.pop_I && uiMouseY >= this.push_I && uiMouseY < this.push_I + this.init_CharEncoder.load();
   }

   private boolean load_Z_F_F_5(float uiMouseX, float uiMouseY) {
      return autismclient.gui.ui.widgets.WindowHeaderHelper.init(
         autismclient.gui.f.NetHelper.init(this.poll_I, this.push_I, this.pop_I, Math.max(this.init_CharEncoder.load(), this.run_I)),
         this.init_CharEncoder.load(),
         uiMouseX,
         uiMouseY
      );
   }

   private int task_I_2() {
      return 4;
   }

   private int pool_I_2() {
      return 2;
   }

   private int index_I_2() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(13);
   }

   private int count_I_2() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(13);
   }

   private int flags_I_2() {
      return 2;
   }

   private int level_I_2() {
      return Math.max(autismclient.gui.ui.UiDensityMetrics.scaledHeight(13), this.init_CharEncoder.update());
   }

   private int init_I_StrArr_2(String... labels) {
      int widest = 0;

      for (String label : labels) {
         widest = Math.max(
            widest,
            autismclient.gui.f.b.k.init(
               this.init_Font,
               label,
               this.init_CharEncoder.init_Identifier_l(autismclient.gui.f.b.l.write),
               this.init_CharEncoder.load(autismclient.gui.f.b.l.write)
            )
         );
      }

      return widest + 10;
   }

   private int frame_I_2() {
      return this.init_I_StrArr_2("Delay:");
   }

   private int block_I_2() {
      return this.init_I_StrArr_2("State:", "Sent:", "Replies:", "Retries:", "Failed:", "Found:", "Progress:");
   }

   private int chunk_I_2() {
      return this.init_I_StrArr_2("Server:", "Alert:", "Error:");
   }

   private int table_I_2() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(14);
   }

   private int store_I_2() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(16);
   }

   private int drain_I_2() {
      return 128;
   }

   private int fill_I_2() {
      return 28;
   }

   private float load_F() {
      return this.o() * 4 + 2;
   }

   private int mark_I_2() {
      return 204;
   }

   private int scan_I_2() {
      return this.mark_I_2();
   }

   private int parse_I_2() {
      return this.mark_I_2();
   }

   private int bind_I_2() {
      return this.mark_I_2();
   }

   private int a_I_2() {
      return this.init_CharEncoder.load() + this.task_I_2() * 2 + this.level_I_2() + this.pool_I_2() * 2 + 48 + this.count_I_2();
   }

   private int b_I_2() {
      return this.mark_I_2();
   }

   private int c_I_2() {
      return this.a_I_2();
   }

   private int d_I_2() {
      return this.a_I_2();
   }

   private int e_I_2() {
      return this.init_CharEncoder.load() + this.task_I_2() * 2 + 48;
   }

   private int f() {
      return 24;
   }

   private int g() {
      return this.o();
   }

   private int h() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(12);
   }

   private int i() {
      return this.o();
   }

   private int j() {
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

   private int n() {
      return 3;
   }

   private int o() {
      return this.h();
   }

   private int init_I_BitSetUtil(autismclient.gui.ui.layout.VerticalSpacerNode slot) {
      int bodyBottom = this.push_I + this.run_I - this.task_I_2();
      int rawViewH = Math.max(0, Math.min(Math.round(slot.update_F()), bodyBottom - Math.round(slot.read_F())));
      return this.init_I_I_10(rawViewH);
   }

   private int init_I_I_10(int rawHeight) {
      return rawHeight <= 0 ? 0 : Math.min(rawHeight, this.load_I_2(rawHeight) + 2);
   }

   private int load_I_2(int viewHeight) {
      return Math.max(0, this.alignHeightToStep(Math.max(0, viewHeight - 2), this.o()));
   }

   private OverlayBounds init_cH_cH_5(OverlayBounds bounds) {
      autismclient.gui.ui.layout.LayoutViewport viewport = this.init_TextFormat.init_RingBuffer();
      int viewportWidth = Math.round(viewport.width());
      int viewportHeight = Math.round(viewport.height());
      int margin = 4;
      int availableWidth = Math.max(1, viewportWidth - margin * 2);
      int availableHeight = Math.max(this.init_CharEncoder.load(), viewportHeight - margin * 2);
      int minWidth = Math.min(this.init_I(), availableWidth);
      int width = Math.max(minWidth, Math.min(bounds.width, availableWidth));
      int minHeight = bounds.collapsed ? this.init_CharEncoder.load() : Math.min(this.load_I(), availableHeight);
      int height = Math.max(minHeight, Math.min(bounds.height, availableHeight));
      int renderedHeight = bounds.collapsed ? this.init_CharEncoder.load() : height;
      int x = Math.max(margin, Math.min(bounds.x, Math.max(margin, viewportWidth - margin - width)));
      int y = Math.max(margin, Math.min(bounds.y, Math.max(margin, viewportHeight - margin - renderedHeight)));
      return new OverlayBounds(x, y, width, height, bounds.visible, bounds.collapsed);
   }

   private String load(ServerData entry) {
      LinkedHashSet<String> guesses = new LinkedHashSet<>();
      String brand = this.close_Str_2().toLowerCase(Locale.ROOT);
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

      if (this.load_List
         .stream()
         .anyMatch(plugin -> "viaversion".equalsIgnoreCase(plugin) || "viabackwards".equalsIgnoreCase(plugin) || "viarewind".equalsIgnoreCase(plugin))) {
         guesses.add("ViaVersion");
      }

      if (this.load_List.stream().anyMatch(plugin -> "geysermc".equalsIgnoreCase(plugin) || "floodgate".equalsIgnoreCase(plugin))) {
         guesses.add("Bedrock Bridge");
      }

      return guesses.isEmpty() ? "--" : String.join(" + ", guesses);
   }

   private String read(ServerData entry) {
      String brand = this.close_Str_2();
      String reportedVersion = this.init_Str_ServerData(entry);
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
         return this.load_List
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

   private String open_Str_2() {
      ServerData entry = init_Minecraft.getCurrentServer();
      if (entry != null && entry.ip != null && !entry.ip.isBlank()) {
         return entry.ip.trim();
      }

      if (init_Minecraft.getConnection() != null && init_Minecraft.getConnection().getConnection() != null) {
         SocketAddress address = init_Minecraft.getConnection().getConnection().getRemoteAddress();
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

   private String push(String rawAddress) {
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

   private String poll_Str_2() {
      if (init_Minecraft.getConnection() != null && init_Minecraft.getConnection().getConnection() != null) {
         if (init_Minecraft.getConnection().getConnection().getRemoteAddress() instanceof InetSocketAddress inet && inet.getAddress() != null) {
            String hostAddress = inet.getAddress().getHostAddress();
            return hostAddress == null ? "" : hostAddress.trim();
         } else {
            return "";
         }
      } else {
         return "";
      }
   }

   private Integer init_Integer_Str_2(String rawAddress) {
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

   private String init_Str_Str_Integer(String host, Integer port) {
      if (host != null && !host.isBlank() && port != null) {
         return host.indexOf(58) >= 0 && !host.startsWith("[") && !host.endsWith("]") ? "[" + host + "]:" + port : host + ":" + port;
      } else {
         return host;
      }
   }

   private Object init_Obj_Obj_StrArr(Object target, String... names) {
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

   private String init_Str_Obj_StrArr(Object target, String... names) {
      Object value = this.init_Obj_Obj_StrArr(target, names);
      if (value == null) {
         return "";
      } else {
         return value instanceof Component component ? component.getString() : String.valueOf(value);
      }
   }

   private int init_I_Obj(Object target, int fallback, String... names) {
      Object value = this.init_Obj_Obj_StrArr(target, names);
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

   private String init_Str_Obj(Object value) {
      if (value instanceof Component component) {
         return component.getString();
      } else {
         return value == null ? "" : String.valueOf(value);
      }
   }

   private void init_V_Str_10(String value, String successMessage, String unavailableMessage) {
      String trimmed = value == null ? "" : value.trim();
      if (!trimmed.isEmpty() && !"--".equals(trimmed) && !"Resolving...".equalsIgnoreCase(trimmed) && !"Failed".equalsIgnoreCase(trimmed)) {
         init_Minecraft.keyboardHandler.setClipboard(trimmed);
         bi.init(successMessage);
      } else {
         bi.write(unavailableMessage);
      }
   }

   private void bind_V_2() {
      String displayedAddress = this.open_Str_2();
      String realIp = this.pop(displayedAddress);
      String resolvedWithPort = this.init_Str_Str_Integer(realIp, this.init_Integer_Str_2(displayedAddress));
      this.init_V_Str_10(resolvedWithPort, "Real IP copied.", "Real IP unavailable.");
   }

   private void update_V_2(String rawAddress) {
      String host = this.push(rawAddress);
      if (!host.isEmpty()) {
         if (!host.equals(this.read_Str)) {
            this.load_Str = null;
            this.read_Str = host;
            this.push_Z = this.auditDnsRunning.contains(host);
         }

         if (this.load_Str == null && this.auditDnsRunning.add(host)) {
            this.push_Z = true;
            Thread t = new Thread(() -> {
               String result;
               try {
                  result = InetAddress.getByName(host).getHostAddress();
               } catch (Exception e) {
                  result = "Failed";
               }

               // A lookup for a host the overlay no longer shows must not overwrite the current one.
               if (host.equals(this.read_Str)) {
                  this.load_Str = result;
                  this.push_Z = false;
               }

               this.auditDnsRunning.remove(host);
            }, "Autism-DNS");
            t.setDaemon(true);
            t.start();
         }
      }
   }

   private String pop(String rawAddress) {
      this.update_V_2(rawAddress);
      String socketIp = this.poll_Str_2();
      if (!socketIp.isBlank()) {
         return socketIp;
      } else if (this.load_Str != null) {
         return this.load_Str;
      } else {
         return this.push_Z ? "Resolving..." : "--";
      }
   }

   private List<String> init_List_13() {
      return this.load_List
         .stream()
         .filter(Objects::nonNull)
         .filter(p -> update_Set.contains(p.toLowerCase(Locale.ROOT)))
         .sorted(String.CASE_INSENSITIVE_ORDER)
         .collect(Collectors.toList());
   }

   private String push_Str_2() {
      if (init_Minecraft.level == null) {
         return "--";
      }

      Identifier worldId = init_Minecraft.level.dimension().identifier();
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

   private List<AutismServerInfoOverlay.DetailRow> init_List_List_Str_3(List<AutismServerInfoOverlay.DetectedPluginEntry> filteredPlugins, String query) {
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
         if (detail != null && !detail.init_Str.isBlank()) {
            String plugin = detail.init_Str;
            String groupTitle = this.init_Str_AutismServerInfoOv(
               detail.init_AutismServerInfoOv_2, detail.init_AutismServerInfoOv, detail.init_AutismServerInfoOv_3
            );
            boolean standaloneCommandHint = this.init_Z_AutismServerInfoOv_3(detail);
            grouped.computeIfAbsent(groupTitle, unused -> new ArrayList<>())
               .add(
                  AutismServerInfoOverlay.DetailRow.init(
                     plugin,
                     detail.init_AutismServerInfoOv_2,
                     detail.init_AutismServerInfoOv_3,
                     detail.init_AutismServerInfoOv,
                     this.h(),
                     standaloneCommandHint ? "" : detail.write_Str,
                     detail.update
                  )
               );
            boolean selected = this.read_Z_Str_Str(plugin, this.sync_Str);
            boolean showDetails = !standaloneCommandHint && (selected || searchActive);
            if (showDetails) {
               for (String command : detail.init_List) {
                  if (!searchActive || this.init_Z_Str(normalizedQuery, plugin, "/" + command, detail.read_Str)) {
                     grouped.get(groupTitle)
                        .add(
                           AutismServerInfoOverlay.DetailRow.init(
                              AutismServerInfoOverlay.DetailRowKind.COMMAND,
                              plugin,
                              "cmd /" + command,
                              "/" + command,
                              detail.init_AutismServerInfoOv_3,
                              detail.init_AutismServerInfoOv,
                              this.i(),
                              this.init_Str_StrArr(plugin, command, detail.read_Str)
                           )
                        );
                  }
               }

               for (String gui : detail.load_List) {
                  if (!searchActive || this.init_Z_Str(normalizedQuery, plugin, gui, detail.read_Str)) {
                     grouped.get(groupTitle)
                        .add(
                           AutismServerInfoOverlay.DetailRow.init(
                              AutismServerInfoOverlay.DetailRowKind.GUI,
                              plugin,
                              "gui " + gui,
                              null,
                              detail.init_AutismServerInfoOv_3,
                              detail.init_AutismServerInfoOv,
                              this.i(),
                              this.init_Str_StrArr(plugin, gui, detail.read_Str)
                           )
                        );
                  }
               }

               for (String channel : detail.read_List.isEmpty() ? this.init_List_Str_3(plugin) : detail.read_List) {
                  String label = this.sync_Str_2(channel);
                  if (!searchActive || this.init_Z_Str(normalizedQuery, plugin, label, detail.read_Str)) {
                     grouped.get(groupTitle)
                        .add(
                           AutismServerInfoOverlay.DetailRow.init(
                              AutismServerInfoOverlay.DetailRowKind.CHANNEL,
                              plugin,
                              "ch " + label,
                              this.open(channel),
                              detail.init_AutismServerInfoOv_3,
                              detail.init_AutismServerInfoOv,
                              this.i(),
                              this.init_Str_StrArr(plugin, label, detail.read_Str)
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
                     int byPlugin = String.CASE_INSENSITIVE_ORDER.compare(a.load == null ? "" : a.load, b.load == null ? "" : b.load);
                     return byPlugin != 0
                        ? byPlugin
                        : Integer.compare(
                           this.init_I_AutismServerInfoOv_2(a.init_AutismServerInfoOv_3), this.init_I_AutismServerInfoOv_2(b.init_AutismServerInfoOv_3)
                        );
                  }
               );
            rows.add(AutismServerInfoOverlay.DetailRow.init(group.getKey(), this.g()));
            rows.addAll(group.getValue());
         }
      }

      return rows;
   }

   private int init_I_AutismServerInfoOv_2(AutismServerInfoOverlay.DetailRowKind type) {
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

   private boolean init_Z_AutismServerInfoOv_3(AutismServerInfoOverlay.DetectedPluginEntry detail) {
      if (detail != null && detail.init_AutismServerInfoOv_3 == AutismServerInfoOverlay.EvidenceSource.SCANNER_AUTOCOMPLETE && detail.init_List.size() == 1) {
         String command = detail.init_List.get(0);
         return command != null && detail.init_Str.equalsIgnoreCase("/" + command);
      } else {
         return false;
      }
   }

   private boolean init_Z_Str(String query, String plugin, String detail, String sourceLabel) {
      return query != null && !query.isBlank() ? this.init_Str_StrArr(plugin, detail, sourceLabel).contains(query) : true;
   }

   private String init_Str_AutismServerInfoOv(
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

      if (resultKind != AutismServerInfoOverlay.DetectionType.FEATURE && !this.read(evidence)) {
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

   private String init_Str_AutismServerInfoOv_2(
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

   private List<String> load_List_5() {
      return List.of("Command Tree", "Command Root Hint", "Plugin Command Hint", "Version Command Hint", "Payload Channel");
   }

   private String init_Str_AutismServerInfoOv_2(AutismServerInfoOverlay.DetectedPluginEntry detail) {
      if (detail == null) {
         return "Other";
      }

      AutismServerInfoOverlay.EvidenceSource evidence = detail.load_AutismServerInfoOv == null
         ? AutismServerInfoOverlay.EvidenceSource.UNKNOWN
         : detail.load_AutismServerInfoOv;
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

   private int update_I(String title) {
      List<String> order = this.load_List_5();
      int index = order.indexOf(title);
      return index < 0 ? order.size() : index;
   }

   private String init_Str_Str(
      String plugin, List<String> commands, List<String> guis, List<String> channels, String sourceLabel, AutismServerInfoOverlay.MatchConfidence confidence
   ) {
      StringBuilder sb = new StringBuilder();
      sb.append(plugin == null ? "" : plugin)
         .append(' ')
         .append(this.init_Str_AutismServerInfoOv(confidence))
         .append(' ')
         .append(sourceLabel == null ? "" : sourceLabel);

      for (String command : commands) {
         sb.append(' ').append(command);
      }

      for (String gui : guis) {
         sb.append(' ').append(gui);
      }

      for (String channel : channels) {
         sb.append(' ').append(this.sync_Str_2(channel));
      }

      return sb.toString().toLowerCase(Locale.ROOT);
   }

   private String init_Str_StrArr(String... parts) {
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

   private List<String> load(Collection<String> plugins) {
      if (plugins != null && !plugins.isEmpty()) {
         Map<String, String> unique = new LinkedHashMap<>();

         for (String plugin : plugins) {
            if (plugin != null && !plugin.isBlank()) {
               String clean = plugin.trim();
               String key = this.load_Str_2(clean);
               if (!key.isBlank()) {
                  String existing = unique.get(key);
                  if (existing == null || this.load_Z_Str_Str_2(clean, existing)) {
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

   private boolean read_Z_Str_Str(String a, String b) {
      return a != null && b != null ? this.load_Str_2(a).equals(this.load_Str_2(b)) : false;
   }

   private boolean flush_Z_2(String plugin) {
      if (plugin != null && !plugin.isBlank()) {
         String key = this.load_Str_2(plugin);

         for (String detected : this.load_List) {
            if (detected != null && this.load_Str_2(detected).equals(key)) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private Collection<AutismServerInfoOverlay.DetectedPluginEntry> init_Collection() {
      if (this.data == this.prev) {
         return this.write_Map.values();
      }

      Map<String, AutismServerInfoOverlay.NamespaceSummary> builders = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

      for (String plugin : this.load((Collection<String>)this.load_List)) {
         String key = this.load_Str_2(plugin);
         if (!key.isBlank()) {
            AutismServerInfoOverlay.NamespaceSummary builder = builders.computeIfAbsent(key, unused -> new AutismServerInfoOverlay.NamespaceSummary());
            if (builder.init_Str == null || builder.init_Str.isBlank() || this.load_Z_Str_Str_2(plugin, builder.init_Str)) {
               builder.init_Str = plugin.trim();
            }
         }
      }

      this.init_V_Map_Map(builders, this.init_Map);
      this.load_V_Map_Map(builders, this.open_Map);
      this.read_V_Map_Map(builders, this.read_Map);
      this.write(builders, this.load_Map);
      Map<String, AutismServerInfoOverlay.DetectedPluginEntry> details = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

      for (Entry<String, AutismServerInfoOverlay.NamespaceSummary> entry : builders.entrySet()) {
         String key = entry.getKey();
         AutismServerInfoOverlay.NamespaceSummary builder = entry.getValue();
         if (builder != null) {
            String displayName = builder.init_Str != null && !builder.init_Str.isBlank() ? builder.init_Str.trim() : key;
            List<String> commands = List.copyOf(builder.init_Set);
            List<String> copyCommands = builder.load.isEmpty() ? commands : List.copyOf(builder.load);
            List<String> guis = List.copyOf(builder.read);
            List<String> channels = this.init_List_Collection(builder.init_List);
            AutismServerInfoOverlay.EvidenceSource evidence = this.update_Map
               .getOrDefault(key, commands.isEmpty() ? AutismServerInfoOverlay.EvidenceSource.UNKNOWN : AutismServerInfoOverlay.EvidenceSource.ROOT_HINT);
            AutismServerInfoOverlay.EvidenceSource copyEvidence = this.sync_Map
               .getOrDefault(key, this.init_AutismServerInfoOv_Str_AutismServerInfoOv_2(displayName, evidence));
            AutismServerInfoOverlay.MatchConfidence confidence = this.flush_Map.getOrDefault(key, this.init_AutismServerInfoOv_AutismServerInfoOv(evidence));
            AutismServerInfoOverlay.DetectionType resultKind = this.init_AutismServerInfoOv_Str(displayName, evidence, confidence);
            String sourceLabel = this.init_Str_AutismServerInfoOv_2(resultKind, evidence, confidence);
            String countLabel = this.init_Str_List(commands, guis, channels);
            String searchText = this.init_Str_Str(displayName, commands, guis, channels, sourceLabel, confidence);
            details.put(
               key,
               new AutismServerInfoOverlay.DetectedPluginEntry(
                  displayName, key, commands, guis, channels, resultKind, evidence, copyEvidence, confidence, sourceLabel, countLabel, copyCommands, searchText
               )
            );
         }
      }

      this.write_Map = Collections.unmodifiableMap(new TreeMap<>(details));
      this.data = this.prev;
      return this.write_Map.values();
   }

   private AutismServerInfoOverlay.DetectedPluginEntry init_AutismServerInfoOv_Str_3(String plugin) {
      if (plugin != null && !plugin.isBlank()) {
         this.init_Collection();
         return this.write_Map.get(this.load_Str_2(plugin));
      } else {
         return null;
      }
   }

   private void init_V_Map_Map(Map<String, AutismServerInfoOverlay.NamespaceSummary> builders, Map<String, List<String>> source) {
      if (source != null && !source.isEmpty()) {
         for (Entry<String, List<String>> entry : source.entrySet()) {
            String key = this.load_Str_2(entry.getKey());
            if (!key.isBlank()) {
               AutismServerInfoOverlay.NamespaceSummary builder = builders.computeIfAbsent(key, unused -> new AutismServerInfoOverlay.NamespaceSummary());
               if ((builder.init_Str == null || builder.init_Str.isBlank()) && entry.getKey() != null) {
                  builder.init_Str = entry.getKey().trim();
               }

               if (entry.getValue() != null) {
                  for (String command : entry.getValue()) {
                     if (command != null && !command.isBlank()) {
                        builder.init_Set.add(command.trim());
                     }
                  }
               }
            }
         }
      }
   }

   private void load_V_Map_Map(Map<String, AutismServerInfoOverlay.NamespaceSummary> builders, Map<String, List<String>> source) {
      if (source != null && !source.isEmpty()) {
         for (Entry<String, List<String>> entry : source.entrySet()) {
            String key = this.load_Str_2(entry.getKey());
            if (!key.isBlank()) {
               AutismServerInfoOverlay.NamespaceSummary builder = builders.computeIfAbsent(key, unused -> new AutismServerInfoOverlay.NamespaceSummary());
               if ((builder.init_Str == null || builder.init_Str.isBlank()) && entry.getKey() != null) {
                  builder.init_Str = entry.getKey().trim();
               }

               if (entry.getValue() != null) {
                  for (String command : entry.getValue()) {
                     if (command != null && !command.isBlank()) {
                        builder.load.add(command.trim());
                     }
                  }
               }
            }
         }
      }
   }

   private void read_V_Map_Map(Map<String, AutismServerInfoOverlay.NamespaceSummary> builders, Map<String, List<String>> source) {
      if (source != null && !source.isEmpty()) {
         for (Entry<String, List<String>> entry : source.entrySet()) {
            String key = this.load_Str_2(entry.getKey());
            if (!key.isBlank()) {
               AutismServerInfoOverlay.NamespaceSummary builder = builders.computeIfAbsent(key, unused -> new AutismServerInfoOverlay.NamespaceSummary());
               if ((builder.init_Str == null || builder.init_Str.isBlank()) && entry.getKey() != null) {
                  builder.init_Str = entry.getKey().trim();
               }

               if (entry.getValue() != null) {
                  for (String gui : entry.getValue()) {
                     if (gui != null && !gui.isBlank()) {
                        builder.read.add(gui.trim());
                     }
                  }
               }
            }
         }
      }
   }

   private void write(Map<String, AutismServerInfoOverlay.NamespaceSummary> builders, Map<String, List<String>> source) {
      if (source != null && !source.isEmpty()) {
         for (Entry<String, List<String>> entry : source.entrySet()) {
            String key = this.load_Str_2(entry.getKey());
            if (!key.isBlank()) {
               AutismServerInfoOverlay.NamespaceSummary builder = builders.computeIfAbsent(key, unused -> new AutismServerInfoOverlay.NamespaceSummary());
               if ((builder.init_Str == null || builder.init_Str.isBlank()) && entry.getKey() != null) {
                  builder.init_Str = entry.getKey().trim();
               }

               if (entry.getValue() != null) {
                  builder.init_List.addAll(entry.getValue());
               }
            }
         }
      }
   }

   private List<String> load_List(String plugin) {
      AutismServerInfoOverlay.DetectedPluginEntry detail = this.init_AutismServerInfoOv_Str_3(plugin);
      return detail == null ? List.of() : detail.init_List;
   }

   private List<String> read_List_2(String plugin) {
      AutismServerInfoOverlay.DetectedPluginEntry detail = this.init_AutismServerInfoOv_Str_3(plugin);
      return detail == null ? List.of() : detail.load_List;
   }

   private List<String> write_List_2(String plugin) {
      AutismServerInfoOverlay.DetectedPluginEntry detail = this.init_AutismServerInfoOv_Str_3(plugin);
      return detail == null ? List.of() : detail.read_List;
   }

   private List<AutismServerInfoOverlay.DetailRow> init_List_Str_I(String queryText, int rowContentWidth) {
      String query = queryText == null ? "" : queryText.trim().toLowerCase(Locale.ROOT);
      String selectedKey = this.sync_Str == null ? "" : this.load_Str_2(this.sync_Str);
      if (this.size == this.valid && Objects.equals(this.write_Str, query) && Objects.equals(this.update_Str, selectedKey) && this.stop == rowContentWidth) {
         return this.read_List;
      }

      List<AutismServerInfoOverlay.DetectedPluginEntry> filtered = new ArrayList<>();

      for (AutismServerInfoOverlay.DetectedPluginEntry detail : this.init_Collection()) {
         if (detail != null && (query.isEmpty() || detail.update.contains(query))) {
            filtered.add(detail);
         }
      }

      this.init_V_List_5(this.init_List_List_Str_3(filtered, query));
      this.write_Str = query;
      this.update_Str = selectedKey;
      this.stop = rowContentWidth;
      this.size = this.valid;
      return this.read_List;
   }

   private boolean write(String plugin, String query) {
      if (plugin != null && query != null && !query.isBlank()) {
         AutismServerInfoOverlay.DetectedPluginEntry detail = this.init_AutismServerInfoOv_Str_3(plugin);
         return detail != null && detail.update.contains(query);
      } else {
         return false;
      }
   }

   private void a_V_2() {
      this.prev++;
      this.valid++;
      this.size = -1;
      this.write_Str = null;
      this.update_Str = null;
      this.stop = -1;
      this.read_List = List.of();
      this.init_IArr = new int[0];
      this.next = 0;
      this.data = -1;
      this.write_Map = Map.of();
      this.prev_V_7();
   }

   private void b_V_2() {
      this.valid++;
      this.size = -1;
      this.write_Str = null;
      this.update_Str = null;
      this.stop = -1;
      this.read_List = List.of();
      this.init_IArr = new int[0];
      this.next = 0;
      this.prev_V_7();
   }

   private int load(List<AutismServerInfoOverlay.DetailRow> rows) {
      if (rows == this.read_List) {
         return this.next;
      }

      int total = 0;

      for (AutismServerInfoOverlay.DetailRow row : rows) {
         if (row != null) {
            total += row.init_I;
         }
      }

      return total;
   }

   private void init_V_List_5(List<AutismServerInfoOverlay.DetailRow> rows) {
      List<AutismServerInfoOverlay.DetailRow> safeRows = rows == null ? List.of() : List.copyOf(rows);
      int[] offsets = new int[safeRows.size()];
      int cursor = 0;

      for (int i = 0; i < safeRows.size(); i++) {
         offsets[i] = cursor;
         AutismServerInfoOverlay.DetailRow row = safeRows.get(i);
         cursor += row == null ? 0 : row.init_I;
      }

      this.read_List = safeRows;
      this.init_IArr = offsets;
      this.next = cursor;
   }

   private int read(int index) {
      return index >= 0 && index < this.init_IArr.length ? this.init_IArr[index] : 0;
   }

   private int read_I_List_I(List<AutismServerInfoOverlay.DetailRow> rows, int scrollOffset) {
      if (rows != null && !rows.isEmpty() && this.init_IArr.length != 0 && scrollOffset > 0) {
         int idx = Arrays.binarySearch(this.init_IArr, scrollOffset);
         if (idx < 0) {
            idx = Math.max(0, -idx - 2);
         }

         while (idx > 0) {
            AutismServerInfoOverlay.DetailRow row = rows.get(idx);
            int rowBottom = this.init_IArr[idx] + (row == null ? 0 : row.init_I);
            if (rowBottom <= scrollOffset) {
               break;
            }

            idx--;
         }

         while (idx < rows.size()) {
            AutismServerInfoOverlay.DetailRow row = rows.get(idx);
            int rowBottom = this.init_IArr[idx] + (row == null ? 0 : row.init_I);
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
   public boolean init_Z_D_3(double mx, double my, int button) {
      if (!this.write_Z) {
         return false;
      }

      try {
         return this.read(mx, my, button);
      } catch (Throwable t) {
         this.init_V_Str_Throwable("mouseClicked", t);
         return false;
      }
   }

   private boolean read(double mx, double my, int button) {
      autismclient.gui.ui.layout.LayoutViewport viewport = this.init_TextFormat.init_RingBuffer();
      float uiMouseX = viewport.toUiX(mx);
      float uiMouseY = viewport.toUiY(my);
      if (this.load_Z_F_F_5(uiMouseX, uiMouseY)) {
         this.init_V_Z(false);
         this.flush_Z = false;
         this.open_Z = false;
         this.close_Z = false;
         return true;
      }

      if (button == 0 && this.init_Z_F_F_6(uiMouseX, uiMouseY)) {
         this.flush_Z = true;
         this.close_Z = false;
         this.init_D = uiMouseX - this.poll_I;
         this.load_D = uiMouseY - this.push_I;
         this.load_F = uiMouseX;
         this.read_F = uiMouseY;
         this.put_I = this.poll_I;
         this.add_I = this.push_I;
         return true;
      }

      if (this.update_Z) {
         return false;
      }

      if (button != 0) {
         return this.init_Z_D_D(mx, my);
      }

      if (this.start == 1 && this.pop_Z && !this.tick_Z) {
         autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics scrollbarMetrics = this.load_PoolEntry_DataUtil_I(
            Math.round(this.init_BitSetUtil_2.load_F()),
            Math.round(this.init_BitSetUtil_2.read_F()),
            Math.round(this.init_BitSetUtil_2.write_F()),
            this.init_I_BitSetUtil(this.init_BitSetUtil_2)
         );
         if (scrollbarMetrics.isScrollable() && scrollbarMetrics.containsTrack(uiMouseX, uiMouseY)) {
            this.open_Z = true;
            this.clear_I = Math.max(0, Math.round(uiMouseY) - scrollbarMetrics.thumbY());
            this.init_CacheEntry.dragToMouseYStepped(scrollbarMetrics, Math.round(uiMouseY), this.clear_I, this.o());
            this.write_V_24();
            return true;
         }
      }

      if (this.start == 1 && this.pop_Z && !this.tick_Z) {
         for (AutismServerInfoOverlay.ClickHotspot region : this.init_List) {
            if (region.init(uiMouseX, uiMouseY)) {
               this.init_TextFormat.load();
               region.init_Runnable.run();
               return true;
            }
         }
      }

      if (this.init_TextFormat.init_Z_D_2(mx, my, button)) {
         return true;
      }

      this.init_TextFormat.load();
      return this.init_Z_D_D(mx, my);
   }

   @Override
   public boolean load_Z_D_3(double mx, double my, int button) {
      if (button == 0 && this.open_Z) {
         this.open_Z = false;
         this.write_V_24();
         return true;
      } else if (this.flush_Z) {
         this.flush_Z = false;
         this.write_V_24();
         return true;
      } else {
         return this.init_TextFormat.load(mx, my, button);
      }
   }

   @Override
   public boolean init_Z_D(double mx, double my, int button, double dx, double dy) {
      if (this.open_Z && button == 0) {
         autismclient.gui.ui.layout.LayoutViewport viewport = this.init_TextFormat.init_RingBuffer();
         float uiMouseY = viewport.toUiY(my);
         autismclient.gui.ui.widgets.ScrollbarMetricsHelper.Metrics scrollbarMetrics = this.load_PoolEntry_DataUtil_I(
            Math.round(this.init_BitSetUtil_2.load_F()),
            Math.round(this.init_BitSetUtil_2.read_F()),
            Math.round(this.init_BitSetUtil_2.write_F()),
            this.init_I_BitSetUtil(this.init_BitSetUtil_2)
         );
         this.init_CacheEntry.dragToMouseYStepped(scrollbarMetrics, Math.round(uiMouseY), this.clear_I, this.o());
         return true;
      }

      if (!this.flush_Z) {
         return this.init_TextFormat.init(mx, my, button, dx, dy);
      }

      autismclient.gui.ui.layout.LayoutViewport viewport = this.init_TextFormat.init_RingBuffer();
      float uiMouseX = viewport.toUiX(mx);
      float uiMouseY = viewport.toUiY(my);
      OverlayBounds clamped = this.init_cH_cH_5(
         new OverlayBounds(Math.round(uiMouseX - (float)this.init_D), Math.round(uiMouseY - (float)this.load_D), this.pop_I, this.run_I, this.write_Z, this.update_Z)
      );
      this.poll_I = clamped.x;
      this.push_I = clamped.y;
      this.close_Z = this.close_Z
         || Math.abs(uiMouseX - this.load_F) >= 3.0F
         || Math.abs(uiMouseY - this.read_F) >= 3.0F
         || this.poll_I != this.put_I
         || this.push_I != this.add_I;
      return true;
   }

   @Override
   public boolean init_Z_D_2(double mx, double my, double amount) {
      if (this.write_Z && !this.update_Z && this.init_Z_D_D(mx, my)) {
         try {
            return this.load_Z_D(mx, my, amount);
         } catch (Throwable t) {
            this.init_V_Str_Throwable("mouseScrolled", t);
            return false;
         }
      } else {
         return false;
      }
   }

   private boolean load_Z_D(double mx, double my, double amount) {
      if (this.start == 1 && this.pop_Z && !this.tick_Z) {
         int viewH = this.init_I_BitSetUtil(this.init_BitSetUtil_2);
         int maxScroll = Math.max(0, this.value - this.load_I_2(viewH));
         this.init_CacheEntry.scrollByWheel(amount, this.o(), maxScroll);
         return true;
      } else {
         return this.init_TextFormat.init_Z_D(mx, my, amount);
      }
   }

   @Override
   public boolean init_Z_I(int keyCode, int scanCode, int modifiers) {
      try {
         return this.write_Z && !this.update_Z && this.init_TextFormat.init_Z_I(keyCode, scanCode, modifiers);
      } catch (Throwable t) {
         this.init_V_Str_Throwable("keyPressed", t);
         return false;
      }
   }

   @Override
   public boolean init_Z_C_I(char chr, int modifiers) {
      try {
         return this.write_Z && !this.update_Z && this.init_TextFormat.init(chr, modifiers);
      } catch (Throwable t) {
         this.init_V_Str_Throwable("charTyped", t);
         return false;
      }
   }

   private void d_V_2() {
      StringBuilder sb = new StringBuilder();
      ServerData entry = init_Minecraft.getCurrentServer();
      String ip = this.open_Str_2();
      String realIp = this.pop(ip);
      List<String> detectedAcs = this.init_List_13();
      String software = this.load(entry);
      String versionNote = this.read(entry);
      sb.append("=== Server Info ===\n");
      sb.append("IP:         ").append(ip).append("\n");
      if (!realIp.equals("--")) {
         sb.append("Real IP:    ").append(realIp).append("\n");
      }

      sb.append("Version:    ").append(this.init_Str_ServerData(entry)).append("\n");
      sb.append("RealVersion: ").append(this.flush_Str_2()).append("\n");
      sb.append("Brand:      ").append(this.close_Str_2()).append("\n");
      if (!"--".equals(software)) {
         sb.append("Software:   ").append(software).append("\n");
      }

      if (init_Minecraft.getConnection() != null) {
         sb.append("Players:    ").append(init_Minecraft.getConnection().getListedOnlinePlayers().size());
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

      sb.append("Difficulty: ").append(init_Minecraft.level != null ? init_Minecraft.level.getDifficulty().getDisplayName().getString() : "--").append("\n");
      sb.append("World:      ").append(this.push_Str_2()).append("\n");
      if (init_Minecraft.level != null) {
         long dayCount = init_Minecraft.level.getOverworldClockTime() / 24000L;
         long timeOfDay = init_Minecraft.level.getOverworldClockTime() % 24000L;
         int hours = (int)((timeOfDay / 1000L + 6L) % 24L);
         int minutes = (int)(timeOfDay % 1000L * 60L / 1000L);
         sb.append("Time:       Day ").append(dayCount).append(" (").append(String.format("%02d:%02d", hours, minutes)).append(")\n");
      } else {
         sb.append("Time:       --\n");
      }

      if (this.tick_Z) {
         sb.append("AntiCheats: Scanning...\n");
      } else if (!this.pop_Z) {
         sb.append("AntiCheats: Probe Plugins First\n");
      } else if (detectedAcs.isEmpty()) {
         sb.append("AntiCheats: None detected\n");
      } else {
         sb.append("AntiCheats: ").append(String.join(", ", detectedAcs)).append("\n");
      }

      init_Minecraft.keyboardHandler.setClipboard(sb.toString());
      bi.init("Server info copied.");
   }

   private void e_V_2() {
      if (this.load_List.isEmpty()) {
         bi.write("No plugins detected.");
      } else {
         Map<String, List<AutismServerInfoOverlay.DetectedPluginEntry>> grouped = new TreeMap<>((a, b) -> {
            int rank = Integer.compare(this.update_I(a), this.update_I(b));
            return rank != 0 ? rank : String.CASE_INSENSITIVE_ORDER.compare(a, b);
         });
         int copyCount = 0;

         for (String plugin : this.load((Collection<String>)this.load_List)) {
            if (plugin != null && !plugin.isBlank()) {
               AutismServerInfoOverlay.DetectedPluginEntry detail = this.init_AutismServerInfoOv_Str_3(plugin);
               if (detail != null
                  && detail.load_AutismServerInfoOv != AutismServerInfoOverlay.EvidenceSource.HELP_HINT
                  && detail.load_AutismServerInfoOv != AutismServerInfoOverlay.EvidenceSource.UNKNOWN) {
                  String uiGroupTitle = this.init_Str_AutismServerInfoOv(
                     detail.init_AutismServerInfoOv_2, detail.init_AutismServerInfoOv, detail.init_AutismServerInfoOv_3
                  );
                  if (!"Command Hints".equals(uiGroupTitle)
                     && !"Other".equals(uiGroupTitle)
                     && (
                        detail.init_AutismServerInfoOv_2 != AutismServerInfoOverlay.DetectionType.FEATURE
                           || detail.load_AutismServerInfoOv == AutismServerInfoOverlay.EvidenceSource.COMMAND_GUI
                     )) {
                     grouped.computeIfAbsent(this.init_Str_AutismServerInfoOv_2(detail), unused -> new ArrayList<>()).add(detail);
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
                  details.sort(Comparator.comparing(detailx -> detailx.init_Str, String.CASE_INSENSITIVE_ORDER));
                  sb.append("\n[").append(group.getKey()).append("]\n");

                  for (AutismServerInfoOverlay.DetectedPluginEntry detail : details) {
                     boolean ac = update_Set.contains(detail.load_Str.toLowerCase(Locale.ROOT));
                     String groupTitle = this.init_Str_AutismServerInfoOv_2(detail);
                     sb.append(ac ? "[AC] " : "- ").append(detail.init_Str);
                     if (!detail.write_List.isEmpty()
                        && !"Plugin Command Hint".equals(groupTitle)
                        && !"Version Command Hint".equals(groupTitle)
                        && !"Payload Channel".equals(groupTitle)) {
                        sb.append(" (").append(detail.write_List.size()).append(" cmds)");
                     }

                     sb.append("\n");
                  }
               }
            }

            init_Minecraft.keyboardHandler.setClipboard(sb.toString());
            bi.init("Plugin list copied.");
         }
      }
   }

   private static final class ProbeObservation {
      final long init_J;
      final boolean init_Z;
      final AutismServerInfoOverlay.ProbeSpec init_AutismServerInfoOv;
      final AutismServerInfoOverlay.ProbeSpec load;
      final List<String> init_List;

      ProbeObservation(
         long generation,
         boolean liveObservation,
         AutismServerInfoOverlay.ProbeSpec probe,
         AutismServerInfoOverlay.ProbeSpec observed,
         List<String> tokens
      ) {
         this.init_J = generation;
         this.init_Z = liveObservation;
         this.init_AutismServerInfoOv = probe;
         this.load = observed;
         this.init_List = tokens;
      }
   }

   private enum ScanStatus {
      NONE,
      COMPLETE,
      PARTIAL,
      FAILED;
   }

   private static final class CommandEvidence {
      final String init_Str;
      final AutismServerInfoOverlay.EvidenceSource init_AutismServerInfoOv;
      final String load;

      CommandEvidence(String command, AutismServerInfoOverlay.EvidenceSource evidence, String guiTitle) {
         this.init_Str = command;
         this.init_AutismServerInfoOv = evidence;
         this.load = guiTitle;
      }
   }

   private enum MatchConfidence {
      EXACT,
      STRONG,
      FEATURE,
      UNKNOWN;
   }

   private static class ClickHotspot {
      final int init_I;
      final int load;
      final int read;
      final int write;
      final Runnable init_Runnable;

      ClickHotspot(int x, int y, int width, int height, Runnable action) {
         this.init_I = x;
         this.load = y;
         this.read = width;
         this.write = height;
         this.init_Runnable = action;
      }

      boolean init(double mx, double my) {
         return mx >= this.init_I && mx < this.init_I + this.read && my >= this.load && my < this.load + this.write;
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
      private float sync = 0.0F;

      ProgressBarNode() {
         this.write = 12.0F;
      }

      AutismServerInfoOverlay.ProgressBarNode init(float progress) {
         this.sync = Math.max(0.0F, Math.min(1.0F, progress));
         return this;
      }

      @Override
      public float init_F_SyncManager_F(autismclient.gui.ui.layout.LayoutContext context, float availableWidth) {
         return 12.0F;
      }

      @Override
      public void init_V_SyncManager(autismclient.gui.ui.layout.LayoutContext context) {
         if (this.init_Z) {
            autismclient.gui.ui.widgets.ProgressBarRenderer.init(
               autismclient.gui.f.UiHelper.init(
                  context.graphics(), context.font(), Math.round(context.mouseX()), Math.round(context.mouseY())
               ),
               autismclient.gui.f.NetHelper.init(Math.round(this.init_F), Math.round(this.load_F), Math.round(this.read_F), Math.round(this.write)),
               this.sync
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
      String init_Str;
      final Set<String> init_Set = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
      final Set<String> load = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
      final Set<String> read = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
      final List<String> init_List = new ArrayList<>();
   }

   private static final class ActiveScanSession {
      final long init_J;
      final String init_Str;
      final ServerScanAttemptTracker init_bS;
      String load_Str;
      final long load_J;
      final Map<String, AutismServerInfoOverlay.PluginAccumulator> init_Map;
      long read_J;
      long write_J;
      final Set<Integer> init_Set = new HashSet<>();
      final Map<Integer, AutismServerInfoOverlay.ProbeSpec> load_Map = new HashMap<>();
      final Deque<AutismServerInfoOverlay.QueuedProbe> init_Deque = new ArrayDeque<>();
      int init_I;
      int load_I;
      int read_I;
      int write_I;
      int update;
      int sync;
      int flush;
      int close;
      int open;

      ActiveScanSession(
         long generation, String serverAddress, String contextSignature, long startedAt, Map<String, AutismServerInfoOverlay.PluginAccumulator> previousEntries
      ) {
         this.init_J = generation;
         this.init_Str = serverAddress == null ? "" : serverAddress;
         this.init_bS = new ServerScanAttemptTracker(generation, this.init_Str);
         this.load_Str = contextSignature == null ? "" : contextSignature;
         this.load_J = startedAt;
         this.init_Map = previousEntries == null ? Map.of() : Collections.unmodifiableMap(new TreeMap<>(previousEntries));
         this.read_J = startedAt;
         this.write_J = startedAt;
      }

      boolean init(long expectedGeneration, String address) {
         return this.init_bS.init(expectedGeneration, address);
      }
   }

   private static final class ProbeSpec {
      final String init_Str;
      final AutismServerInfoOverlay.ProbeKind init_AutismServerInfoOv;
      final String load;

      ProbeSpec(String query, AutismServerInfoOverlay.ProbeKind kind, String hint) {
         this.init_Str = query;
         this.init_AutismServerInfoOv = kind;
         this.load = hint;
      }
   }

   private static final class TabBarNode extends autismclient.gui.ui.layout.LayoutNode {
      private static final int init_I = 2;
      private String[] init_StrArr = new String[0];
      private int load_I = 0;
      private IntConsumer init_IntConsumer;

      AutismServerInfoOverlay.TabBarNode init_AutismServerInfoOv_StrArr(String... tabs) {
         this.init_StrArr = tabs == null ? new String[0] : (String[])tabs.clone();
         this.init_AutismServerInfoOv_I(this.load_I);
         this.load_V();
         return this;
      }

      AutismServerInfoOverlay.TabBarNode init_AutismServerInfoOv_I(int activeIndex) {
         this.load_I = Math.max(0, Math.min(Math.max(0, this.init_StrArr.length - 1), activeIndex));
         return this;
      }

      AutismServerInfoOverlay.TabBarNode init_AutismServerInfoOv_IntConsumer(IntConsumer onSelect) {
         this.init_IntConsumer = onSelect;
         return this;
      }

      @Override
      public float init_F_SyncManager_F(autismclient.gui.ui.layout.LayoutContext context, float availableWidth) {
         return Math.max(autismclient.gui.ui.UiDensityMetrics.scaledHeight(13), context.theme().update());
      }

      @Override
      public void init_V_SyncManager(autismclient.gui.ui.layout.LayoutContext context) {
         if (this.init_Z && this.init_StrArr.length != 0) {
            autismclient.gui.f.SyncManager uiContext = autismclient.gui.f.UiHelper.init(
               context.graphics(), context.font(), Math.round(context.mouseX()), Math.round(context.mouseY())
            );

            for (int i = 0; i < this.init_StrArr.length; i++) {
               autismclient.gui.f.NetHelper bounds = this.init_NetHelper_I(i);
               boolean active = i == this.load_I;
               TabStripRenderer.init(uiContext, bounds, this.init_StrArr[i], active);
            }
         }
      }

      @Override
      public boolean init_Z_SyncManager_2(autismclient.gui.ui.layout.LayoutContext context, float mouseX, float mouseY, int button) {
         if (this.init_Z && this.load_Z && button == 0 && this.init_StrArr.length != 0) {
            for (int i = 0; i < this.init_StrArr.length; i++) {
               if (this.init_NetHelper_I(i).init_Z(Math.round(mouseX), Math.round(mouseY))) {
                  if (i != this.load_I) {
                     this.init_AutismServerInfoOv_I(i);
                     if (this.init_IntConsumer != null) {
                        this.init_IntConsumer.accept(i);
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

      private autismclient.gui.f.NetHelper init_NetHelper_I(int index) {
         return TabStripRenderer.init(
            autismclient.gui.f.NetHelper.init(Math.round(this.init_F), Math.round(this.load_F), Math.round(this.read_F), Math.round(this.write)),
            this.init_StrArr.length,
            index,
            2
         );
      }
   }

   private static final class DetailRow {
      final AutismServerInfoOverlay.DetailRowKind init_AutismServerInfoOv_3;
      final String init_Str;
      final String load;
      final String read;
      final String write;
      final AutismServerInfoOverlay.DetectionType init_AutismServerInfoOv_2;
      final AutismServerInfoOverlay.EvidenceSource init_AutismServerInfoOv_4;
      final AutismServerInfoOverlay.MatchConfidence init_AutismServerInfoOv;
      final int init_I;
      final String update;
      final String sync;

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
         this.init_AutismServerInfoOv_3 = type;
         this.init_Str = title;
         this.load = plugin;
         this.read = label;
         this.write = actionCommand;
         this.init_AutismServerInfoOv_2 = resultKind == null ? AutismServerInfoOverlay.DetectionType.PLUGIN : resultKind;
         this.init_AutismServerInfoOv_4 = evidence == null ? AutismServerInfoOverlay.EvidenceSource.UNKNOWN : evidence;
         this.init_AutismServerInfoOv = confidence == null ? AutismServerInfoOverlay.MatchConfidence.UNKNOWN : confidence;
         this.init_I = Math.max(1, height);
         this.update = countLabel == null ? "" : countLabel;
         this.sync = searchText == null ? "" : searchText;
      }

      static AutismServerInfoOverlay.DetailRow init(String title, int height) {
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
            init(title)
         );
      }

      static AutismServerInfoOverlay.DetailRow init(
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

      static AutismServerInfoOverlay.DetailRow init(
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

      boolean init() {
         return this.init_AutismServerInfoOv_3 == AutismServerInfoOverlay.DetailRowKind.HEADER;
      }

      private static String init(String text) {
         return text == null ? "" : text.toLowerCase(Locale.ROOT);
      }
   }

   private record CommandHistoryEntry(String init_Str, String load, long init_J) {
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
      private float sync = 0.0F;
      private float flush = 1.0F;
      private float close = 0.0F;
      private float open = 0.0F;
      private boolean write_Z = false;
      private Consumer<Float> init_Consumer;

      SliderNode() {
         this.write = 12.0F;
      }

      AutismServerInfoOverlay.SliderNode init_AutismServerInfoOv(float min, float max) {
         this.sync = min;
         this.flush = Math.max(min, max);
         return this.load_AutismServerInfoOv(this.close);
      }

      AutismServerInfoOverlay.SliderNode init_AutismServerInfoOv(float step) {
         this.open = Math.max(0.0F, step);
         return this.load_AutismServerInfoOv(this.close);
      }

      AutismServerInfoOverlay.SliderNode load_AutismServerInfoOv(float value) {
         float snapped = this.load_F(this.init_F(value));
         if (Math.abs(this.close - snapped) > 1.0E-4F) {
            this.close = snapped;
            if (this.init_Consumer != null) {
               this.init_Consumer.accept(this.close);
            }
         }

         return this;
      }

      AutismServerInfoOverlay.SliderNode init(Consumer<Float> onChange) {
         this.init_Consumer = onChange;
         return this;
      }

      @Override
      public float init_F_SyncManager_F(autismclient.gui.ui.layout.LayoutContext context, float availableWidth) {
         return 12.0F;
      }

      @Override
      public void init_V_SyncManager(autismclient.gui.ui.layout.LayoutContext context) {
         if (this.init_Z) {
            autismclient.gui.f.NetHelper bounds = autismclient.gui.f.NetHelper.init(
               Math.round(this.init_F), Math.round(this.load_F), Math.round(this.read_F), Math.round(this.write)
            );
            autismclient.gui.f.SyncManager uiContext = autismclient.gui.f.UiHelper.init(
               context.graphics(), context.font(), Math.round(context.mouseX()), Math.round(context.mouseY())
            );
            SliderRenderer.init(uiContext, bounds, SliderRenderer.init_D_D(this.close, this.sync, this.flush), this.init_Z_F_F(context.mouseX(), context.mouseY()) || this.write_Z);
         }
      }

      @Override
      public boolean init_Z_SyncManager_2(autismclient.gui.ui.layout.LayoutContext context, float mouseX, float mouseY, int button) {
         if (button == 0 && this.init_Z_F_F(mouseX, mouseY)) {
            this.write_Z = true;
            this.init_V(mouseX);
            return true;
         } else {
            return false;
         }
      }

      @Override
      public boolean load_Z_SyncManager(autismclient.gui.ui.layout.LayoutContext context, float mouseX, float mouseY, int button) {
         if (button == 0 && this.write_Z) {
            this.write_Z = false;
            return true;
         } else {
            return false;
         }
      }

      @Override
      public boolean init_Z_SyncManager(autismclient.gui.ui.layout.LayoutContext context, float mouseX, float mouseY, int button, float deltaX, float deltaY) {
         if (this.write_Z && button == 0) {
            this.init_V(mouseX);
            return true;
         } else {
            return false;
         }
      }

      private void init_V(float mouseX) {
         this.load_AutismServerInfoOv(
            (float)SliderRenderer.init(mouseX, Math.round(this.init_F), Math.round(this.read_F), this.sync, this.flush, this.open <= 0.0F ? 9.999999747378752E-5 : this.open)
         );
      }

      private float init_F(float value) {
         return Math.max(this.sync, Math.min(this.flush, value));
      }

      private float load_F(float value) {
         return this.open <= 0.0F ? value : this.sync + Math.round((value - this.sync) / this.open) * this.open;
      }
   }

   private static final class DetectedPluginEntry {
      final String init_Str;
      final String load_Str;
      final List<String> init_List;
      final List<String> load_List;
      final List<String> read_List;
      final AutismServerInfoOverlay.DetectionType init_AutismServerInfoOv_2;
      final AutismServerInfoOverlay.EvidenceSource init_AutismServerInfoOv_3;
      final AutismServerInfoOverlay.EvidenceSource load_AutismServerInfoOv;
      final AutismServerInfoOverlay.MatchConfidence init_AutismServerInfoOv;
      final String read_Str;
      final String write_Str;
      final List<String> write_List;
      final String update;

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
         this.init_Str = displayName == null ? "" : displayName;
         this.load_Str = key == null ? "" : key;
         this.init_List = commands == null ? List.of() : commands;
         this.load_List = guis == null ? List.of() : guis;
         this.read_List = channels == null ? List.of() : channels;
         this.init_AutismServerInfoOv_2 = resultKind == null ? AutismServerInfoOverlay.DetectionType.PLUGIN : resultKind;
         this.init_AutismServerInfoOv_3 = evidence == null ? AutismServerInfoOverlay.EvidenceSource.UNKNOWN : evidence;
         this.load_AutismServerInfoOv = copyEvidence == null ? AutismServerInfoOverlay.EvidenceSource.UNKNOWN : copyEvidence;
         this.init_AutismServerInfoOv = confidence == null ? AutismServerInfoOverlay.MatchConfidence.UNKNOWN : confidence;
         this.read_Str = sourceLabel == null ? "" : sourceLabel;
         this.write_Str = countLabel == null ? "" : countLabel;
         this.write_List = copyCommands == null ? List.of() : copyCommands;
         this.update = searchText == null ? "" : searchText;
      }
   }

   private static final class PluginAccumulator {
      String init_Str;
      boolean init_Z;
      AutismServerInfoOverlay.DetectionType init_AutismServerInfoOv_2;
      AutismServerInfoOverlay.EvidenceSource init_AutismServerInfoOv_3 = AutismServerInfoOverlay.EvidenceSource.UNKNOWN;
      AutismServerInfoOverlay.EvidenceSource load_AutismServerInfoOv = AutismServerInfoOverlay.EvidenceSource.UNKNOWN;
      AutismServerInfoOverlay.MatchConfidence init_AutismServerInfoOv = AutismServerInfoOverlay.MatchConfidence.FEATURE;
      final Set<String> init_Set = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
      final Set<String> load_Set = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
      final Set<String> read = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
      final Set<String> write = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
   }

   private static final class QueuedProbe {
      final int init_I;
      final AutismServerInfoOverlay.ProbeSpec init_AutismServerInfoOv;
      final int load;

      QueuedProbe(int id, AutismServerInfoOverlay.ProbeSpec spec) {
         this(id, spec, 0);
      }

      QueuedProbe(int id, AutismServerInfoOverlay.ProbeSpec spec, int attempt) {
         this.init_I = id;
         this.init_AutismServerInfoOv = spec;
         this.load = attempt;
      }
   }
}
