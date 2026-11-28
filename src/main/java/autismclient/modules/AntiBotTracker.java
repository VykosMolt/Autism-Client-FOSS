package autismclient.modules;

import autismclient.AutismClientAddon;
import autismclient.util.TabPlayerListParser;
import com.mojang.authlib.GameProfile;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket.Action;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket.Entry;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class AntiBotTracker {
   private static final Minecraft MC = Minecraft.getInstance();
   private static final int MIN_TICKS_FOR_INFO_CHECK = 40;
   private static final int MIN_TICKS_FOR_PACKET_CHECK = 40;
   private static final double GROUND_PROBE_DEPTH = 0.3;
   private static final double GROUND_PROBE_INFLATE = 0.03;
   private static final Set<UUID> confirmedBotUuids = new HashSet<>();
   private static final Set<UUID> loggedHiddenUuids = new HashSet<>();
   private static final Map<Long, double[]> knownBotSpots = new HashMap<>();
   private static final int MAX_TRACKED_ENTRIES = 4096;
   private static final int MAX_BOT_SPOTS = 512;
   private static final double BOT_SPOT_MATCH_RADIUS = 0.15;
   private static final double BOT_SPOT_MAX_MOVEMENT = 0.02;
   private static final Map<Integer, Integer> duplicateNameFlags = new HashMap<>();
   private static Object duplicateCacheLevel;
   private static int duplicateCacheLevelPlayerCount = -1;
   private static int duplicateCacheOnlinePlayerCount = -1;
   private static int duplicateCacheInfoRevision = -1;
   private static int playerInfoRevision;
   private static final Set<Integer> watchedEntityIds = new HashSet<>();
   private static final Set<Integer> receivedAttributesEntityIds = new HashSet<>();
   private static final Set<Integer> swungArmEntityIds = new HashSet<>();
   private static final Set<Integer> critHitEntityIds = new HashSet<>();
   private static final Set<Integer> hitByLocalPlayerEntityIds = new HashSet<>();
   private static final Map<Integer, Double> maxDistanceSqByEntityId = new HashMap<>();
   private static final Map<Integer, Integer> airGroundViolationByEntityId = new HashMap<>();
   private static final Map<Integer, Double> lastServerYByEntityId = new HashMap<>();
   private static final Set<UUID> receivedAttributesUuids = new HashSet<>();
   private static final Set<UUID> addedPlayerInfoUuids = new HashSet<>();
   private static final Set<UUID> receivedGameModeUuids = new HashSet<>();
   private static Object watchedLevel;
   private static AntiBotModule cachedAntiBotModule;
   private static int cachedModuleRevision = -1;

   private AntiBotTracker() {
   }

   private static AntiBotModule getActiveModule() {
      int rev = al.init_I();
      if (rev != cachedModuleRevision) {
         Module m = al.init_Module_Str("antibot");
         cachedAntiBotModule = m instanceof AntiBotModule a && m.isEnabled() ? a : null;
         cachedModuleRevision = rev;
      }

      return cachedAntiBotModule;
   }

   public static boolean isBot(Entity entity) {
      AntiBotModule m = getActiveModule();
      if (m == null) {
         return false;
      }

      if (entity instanceof Player player && MC.player != null && player != MC.player) {
         UUID uuid = player.getUUID();
         if (hasVerifiableChat(player)) {
            if (confirmedBotUuids.remove(uuid)) {
               loggedHiddenUuids.remove(uuid);
            }

            return false;
         } else if (confirmedBotUuids.contains(uuid)) {
            return true;
         } else {
            String reason = getHardBotReason(player);
            if (reason != null) {
               markConfirmedBot(uuid);
               recordBotLocation(player);
               logHiddenPlayerOnce(player, reason);
               return true;
            } else if (isMissingPlayerInfo(player)) {
               recordBotLocation(player);
               logHiddenPlayerOnce(player, "no player-info entry");
               return true;
            } else {
               String live = getLiveHeuristicReason(player, m);
               if (live != null) {
                  logHiddenPlayerOnce(player, live);
                  return true;
               } else if (isStationaryAtKnownBotSpot(player)) {
                  logHiddenPlayerOnce(player, "known bot location");
                  return true;
               } else {
                  return false;
               }
            }
         }
      } else {
         return false;
      }
   }

   private static boolean hasVerifiableChat(Player player) {
      ClientPacketListener conn = MC.getConnection();
      if (conn == null) {
         return false;
      }

      PlayerInfo info = conn.getPlayerInfo(player.getUUID());
      return info != null && info.hasVerifiableChat();
   }

   private static void markConfirmedBot(UUID uuid) {
      if (confirmedBotUuids.size() >= 4096) {
         confirmedBotUuids.clear();
      }

      confirmedBotUuids.add(uuid);
   }

   private static void recordBotLocation(Player player) {
      if (knownBotSpots.size() >= 512) {
         knownBotSpots.clear();
      }

      knownBotSpots.put(player.blockPosition().asLong(), new double[]{player.getX(), player.getY(), player.getZ()});
   }

   private static boolean isStationaryAtKnownBotSpot(Player player) {
      if (knownBotSpots.isEmpty()) {
         return false;
      }

      double moved = Math.abs(player.getX() - player.xOld) + Math.abs(player.getZ() - player.zOld);
      if (moved > 0.02) {
         return false;
      }

      double[] spot = knownBotSpots.get(player.blockPosition().asLong());
      if (spot == null) {
         return false;
      }

      double dx = player.getX() - spot[0];
      double dy = player.getY() - spot[1];
      double dz = player.getZ() - spot[2];
      return dx * dx + dy * dy + dz * dz <= 0.0225;
   }

   private static String getHardBotReason(Player player) {
      if (getDuplicateNameFlags().getOrDefault(player.getId(), 0) == 2) {
         return "clone of a verified player's name";
      } else {
         float pitch = player.getXRot();
         if (Math.abs(pitch) > 90.0F) {
            return "impossible pitch " + pitch;
         } else {
            GameProfile profile = player.getGameProfile();
            if (profile != null && TabPlayerListParser.isPlausibleName(profile.name())) {
               return ArmorFilterMatcher.isNpcUuid(player.getUUID()) ? "NPC uuid (version 2)" : null;
            } else {
               return "invalid profile name '" + (profile == null ? "<null>" : profile.name()) + "'";
            }
         }
      }
   }

   private static String getLiveHeuristicReason(Player player, AntiBotModule m) {
      int id = player.getId();
      if (getDuplicateNameFlags().getOrDefault(id, 0) == 1) {
         return "duplicate name";
      }

      if (m.isCheckEnabled(m.floatingGround) && airGroundViolationByEntityId.getOrDefault(id, 0) >= m.minAgeTicks()) {
         return "on ground over air";
      }

      boolean watched = isWatchedEntity(id);
      UUID uuid = player.getUUID();
      if (m.isCheckEnabled(m.noAttributes) && watched && player.tickCount >= 40 && !receivedAttributesEntityIds.contains(id) && !receivedAttributesUuids.contains(uuid)) {
         return "no attributes";
      }

      if (m.isCheckEnabled(m.noGameMode) && player.tickCount >= 40 && addedPlayerInfoUuids.contains(uuid) && !receivedGameModeUuids.contains(uuid)) {
         return "no game mode";
      }

      if (!m.isCustomMode()) {
         return null;
      }

      if (m.minAge.get() && player.tickCount < m.ageTicks.get()) {
         return "too young";
      }

      if (m.alwaysNear.get() && watched) {
         double range = m.nearRange.get();
         if (maxDistanceSqByEntityId.getOrDefault(id, 1.7976931348623157E308) <= range * range) {
            return "always near";
         }
      }

      if (m.needHit.get() && watched && !hitByLocalPlayerEntityIds.contains(id)) {
         return "never hit";
      } else if (m.needSwing.get() && watched && !swungArmEntityIds.contains(id)) {
         return "never swung";
      } else if (m.needCrit.get() && watched && !critHitEntityIds.contains(id)) {
         return "never crit-hit";
      } else if (m.oddScale.get() && Math.abs(player.getAttributeValue(Attributes.SCALE) - 1.0) > 1.0E-4) {
         return "odd scale";
      } else {
         return m.requireArmor.get() && !matchesArmorFilter(player, m) ? "armor rule" : null;
      }
   }

   private static boolean matchesArmorFilter(Player player, AntiBotModule m) {
      return ArmorFilterMatcher.matches(m.armorHead.get(), getItemPath(player.getItemBySlot(EquipmentSlot.HEAD)))
         && ArmorFilterMatcher.matches(m.armorChest.get(), getItemPath(player.getItemBySlot(EquipmentSlot.CHEST)))
         && ArmorFilterMatcher.matches(m.armorLegs.get(), getItemPath(player.getItemBySlot(EquipmentSlot.LEGS)))
         && ArmorFilterMatcher.matches(m.armorFeet.get(), getItemPath(player.getItemBySlot(EquipmentSlot.FEET)));
   }

   private static String getItemPath(ItemStack stack) {
      return stack != null && !stack.isEmpty() ? BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath() : "";
   }

   private static void logHiddenPlayerOnce(Player player, String reason) {
      if (loggedHiddenUuids.size() >= 4096) {
         loggedHiddenUuids.clear();
      }

      if (loggedHiddenUuids.add(player.getUUID())) {
         GameProfile p = player.getGameProfile();
         AutismClientAddon.LOG.debug("[antibot] hiding player id={} name='{}' — {}", player.getId(), p == null ? "?" : p.name(), reason);
      }
   }

   public static boolean shouldIgnoreEntity(Entity entity) {
      return isBot(entity);
   }

   public static boolean isConfirmedBotUuid(UUID uuid) {
      return uuid != null && confirmedBotUuids.contains(uuid);
   }

   public static void tick() {
      AntiBotModule m = getActiveModule();
      if (m != null && MC.level != null && MC.player != null) {
         ensureCurrentLevel();
         rebuildDuplicateNameCache();
         if (!watchedEntityIds.isEmpty()) {
            for (Player p : MC.level.players()) {
               if (p != MC.player && watchedEntityIds.contains(p.getId())) {
                  maxDistanceSqByEntityId.merge(p.getId(), MC.player.distanceToSqr(p), Math::max);
               }
            }
         }
      } else {
         resetAll();
      }
   }

   public static void resetAll() {
      confirmedBotUuids.clear();
      loggedHiddenUuids.clear();
      knownBotSpots.clear();
      duplicateNameFlags.clear();
      duplicateCacheLevel = null;
      duplicateCacheLevelPlayerCount = -1;
      resetLevelTracking();
      receivedAttributesUuids.clear();
      addedPlayerInfoUuids.clear();
      receivedGameModeUuids.clear();
   }

   private static void resetLevelTracking() {
      watchedEntityIds.clear();
      receivedAttributesEntityIds.clear();
      swungArmEntityIds.clear();
      critHitEntityIds.clear();
      hitByLocalPlayerEntityIds.clear();
      maxDistanceSqByEntityId.clear();
      airGroundViolationByEntityId.clear();
      lastServerYByEntityId.clear();
      watchedLevel = MC.level;
   }

   private static void ensureCurrentLevel() {
      if (watchedLevel != MC.level) {
         resetLevelTracking();
      }
   }

   private static boolean isWatchedEntity(int id) {
      return watchedLevel == MC.level && watchedEntityIds.contains(id);
   }

   public static void onPlayerSpawnPacket(int id) {
      if (getActiveModule() != null && MC.level != null && MC.player != null) {
         ensureCurrentLevel();
         if (MC.level.getEntity(id) instanceof Player p && p != MC.player) {
            untrackEntity(id);
            if (watchedEntityIds.size() < 4096) {
               watchedEntityIds.add(id);
               maxDistanceSqByEntityId.put(id, MC.player.distanceToSqr(p));
            }
         }
      }
   }

   public static void onRemoveEntitiesPacket(IntList ids) {
      if (!watchedEntityIds.isEmpty() || !airGroundViolationByEntityId.isEmpty() || !lastServerYByEntityId.isEmpty()) {
         for (int i = 0; i < ids.size(); i++) {
            untrackEntity(ids.getInt(i));
         }
      }
   }

   private static void untrackEntity(int id) {
      watchedEntityIds.remove(id);
      receivedAttributesEntityIds.remove(id);
      swungArmEntityIds.remove(id);
      critHitEntityIds.remove(id);
      hitByLocalPlayerEntityIds.remove(id);
      maxDistanceSqByEntityId.remove(id);
      airGroundViolationByEntityId.remove(id);
      lastServerYByEntityId.remove(id);
   }

   public static void onUpdateAttributesPacket(int id) {
      if (getActiveModule() != null && watchedEntityIds.contains(id)) {
         receivedAttributesEntityIds.add(id);
         if (MC.level != null && MC.level.getEntity(id) instanceof Player p && receivedAttributesUuids.size() < 4096) {
            receivedAttributesUuids.add(p.getUUID());
         }
      }
   }

   public static void onAnimatePacket(int id, int action) {
      if (getActiveModule() != null && watchedEntityIds.contains(id)) {
         if (action == 0 || action == 3) {
            swungArmEntityIds.add(id);
         } else if (action == 4 || action == 5) {
            critHitEntityIds.add(id);
         }
      }
   }

   public static void onLocalPlayerAttackEntity(Entity target) {
      if (getActiveModule() != null && target instanceof Player && watchedEntityIds.contains(target.getId())) {
         hitByLocalPlayerEntityIds.add(target.getId());
      }
   }

   public static void onPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket packet) {
      playerInfoRevision++;
      if (getActiveModule() != null) {
         EnumSet<Action> actions = packet.actions();
         boolean added = actions.contains(Action.ADD_PLAYER);
         boolean gameMode = actions.contains(Action.UPDATE_GAME_MODE);

         for (Entry entry : packet.entries()) {
            UUID uuid = entry.profileId();
            if (added && addedPlayerInfoUuids.size() < 4096) {
               addedPlayerInfoUuids.add(uuid);
               receivedGameModeUuids.remove(uuid);
            }

            if (gameMode) {
               receivedGameModeUuids.add(uuid);
            }
         }
      }
   }

   public static void onPlayerInfoRemovePacket(List<UUID> uuids) {
      playerInfoRevision++;

      for (UUID uuid : uuids) {
         addedPlayerInfoUuids.remove(uuid);
         receivedGameModeUuids.remove(uuid);
      }
   }

   public static void onEntityMovePacket(Entity entity) {
      AntiBotModule m = getActiveModule();
      if (m != null
         && m.isCheckEnabled(m.floatingGround)
         && MC.level != null
         && entity instanceof Player p
         && p != MC.player
         && p.level() == MC.level) {
         int id = p.getId();
         if (!p.isPassenger() && !p.isSpectator() && !p.isFallFlying() && !p.isSleeping() && !p.onClimbable()) {
            Vec3 at = p.getPositionCodec().getBase();
            AABB box = p.getBoundingBox().move(at.x - p.getX(), at.y - p.getY(), at.z - p.getZ());
            if (MC.level.isLoaded(BlockPos.containing(box.minX, at.y, box.minZ))
               && MC.level.isLoaded(BlockPos.containing(box.maxX, at.y, box.maxZ))) {
               Double lastY = lastServerYByEntityId.put(id, at.y);
               if (lastServerYByEntityId.size() > 4096) {
                  lastServerYByEntityId.clear();
               }

               if (!intersectsAnyFluid(box) && (lastY == null || !(at.y < lastY - 0.001) || !p.onGround())) {
                  int vl = ArmorFilterMatcher.groundViolation(airGroundViolationByEntityId.getOrDefault(id, 0), p.onGround(), hasSupportingCollisionBelow(p, box));
                  if (vl <= 0) {
                     airGroundViolationByEntityId.remove(id);
                  } else if (airGroundViolationByEntityId.size() < 4096 || airGroundViolationByEntityId.containsKey(id)) {
                     airGroundViolationByEntityId.put(id, vl);
                  }
               }
            }
         }
      }
   }

   private static boolean hasSupportingCollisionBelow(Player p, AABB box) {
      AABB slice = new AABB(box.minX - 0.03, box.minY - 0.3, box.minZ - 0.03, box.maxX + 0.03, box.minY + 0.001, box.maxZ + 0.03);
      CollisionContext context = CollisionContext.of(p);
      MutableBlockPos pos = new MutableBlockPos();

      for (int x = Mth.floor(slice.minX); x <= Mth.floor(slice.maxX); x++) {
         for (int z = Mth.floor(slice.minZ); z <= Mth.floor(slice.maxZ); z++) {
            for (int y = Mth.floor(slice.minY) - 1; y <= Mth.floor(slice.maxY); y++) {
               pos.set(x, y, z);
               BlockState state = MC.level.getBlockState(pos);
               if (!state.isAir()) {
                  VoxelShape shape = state.getCollisionShape(MC.level, pos, context);
                  if (!shape.isEmpty()) {
                     for (AABB part : shape.toAabbs()) {
                        if (part.move(x, y, z).intersects(slice)) {
                           return true;
                        }
                     }
                  }
               }
            }
         }
      }

      return !MC.level.getEntities(p, slice, other -> other.canBeCollidedWith(p)).isEmpty();
   }

   private static boolean intersectsAnyFluid(AABB box) {
      MutableBlockPos pos = new MutableBlockPos();

      for (int x = Mth.floor(box.minX); x <= Mth.floor(box.maxX); x++) {
         for (int y = Mth.floor(box.minY) - 1; y <= Mth.floor(box.maxY); y++) {
            for (int z = Mth.floor(box.minZ); z <= Mth.floor(box.maxZ); z++) {
               if (!MC.level.getFluidState(pos.set(x, y, z)).isEmpty()) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   private static boolean isMissingPlayerInfo(Player player) {
      ClientPacketListener conn = MC.getConnection();
      return conn == null ? false : player.tickCount >= 40 && conn.getPlayerInfo(player.getUUID()) == null;
   }

   private static Map<Integer, Integer> getDuplicateNameFlags() {
      ClientLevel level = MC.level;
      ClientPacketListener conn = MC.getConnection();
      if (level != null && conn != null) {
         if (level != duplicateCacheLevel || level.players().size() != duplicateCacheLevelPlayerCount || conn.getOnlinePlayers().size() != duplicateCacheOnlinePlayerCount || playerInfoRevision != duplicateCacheInfoRevision) {
            rebuildDuplicateNameCache();
         }

         return duplicateNameFlags;
      } else {
         return duplicateNameFlags;
      }
   }

   private static void rebuildDuplicateNameCache() {
      ClientLevel level = MC.level;
      ClientPacketListener conn = MC.getConnection();
      duplicateNameFlags.clear();
      if (level != null && conn != null) {
         duplicateCacheLevel = level;
         duplicateCacheLevelPlayerCount = level.players().size();
         duplicateCacheOnlinePlayerCount = conn.getOnlinePlayers().size();
         duplicateCacheInfoRevision = playerInfoRevision;
         Map<String, List<Player>> entitiesByName = new HashMap<>();

         for (Player p : level.players()) {
            if (p != MC.player) {
               GameProfile profile = p.getGameProfile();
               if (profile != null && profile.name() != null) {
                  entitiesByName.computeIfAbsent(profile.name(), k -> new ArrayList<>()).add(p);
               }
            }
         }

         if (!entitiesByName.isEmpty()) {
            List<ArmorFilterMatcher.Identity> identities = new ArrayList<>();
            Set<UUID> withEntity = new HashSet<>();
            Collection<PlayerInfo> listed = conn.getListedOnlinePlayers();

            for (List<Player> players : entitiesByName.values()) {
               for (Player p : players) {
                  withEntity.add(p.getUUID());
                  identities.add(buildPlayerIdentitySnapshot(p.getUUID(), p.getGameProfile().name(), p.getId(), conn.getPlayerInfo(p.getUUID()), listed));
               }
            }

            GameProfile self = MC.player.getGameProfile();
            boolean selfNamed = self != null && entitiesByName.containsKey(self.name());
            if (selfNamed) {
               identities.add(new ArmorFilterMatcher.Identity(self.id(), self.name(), -1, true, true, true, true, false));
            }

            for (PlayerInfo info : conn.getOnlinePlayers()) {
               GameProfile profile = info.getProfile();
               if (profile != null
                  && profile.name() != null
                  && !withEntity.contains(profile.id())
                  && (!selfNamed || !profile.id().equals(self.id()))
                  && entitiesByName.containsKey(profile.name())) {
                  identities.add(buildPlayerIdentitySnapshot(profile.id(), profile.name(), -1, info, listed));
               }
            }

            duplicateNameFlags.putAll(ArmorFilterMatcher.duplicateNameFlags(identities));
         }
      } else {
         duplicateCacheLevel = null;
         duplicateCacheLevelPlayerCount = -1;
      }
   }

   private static ArmorFilterMatcher.Identity buildPlayerIdentitySnapshot(UUID uuid, String name, int entityId, PlayerInfo info, Collection<PlayerInfo> listed) {
      boolean proven = info != null && info.hasVerifiableChat();
      boolean isListed = info != null && listed.contains(info);
      boolean gameMode = info != null && receivedGameModeUuids.contains(uuid);
      boolean latency = info != null && info.getLatency() > 0;
      boolean known = confirmedBotUuids.contains(uuid) || ArmorFilterMatcher.isNpcUuid(uuid);
      return new ArmorFilterMatcher.Identity(uuid, name, entityId, proven, isListed, gameMode, latency, known);
   }
}
