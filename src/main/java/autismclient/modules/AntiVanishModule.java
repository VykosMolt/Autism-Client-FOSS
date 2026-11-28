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
   private static final AtomicInteger nextRequestId = new AtomicInteger(-1000);
   private final autismclient.util.AntiVanishRosterTracker rosterTracker = new autismclient.util.AntiVanishRosterTracker();
   private final autismclient.util.PlayerQuitEventTracker quitTracker = new autismclient.util.PlayerQuitEventTracker();
   private final Map<UUID, AntiVanishModule.Detection> alertedDetections = new LinkedHashMap<>();
   private final VanishActivityDetector activityDetector = new VanishActivityDetector();
   private volatile long epoch;
   private long lastActivityAlertAt = -1L;
   private final Map<UUID, AntiVanishModule.Detection> detections = new LinkedHashMap<>();
   private final Map<UUID, GameType> lastGameModes = new HashMap<>();
   private final Map<Integer, AntiVanishModule.SuggestionProbe> pendingSuggestionProbes = new HashMap<>();
   private final Map<String, String> unusedNameMap = new HashMap<>();
   private ClientPacketListener connection;
   private Object level;
   private long lastRequestAt = -1L;
   private long bannerUntil;
   private UUID bannerPlayer;
   private AntiVanishModule.RosterRequest request;
   private long nextListProbeAt;
   private long nextAlertBatchAt;
   private long sessionStartAt;
   private final Set<UUID> seenProfiles = new HashSet<>();
   private boolean seenProfilesOverflowed;
   private boolean listProbeUnavailable;
   private boolean sendingListProbe;
   private String bannerNames = "";

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

   private static long nowMillis() {
      return System.nanoTime() / 1000000L;
   }

   @Override
   public String info() {
      return this.detections.isEmpty()
         ? (this.worldActivityEnabled() && !this.activityDetector.unexplained(nowMillis()).isEmpty() ? "Activity" : "")
         : Integer.toString(this.detections.size());
   }

   @Override
   public void onEnable() {
      this.resetState();
      if (this.refreshSession()) {
         this.scanRoster(nowMillis());
      }
   }

   @Override
   public void onDisable() {
      this.resetState();
   }

   @Override
   public void onGameJoin() {
      this.resetState();
   }

   @Override
   public void onGameLeft() {
      this.resetState();
   }

   private boolean refreshSession() {
      if (MC.player != null && MC.level != null && MC.getConnection() != null) {
         if (this.connection != MC.getConnection() || this.level != MC.level) {
            this.resetState();
            this.connection = MC.getConnection();
            this.level = MC.level;
            this.sessionStartAt = nowMillis();
            this.nextListProbeAt = this.sessionStartAt + 3000L;
         }

         return true;
      } else {
         return false;
      }
   }

   @Override
   public void tick() {
      if (!this.refreshSession()) {
         this.resetState();
      } else {
         long at = nowMillis();
         this.scanRoster(at);
         if (this.worldActivityEnabled()) {
            this.activityDetector.expire(at);
            List<autismclient.util.UnexplainedBlockTracker.ActivityReading> readings = this.activityDetector.unexplained(at);
            if (!readings.isEmpty() && (this.lastActivityAlertAt < 0L || at - this.lastActivityAlertAt >= 10000L) && this.bool("chat-alerts")) {
               this.lastActivityAlertAt = at;
               AutismClientMessaging.sendPrefixed(
                  "Unexplained block activity: "
                     + describeOffset(readings.getFirst().pos())
                     + ". Possible hidden activity; no player identity established."
               );
            }
         } else {
            this.activityDetector.init();
         }

         if (!this.bool("vanish-tracker")) {
            this.rosterTracker.write();
            this.quitTracker.init();
            this.detections.clear();
            this.alertedDetections.clear();
            this.pendingSuggestionProbes.clear();
            this.request = null;
            this.clearBanner();
         } else {
            if (!this.bool("environmental")) {
               this.rosterTracker.resetProbes();
            }

            if (this.bool("completion-probe")) {
               this.tickListProbe(at);
            } else {
               this.request = null;
               this.rosterTracker.load();
            }

            this.pendingSuggestionProbes.values().removeIf(p -> at - p.at > 4000L);
            if (!this.bool("completion-probe")) {
               this.pendingSuggestionProbes.clear();
            }

            if (this.bool("completion-probe")
               && this.rosterTracker.hasRecentHiddenAction(at)
               && this.pendingSuggestionProbes.isEmpty()
               && (this.lastRequestAt < 0L || at - this.lastRequestAt >= 1500L)) {
               this.sendSuggestionProbe(at);
            }

            this.updateDetections(at);
         }
      }
   }

   public static void onPacket(ClientPacketListener handler, Packet<?> packet) {
      AntiVanishModule module = instance();
      if (module != null && module.isEnabled() && MC.isSameThread() && handler == MC.getConnection() && module.refreshSession()) {
         module.handleTrackedPacket(packet, nowMillis());
      }
   }

   private boolean worldActivityEnabled() {
      return this.bool("vanish-tracker") && this.bool("world-activity") && !this.bool("player-filter");
   }

   public static void onBlockUpdate(ClientPacketListener handler, BlockPos pos, BlockState state) {
      AntiVanishModule module = instance();
      if (module != null && module.isEnabled() && MC.isSameThread() && handler == MC.getConnection() && module.refreshSession() && module.worldActivityEnabled()) {
         long at = nowMillis();
         if (at - module.sessionStartAt >= 3000L) {
            module.activityDetector.init(pos, state, at);
         }
      }
   }

   public static void onSectionUpdate(ClientPacketListener handler, ClientboundSectionBlocksUpdatePacket packet) {
      AntiVanishModule module = instance();
      if (module != null && module.isEnabled() && MC.isSameThread() && handler == MC.getConnection() && module.refreshSession() && module.worldActivityEnabled()) {
         long at = nowMillis();
         module.activityDetector.init(packet, at, at - module.sessionStartAt >= 3000L);
      }
   }

   private static String describeOffset(BlockPos pos) {
      double dx = pos.getX() + 0.5 - MC.player.getX();
      double dz = pos.getZ() + 0.5 - MC.player.getZ();
      String[] directions = new String[]{"S", "SW", "W", "NW", "N", "NE", "E", "SE"};
      int direction = Math.floorMod((int)Math.round(Math.atan2(-dx, dz) / 0.7853981633974483), 8);
      return Math.round(Math.sqrt(dx * dx + dz * dz)) + "m " + directions[direction];
   }

   private void handleTrackedPacket(Packet<?> packet, long at) {
      if (!this.worldActivityEnabled() || !this.activityDetector.init_Z_Packet_J(packet, at)) {
         if (packet instanceof ClientboundPlayerInfoUpdatePacket info) {
            if (this.bool("gamemode-alerts") && info.actions().contains(Action.UPDATE_GAME_MODE) && !info.actions().contains(Action.ADD_PLAYER)) {
               for (net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket.Entry entry : info.entries()) {
                  GameType previous = this.lastGameModes.get(entry.profileId());
                  PlayerInfo player = this.connection.getPlayerInfo(entry.profileId());
                  if (previous != null && entry.gameMode() != null && previous != entry.gameMode() && player != null && this.isTrackedInfo(player)) {
                     AutismClientMessaging.sendPrefixed("§bGamemode: §f" + player.getProfile().name() + " §7-> " + entry.gameMode().getName());
                  }
               }
            }

            this.scanRoster(at);
            this.handleLatencyUpdate(info, at);
            this.updateDetections(at);
         } else if (packet instanceof ClientboundPlayerInfoRemovePacket) {
            this.scanRoster(at);
            this.updateDetections(at);
         } else if (packet instanceof ClientboundSystemChatPacket chat) {
            if (!chat.overlay()) {
               String text = chat.content() == null ? "" : chat.content().getString();
               this.rosterTracker.init(text, at);
               this.quitTracker.onChat(chat.content(), at);
            }

            this.updateDetections(at);
         } else if (this.bool("vanish-tracker")) {
            this.scanRoster(at);
            if (packet instanceof ClientboundCommandSuggestionsPacket reply) {
               AntiVanishModule.SuggestionProbe request = this.pendingSuggestionProbes.remove(reply.id());
               if (request == null || !this.bool("completion-probe") || at - request.at > 4000L) {
                  return;
               }

               Suggestions suggestions = reply.toSuggestions();
               if (suggestions.getRange().getStart() != request.rangeStart
                  || suggestions.getRange().getEnd() != request.rangeStart
                  || suggestions.getList().size() > 512) {
                  return;
               }

               this.rosterTracker.load(suggestions.getList().stream().map(s -> s.getText()).toList(), request.at, at);
            } else if (packet instanceof ClientboundAnimatePacket animation) {
               if (animation.getAction() != 0 && animation.getAction() != 3) {
                  return;
               }

               this.noteEntityActivity(animation.getId(), "swing", at);
            } else if (packet instanceof ClientboundBlockDestructionPacket dig) {
               if (dig.getProgress() < 0 || dig.getProgress() > 9 || MC.level.getBlockState(dig.getPos()).isAir()) {
                  return;
               }

               if (!(MC.level.getEntity(dig.getId()) instanceof Player player) || player.getEyePosition().distanceToSqr(Vec3.atCenterOf(dig.getPos())) > 42.25) {
                  return;
               }

               this.noteEntityActivity(dig.getId(), "dig@" + dig.getPos().asLong(), at);
            }

            this.updateDetections(at);
         }
      }
   }

   private void noteEntityActivity(int entityId, String action, long at) {
      if (this.bool("environmental")
         && MC.level.getEntity(entityId) instanceof Player player
         && player != MC.player
         && player.isAlive()
         && player.tickCount >= 5
         && !(player.distanceToSqr(MC.player) > (double)this.integer("range") * this.integer("range"))) {
         String name = player.getGameProfile().name();
         boolean npc = ArmorFilterMatcher.init_Z_UUID(player.getUUID()) || AntiBotTracker.isConfirmedBotUuid(player.getUUID());
         this.rosterTracker.init(new autismclient.util.AntiVanishRosterTracker.ActorInfo(player.getUUID(), name, true, npc), action, at);
      }
   }

   private boolean isTrackedInfo(PlayerInfo info) {
      if (info != null && info.getProfile() != null) {
         GameProfile profile = info.getProfile();
         return this.isTrackedPlayer(profile.id(), profile.name());
      } else {
         return false;
      }
   }

   private boolean isTrackedPlayer(UUID id, String name) {
      return id != null
         && !id.equals(MC.player.getUUID())
         && TabPlayerListParser.isPlausibleName(name)
         && !ArmorFilterMatcher.init_Z_UUID(id)
         && !AntiBotTracker.isConfirmedBotUuid(id)
         && this.matchesPlayerFilter(name);
   }

   private boolean matchesPlayerFilter(String name) {
      return !this.bool("player-filter") || this.list("players").stream().anyMatch(watched -> watched.equalsIgnoreCase(name));
   }

   private void scanRoster(long at) {
      List<autismclient.util.AntiVanishRosterTracker.RosterProfile> roster = new ArrayList<>();
      Collection<PlayerInfo> listed = this.connection.getListedOnlinePlayers();

      for (PlayerInfo info : this.connection.getOnlinePlayers()) {
         if (info != null && info.getProfile() != null) {
            GameProfile profile = info.getProfile();
            if (!this.seenProfilesOverflowed) {
               this.seenProfiles.add(profile.id());
               if (this.seenProfiles.size() > 8192) {
                  this.seenProfiles.clear();
                  this.seenProfilesOverflowed = true;
                  this.rosterTracker.read();
               }
            }

            roster.add(new autismclient.util.AntiVanishRosterTracker.RosterProfile(profile.id(), profile.name(), listed.contains(info), this.isTrackedInfo(info)));
         }
      }

      this.rosterTracker.init(roster, at);
      this.quitTracker.updateRoster(roster, at);
      this.lastGameModes.clear();

      for (PlayerInfo info : this.connection.getOnlinePlayers()) {
         if (info != null && info.getProfile() != null) {
            this.lastGameModes.put(info.getProfile().id(), info.getGameMode());
         }
      }
   }

   private void handleLatencyUpdate(ClientboundPlayerInfoUpdatePacket packet, long at) {
      if (this.bool("vanish-tracker")
         && !this.seenProfilesOverflowed
         && at - this.sessionStartAt >= 5000L
         && packet.actions().equals(EnumSet.of(Action.UPDATE_LATENCY))
         && packet.entries().size() <= 4096) {
         Set<UUID> all = new HashSet<>();
         Set<UUID> unknown = new HashSet<>();

         for (net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket.Entry entry : packet.entries()) {
            UUID id = entry.profileId();
            if (id == null || !all.add(id) || entry.latency() < 0 || entry.latency() > 120000) {
               return;
            }

            if (this.connection.getPlayerInfo(id) == null && !this.seenProfiles.contains(id) && !ArmorFilterMatcher.init_Z_UUID(id) && !AntiBotTracker.isConfirmedBotUuid(id)
               )
             {
               unknown.add(id);
            }
         }

         if (all.contains(MC.player.getUUID())) {
            for (PlayerInfo info : this.connection.getListedOnlinePlayers()) {
               if (!all.contains(info.getProfile().id())) {
                  return;
               }
            }

            this.rosterTracker.init(unknown, at);
         }
      }
   }

   private boolean isTrackedAlert(UUID id, String name, boolean unresolved) {
      return !unresolved
         ? this.isTrackedPlayer(id, name)
         : id != null && !id.equals(MC.player.getUUID()) && !ArmorFilterMatcher.init_Z_UUID(id) && !AntiBotTracker.isConfirmedBotUuid(id) && !this.bool("player-filter");
   }

   private void sendSuggestionProbe(long at) {
      String command = "minecraft:msg ";
      int id = nextRequestId.getAndUpdate(value -> value == -2147483648 ? -1000 : value - 1);
      this.pendingSuggestionProbes.put(id, new AntiVanishModule.SuggestionProbe(at, command.length()));
      this.lastRequestAt = at;

      try {
         this.connection.send(new ServerboundCommandSuggestionPacket(id, command));
      } catch (RuntimeException ignored) {
         this.pendingSuggestionProbes.remove(id);
      }
   }

   private void tickListProbe(long at) {
      if (this.request != null && at - this.request.at > 4000L) {
         this.request = null;
         this.listProbeUnavailable = true;
         this.rosterTracker.load();
      }

      if (!this.listProbeUnavailable && this.request == null && at >= this.nextListProbeAt) {
         String command = null;

         for (String root : List.of("minecraft:list", "list")) {
            CommandNode<ClientSuggestionProvider> node = this.connection.getCommands().getRoot().getChild(root);
            if (node != null && node.getChild("uuids") != null) {
               command = root + " uuids";
               break;
            }
         }

         if (command == null) {
            this.nextListProbeAt = at + 30000L;
         } else {
            this.request = new AntiVanishModule.RosterRequest(this.connection, this.level, command, at);
            this.sendingListProbe = true;

            try {
               this.connection.sendCommand(command);
            } catch (RuntimeException ignored) {
               this.request = null;
               this.listProbeUnavailable = true;
            } finally {
               this.sendingListProbe = false;
            }
         }
      }
   }

   public static boolean onSystemChat(ClientPacketListener handler, ClientboundSystemChatPacket packet) {
      AntiVanishModule module = instance();
      if (module != null
         && module.isEnabled()
         && MC.isSameThread()
         && handler == MC.getConnection()
         && !packet.overlay()
         && module.bool("vanish-tracker")
         && module.bool("completion-probe")) {
         AntiVanishModule.RosterRequest request = module.request;
         long at = nowMillis();
         if (request != null
            && request.connection == handler
            && request.level == MC.level
            && at >= request.at
            && at - request.at <= 4000L) {
            Optional<List<autismclient.util.ListCommandPlayerParser.ListedPlayer>> parsed = autismclient.util.ListCommandPlayerParser.init(packet.content());
            if (parsed.isEmpty()) {
               return false;
            }

            module.request = null;
            module.scanRoster(at);
            Set<UUID> listed = handler.getListedOnlinePlayers().stream().map(p -> p.getProfile().id()).collect(Collectors.toSet());
            List<autismclient.util.AntiVanishRosterTracker.RosterProfile> profiles = parsed.get()
               .stream()
               .map(
                  p -> new autismclient.util.AntiVanishRosterTracker.RosterProfile(
                     p.uuid(), p.name(), listed.contains(p.uuid()), module.isTrackedPlayer(p.uuid(), p.name())
                  )
               )
               .toList();
            module.rosterTracker.init(profiles, request.at, at);
            module.nextListProbeAt = at + (module.rosterTracker.needsRecheck() ? 2000 : 10000);
            module.updateDetections(at);
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
         long epoch = this.epoch;
         MC.execute(() -> {
            if (epoch == this.epoch && this.isEnabled() && MC.level == this.level && MC.player != null && this.worldActivityEnabled()) {
               this.activityDetector.init_V_Packet_J(packet, nowMillis());
            }
         });
      }

      if (packet instanceof ServerboundChatCommandPacket command && !this.sendingListProbe) {
         String text = command.command().trim();
         if (text.equals("list uuids") || text.equals("minecraft:list uuids")) {
            MC.execute(() -> {
               this.request = null;
               this.nextListProbeAt = nowMillis() + 10000L;
            });
         }
      }

      return false;
   }

   private boolean isStillSuspect(UUID id, AntiVanishModule.Detection detection, long at) {
      return detection.possibleVanish ? this.quitTracker.isSuspect(id, at) && !this.rosterTracker.init(id, at) : this.rosterTracker.init(id, at);
   }

   private void updateDetections(long at) {
      if (!this.bool("vanish-tracker")) {
         this.rosterTracker.write();
         this.quitTracker.init();
         this.detections.clear();
         this.alertedDetections.clear();
         this.pendingSuggestionProbes.clear();
         this.request = null;
         this.clearBanner();
      } else {
         if (!this.bool("environmental")) {
            this.rosterTracker.resetProbes();
         }

         this.detections
            .entrySet()
            .removeIf(
               e -> e.getValue().expiresAt <= at
                  || !this.isStillSuspect(e.getKey(), e.getValue(), at)
                  || !this.isTrackedAlert(e.getKey(), e.getValue().playerName, e.getValue().unresolved)
            );
         if (this.bannerPlayer != null && (!this.detections.containsKey(this.bannerPlayer) || this.detections.get(this.bannerPlayer).possibleVanish)) {
            this.clearBanner();
         }

         for (autismclient.util.AntiVanishRosterTracker.VanishAlert alert : this.rosterTracker.collectAlerts(at)) {
            if (this.isTrackedAlert(alert.uuid(), alert.name(), alert.uuidOnly())) {
               AntiVanishModule.Detection detection = new AntiVanishModule.Detection(
                  alert.name(), "Hidden player: " + alert.load(), 100, at + 20000L, alert.uuidOnly(), false
               );
               this.detections.put(alert.uuid(), detection);
               this.alertedDetections.put(alert.uuid(), detection);
            }
         }

         for (autismclient.util.PlayerQuitEventTracker.QuitAlert alert : this.quitTracker.collectAlerts(at)) {
            if (!this.rosterTracker.init(alert.uuid(), at) && this.isTrackedPlayer(alert.uuid(), alert.name())) {
               AntiVanishModule.Detection detection = new AntiVanishModule.Detection(
                  alert.name(), "Possible vanish: left TAB without a matching leave message", 0, alert.expiresAt(), false, true
               );
               this.detections.put(alert.uuid(), detection);
               this.alertedDetections.put(alert.uuid(), detection);
            }
         }

         this.alertedDetections.entrySet().removeIf(e -> !e.getValue().equals(this.detections.get(e.getKey())));
         if (!this.alertedDetections.isEmpty() && at >= this.nextAlertBatchAt) {
            this.nextAlertBatchAt = at + 1500L;
            ArrayList<Entry<UUID, AntiVanishModule.Detection>> batch = new ArrayList<>(this.alertedDetections.entrySet());
            this.alertedDetections.clear();
            this.announceBatch(batch.stream().filter(e -> !e.getValue().possibleVanish).toList(), false, at);
            this.announceBatch(batch.stream().filter(e -> e.getValue().possibleVanish).toList(), true, at);
         }
      }
   }

   private void announceBatch(List<Entry<UUID, AntiVanishModule.Detection>> batch, boolean possible, long at) {
      if (!batch.isEmpty()) {
         String names = batch.stream().limit(3L).map(e -> e.getValue().playerName).collect(Collectors.joining(", "));
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
               this.bannerPlayer = batch.getFirst().getKey();
               this.bannerUntil = at + 7000L;
               this.bannerNames = names;
               if (this.bool("alert-sound")) {
                  MC.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_PLING, 1.0F));
               }
            }
         }
      }
   }

   private void clearBanner() {
      this.bannerPlayer = null;
      this.bannerUntil = 0L;
      this.bannerNames = "";
   }

   private void resetState() {
      this.rosterTracker.write();
      this.quitTracker.init();
      this.detections.clear();
      this.alertedDetections.clear();
      this.lastGameModes.clear();
      this.pendingSuggestionProbes.clear();
      this.unusedNameMap.clear();
      this.epoch++;
      this.activityDetector.init();
      this.lastActivityAlertAt = -1L;
      this.connection = null;
      this.level = null;
      this.lastRequestAt = -1L;
      this.clearBanner();
      this.request = null;
      this.nextListProbeAt = this.nextAlertBatchAt = this.sessionStartAt = 0L;
      this.listProbeUnavailable = this.sendingListProbe = false;
      this.seenProfiles.clear();
      this.seenProfilesOverflowed = false;
   }

   public static boolean isHudVisible() {
      AntiVanishModule module = instance();
      return module != null
         && module.isEnabled()
         && module.bool("hud-list")
         && (!module.detections.isEmpty() || module.worldActivityEnabled() && !module.activityDetector.unexplained(nowMillis()).isEmpty());
   }

   private List<AntiVanishModule.HudRow> snapshotRows() {
      long at = nowMillis();
      List<AntiVanishModule.HudRow> rows = new ArrayList<>(
         this.detections
            .entrySet()
            .stream()
            .filter(e -> e.getValue().expiresAt > at && this.isStillSuspect(e.getKey(), e.getValue(), at))
            .map(Entry::getValue)
            .sorted(Comparator.comparing(AntiVanishModule.Detection::playerName, String.CASE_INSENSITIVE_ORDER))
            .limit(4L)
            .map(d -> new AntiVanishModule.HudRow(d.playerName, d.message, d.init_I))
            .toList()
      );
      if (this.worldActivityEnabled()) {
         for (autismclient.util.UnexplainedBlockTracker.ActivityReading reading : this.activityDetector.unexplained(at)) {
            rows.add(new AntiVanishModule.HudRow(describeOffset(reading.pos()), reading.label(), 0));
         }
      }

      return List.copyOf(rows);
   }

   public static String statusLabel(AntiVanishModule.HudRow entry) {
      if (entry == null) {
         return "WATCH";
      }

      if ("CRITICAL".equalsIgnoreCase(entry.severity())) {
         return "ALERT";
      }

      String reason = entry.load() == null ? "" : entry.load();
      String lower = reason.toLowerCase(Locale.ROOT);
      int idx = lower.indexOf("rank detection:");
      if (idx >= 0) {
         AntiVanishModule module = instance();
         String name = entry.severity() == null ? "" : entry.severity().trim();
         if (module != null && !name.isBlank()) {
            String glyph = module.unusedNameMap.get(name.toLowerCase(Locale.ROOT));
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

   public static String roleLabel(AntiVanishModule.HudRow entry) {
      if (entry == null) {
         return "Staff";
      } else if ("CRITICAL".equalsIgnoreCase(entry.severity())) {
         AntiVanishModule module = instance();
         String summary = module == null ? "" : module.bannerNames;
         return summary != null && !summary.isBlank() ? summary : "watching";
      } else {
         return entryRole(entry);
      }
   }

   public static boolean isBannerActive() {
      AntiVanishModule module = instance();
      return module != null && module.isEnabled() && nowMillis() < module.bannerUntil;
   }

   public static String currentBannerNames() {
      AntiVanishModule module = instance();
      return module == null ? "" : module.bannerNames;
   }

   public static List<AntiVanishModule.HudRow> entries() {
      AntiVanishModule module = instance();
      return module == null ? List.of() : module.snapshotRows();
   }

   public static String entryRole(AntiVanishModule.HudRow entry) {
      if (entry == null) {
         return "Staff";
      } else {
         String name = entry.severity() == null ? "" : entry.severity().trim();
         if (!name.isBlank() && !"CRITICAL".equalsIgnoreCase(name) && !"Unknown".equalsIgnoreCase(name) && !"You".equalsIgnoreCase(name)) {
            return name.length() <= 16 ? name : name.substring(0, 16);
         } else {
            return "Staff";
         }
      }
   }

   public static String entryTag(AntiVanishModule.HudRow entry) {
      if (entry == null) {
         return "";
      } else {
         String reason = entry.load() == null ? "" : entry.load().trim();
         String lower = reason.toLowerCase(Locale.ROOT);
         if ("CRITICAL".equalsIgnoreCase(entry.severity())) {
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

   private static AntiVanishModule instance() {
      return al.init_Module_Str("anti-vanish") instanceof AntiVanishModule watcher ? watcher : null;
   }

   public static String debugSummary() {
      AntiVanishModule module = instance();
      return module != null && module.isEnabled()
         ? "known=" + module.rosterTracker.trackedCount() + " det=" + module.detections.size() + " probes=" + module.pendingSuggestionProbes.size()
         : "off";
   }

   private record RosterRequest(ClientPacketListener connection, Object level, String command, long at) {
   }

   private record SuggestionProbe(long at, int rangeStart) {
   }

   private record Detection(String playerName, String message, int init_I, long expiresAt, boolean unresolved, boolean possibleVanish) {
   }

   public record HudRow(String severity, String load, int init_I) {
   }
}
