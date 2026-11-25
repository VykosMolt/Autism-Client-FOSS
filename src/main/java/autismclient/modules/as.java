package autismclient.modules;

import autismclient.api.module.BoolSetting;
import autismclient.api.module.StringListSetting;
import autismclient.api.module.StringSetting;
import autismclient.mixin.accessor.AutismChatComponentAccessor;
import com.mojang.authlib.GameProfile;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundDisguisedChatPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerChatPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundResetScorePacket;
import net.minecraft.network.protocol.game.ClientboundSetDisplayObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.network.protocol.game.ClientboundSetScorePacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.network.protocol.game.ClientboundTabListPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket.Entry;
import net.minecraft.world.entity.player.Player;

public final class as extends Module {
   private static final Random init_Random = new Random();
   private static final Map<String, String> init_Map = new ConcurrentHashMap<>();
   private static final Set<String> init_Set = ConcurrentHashMap.newKeySet();
   private static final List<String> init_List = init_List_14();
   private static final Pattern init_Pattern = Pattern.compile("(?<![A-Za-z0-9_])([A-Za-z0-9_]{3,16})(?![A-Za-z0-9_])");
   private static final int init_I = 2048;
   private static final int load_I = 2048;
   private static final long init_J = 250L;
   private static final Object init_Obj = new Object();
   private static final Object load_Obj = new Object();
   private static final LinkedHashMap<String, String> init_LinkedHashMap = new LinkedHashMap<>(128, 0.75F, true);
   private static final LinkedHashMap<String, String> load_LinkedHashMap = new LinkedHashMap<>(128, 0.75F, true);
   private static final AtomicLong init_AtomicLong = new AtomicLong();
   private static volatile as.Inner_BufferHelper init_as_BufferHelper = as.Inner_BufferHelper.init_as_BufferHelper();
   private static volatile long snapshotBucket = Long.MIN_VALUE;
   private static volatile boolean init_Z;
   private static volatile boolean load_Z;
   private static volatile boolean read_Z;
   private static volatile boolean write;
   private int read_I;

   public as() {
      super("name-censor", "NameCensor", ae.write, "Replaces player names with aliases.");
      this.add(new BoolSetting("censor-self", "Censor Self", true).description("Hide your own name."));
      this.add(new StringListSetting("names", "Names", "").description("Extra names to hide.").playerNameList());
      this.add(new StringSetting("self-alias", "Self Alias", "").description("Your alias"));
      this.add(new StringListSetting("custom-aliases", "Custom Aliases", "").description("Player=alias, e.g. Notch=Steve.").playerNameList());
      this.add(new BoolSetting("censor-everyone", "Censor Everyone", false).description("Hide known player names."));
      this.add(new BoolSetting("censor-everything", "Censor Everything", false).description("Aggressively hide username-shaped text."));
      this.add(new BoolSetting("hide-skins-all", "Hide All Skins", false).description("Default skin for everyone."));
      this.add(
         new BoolSetting("hide-skin-self", "Hide My Skin", false)
            .description("Your default skin")
            .visibleWhen(() -> !Boolean.parseBoolean(this.value("hide-skins-all")))
      );
   }

   @Override
   public void onEnable() {
      this.load_V_22();
      this.read_I = init_I_22();
      read_V_16();
      update();
      sync();
   }

   @Override
   public void onDisable() {
      this.load_V_22();
      this.read_I = 0;
      write_V_16();
      update();
      sync();
   }

   @Override
   public void onGameJoin() {
      write_V_16();
      read_V_16();
   }

   @Override
   public void onGameLeft() {
      write_V_16();
   }

   @Override
   public boolean onPacketReceive(Packet<?> packet) {
      init(packet);
      return false;
   }

   @Override
   public void tick() {
      int currentSkinState = init_I_22();
      if (currentSkinState != this.read_I) {
         this.read_I = currentSkinState;
         sync();
      }
   }

   @Override
   protected void onOptionValueChanged(String optionId) {
      int previousSkinState = init_I_22();
      this.load_V_22();
      init_as_BufferHelper = as.Inner_BufferHelper.init_as_BufferHelper();
      init_AtomicLong.incrementAndGet();
      if (this.isEnabled()) {
         update();
         if (init_I_22() != previousSkinState) {
            sync();
         }
      }
   }

   @Override
   protected void onSettingsReset() {
      this.load_V_22();
      init_as_BufferHelper = as.Inner_BufferHelper.init_as_BufferHelper();
      init_AtomicLong.incrementAndGet();
      if (this.isEnabled()) {
         update();
         sync();
      }
   }

   public static void init_V_29() {
      if (init_Module() instanceof as censor) {
         censor.load_V_22();
      } else {
         init_Z = false;
         load_Z = false;
         read_Z = false;
         write = false;
      }
   }

   private void load_V_22() {
      boolean enabled = this.isEnabled();
      init_Z = enabled;
      load_Z = enabled && this.bool("hide-skins-all");
      read_Z = enabled && this.bool("hide-skin-self");
      write = enabled && this.bool("censor-everything");
   }

   private static int init_I_22() {
      return (load_Z ? 2 : 0) | (read_Z ? 1 : 0);
   }

   public static Component init_Component(Component component) {
      return init_Component_Component_Z(component, false);
   }

   public static Component load(Component component) {
      return init_Component_Component_Z(component, true);
   }

   private static Component init_Component_Component_Z(Component component, boolean learn) {
      if (init_Z_33() && component != null) {
         boolean learnActive = learn && write;
         as.Inner_BufferHelper targets = init_as_BufferHelper();
         if (!learnActive && targets.init_List().isEmpty()) {
            return component;
         }

         List<as.Inner_ConfigStore> parts = new ArrayList<>();
         StringBuilder raw = new StringBuilder();

         for (Component part : component.toFlatList()) {
            String text = part.getString();
            if (!text.isEmpty()) {
               int start = raw.length();
               raw.append(text);
               parts.add(new as.Inner_ConfigStore(text, part.getStyle(), start, raw.length()));
            }
         }

         if (learnActive) {
            init_V(raw.toString());
            targets = init_as_BufferHelper();
         }

         if (targets.init_List().isEmpty()) {
            return component;
         }

         as.Inner_StreamUtil normalized = init_as_StreamUtil(raw.toString());
         List<as.Inner_CachePool> replacements = init_List_as_StreamUtil_as_BufferHelper(normalized, targets);
         if (replacements.isEmpty()) {
            return component;
         }

         MutableComponent out = Component.empty();
         int cursor = 0;

         for (as.Inner_CachePool replacement : replacements) {
            init(out, parts, cursor, replacement.init_I());
            out.append(Component.literal(replacement.init_Str()).withStyle(init_Style_List_I(parts, replacement.init_I())));
            cursor = replacement.load();
         }

         init(out, parts, cursor, raw.length());
         return out;
      } else {
         return component;
      }
   }

   public static String init_Str_2(String text) {
      return init_Str_Str_Z(text, false);
   }

   public static String load_Str(String text) {
      return init_Str_Str_Z(text, true);
   }

   private static String init_Str_Str_Z(String text, boolean learn) {
      if (init_Z_33() && text != null && !text.isEmpty()) {
         if (learn && write) {
            init_V(text);
         }

         return init_Str_Str_as_BufferHelper(text, init_as_BufferHelper());
      } else {
         return text;
      }
   }

   private static boolean load_Z_14() {
      return load_Z;
   }

   private static boolean read_Z_15() {
      return read_Z;
   }

   public static boolean init(GameProfile profile) {

      if (load_Z) {
         return true;
      }

      if (read_Z && profile != null) {
         Minecraft mc = Minecraft.getInstance();
         if (mc != null && mc.getUser() != null) {
            if (profile.id() != null && profile.id().equals(mc.getUser().getProfileId())) {
               return true;
            }

            String localName = mc.getUser().getName();
            return localName != null && localName.equalsIgnoreCase(profile.name());
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private static String init_Str_Str_as_BufferHelper(String text, as.Inner_BufferHelper targets) {
      if (!targets.init_List().isEmpty() && text != null && !text.isEmpty()) {
         as.Inner_StreamUtil normalized = init_as_StreamUtil(text);
         List<as.Inner_CachePool> replacements = init_List_as_StreamUtil_as_BufferHelper(normalized, targets);
         if (replacements.isEmpty()) {
            return text;
         }

         StringBuilder out = new StringBuilder();
         int cursor = 0;

         for (as.Inner_CachePool replacement : replacements) {
            out.append(text, cursor, replacement.init_I());
            out.append(replacement.init_Str());
            cursor = replacement.load();
         }

         out.append(text, cursor, text.length());
         return out.toString();
      } else {
         return text;
      }
   }

   private static List<as.Inner_CachePool> init_List_as_StreamUtil_as_BufferHelper(as.Inner_StreamUtil normalized, as.Inner_BufferHelper targets) {
      if (normalized.init_Str().isEmpty()) {
         return List.of();
      }

      List<as.Inner_CachePool> replacements = new ArrayList<>();
      boolean[] used = new boolean[normalized.init_I()];

      for (as.Inner_DataUtil target : targets.load()) {
         Matcher matcher = target.init_Pattern().matcher(normalized.init_Str());

         while (matcher.find()) {
            int rawStart = normalized.init(matcher.start());
            int rawEnd = normalized.init(matcher.end() - 1) + 1;
            if (rawStart >= 0 && rawEnd > rawStart && !init(used, rawStart, rawEnd)) {
               for (int i = rawStart; i < rawEnd; i++) {
                  used[i] = true;
               }

               replacements.add(new as.Inner_CachePool(rawStart, rawEnd, read_Str(target.init_Str())));
            }
         }
      }

      replacements.sort(Comparator.comparingInt(as.Inner_CachePool::init_I));
      return replacements;
   }

   private static boolean init(boolean[] used, int start, int end) {
      for (int i = Math.max(0, start); i < Math.min(used.length, end); i++) {
         if (used[i]) {
            return true;
         }
      }

      return false;
   }

   private static as.Inner_StreamUtil init_as_StreamUtil(String raw) {
      StringBuilder text = new StringBuilder(raw.length());
      List<Integer> rawIndexes = new ArrayList<>(raw.length());

      for (int i = 0; i < raw.length(); i++) {
         char c = raw.charAt(i);
         if (c == 167 && i + 1 < raw.length()) {
            i++;
         } else {
            rawIndexes.add(i);
            text.append(c);
         }
      }

      return new as.Inner_StreamUtil(text.toString(), rawIndexes, raw.length());
   }

   private static void init(MutableComponent out, List<as.Inner_ConfigStore> parts, int start, int end) {
      if (end > start) {
         for (as.Inner_ConfigStore part : parts) {
            int overlapStart = Math.max(start, part.init_I());
            int overlapEnd = Math.min(end, part.load());
            if (overlapEnd > overlapStart) {
               int localStart = overlapStart - part.init_I();
               int localEnd = overlapEnd - part.init_I();
               out.append(Component.literal(part.init_Str().substring(localStart, localEnd)).withStyle(part.init_Style()));
            }
         }
      }
   }

   private static Style init_Style_List_I(List<as.Inner_ConfigStore> parts, int rawIndex) {
      for (as.Inner_ConfigStore part : parts) {
         if (rawIndex >= part.init_I() && rawIndex < part.load()) {
            return part.init_Style();
         }
      }

      return Style.EMPTY;
   }

   private static String read_Str(String name) {
      Module module = init_Module();
      if (module != null) {
         String custom = init_Str_Module_Str(module, name);
         if (!custom.isBlank()) {
            return custom;
         }
      }

      return sync(name);
   }

   private static String init_Str_Module_Str(Module module, String name) {
      Minecraft mc = Minecraft.getInstance();
      if (mc != null && mc.getUser() != null && Boolean.parseBoolean(module.value("censor-self")) && mc.getUser().getName().equalsIgnoreCase(name)) {
         String selfAlias = module.value("self-alias").trim();
         if (!selfAlias.isEmpty()) {
            return selfAlias;
         }
      }

      for (String entry : module.list("custom-aliases")) {
         int split = init_I_2(entry);
         if (split > 0) {
            String rawName = entry.substring(0, split).trim();
            String alias = entry.substring(split + 1).trim();
            if (!rawName.isEmpty() && !alias.isEmpty() && rawName.equalsIgnoreCase(name)) {
               return alias;
            }
         }
      }

      return "";
   }

   private static int init_I_2(String entry) {
      int split = entry.indexOf(61);
      return split >= 0 ? split : entry.indexOf(58);
   }

   private static as.Inner_BufferHelper init_as_BufferHelper() {
      Module module = init_Module();
      if (module != null && module.isEnabled()) {
         long revision = init_AtomicLong.get();
         long refreshBucket = System.currentTimeMillis() / 250L;
         as.Inner_BufferHelper cached = init_as_BufferHelper;
         // Hot path (every getHoverName): reuse this bucket's snapshot without building the settings key.
         if (cached.init_J() == revision && snapshotBucket == refreshBucket) {
            return cached;
         }

         String settingsKey = init_Str_Module_J(module, refreshBucket);
         if (cached.init_J() == revision && cached.init_Str().equals(settingsKey)) {
            return cached;
         }

         synchronized (load_Obj) {
            cached = init_as_BufferHelper;
            if (cached.init_J() == revision && cached.init_Str().equals(settingsKey)) {
               return cached;
            }

            Set<String> names = new LinkedHashSet<>();
            Minecraft mc = Minecraft.getInstance();
            if (Boolean.parseBoolean(module.value("censor-self")) && mc != null && mc.getUser() != null) {
               init_V_Set_Str(names, mc.getUser().getName());
            }

            for (String entry : module.list("names")) {
               init_V_Set_Str(names, entry);
            }

            for (String entry : module.list("custom-aliases")) {
               int split = init_I_2(entry);
               if (split > 0) {
                  init_V_Set_Str(names, entry.substring(0, split));
               } else if (split < 0) {
                  init_V_Set_Str(names, entry);
               }
            }

            boolean censorEveryone = Boolean.parseBoolean(module.value("censor-everyone"));
            boolean censorEverything = Boolean.parseBoolean(module.value("censor-everything"));
            if (censorEveryone || censorEverything) {
               init(names);
               init_V_Set_Minecraft(names, mc);
            }

            if (censorEverything) {
               load(names);
            }

            List<String> sorted = new ArrayList<>(names);
            sorted.sort(Comparator.comparingInt(String::length).reversed());
            List<as.Inner_DataUtil> patterns = sorted.stream()
               .map(name -> new as.Inner_DataUtil(name, Pattern.compile("(?i)(?<![A-Za-z0-9_])" + Pattern.quote(name) + "(?![A-Za-z0-9_])")))
               .toList();
            as.Inner_BufferHelper snapshot = new as.Inner_BufferHelper(revision, settingsKey, List.copyOf(sorted), List.copyOf(patterns));
            init_as_BufferHelper = snapshot;
            snapshotBucket = refreshBucket;
            return snapshot;
         }
      } else {
         return as.Inner_BufferHelper.init_as_BufferHelper();
      }
   }

   private static String init_Str_Module_J(Module module, long refreshBucket) {
      return refreshBucket
         + "|"
         + module.value("censor-self")
         + "|"
         + module.value("censor-everyone")
         + "|"
         + module.value("censor-everything")
         + "|"
         + module.value("self-alias")
         + "|"
         + String.join("\u001f", module.list("names"))
         + "|"
         + String.join("\u001f", module.list("custom-aliases"));
   }

   private static void init_V_Set_Str(Set<String> names, String name) {
      if (name != null) {
         String trimmed = name.trim();
         if (!trimmed.isEmpty()) {
            names.add(trimmed);
         }
      }
   }

   private static void init(Set<String> names) {
      synchronized (init_Obj) {
         names.addAll(init_LinkedHashMap.values());
      }
   }

   private static void load(Set<String> names) {
      synchronized (init_Obj) {
         names.addAll(load_LinkedHashMap.values());
      }
   }

   private static void init_V_Set_Minecraft(Set<String> names, Minecraft mc) {
      if (mc != null) {
         if (mc.getConnection() != null) {
            for (PlayerInfo info : mc.getConnection().getOnlinePlayers()) {
               if (info != null && info.getProfile() != null) {
                  load(names, info.getProfile().name());
               }
            }
         }

         if (mc.level != null) {
            for (Player player : mc.level.players()) {
               if (player != null && player.getGameProfile() != null) {
                  load(names, player.getGameProfile().name());
               }
            }
         }
      }
   }

   private static void load(Set<String> names, String name) {
      String cleaned = write(name);
      if (cleaned != null) {
         names.add(cleaned);
      }
   }

   private static void read_V_16() {
      Module module = init_Module();
      if (module != null && module.isEnabled() && load(module)) {
         Minecraft mc = Minecraft.getInstance();
         if (mc != null) {
            if (mc.getConnection() != null) {
               for (PlayerInfo info : mc.getConnection().getOnlinePlayers()) {
                  if (info != null && info.getProfile() != null) {
                     load_V(info.getProfile().name());
                  }
               }
            }

            if (mc.level != null) {
               for (Player player : mc.level.players()) {
                  if (player != null && player.getGameProfile() != null) {
                     load_V(player.getGameProfile().name());
                  }
               }
            }
         }
      }
   }

   private static void init(Packet<?> packet) {
      Module module = init_Module();
      if (module != null && module.isEnabled() && packet != null) {
         boolean reliable = load(module);
         boolean aggressive = init(module);
         if (reliable || aggressive) {
            if (packet instanceof ClientboundPlayerInfoUpdatePacket infoPacket) {
               for (Entry entry : infoPacket.entries()) {
                  if (entry != null) {
                     GameProfile profile = entry.profile();
                     if (profile != null && reliable) {
                        load_V(profile.name());
                     }

                     if (aggressive) {
                        init_V(entry.displayName());
                     }
                  }
               }
            } else if (packet instanceof ClientboundSetPlayerTeamPacket teamPacket) {
               if (reliable) {
                  for (String player : teamPacket.getPlayers()) {
                     load_V(player);
                  }
               }

               if (aggressive) {
                  teamPacket.getParameters().ifPresent(parameters -> {
                     init_V(parameters.displayName());
                     init_V(parameters.playerPrefix());
                     init_V(parameters.playerSuffix());
                  });
               }
            } else if (packet instanceof ClientboundSetObjectivePacket objectivePacket) {
               if (aggressive) {
                  read_V(objectivePacket.getObjectiveName());
                  init_V(objectivePacket.getDisplayName());
               }
            } else if (packet instanceof ClientboundSetScorePacket scorePacket) {
               if (aggressive) {
                  read_V(scorePacket.owner());
                  read_V(scorePacket.objectiveName());
                  scorePacket.display().ifPresent(as::init_V);
               }
            } else if (packet instanceof ClientboundResetScorePacket resetScorePacket) {
               if (aggressive) {
                  read_V(resetScorePacket.owner());
                  read_V(resetScorePacket.objectiveName());
               }
            } else if (packet instanceof ClientboundSetDisplayObjectivePacket displayObjectivePacket) {
               if (aggressive) {
                  read_V(displayObjectivePacket.getObjectiveName());
               }
            } else if (packet instanceof ClientboundTabListPacket tabListPacket) {
               if (aggressive) {
                  init_V(tabListPacket.header());
                  init_V(tabListPacket.footer());
               }
            } else if (packet instanceof ClientboundSystemChatPacket chatPacket) {
               if (aggressive) {
                  init_V(chatPacket.content());
               }
            } else if (packet instanceof ClientboundDisguisedChatPacket disguisedChatPacket) {
               if (aggressive) {
                  init_V(disguisedChatPacket.message());
                  init_V(disguisedChatPacket.chatType().name());
                  disguisedChatPacket.chatType().targetName().ifPresent(as::init_V);
               }
            } else if (packet instanceof ClientboundPlayerChatPacket playerChatPacket && aggressive) {
               init_V(playerChatPacket.body().content());
               init_V(playerChatPacket.unsignedContent().orElse(Component.empty()));
               init_V(playerChatPacket.chatType().name());
               playerChatPacket.chatType().targetName().ifPresent(as::init_V);
            }
         }
      }
   }

   private static void init_V(Component component) {
      if (component != null) {
         init_V(component.getString());
      }
   }

   private static void init_V(String text) {
      if (write_Z_6() && text != null && !text.isEmpty()) {
         as.Inner_StreamUtil normalized = init_as_StreamUtil(text);
         Matcher matcher = init_Pattern.matcher(normalized.init_Str());

         while (matcher.find()) {
            read_V(matcher.group(1));
         }
      }
   }

   private static boolean write_Z_6() {
      return write;
   }

   private static boolean init(Module module) {
      return Boolean.parseBoolean(module.value("censor-everything"));
   }

   private static boolean load(Module module) {
      return Boolean.parseBoolean(module.value("censor-everyone")) || Boolean.parseBoolean(module.value("censor-everything"));
   }

   private static void load_V(String name) {
      String cleaned = write(name);
      init(init_LinkedHashMap, 2048, cleaned);
   }

   private static void read_V(String name) {
      String cleaned = update(name);
      init(load_LinkedHashMap, 2048, cleaned);
   }

   private static void init(LinkedHashMap<String, String> names, int maxSize, String cleaned) {
      if (cleaned != null) {
         String key = cleaned.toLowerCase(Locale.ROOT);
         synchronized (init_Obj) {
            if (names.containsKey(key)) {
               names.get(key);
               return;
            }

            while (names.size() >= maxSize) {
               String eldest = names.keySet().iterator().next();
               names.remove(eldest);
            }

            names.put(key, cleaned);
         }

         init_AtomicLong.incrementAndGet();
      }
   }

   private static String write(String name) {
      String cleaned = update(name);
      if (cleaned == null) {
         return null;
      }

      String lower = cleaned.toLowerCase(Locale.ROOT);
      return lower.matches("slot_\\d+") ? null : cleaned;
   }

   private static String update(String name) {
      if (name == null) {
         return null;
      } else {
         String trimmed = name.trim();
         if (trimmed.isEmpty()) {
            return null;
         } else {
            return init_Pattern.matcher(trimmed).matches() ? trimmed : null;
         }
      }
   }

   private static void write_V_16() {
      boolean changed;
      synchronized (init_Obj) {
         changed = !init_LinkedHashMap.isEmpty() || !load_LinkedHashMap.isEmpty();
         init_LinkedHashMap.clear();
         load_LinkedHashMap.clear();
      }

      if (!init_Map.isEmpty() || !init_Set.isEmpty()) {
         init_Map.clear();
         init_Set.clear();
         changed = true;
      }

      if (changed) {
         init_AtomicLong.incrementAndGet();
      }

      init_as_BufferHelper = as.Inner_BufferHelper.init_as_BufferHelper();
   }

   private static String sync(String name) {
      return init_Map.computeIfAbsent(name.toLowerCase(Locale.ROOT), ignored -> init_Str_10());
   }

   private static String init_Str_10() {
      synchronized (init_Set) {
         if (init_Set.size() >= init_List.size()) {
            return init_List.get(init_Random.nextInt(init_List.size()));
         }

         String alias;
         do {
            alias = init_List.get(init_Random.nextInt(init_List.size()));
         } while (!init_Set.add(alias));

         return alias;
      }
   }

   public static boolean init_Z_33() {
      return init_Z;
   }

   private static Module init_Module() {
      return al.init_Module_Str("name-censor");
   }

   private static void update() {
      Minecraft mc = Minecraft.getInstance();
      if (mc != null && mc.gui != null && mc.gui.hud.getChat() != null) {
         try {
            ((AutismChatComponentAccessor)mc.gui.hud.getChat()).autism$refreshTrimmedMessages();
         } catch (Throwable var2) {
         }
      }
   }

   private static void sync() {
      Minecraft mc = Minecraft.getInstance();
      if (mc != null) {
         am.init_V();
      }
   }

   private static List<String> init_List_14() {
      String[] first = new String[]{"自闭", "包包", "数据", "方块", "混凝", "粗筑", "硬核", "延迟", "红石", "字节", "队列", "握手", "同步", "像素", "栈帧", "缓存", "抽象", "水泥", "裂纹", "灰墙"};
      String[] second = new String[]{"大师", "狂热", "小子", "工头", "指挥", "幽默", "爆笑", "飞包", "硬墙", "乱流", "神经", "电波", "铁板", "奇观", "回声", "巨构"};
      List<String> words = new ArrayList<>(first.length * second.length);

      for (String a : first) {
         for (String b : second) {
            String word = a + b;
            if (word.length() <= 6) {
               words.add(word);
            }
         }
      }

      return List.copyOf(words);
   }

   private record Inner_BufferHelper(long init_J, String init_Str, List<String> init_List, List<as.Inner_DataUtil> load) {
      private static as.Inner_BufferHelper init_as_BufferHelper() {
         return new as.Inner_BufferHelper(-1L, "", List.of(), List.of());
      }
   }

   private record Inner_CachePool(int init_I, int load, String init_Str) {
   }

   private record Inner_ConfigStore(String init_Str, Style init_Style, int init_I, int load) {
   }

   private record Inner_DataUtil(String init_Str, Pattern init_Pattern) {
   }

   private record Inner_StreamUtil(String init_Str, List<Integer> init_List, int init_I) {
      int init(int normalizedIndex) {
         return normalizedIndex >= 0 && normalizedIndex < this.init_List.size() ? this.init_List.get(normalizedIndex) : -1;
      }
   }
}
