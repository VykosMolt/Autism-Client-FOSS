package autismclient.util;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class PluginSignatureCatalog {
   private static final int init_I = 512;
   private static final Pattern init_Pattern = Pattern.compile("(?i)\\b[a-z0-9_.-]{2,64}:[a-z0-9_./-]{1,128}\\b");
   private static final Map<String, PluginSignatureCatalog.Inner_ConfigStore> init_Map = new HashMap<>();
   private static final Map<String, String> load_Map = Map.ofEntries(
      Map.entry("vv", "ViaVersion"),
      Map.entry("viaversion", "ViaVersion"),
      Map.entry("viabackwards", "ViaBackwards"),
      Map.entry("viarewind", "ViaRewind"),
      Map.entry("bungeecord", "BungeeCord"),
      Map.entry("velocity", "Velocity"),
      Map.entry("geyser", "Geyser"),
      Map.entry("floodgate", "Floodgate"),
      Map.entry("worldedit", "WorldEdit"),
      Map.entry("fawe", "FAWE"),
      Map.entry("fastasyncworldedit", "FastAsyncWorldEdit"),
      Map.entry("worldguard", "WorldGuard"),
      Map.entry("coreprotect", "CoreProtect"),
      Map.entry("claimmaps", "ClaimMessenger"),
      Map.entry("claimmessenger", "ClaimMessenger"),
      Map.entry("openpartiesandclaims", "Open Parties and Claims"),
      Map.entry("openpac", "Open Parties and Claims"),
      Map.entry("ftbchunks", "FTB Chunks"),
      Map.entry("ftbteams", "FTB Teams"),
      Map.entry("griefprevention", "GriefPrevention"),
      Map.entry("griefdefender", "GriefDefender"),
      Map.entry("residence", "Residence"),
      Map.entry("lands", "Lands"),
      Map.entry("towny", "Towny"),
      Map.entry("townyadvanced", "Towny"),
      Map.entry("plotsquared", "PlotSquared"),
      Map.entry("bentobox", "BentoBox"),
      Map.entry("huskclaims", "HuskClaims"),
      Map.entry("husktowns", "HuskTowns"),
      Map.entry("protectionstones", "ProtectionStones"),
      Map.entry("redprotect", "RedProtect"),
      Map.entry("claimchunk", "ClaimChunk"),
      Map.entry("lwc", "LWC"),
      Map.entry("lwcx", "LWCX"),
      Map.entry("factions", "Factions"),
      Map.entry("kingdoms", "Kingdoms"),
      Map.entry("kingdomsx", "KingdomsX"),
      Map.entry("luckperms", "LuckPerms"),
      Map.entry("tab", "TAB"),
      Map.entry("sr", "SkinsRestorer"),
      Map.entry("skinsrestorer", "SkinsRestorer"),
      Map.entry("authme", "AuthMeReloaded"),
      Map.entry("authmereloaded", "AuthMeReloaded"),
      Map.entry("librelogin", "LibreLogin"),
      Map.entry("nlogin", "nLogin"),
      Map.entry("openlogin", "OpeNLogin"),
      Map.entry("limboauth", "LimboAuth"),
      Map.entry("loginsecurity", "LoginSecurity"),
      Map.entry("fastlogin", "FastLogin"),
      Map.entry("bungeeguard", "BungeeGuard"),
      Map.entry("emotecraft", "Emotecraft"),
      Map.entry("journeymap", "JourneyMap"),
      Map.entry("worldinfo", "WorldInfo"),
      Map.entry("xaerominimap", "Xaero Minimap"),
      Map.entry("xaeroworldmap", "Xaero World Map"),
      Map.entry("voxelmap", "VoxelMap"),
      Map.entry("cardinal-components", "Cardinal Components API"),
      Map.entry("cca", "Cardinal Components API"),
      Map.entry("polymer", "Polymer"),
      Map.entry("quickshop", "QuickShop-Hikari"),
      Map.entry("quickshophikari", "QuickShop-Hikari"),
      Map.entry("quickshop-hikari", "QuickShop-Hikari"),
      Map.entry("quickshopreremake", "QuickShop Reremake"),
      Map.entry("economyshopgui", "EconomyShopGUI"),
      Map.entry("economyshopguipremium", "EconomyShopGUI Premium"),
      Map.entry("economyshopguiplus", "EconomyShopGUI"),
      Map.entry("esgui", "EconomyShopGUI"),
      Map.entry("shopguiplus", "ShopGUI+"),
      Map.entry("shopgui", "ShopGUI+"),
      Map.entry("bossshoppro", "BossShopPro"),
      Map.entry("bossshop", "BossShop"),
      Map.entry("chestshop", "ChestShop"),
      Map.entry("shopchest", "ShopChest"),
      Map.entry("shopkeepers", "Shopkeepers"),
      Map.entry("ultimateshop", "UltimateShop"),
      Map.entry("excellentshop", "ExcellentShop"),
      Map.entry("zshop", "zShop"),
      Map.entry("guishop", "GUIShop"),
      Map.entry("simpleshop", "SimpleShop"),
      Map.entry("simpleshopgui", "SimpleShopGUI"),
      Map.entry("shopx", "Shop X"),
      Map.entry("bettershop", "BetterShop"),
      Map.entry("bettershops", "Better Shops"),
      Map.entry("deluxemenus", "DeluxeMenus"),
      Map.entry("commandpanels", "CommandPanels"),
      Map.entry("interactivechat", "InteractiveChat"),
      Map.entry("interchat", "InteractiveChat"),
      Map.entry("auctionmaster", "AuctionMaster"),
      Map.entry("crazyauctions", "CrazyAuctions"),
      Map.entry("zauctionhouse", "zAuctionHouse"),
      Map.entry("playerauctions", "PlayerAuctions"),
      Map.entry("auctionguiplus", "AuctionGUI+"),
      Map.entry("excellentauctionhouse", "ExcellentAuctionHouse"),
      Map.entry("excellentauctions", "ExcellentAuctions"),
      Map.entry("axauctions", "AxAuctions"),
      Map.entry("axauctionhouse", "AxAuctionHouse"),
      Map.entry("nexusauctionhouse", "NexusAuctionHouse"),
      Map.entry("deluxeauctions", "DeluxeAuctions"),
      Map.entry("outauction", "OutAuction"),
      Map.entry("fauction", "FAuction"),
      Map.entry("auctionhouseplus", "AuctionHousePlus"),
      Map.entry("crazycrates", "CrazyCrates"),
      Map.entry("crazycrate", "CrazyCrates"),
      Map.entry("excellentcrates", "ExcellentCrates"),
      Map.entry("excellentcrate", "ExcellentCrates"),
      Map.entry("goldencrates", "GoldenCrates"),
      Map.entry("goldencrate", "GoldenCrates"),
      Map.entry("cratereloaded", "CrateReloaded"),
      Map.entry("magiccrates", "MagicCrates"),
      Map.entry("magiccrate", "MagicCrates"),
      Map.entry("simplelootcrates", "SimpleLootCrates"),
      Map.entry("simplelootcrate", "SimpleLootCrates"),
      Map.entry("axcrates", "AxCrates"),
      Map.entry("azcrates", "AzCrates"),
      Map.entry("cratesplus", "CratesPlus"),
      Map.entry("cratekeys", "CrateKeys"),
      Map.entry("mysterycrates", "MysteryCrates"),
      Map.entry("phoenixcrates", "PhoenixCrates"),
      Map.entry("advancedcrates", "AdvancedCrates"),
      Map.entry("specializedcrates", "SpecializedCrates"),
      Map.entry("oraxen", "Oraxen"),
      Map.entry("itemsadder", "ItemsAdder"),
      Map.entry("nexo", "Nexo"),
      Map.entry("modelengine", "ModelEngine"),
      Map.entry("mythicmobs", "MythicMobs"),
      Map.entry("mythiclib", "MythicLib"),
      Map.entry("mmoitems", "MMOItems"),
      Map.entry("placeholderapi", "PlaceholderAPI"),
      Map.entry("protocollib", "ProtocolLib"),
      Map.entry("packetevents", "PacketEvents"),
      Map.entry("axiom", "Axiom Paper Plugin"),
      Map.entry("vault", "Vault"),
      Map.entry("essentials", "EssentialsX"),
      Map.entry("essentialsx", "EssentialsX"),
      Map.entry("cmi", "CMI"),
      Map.entry("citizens", "Citizens"),
      Map.entry("fancyholograms", "FancyHolograms"),
      Map.entry("fancynpcs", "FancyNpcs"),
      Map.entry("voicechat", "Simple Voice Chat"),
      Map.entry("vc", "Simple Voice Chat"),
      Map.entry("plasmo", "Plasmo Voice"),
      Map.entry("plasmovoice", "Plasmo Voice"),
      Map.entry("minecraftafk", "MinecraftAFK"),
      Map.entry("donutaddon", "DonutAddon"),
      Map.entry("donutsmp", "DonutSMP"),
      Map.entry("nighthawk", "Nighthawk"),
      Map.entry("feather", "Feather"),
      Map.entry("proantitab", "ProAntiTab"),
      Map.entry("pat", "ProAntiTab"),
      Map.entry("consumable_optimizer", "Consumable Optimizer"),
      Map.entry("clientcrystal", "FastCrystal"),
      Map.entry("fastcrystal", "FastCrystal"),
      Map.entry("optiplus", "OptiPlus"),
      Map.entry("optiplus-crystal", "OptiPlus Crystal"),
      Map.entry("optiplus-anchor", "OptiPlus Anchor"),
      Map.entry("fabric", "Fabric"),
      Map.entry("fabric-networking-api-v1", "Fabric Networking"),
      Map.entry("fabric-networking-v0", "Fabric Networking"),
      Map.entry("labymod", "LabyMod"),
      Map.entry("labymod3", "LabyMod"),
      Map.entry("essential", "Essential")
   );
   private static final Set<String> init_Set = Set.of("minecraft", "brigadier", "autism", "autismclient", "c");
   private static final Set<String> load_Set = Set.of(
      "minecraft:register", "minecraft:unregister", "register", "unregister", "minecraft:brand", "brand", "bungeecord", "bungeecord:main"
   );

   private PluginSignatureCatalog() {
   }

   public static boolean init_Z(String channel) {
      String normalized = write(channel);
      if (normalized.isBlank()) {
         return false;
      }

      if (load_Set.contains(normalized)) {
         return true;
      }

      int colon = normalized.indexOf(58);
      if (colon <= 0) {
         return read_Z(normalized);
      }

      String namespace = normalized.substring(0, colon);
      return !init_Set.contains(namespace);
   }

   public static synchronized boolean init(String serverAddress, String brand, AutismPayloadSupport.Inner_StreamUtil snapshot) {
      if (snapshot == null) {
         return false;
      }

      if (!"S2C".equalsIgnoreCase(snapshot.init_Str())) {
         return false;
      }

      String addressKey = update(serverAddress);
      if (addressKey.isBlank()) {
         return false;
      }

      PluginSignatureCatalog.Inner_ConfigStore state = init_Map.computeIfAbsent(addressKey, unused -> new PluginSignatureCatalog.Inner_ConfigStore());
      state.init_Str = sync(brand);
      boolean changed = false;
      String channel = write(snapshot.load_Str());
      if (!channel.isBlank() && init_Z(channel)) {
         changed |= state.init(channel, snapshot.init_Str());
      }

      if (load_Z(channel)) {
         for (String registered : init(snapshot.init_BArr())) {
            if (init_Z(registered)) {
               changed |= state.load(registered, snapshot.init_Str());
            }
         }
      } else {
         for (String embedded : load(snapshot.init_BArr())) {
            if (init_Z(embedded)) {
               changed |= state.read(embedded, snapshot.init_Str());
            }
         }
      }

      if (changed) {
         state.init_J++;
      }

      return changed;
   }

   public static synchronized List<PluginSignatureCatalog.Inner_CachePool> init_List(String serverAddress, String brand) {
      PluginSignatureCatalog.Inner_ConfigStore state = init_Map.get(update(serverAddress));
      if (state == null) {
         return List.of();
      }

      Map<String, PluginSignatureCatalog.Inner_StreamUtil> byPlugin = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

      for (PluginSignatureCatalog.Inner_DataUtil stats : state.init_List()) {
         if (stats.init_Z()) {
            PluginSignatureCatalog.Inner_BufferHelper identity = init_bQ_BufferHelper(stats.init_Str);
            if (identity != null && !identity.init().isBlank() && !identity.load().isBlank()) {
               PluginSignatureCatalog.Inner_StreamUtil acc = byPlugin.computeIfAbsent(identity.load(), unused -> new PluginSignatureCatalog.Inner_StreamUtil(identity.init(), identity.load()));
               acc.init(stats);
            }
         }
      }

      if (byPlugin.isEmpty()) {
         return List.of();
      }

      List<PluginSignatureCatalog.Inner_CachePool> out = new ArrayList<>();

      for (PluginSignatureCatalog.Inner_StreamUtil acc : byPlugin.values()) {
         out.add(acc.init());
      }

      return out;
   }

   public static synchronized long init_J(String serverAddress, String brand) {
      PluginSignatureCatalog.Inner_ConfigStore state = init_Map.get(update(serverAddress));
      return state == null ? 0L : state.init_J;
   }

   public static synchronized String init_Str(String serverAddress, String brand) {
      PluginSignatureCatalog.Inner_ConfigStore state = init_Map.get(update(serverAddress));
      if (state == null) {
         return "";
      }

      List<String> channels = new ArrayList<>(state.init_Set());
      channels.sort(String.CASE_INSENSITIVE_ORDER);
      if (channels.size() > 96) {
         channels = new ArrayList<>(channels.subList(0, 96));
      }

      return String.join(",", channels).toLowerCase(Locale.ROOT);
   }

   public static synchronized void init() {
      init_Map.clear();
   }

   private static boolean load_Z(String channel) {
      String normalized = write(channel);
      return "minecraft:register".equals(normalized)
         || "register".equals(normalized)
         || "minecraft:unregister".equals(normalized)
         || "unregister".equals(normalized);
   }

   private static boolean read_Z(String channel) {
      String normalized = write(channel);
      return "bungeecord".equals(normalized) || "register".equals(normalized) || "unregister".equals(normalized);
   }

   private static List<String> init(byte[] rawBytes) {
      if (rawBytes != null && rawBytes.length != 0) {
         LinkedHashSet<String> channels = new LinkedHashSet<>();
         String text = new String(rawBytes, StandardCharsets.UTF_8);

         for (String part : text.split("[\\u0000\\r\\n\\t ,;]+")) {
            String token = write(part);
            if (!token.isBlank() && (token.indexOf(58) > 0 || read_Z(token))) {
               channels.add(token);
            }
         }

         channels.addAll(load(rawBytes));
         return List.copyOf(channels);
      } else {
         return List.of();
      }
   }

   private static List<String> load(byte[] rawBytes) {
      if (rawBytes != null && rawBytes.length != 0) {
         String text = new String(rawBytes, StandardCharsets.UTF_8);
         Matcher matcher = init_Pattern.matcher(text);
         LinkedHashSet<String> channels = new LinkedHashSet<>();

         while (matcher.find()) {
            String channel = write(matcher.group());
            if (!channel.isBlank()) {
               channels.add(channel);
            }

            if (channels.size() >= 512) {
               break;
            }
         }

         return List.copyOf(channels);
      } else {
         return List.of();
      }
   }

   private static PluginSignatureCatalog.Inner_BufferHelper init_bQ_BufferHelper(String channel) {
      String normalized = write(channel);
      if (normalized.isBlank()) {
         return null;
      }

      if ("bungeecord".equals(normalized) || "bungeecord:main".equals(normalized)) {
         return new PluginSignatureCatalog.Inner_BufferHelper("BungeeCord", "bungeecord");
      }

      if (!"register".equals(normalized) && !"unregister".equals(normalized)) {
         int colon = normalized.indexOf(58);
         if (colon <= 0) {
            return null;
         }

         String namespace = normalized.substring(0, colon);
         if (init_Set.contains(namespace)) {
            return null;
         }

         String alias = load_Map.get(namespace);
         if (alias != null) {
            return new PluginSignatureCatalog.Inner_BufferHelper(alias, namespace);
         }

         String presetLabel = load_Str(normalized);
         return presetLabel != null && !presetLabel.isBlank()
            ? new PluginSignatureCatalog.Inner_BufferHelper(presetLabel, namespace)
            : new PluginSignatureCatalog.Inner_BufferHelper(read_Str(namespace), namespace);
      } else {
         return null;
      }
   }

   private static String load_Str(String channel) {
      for (PayloadChannelFilterManager.Inner_CachePool preset : PayloadChannelFilterManager.write_List()) {
         if (PayloadChannelFilterManager.init_Z_bB_CachePool(preset) && PayloadChannelFilterManager.init_Z_bB_CachePool_Str(preset, channel)) {
            return preset.load();
         }
      }

      return null;
   }

   private static String read_Str(String namespace) {
      String clean = namespace == null ? "" : namespace.trim();
      if (clean.isBlank()) {
         return "";
      }

      String[] parts = clean.split("[_\\-.]+");
      StringBuilder sb = new StringBuilder();

      for (String part : parts) {
         if (!part.isBlank()) {
            if (!sb.isEmpty()) {
               sb.append(' ');
            }

            if (part.length() <= 3 && part.chars().allMatch(Character::isLetter)) {
               sb.append(part.toUpperCase(Locale.ROOT));
            } else {
               sb.append(Character.toUpperCase(part.charAt(0)));
               if (part.length() > 1) {
                  sb.append(part.substring(1));
               }
            }
         }
      }

      return sb.isEmpty() ? clean : sb.toString();
   }

   private static String write(String channel) {
      return channel == null ? "" : channel.trim().toLowerCase(Locale.ROOT);
   }

   private static String update(String address) {
      return address == null ? "" : address.trim().toLowerCase(Locale.ROOT);
   }

   private static String sync(String brand) {
      return brand == null ? "" : brand.trim().toLowerCase(Locale.ROOT);
   }

   public static String init_Str(String value) {
      if (value == null) {
         return "";
      }

      String clean = value.trim().toLowerCase(Locale.ROOT);
      if (clean.isBlank()) {
         return "";
      }

      int colon = clean.indexOf(58);
      if (colon > 0) {
         clean = clean.substring(0, colon);
      }

      String alias = load_Map.get(clean);
      return alias != null ? flush(alias) : flush(clean);
   }

   public static List<String> init_List(String pluginName) {
      String canonical = init_Str(pluginName);
      if (canonical.isBlank()) {
         return List.of();
      }

      LinkedHashSet<String> namespaces = new LinkedHashSet<>();

      for (Entry<String, String> entry : load_Map.entrySet()) {
         if (init_Str(entry.getKey()).equals(canonical) || init_Str(entry.getValue()).equals(canonical)) {
            namespaces.add(entry.getKey());
         }
      }

      String fromName = close(pluginName);
      if (!fromName.isBlank() && !init_Set.contains(fromName)) {
         namespaces.add(fromName);
      }

      return List.copyOf(namespaces);
   }

   private static String flush(String value) {
      String text = value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
      StringBuilder out = new StringBuilder(text.length());

      for (int i = 0; i < text.length(); i++) {
         char ch = text.charAt(i);
         if (Character.isLetterOrDigit(ch)) {
            out.append(ch);
         }
      }

      return out.toString();
   }

   private static String close(String pluginName) {
      if (pluginName == null) {
         return "";
      }

      String clean = pluginName.trim().toLowerCase(Locale.ROOT);
      if (clean.isBlank()) {
         return "";
      }

      int colon = clean.indexOf(58);
      if (colon > 0) {
         clean = clean.substring(0, colon);
      }

      clean = clean.replace('+', 'p');
      StringBuilder sb = new StringBuilder(clean.length());
      boolean lastWasSeparator = false;

      for (int i = 0; i < clean.length(); i++) {
         char ch = clean.charAt(i);
         if (Character.isLetterOrDigit(ch)) {
            sb.append(ch);
            lastWasSeparator = false;
         } else if ((ch == '_' || ch == '-' || ch == '.' || Character.isWhitespace(ch)) && !lastWasSeparator && !sb.isEmpty()) {
            sb.append('_');
            lastWasSeparator = true;
         }
      }

      while (!sb.isEmpty() && sb.charAt(sb.length() - 1) == '_') {
         sb.deleteCharAt(sb.length() - 1);
      }

      return sb.toString();
   }

   private record Inner_BufferHelper(String init, String load) {
      Inner_BufferHelper {
         init = init == null ? "" : init.trim();
         load = PluginSignatureCatalog.init_Str(load != null && !load.isBlank() ? load : init);
      }
   }

   public record Inner_CachePool(String init_Str, String load, List<String> init_List, int init_I, String read) {
   }

   private static final class Inner_ConfigStore {
      String init_Str = "";
      long init_J;
      final Set<String> init_Set = new LinkedHashSet<>();
      final Set<String> load = new LinkedHashSet<>();
      final Set<String> read = new LinkedHashSet<>();
      final Map<String, PluginSignatureCatalog.Inner_DataUtil> init_Map = new LinkedHashMap<>();

      boolean init(String channel, String direction) {
         return this.init(this.init_Set, channel, "live", direction);
      }

      boolean load(String channel, String direction) {
         return this.init(this.load, channel, "registered", direction);
      }

      boolean read(String channel, String direction) {
         return this.init(this.read, channel, "embedded", direction);
      }

      Set<String> init_Set() {
         LinkedHashSet<String> out = new LinkedHashSet<>();
         out.addAll(this.init_Set);
         out.addAll(this.load);
         out.addAll(this.read);
         return Collections.unmodifiableSet(out);
      }

      List<PluginSignatureCatalog.Inner_DataUtil> init_List() {
         return List.copyOf(this.init_Map.values());
      }

      private boolean init(Set<String> set, String channel, String source, String direction) {
         String normalized = PluginSignatureCatalog.write(channel);
         if (normalized.isBlank()) {
            return false;
         }

         if (this.init_Set.size() + this.load.size() + this.read.size() >= 512
            && !this.init_Set.contains(normalized)
            && !this.load.contains(normalized)
            && !this.read.contains(normalized)) {
            return false;
         }

         PluginSignatureCatalog.Inner_DataUtil stats = this.init_Map.computeIfAbsent(normalized, PluginSignatureCatalog.Inner_DataUtil::new);
         int beforeScore = stats.init_I();
         stats.init(source, direction);
         return set.add(normalized) || stats.init_I() != beforeScore;
      }
   }

   private static final class Inner_DataUtil {
      final String init_Str;
      int init_I;
      int load;
      int read;
      final Set<String> init_Set = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

      Inner_DataUtil(String channel) {
         this.init_Str = PluginSignatureCatalog.write(channel);
      }

      void init(String source, String direction) {
         switch (source) {
            case "registered":
               this.load++;
               break;
            case "embedded":
               this.read++;
               break;
            default:
               this.init_I++;
         }

         if (direction != null && !direction.isBlank()) {
            this.init_Set.add(direction.trim().toUpperCase(Locale.ROOT));
         }
      }

      int init_I() {
         return this.init_I * 4 + this.load * 3 + this.read;
      }

      int load_I() {
         return this.init_I > 0 ? 4 : (this.load > 0 ? 3 : (this.read > 0 ? 1 : 0));
      }

      String init_Str() {
         return this.init_I > 0 ? "live" : (this.load > 0 ? "registered" : (this.read > 0 ? "embedded" : ""));
      }

      boolean init_Z() {
         return this.init_Set.contains("S2C");
      }

      String load_Str() {
         StringBuilder sb = new StringBuilder(this.init_Str);
         String source = this.init_Str();
         if (!source.isBlank()) {
            sb.append(" [").append(source);
         }

         if (!this.init_Set.isEmpty()) {
            sb.append(" ").append(String.join("/", this.init_Set));
         }

         int count = this.init_I + this.load + this.read;
         if (count > 1) {
            sb.append(" x").append(count);
         }

         if (!source.isBlank()) {
            sb.append(']');
         }

         return sb.toString();
      }
   }

   private static final class Inner_StreamUtil {
      final String init_Str;
      String load;
      int init_I;
      String read = "";
      final Set<String> init_Set = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

      Inner_StreamUtil(String displayName, String key) {
         this.load = displayName == null ? "" : displayName;
         this.init_Str = key == null ? "" : key;
      }

      void init(PluginSignatureCatalog.Inner_DataUtil stats) {
         if (stats != null && stats.init_Str != null && !stats.init_Str.isBlank()) {
            this.init_I = this.init_I + stats.init_I();
            if (this.read.isBlank() || stats.load_I() > this.init(this.read)) {
               this.read = stats.init_Str();
            }

            this.init_Set.add(stats.load_Str());
         }
      }

      PluginSignatureCatalog.Inner_CachePool init() {
         return new PluginSignatureCatalog.Inner_CachePool(this.load, this.init_Str, List.copyOf(this.init_Set), this.init_I, this.read);
      }

      private int init(String source) {
         byte var10000;
         switch (source == null ? "" : source) {
            case "live":
               var10000 = 4;
               break;
            case "registered":
               var10000 = 3;
               break;
            case "embedded":
               var10000 = 1;
               break;
            default:
               var10000 = 0;
         }

         return var10000;
      }
   }
}
