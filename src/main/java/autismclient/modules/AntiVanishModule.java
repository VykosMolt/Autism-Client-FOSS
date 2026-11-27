package autismclient.modules;

import autismclient.api.module.BoolSetting;
import autismclient.api.module.IntSetting;
import autismclient.api.module.StringListSetting;
import autismclient.util.AutismClientMessaging;
import autismclient.util.TabPlayerListParser;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.tree.CommandNode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket;
import net.minecraft.network.protocol.game.ClientboundCommandSuggestionsPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundSectionBlocksUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;
import net.minecraft.network.protocol.game.ServerboundCommandSuggestionPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket.Action;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public final class AntiVanishModule extends Module {
   private static final long init_J = 20000L;
   private static final long load_J = 1500L;
   private static final AtomicInteger init_AtomicInteger = new AtomicInteger(-1000);
   private final autismclient.util.AntiVanishRosterTracker init_CachePool = new autismclient.util.AntiVanishRosterTracker();
   private final autismclient.util.PlayerQuitEventTracker init_StreamUtil = new autismclient.util.PlayerQuitEventTracker();
   private final Map<UUID, AntiVanishModule.Inner_DataUtil> init_Map = new LinkedHashMap<>();
   private final VanishActivityDetector init_UiHelper = new VanishActivityDetector();
   private volatile long read_J;
   private long write_J = -1L;
   private final Map<UUID, AntiVanishModule.Inner_DataUtil> load_Map = new LinkedHashMap<>();
   private final Map<UUID, GameType> read_Map = new HashMap<>();
   private final Map<Integer, AntiVanishModule.Inner_CachePool> write_Map = new HashMap<>();
   private final Map<String, String> update_Map = new HashMap<>();
   private ClientPacketListener init_ClientPacketListen;
   private Object init_Obj;
   private long update_J = -1L;
   private long sync;
   private UUID init_UUID;
   private AntiVanishModule.Inner_BufferHelper init_SyncManager_Buffer;
   private long flush;
   private long close;
   private long open;
   private final Set<UUID> init_Set = new HashSet<>();
   private boolean init_Z;
   private boolean load_Z;
   private boolean read_Z;
   private String init_Str = "";

   public AntiVanishModule() {
      super("anti-vanish", "AntiVanish", ae.write, "Discovers online players outside TAB without needing earlier sightings.");
      this.add(
         new BoolSetting("vanish-tracker", "Vanish Tracker", true)
            .group("Detection")
            .description("Watch possible vanish on unexplained TAB departures and discover unseen online players.")
            .build()
      );
      this.add(
         new BoolSetting("completion-probe", "Tab Probe", true)
            .group("Detection")
            .description("Discover unseen online players with read-only server roster checks.")
            .build()
      );
      this.add(new BoolSetting("gamemode-alerts", "Gamemode Alerts", false).group("Detection").description("Notify actual gamemode switches.").build());
      this.add(new BoolSetting("player-filter", "Player Filter", false).group("Detection").description("Watch only the selected player names.").build());
      this.add(new StringListSetting("players", "Players", "").group("Detection").playerNameList().visibleWhen(() -> this.bool("player-filter")).build());
      this.add(
         new BoolSetting("environmental", "Player Activity", true)
            .group("Sensors")
            .description("Confirm hidden identities with server-sent player actions. World effects never identify a player.")
            .build()
      );
      this.add(
         new BoolSetting("world-activity", "World Activity", true)
            .group("Sensors")
            .description("Show repeated unexplained block activity separately; it is not proof of vanish.")
            .build()
      );
      this.add(
         new IntSetting("range", "Detection Range", 64, 8, 160, 8)
            .group("Sensors")
            .description("Watch player actions within this distance.")
            .visibleWhen(() -> false)
            .build()
      );
      this.add(
         new BoolSetting("critical-alert", "Critical Alert", true)
            .group("Alerts")
            .description("Notify corroborated hidden-player activity.")
            .visibleWhen(() -> false)
            .build()
      );
      this.add(
         new BoolSetting("alert-sound", "Warning Sound", true)
            .group("Alerts")
            .description("Play a warning for corroborated activity.")
            .visibleWhen(() -> false)
            .build()
      );
      this.add(new BoolSetting("hud-list", "Vanish HUD", true).group("Alerts").description("Show hidden-player detections and anonymous activity.").build());
      this.add(new BoolSetting("chat-alerts", "Chat Alerts", true).group("Alerts").description("Log detections and anonymous activity.").build());
   }

   private static long init_J_5() {
      return System.nanoTime() / 1000000L;
   }

   @Override
   public String info() {
      return this.load_Map.isEmpty()
         ? (this.write() && !this.init_UiHelper.init_List_J(init_J_5()).isEmpty() ? "Activity" : "")
         : Integer.toString(this.load_Map.size());
   }

   @Override
   public void onEnable() {
      this.load_V_14();
      if (this.read()) {
         this.init_V_J(init_J_5());
      }
   }

   @Override
   public void onDisable() {
      this.load_V_14();
   }

   @Override
   public void onGameJoin() {
      this.load_V_14();
   }

   @Override
   public void onGameLeft() {
      this.load_V_14();
   }

   private boolean read() {
      if (MC.player != null && MC.level != null && MC.getConnection() != null) {
         if (this.init_ClientPacketListen != MC.getConnection() || this.init_Obj != MC.level) {
            this.load_V_14();
            this.init_ClientPacketListen = MC.getConnection();
            this.init_Obj = MC.level;
            this.open = init_J_5();
            this.flush = this.open + 3000L;
         }

         return true;
      } else {
         return false;
      }
   }

   @Override
   public void tick() {
      if (!this.read()) {
         this.load_V_14();
      } else {
         long at = init_J_5();
         this.init_V_J(at);
         if (this.write()) {
            this.init_UiHelper.init_V_J(at);
            List<autismclient.util.UnexplainedBlockTracker.Inner_BufferHelper> readings = this.init_UiHelper.init_List_J(at);
            if (!readings.isEmpty() && (this.write_J < 0L || at - this.write_J >= 10000L) && this.bool("chat-alerts")) {
               this.write_J = at;
               AutismClientMessaging.sendPrefixed(
                  "Unexplained block activity: "
                     + init_Str_BlockPos(readings.getFirst().init_BlockPos())
                     + ". Possible hidden activity; no player identity established."
               );
            }
         } else {
            this.init_UiHelper.init();
         }

         if (!this.bool("vanish-tracker")) {
            this.init_CachePool.write();
            this.init_StreamUtil.init();
            this.load_Map.clear();
            this.init_Map.clear();
            this.write_Map.clear();
            this.init_SyncManager_Buffer = null;
            this.init_V_17();
         } else {
            if (!this.bool("environmental")) {
               this.init_CachePool.init_V();
            }

            if (this.bool("completion-probe")) {
               this.read_V_J(at);
            } else {
               this.init_SyncManager_Buffer = null;
               this.init_CachePool.load();
            }

            this.write_Map.values().removeIf(p -> at - p.init_J > 4000L);
            if (!this.bool("completion-probe")) {
               this.write_Map.clear();
            }

            if (this.bool("completion-probe")
               && this.init_CachePool.init_Z_J(at)
               && this.write_Map.isEmpty()
               && (this.update_J < 0L || at - this.update_J >= 1500L)) {
               this.load_V_J(at);
            }

            this.write_V_J(at);
         }
      }
   }

   public static void init_V_ClientPacketListen_Packet(ClientPacketListener handler, Packet<?> packet) {
      AntiVanishModule module = init_SyncManager();
      if (module != null && module.isEnabled() && MC.isSameThread() && handler == MC.getConnection() && module.read()) {
         module.init_V_Packet_J(packet, init_J_5());
      }
   }

   private boolean write() {
      return this.bool("vanish-tracker") && this.bool("world-activity") && !this.bool("player-filter");
   }

   public static void init_V_ClientPacketListen(ClientPacketListener handler, BlockPos pos, BlockState state) {
      AntiVanishModule module = init_SyncManager();
      if (module != null && module.isEnabled() && MC.isSameThread() && handler == MC.getConnection() && module.read() && module.write()) {
         long at = init_J_5();
         if (at - module.open >= 3000L) {
            module.init_UiHelper.init(pos, state, at);
         }
      }
   }

   public static void init_V_ClientPacketListen_ClientboundSection(ClientPacketListener handler, ClientboundSectionBlocksUpdatePacket packet) {
      AntiVanishModule module = init_SyncManager();
      if (module != null && module.isEnabled() && MC.isSameThread() && handler == MC.getConnection() && module.read() && module.write()) {
         long at = init_J_5();
         module.init_UiHelper.init(packet, at, at - module.open >= 3000L);
      }
   }

   private static String init_Str_BlockPos(BlockPos pos) {
      double dx = pos.getX() + 0.5 - MC.player.getX();
      double dz = pos.getZ() + 0.5 - MC.player.getZ();
      String[] directions = new String[]{"S", "SW", "W", "NW", "N", "NE", "E", "SE"};
      int direction = Math.floorMod((int)Math.round(Math.atan2(-dx, dz) / 0.7853981633974483), 8);
      return Math.round(Math.sqrt(dx * dx + dz * dz)) + "m " + directions[direction];
   }

   private void init_V_Packet_J(Packet<?> packet, long at) {
      if (!this.write() || !this.init_UiHelper.init_Z_Packet_J(packet, at)) {
         if (packet instanceof ClientboundPlayerInfoUpdatePacket info) {
            if (this.bool("gamemode-alerts") && info.actions().contains(Action.UPDATE_GAME_MODE) && !info.actions().contains(Action.ADD_PLAYER)) {
               for (net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket.Entry entry : info.entries()) {
                  GameType previous = this.read_Map.get(entry.profileId());
                  PlayerInfo player = this.init_ClientPacketListen.getPlayerInfo(entry.profileId());
                  if (previous != null && entry.gameMode() != null && previous != entry.gameMode() && player != null && this.init_Z_PlayerInfo(player)) {
                     AutismClientMessaging.sendPrefixed("§bGamemode: §f" + player.getProfile().name() + " §7-> " + entry.gameMode().getName());
                  }
               }
            }

            this.init_V_J(at);
            this.init_V_ClientboundPlayerI_J(info, at);
            this.write_V_J(at);
         } else if (packet instanceof ClientboundPlayerInfoRemovePacket) {
            this.init_V_J(at);
            this.write_V_J(at);
         } else if (packet instanceof ClientboundSystemChatPacket chat) {
            if (!chat.overlay()) {
               String text = chat.content() == null ? "" : chat.content().getString();
               this.init_CachePool.init(text, at);
               this.init_StreamUtil.init_V_Component_J(chat.content(), at);
            }

            this.write_V_J(at);
         } else if (this.bool("vanish-tracker")) {
            this.init_V_J(at);
            if (packet instanceof ClientboundCommandSuggestionsPacket reply) {
               AntiVanishModule.Inner_CachePool request = this.write_Map.remove(reply.id());
               if (request == null || !this.bool("completion-probe") || at - request.init_J > 4000L) {
                  return;
               }

               Suggestions suggestions = reply.toSuggestions();
               if (suggestions.getRange().getStart() != request.init_I
                  || suggestions.getRange().getEnd() != request.init_I
                  || suggestions.getList().size() > 512) {
                  return;
               }

               this.init_CachePool.load(suggestions.getList().stream().map(s -> s.getText()).toList(), request.init_J, at);
            } else if (packet instanceof ClientboundAnimatePacket animation) {
               if (animation.getAction() != 0 && animation.getAction() != 3) {
                  return;
               }

               this.init_V_I_2(animation.getId(), "swing", at);
            } else if (packet instanceof ClientboundBlockDestructionPacket dig) {
               if (dig.getProgress() < 0 || dig.getProgress() > 9 || MC.level.getBlockState(dig.getPos()).isAir()) {
                  return;
               }

               if (!(MC.level.getEntity(dig.getId()) instanceof Player player) || player.getEyePosition().distanceToSqr(Vec3.atCenterOf(dig.getPos())) > 42.25) {
                  return;
               }

               this.init_V_I_2(dig.getId(), "dig@" + dig.getPos().asLong(), at);
            }

            this.write_V_J(at);
         }
      }
   }

   private void init_V_I_2(int entityId, String action, long at) {
      if (this.bool("environmental")
         && MC.level.getEntity(entityId) instanceof Player player
         && player != MC.player
         && player.isAlive()
         && player.tickCount >= 5
         && !(player.distanceToSqr(MC.player) > (double)this.integer("range") * this.integer("range"))) {
         String name = player.getGameProfile().name();
         boolean npc = ArmorFilterMatcher.init_Z_UUID(player.getUUID()) || AntiBotTracker.isConfirmedBotUuid(player.getUUID());
         this.init_CachePool.init(new autismclient.util.AntiVanishRosterTracker.Inner_DataUtil(player.getUUID(), name, true, npc), action, at);
      }
   }

   private boolean init_Z_PlayerInfo(PlayerInfo info) {
      if (info != null && info.getProfile() != null) {
         GameProfile profile = info.getProfile();
         return this.init_Z_UUID_Str(profile.id(), profile.name());
      } else {
         return false;
      }
   }

   private boolean init_Z_UUID_Str(UUID id, String name) {
      return id != null
         && !id.equals(MC.player.getUUID())
         && TabPlayerListParser.load_Z(name)
         && !ArmorFilterMatcher.init_Z_UUID(id)
         && !AntiBotTracker.isConfirmedBotUuid(id)
         && this.init_Z_Str(name);
   }

   private boolean init_Z_Str(String name) {
      return !this.bool("player-filter") || this.list("players").stream().anyMatch(watched -> watched.equalsIgnoreCase(name));
   }

   private void init_V_J(long at) {
      List<autismclient.util.AntiVanishRosterTracker.Inner_ConfigStore> roster = new ArrayList<>();
      Collection<PlayerInfo> listed = this.init_ClientPacketListen.getListedOnlinePlayers();

      for (PlayerInfo info : this.init_ClientPacketListen.getOnlinePlayers()) {
         if (info != null && info.getProfile() != null) {
            GameProfile profile = info.getProfile();
            if (!this.init_Z) {
               this.init_Set.add(profile.id());
               if (this.init_Set.size() > 8192) {
                  this.init_Set.clear();
                  this.init_Z = true;
                  this.init_CachePool.read();
               }
            }

            roster.add(new autismclient.util.AntiVanishRosterTracker.Inner_ConfigStore(profile.id(), profile.name(), listed.contains(info), this.init_Z_PlayerInfo(info)));
         }
      }

      this.init_CachePool.init(roster, at);
      this.init_StreamUtil.init_V_Collection_J(roster, at);
      this.read_Map.clear();

      for (PlayerInfo info : this.init_ClientPacketListen.getOnlinePlayers()) {
         if (info != null && info.getProfile() != null) {
            this.read_Map.put(info.getProfile().id(), info.getGameMode());
         }
      }
   }

   private void init_V_ClientboundPlayerI_J(ClientboundPlayerInfoUpdatePacket packet, long at) {
      if (this.bool("vanish-tracker")
         && !this.init_Z
         && at - this.open >= 5000L
         && packet.actions().equals(EnumSet.of(Action.UPDATE_LATENCY))
         && packet.entries().size() <= 4096) {
         Set<UUID> all = new HashSet<>();
         Set<UUID> unknown = new HashSet<>();

         for (net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket.Entry entry : packet.entries()) {
            UUID id = entry.profileId();
            if (id == null || !all.add(id) || entry.latency() < 0 || entry.latency() > 120000) {
               return;
            }

            if (this.init_ClientPacketListen.getPlayerInfo(id) == null && !this.init_Set.contains(id) && !ArmorFilterMatcher.init_Z_UUID(id) && !AntiBotTracker.isConfirmedBotUuid(id)
               )
             {
               unknown.add(id);
            }
         }

         if (all.contains(MC.player.getUUID())) {
            for (PlayerInfo info : this.init_ClientPacketListen.getListedOnlinePlayers()) {
               if (!all.contains(info.getProfile().id())) {
                  return;
               }
            }

            this.init_CachePool.init(unknown, at);
         }
      }
   }

   private boolean init_Z_UUID_2(UUID id, String name, boolean unresolved) {
      return !unresolved
         ? this.init_Z_UUID_Str(id, name)
         : id != null && !id.equals(MC.player.getUUID()) && !ArmorFilterMatcher.init_Z_UUID(id) && !AntiBotTracker.isConfirmedBotUuid(id) && !this.bool("player-filter");
   }

   private void load_V_J(long at) {
      String command = "minecraft:msg ";
      int id = init_AtomicInteger.getAndUpdate(value -> value == -2147483648 ? -1000 : value - 1);
      this.write_Map.put(id, new AntiVanishModule.Inner_CachePool(at, command.length()));
      this.update_J = at;

      try {
         this.init_ClientPacketListen.send(new ServerboundCommandSuggestionPacket(id, command));
      } catch (RuntimeException ignored) {
         this.write_Map.remove(id);
      }
   }

   private void read_V_J(long at) {
      if (this.init_SyncManager_Buffer != null && at - this.init_SyncManager_Buffer.init_J > 4000L) {
         this.init_SyncManager_Buffer = null;
         this.load_Z = true;
         this.init_CachePool.load();
      }

      if (!this.load_Z && this.init_SyncManager_Buffer == null && at >= this.flush) {
         String command = null;

         for (String root : List.of("minecraft:list", "list")) {
            CommandNode<ClientSuggestionProvider> node = this.init_ClientPacketListen.getCommands().getRoot().getChild(root);
            if (node != null && node.getChild("uuids") != null) {
               command = root + " uuids";
               break;
            }
         }

         if (command == null) {
            this.flush = at + 30000L;
         } else {
            this.init_SyncManager_Buffer = new AntiVanishModule.Inner_BufferHelper(this.init_ClientPacketListen, this.init_Obj, command, at);
            this.read_Z = true;

            try {
               this.init_ClientPacketListen.sendCommand(command);
            } catch (RuntimeException ignored) {
               this.init_SyncManager_Buffer = null;
               this.load_Z = true;
            } finally {
               this.read_Z = false;
            }
         }
      }
   }

   public static boolean init_Z_ClientPacketListen_ClientboundSystemC(ClientPacketListener handler, ClientboundSystemChatPacket packet) {
      AntiVanishModule module = init_SyncManager();
      if (module != null
         && module.isEnabled()
         && MC.isSameThread()
         && handler == MC.getConnection()
         && !packet.overlay()
         && module.bool("vanish-tracker")
         && module.bool("completion-probe")) {
         AntiVanishModule.Inner_BufferHelper request = module.init_SyncManager_Buffer;
         long at = init_J_5();
         if (request != null
            && request.init_ClientPacketListen == handler
            && request.init_Obj == MC.level
            && at >= request.init_J
            && at - request.init_J <= 4000L) {
            Optional<List<autismclient.util.ListCommandPlayerParser.Inner_DataUtil>> parsed = autismclient.util.ListCommandPlayerParser.init(packet.content());
            if (parsed.isEmpty()) {
               return false;
            }

            module.init_SyncManager_Buffer = null;
            module.init_V_J(at);
            Set<UUID> listed = handler.getListedOnlinePlayers().stream().map(p -> p.getProfile().id()).collect(Collectors.toSet());
            List<autismclient.util.AntiVanishRosterTracker.Inner_ConfigStore> profiles = parsed.get()
               .stream()
               .map(
                  p -> new autismclient.util.AntiVanishRosterTracker.Inner_ConfigStore(
                     p.init_UUID(), p.init_Str(), listed.contains(p.init_UUID()), module.init_Z_UUID_Str(p.init_UUID(), p.init_Str())
                  )
               )
               .toList();
            module.init_CachePool.init(profiles, request.init_J, at);
            module.flush = at + (module.init_CachePool.init_Z() ? 2000 : 10000);
            module.write_V_J(at);
            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   @Override
   public boolean onPacketSend(Packet<?> packet) {
      if (packet instanceof ServerboundPlayerActionPacket || packet instanceof ServerboundUseItemOnPacket || packet instanceof ServerboundChatCommandPacket) {
         long epoch = this.read_J;
         MC.execute(() -> {
            if (epoch == this.read_J && this.isEnabled() && MC.level == this.init_Obj && MC.player != null && this.write()) {
               this.init_UiHelper.init_V_Packet_J(packet, init_J_5());
            }
         });
      }

      if (packet instanceof ServerboundChatCommandPacket command && !this.read_Z) {
         String text = command.command().trim();
         if (text.equals("list uuids") || text.equals("minecraft:list uuids")) {
            MC.execute(() -> {
               this.init_SyncManager_Buffer = null;
               this.flush = init_J_5() + 10000L;
            });
         }
      }

      return false;
   }

   private boolean init_Z_UUID(UUID id, AntiVanishModule.Inner_DataUtil detection, long at) {
      return detection.load_Z ? this.init_StreamUtil.init_Z_UUID_J(id, at) && !this.init_CachePool.init(id, at) : this.init_CachePool.init(id, at);
   }

   private void write_V_J(long at) {
      if (!this.bool("vanish-tracker")) {
         this.init_CachePool.write();
         this.init_StreamUtil.init();
         this.load_Map.clear();
         this.init_Map.clear();
         this.write_Map.clear();
         this.init_SyncManager_Buffer = null;
         this.init_V_17();
      } else {
         if (!this.bool("environmental")) {
            this.init_CachePool.init_V();
         }

         this.load_Map
            .entrySet()
            .removeIf(
               e -> e.getValue().init_J <= at
                  || !this.init_Z_UUID(e.getKey(), e.getValue(), at)
                  || !this.init_Z_UUID_2(e.getKey(), e.getValue().init_Str, e.getValue().init_Z)
            );
         if (this.init_UUID != null && (!this.load_Map.containsKey(this.init_UUID) || this.load_Map.get(this.init_UUID).load_Z)) {
            this.init_V_17();
         }

         for (autismclient.util.AntiVanishRosterTracker.Inner_StreamUtil alert : this.init_CachePool.init_List_J(at)) {
            if (this.init_Z_UUID_2(alert.init_UUID(), alert.init_Str(), alert.init_Z())) {
               AntiVanishModule.Inner_DataUtil detection = new AntiVanishModule.Inner_DataUtil(
                  alert.init_Str(), "Hidden player: " + alert.load(), 100, at + 20000L, alert.init_Z(), false
               );
               this.load_Map.put(alert.init_UUID(), detection);
               this.init_Map.put(alert.init_UUID(), detection);
            }
         }

         for (autismclient.util.PlayerQuitEventTracker.Inner_DataUtil alert : this.init_StreamUtil.init_List_J(at)) {
            if (!this.init_CachePool.init(alert.init_UUID(), at) && this.init_Z_UUID_Str(alert.init_UUID(), alert.init_Str())) {
               AntiVanishModule.Inner_DataUtil detection = new AntiVanishModule.Inner_DataUtil(
                  alert.init_Str(), "Possible vanish: left TAB without a matching leave message", 0, alert.init_J(), false, true
               );
               this.load_Map.put(alert.init_UUID(), detection);
               this.init_Map.put(alert.init_UUID(), detection);
            }
         }

         this.init_Map.entrySet().removeIf(e -> !e.getValue().equals(this.load_Map.get(e.getKey())));
         if (!this.init_Map.isEmpty() && at >= this.close) {
            this.close = at + 1500L;
            ArrayList<Entry<UUID, AntiVanishModule.Inner_DataUtil>> batch = new ArrayList<>(this.init_Map.entrySet());
            this.init_Map.clear();
            this.init_V_List(batch.stream().filter(e -> !e.getValue().load_Z).toList(), false, at);
            this.init_V_List(batch.stream().filter(e -> e.getValue().load_Z).toList(), true, at);
         }
      }
   }

   private void init_V_List(List<Entry<UUID, AntiVanishModule.Inner_DataUtil>> batch, boolean possible, long at) {
      if (!batch.isEmpty()) {
         String names = batch.stream().limit(3L).map(e -> e.getValue().init_Str).collect(Collectors.joining(", "));
         if (batch.size() > 3) {
            names = names + " (+" + (batch.size() - 3) + ")";
         }

         String title = possible ? "Possible vanish: " : "Online outside TAB: ";
         if (this.bool("chat-alerts")) {
            AutismClientMessaging.sendPrefixed("§e" + title + "§f" + names + (possible ? " (left TAB without a leave message)" : ""));
         }

         if (this.bool("critical-alert")) {
            autismclient.util.bi.read(title + names);
            if (!possible) {
               this.init_UUID = batch.getFirst().getKey();
               this.sync = at + 7000L;
               this.init_Str = names;
               if (this.bool("alert-sound")) {
                  MC.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_PLING, 1.0F));
               }
            }
         }
      }
   }

   private void init_V_17() {
      this.init_UUID = null;
      this.sync = 0L;
      this.init_Str = "";
   }

   private void load_V_14() {
      this.init_CachePool.write();
      this.init_StreamUtil.init();
      this.load_Map.clear();
      this.init_Map.clear();
      this.read_Map.clear();
      this.write_Map.clear();
      this.update_Map.clear();
      this.read_J++;
      this.init_UiHelper.init();
      this.write_J = -1L;
      this.init_ClientPacketListen = null;
      this.init_Obj = null;
      this.update_J = -1L;
      this.init_V_17();
      this.init_SyncManager_Buffer = null;
      this.flush = this.close = this.open = 0L;
      this.load_Z = this.read_Z = false;
      this.init_Set.clear();
      this.init_Z = false;
   }

   public static boolean init_Z_19() {
      AntiVanishModule module = init_SyncManager();
      return module != null
         && module.isEnabled()
         && module.bool("hud-list")
         && (!module.load_Map.isEmpty() || module.write() && !module.init_UiHelper.init_List_J(init_J_5()).isEmpty());
   }

   private List<AntiVanishModule.Inner_StreamUtil> load_List_2() {
      long at = init_J_5();
      List<AntiVanishModule.Inner_StreamUtil> rows = new ArrayList<>(
         this.load_Map
            .entrySet()
            .stream()
            .filter(e -> e.getValue().init_J > at && this.init_Z_UUID(e.getKey(), e.getValue(), at))
            .map(Entry::getValue)
            .sorted(Comparator.comparing(AntiVanishModule.Inner_DataUtil::init_Str, String.CASE_INSENSITIVE_ORDER))
            .limit(4L)
            .map(d -> new AntiVanishModule.Inner_StreamUtil(d.init_Str, d.load_Str, d.init_I))
            .toList()
      );
      if (this.write()) {
         for (autismclient.util.UnexplainedBlockTracker.Inner_BufferHelper reading : this.init_UiHelper.init_List_J(at)) {
            rows.add(new AntiVanishModule.Inner_StreamUtil(init_Str_BlockPos(reading.init_BlockPos()), reading.init_Str(), 0));
         }
      }

      return List.copyOf(rows);
   }

   public static String init_Str_SyncManager_Stream(AntiVanishModule.Inner_StreamUtil entry) {
      if (entry == null) {
         return "WATCH";
      }

      if ("CRITICAL".equalsIgnoreCase(entry.init_Str())) {
         return "ALERT";
      }

      String reason = entry.load() == null ? "" : entry.load();
      String lower = reason.toLowerCase(Locale.ROOT);
      int idx = lower.indexOf("rank detection:");
      if (idx >= 0) {
         AntiVanishModule module = init_SyncManager();
         String name = entry.init_Str() == null ? "" : entry.init_Str().trim();
         if (module != null && !name.isBlank()) {
            String glyph = module.update_Map.get(name.toLowerCase(Locale.ROOT));
            if (glyph != null && !glyph.isBlank()) {
               return glyph.length() <= 12 ? glyph : glyph.substring(0, 12);
            }
         }

         String word = reason.substring(idx + "rank detection:".length()).trim();
         if (!word.isBlank()) {
            String up = word.toUpperCase(Locale.ROOT);
            return up.length() <= 12 ? up : up.substring(0, 12);
         } else {
            return "RANK";
         }
      } else if (lower.startsWith("unexplained")) {
         return "ACTIVITY";
      } else if (lower.startsWith("possible vanish:")) {
         return "POSSIBLE";
      } else if (lower.startsWith("hidden player:")) {
         return "HIDDEN";
      } else if (lower.startsWith("vanish event")) {
         return "VANISH";
      } else if (lower.startsWith("invisible entity")) {
         return "INVIS";
      } else if (lower.startsWith("suspicious sound")) {
         return "SOUND";
      } else if (lower.startsWith("camera aberration")) {
         return "CAMERA";
      } else if (lower.startsWith("ghost particle")) {
         return "PARTICLE";
      } else if (lower.startsWith("block")) {
         return "BLOCK";
      } else {
         return lower.startsWith("chunk") ? "CHUNK" : "WATCH";
      }
   }

   public static String load_Str_SyncManager_Stream(AntiVanishModule.Inner_StreamUtil entry) {
      if (entry == null) {
         return "Staff";
      } else if ("CRITICAL".equalsIgnoreCase(entry.init_Str())) {
         AntiVanishModule module = init_SyncManager();
         String summary = module == null ? "" : module.init_Str;
         return summary != null && !summary.isBlank() ? summary : "watching";
      } else {
         return read_Str_SyncManager_Stream(entry);
      }
   }

   public static boolean load_Z_8() {
      AntiVanishModule module = init_SyncManager();
      return module != null && module.isEnabled() && init_J_5() < module.sync;
   }

   public static String init_Str_5() {
      AntiVanishModule module = init_SyncManager();
      return module == null ? "" : module.init_Str;
   }

   public static List<AntiVanishModule.Inner_StreamUtil> init_List_8() {
      AntiVanishModule module = init_SyncManager();
      return module == null ? List.of() : module.load_List_2();
   }

   public static String read_Str_SyncManager_Stream(AntiVanishModule.Inner_StreamUtil entry) {
      if (entry == null) {
         return "Staff";
      } else {
         String name = entry.init_Str() == null ? "" : entry.init_Str().trim();
         if (!name.isBlank() && !"CRITICAL".equalsIgnoreCase(name) && !"Unknown".equalsIgnoreCase(name) && !"You".equalsIgnoreCase(name)) {
            return name.length() <= 16 ? name : name.substring(0, 16);
         } else {
            return "Staff";
         }
      }
   }

   public static String write_Str_SyncManager_Stream(AntiVanishModule.Inner_StreamUtil entry) {
      if (entry == null) {
         return "";
      } else {
         String reason = entry.load() == null ? "" : entry.load().trim();
         String lower = reason.toLowerCase(Locale.ROOT);
         if ("CRITICAL".equalsIgnoreCase(entry.init_Str())) {
            return "WATCH";
         } else if (lower.startsWith("unexplained")) {
            return "Activity";
         } else if (lower.startsWith("possible vanish:")) {
            return "Possible";
         } else if (lower.startsWith("hidden player:")) {
            return "Hidden";
         } else if (lower.startsWith("vanish event")) {
            return "Vanish";
         } else if (lower.startsWith("rank detection")) {
            return "Rank";
         } else if (lower.startsWith("invisible entity")) {
            return "Invis";
         } else if (lower.startsWith("entity packet spike")) {
            return "Packets";
         } else if (lower.startsWith("camera aberration")) {
            return "Camera";
         } else if (lower.startsWith("ghost particle")) {
            return "Particle";
         } else if (lower.startsWith("suspicious sound")) {
            return "Sound";
         } else if (lower.startsWith("block")) {
            return "Block";
         } else if (lower.startsWith("chunk")) {
            return "Chunk";
         } else {
            return reason.length() <= 10 ? reason : reason.substring(0, 10);
         }
      }
   }

   private static AntiVanishModule init_SyncManager() {
      return al.init_Module_Str("anti-vanish") instanceof AntiVanishModule watcher ? watcher : null;
   }

   public static String load_Str() {
      AntiVanishModule module = init_SyncManager();
      return module != null && module.isEnabled()
         ? "known=" + module.init_CachePool.init_I() + " det=" + module.load_Map.size() + " probes=" + module.write_Map.size()
         : "off";
   }

   private record Inner_BufferHelper(ClientPacketListener init_ClientPacketListen, Object init_Obj, String init_Str, long init_J) {
   }

   private record Inner_CachePool(long init_J, int init_I) {
   }

   private record Inner_DataUtil(String init_Str, String load_Str, int init_I, long init_J, boolean init_Z, boolean load_Z) {
   }

   public record Inner_StreamUtil(String init_Str, String load, int init_I) {
   }
}
