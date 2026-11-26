package autismclient.modules;

import autismclient.AutismClientAddon;
import autismclient.util.bO;
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

public final class ByteReader {
   private static final Minecraft init_Minecraft = Minecraft.getInstance();
   private static final int init_I = 40;
   private static final int load_I = 40;
   private static final double init_D = 0.3;
   private static final double load_D = 0.03;
   private static final Set<UUID> init_Set = new HashSet<>();
   private static final Set<UUID> load_Set = new HashSet<>();
   private static final Map<Long, double[]> init_Map = new HashMap<>();
   private static final int read_I = 4096;
   private static final int write_I = 512;
   private static final double read_D = 0.15;
   private static final double write_D = 0.02;
   private static final Map<Integer, Integer> load_Map = new HashMap<>();
   private static Object init_Obj;
   private static int update_I = -1;
   private static int sync_I = -1;
   private static int flush_I = -1;
   private static int close_I;
   private static final Set<Integer> read_Set = new HashSet<>();
   private static final Set<Integer> write_Set = new HashSet<>();
   private static final Set<Integer> update_Set = new HashSet<>();
   private static final Set<Integer> sync_Set = new HashSet<>();
   private static final Set<Integer> flush_Set = new HashSet<>();
   private static final Map<Integer, Double> read_Map = new HashMap<>();
   private static final Map<Integer, Integer> write_Map = new HashMap<>();
   private static final Map<Integer, Double> update_Map = new HashMap<>();
   private static final Set<UUID> close_Set = new HashSet<>();
   private static final Set<UUID> open_Set = new HashSet<>();
   private static final Set<UUID> poll = new HashSet<>();
   private static Object load_Obj;
   private static AntiBotModule init_NetHelper;
   private static int open_I = -1;

   private ByteReader() {
   }

   private static AntiBotModule init_NetHelper() {
      int rev = al.init_I();
      if (rev != open_I) {
         Module m = al.init_Module_Str("antibot");
         init_NetHelper = m instanceof AntiBotModule a && m.isEnabled() ? a : null;
         open_I = rev;
      }

      return init_NetHelper;
   }

   public static boolean init_Z(Entity entity) {
      AntiBotModule m = init_NetHelper();
      if (m == null) {
         return false;
      }

      if (entity instanceof Player player && init_Minecraft.player != null && player != init_Minecraft.player) {
         UUID uuid = player.getUUID();
         if (init_Z(player)) {
            if (init_Set.remove(uuid)) {
               load_Set.remove(uuid);
            }

            return false;
         } else if (init_Set.contains(uuid)) {
            return true;
         } else {
            String reason = init_Str(player);
            if (reason != null) {
               init_V(uuid);
               init_V(player);
               init(player, reason);
               return true;
            } else if (read(player)) {
               init_V(player);
               init(player, "no player-info entry");
               return true;
            } else {
               String live = init_Str(player, m);
               if (live != null) {
                  init(player, live);
                  return true;
               } else if (load(player)) {
                  init(player, "known bot location");
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

   private static boolean init_Z(Player player) {
      ClientPacketListener conn = init_Minecraft.getConnection();
      if (conn == null) {
         return false;
      }

      PlayerInfo info = conn.getPlayerInfo(player.getUUID());
      return info != null && info.hasVerifiableChat();
   }

   private static void init_V(UUID uuid) {
      if (init_Set.size() >= 4096) {
         init_Set.clear();
      }

      init_Set.add(uuid);
   }

   private static void init_V(Player player) {
      if (init_Map.size() >= 512) {
         init_Map.clear();
      }

      init_Map.put(player.blockPosition().asLong(), new double[]{player.getX(), player.getY(), player.getZ()});
   }

   private static boolean load(Player player) {
      if (init_Map.isEmpty()) {
         return false;
      }

      double moved = Math.abs(player.getX() - player.xOld) + Math.abs(player.getZ() - player.zOld);
      if (moved > 0.02) {
         return false;
      }

      double[] spot = init_Map.get(player.blockPosition().asLong());
      if (spot == null) {
         return false;
      }

      double dx = player.getX() - spot[0];
      double dy = player.getY() - spot[1];
      double dz = player.getZ() - spot[2];
      return dx * dx + dy * dy + dz * dz <= 0.0225;
   }

   private static String init_Str(Player player) {
      if (init_Map().getOrDefault(player.getId(), 0) == 2) {
         return "clone of a verified player's name";
      } else {
         float pitch = player.getXRot();
         if (Math.abs(pitch) > 90.0F) {
            return "impossible pitch " + pitch;
         } else {
            GameProfile profile = player.getGameProfile();
            if (profile != null && bO.load_Z(profile.name())) {
               return ConfigStore.init_Z_UUID(player.getUUID()) ? "NPC uuid (version 2)" : null;
            } else {
               return "invalid profile name '" + (profile == null ? "<null>" : profile.name()) + "'";
            }
         }
      }
   }

   private static String init_Str(Player player, AntiBotModule m) {
      int id = player.getId();
      if (init_Map().getOrDefault(id, 0) == 1) {
         return "duplicate name";
      }

      if (m.init_Z(m.init_BoolSetting) && write_Map.getOrDefault(id, 0) >= m.init_I_8()) {
         return "on ground over air";
      }

      boolean watched = init_Z(id);
      UUID uuid = player.getUUID();
      if (m.init_Z(m.load_BoolSetting) && watched && player.tickCount >= 40 && !write_Set.contains(id) && !close_Set.contains(uuid)) {
         return "no attributes";
      }

      if (m.init_Z(m.read_BoolSetting) && player.tickCount >= 40 && open_Set.contains(uuid) && !poll.contains(uuid)) {
         return "no game mode";
      }

      if (!m.init_Z_13()) {
         return null;
      }

      if (m.write_BoolSetting.get() && player.tickCount < m.load_IntSetting.get()) {
         return "too young";
      }

      if (m.update_BoolSetting.get() && watched) {
         double range = m.init_DoubleSetting.get();
         if (read_Map.getOrDefault(id, 1.7976931348623157E308) <= range * range) {
            return "always near";
         }
      }

      if (m.sync.get() && watched && !flush_Set.contains(id)) {
         return "never hit";
      } else if (m.flush.get() && watched && !update_Set.contains(id)) {
         return "never swung";
      } else if (m.close.get() && watched && !sync_Set.contains(id)) {
         return "never crit-hit";
      } else if (m.open.get() && Math.abs(player.getAttributeValue(Attributes.SCALE) - 1.0) > 1.0E-4) {
         return "odd scale";
      } else {
         return m.poll.get() && !init_Z(player, m) ? "armor rule" : null;
      }
   }

   private static boolean init_Z(Player player, AntiBotModule m) {
      return ConfigStore.init_Z_Str_Str(m.load_ChoiceSetting.get(), init(player.getItemBySlot(EquipmentSlot.HEAD)))
         && ConfigStore.init_Z_Str_Str(m.read_ChoiceSetting.get(), init(player.getItemBySlot(EquipmentSlot.CHEST)))
         && ConfigStore.init_Z_Str_Str(m.write_ChoiceSetting.get(), init(player.getItemBySlot(EquipmentSlot.LEGS)))
         && ConfigStore.init_Z_Str_Str(m.update_ChoiceSetting.get(), init(player.getItemBySlot(EquipmentSlot.FEET)));
   }

   private static String init(ItemStack stack) {
      return stack != null && !stack.isEmpty() ? BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath() : "";
   }

   private static void init(Player player, String reason) {
      if (load_Set.size() >= 4096) {
         load_Set.clear();
      }

      if (load_Set.add(player.getUUID())) {
         GameProfile p = player.getGameProfile();
         AutismClientAddon.LOG.debug("[antibot] hiding player id={} name='{}' — {}", player.getId(), p == null ? "?" : p.name(), reason);
      }
   }

   public static boolean load_Z(Entity entity) {
      return init_Z(entity);
   }

   public static boolean init_Z(UUID uuid) {
      return uuid != null && init_Set.contains(uuid);
   }

   public static void init_V() {
      AntiBotModule m = init_NetHelper();
      if (m != null && init_Minecraft.level != null && init_Minecraft.player != null) {
         write();
         update();
         if (!read_Set.isEmpty()) {
            for (Player p : init_Minecraft.level.players()) {
               if (p != init_Minecraft.player && read_Set.contains(p.getId())) {
                  read_Map.merge(p.getId(), init_Minecraft.player.distanceToSqr(p), Math::max);
               }
            }
         }
      } else {
         load();
      }
   }

   public static void load() {
      init_Set.clear();
      load_Set.clear();
      init_Map.clear();
      load_Map.clear();
      init_Obj = null;
      update_I = -1;
      read();
      close_Set.clear();
      open_Set.clear();
      poll.clear();
   }

   private static void read() {
      read_Set.clear();
      write_Set.clear();
      update_Set.clear();
      sync_Set.clear();
      flush_Set.clear();
      read_Map.clear();
      write_Map.clear();
      update_Map.clear();
      load_Obj = init_Minecraft.level;
   }

   private static void write() {
      if (load_Obj != init_Minecraft.level) {
         read();
      }
   }

   private static boolean init_Z(int id) {
      return load_Obj == init_Minecraft.level && read_Set.contains(id);
   }

   public static void init_V(int id) {
      if (init_NetHelper() != null && init_Minecraft.level != null && init_Minecraft.player != null) {
         write();
         if (init_Minecraft.level.getEntity(id) instanceof Player p && p != init_Minecraft.player) {
            read(id);
            if (read_Set.size() < 4096) {
               read_Set.add(id);
               read_Map.put(id, init_Minecraft.player.distanceToSqr(p));
            }
         }
      }
   }

   public static void init(IntList ids) {
      if (!read_Set.isEmpty() || !write_Map.isEmpty() || !update_Map.isEmpty()) {
         for (int i = 0; i < ids.size(); i++) {
            read(ids.getInt(i));
         }
      }
   }

   private static void read(int id) {
      read_Set.remove(id);
      write_Set.remove(id);
      update_Set.remove(id);
      sync_Set.remove(id);
      flush_Set.remove(id);
      read_Map.remove(id);
      write_Map.remove(id);
      update_Map.remove(id);
   }

   public static void load(int id) {
      if (init_NetHelper() != null && read_Set.contains(id)) {
         write_Set.add(id);
         if (init_Minecraft.level != null && init_Minecraft.level.getEntity(id) instanceof Player p && close_Set.size() < 4096) {
            close_Set.add(p.getUUID());
         }
      }
   }

   public static void init(int id, int action) {
      if (init_NetHelper() != null && read_Set.contains(id)) {
         if (action == 0 || action == 3) {
            update_Set.add(id);
         } else if (action == 4 || action == 5) {
            sync_Set.add(id);
         }
      }
   }

   public static void init_V(Entity target) {
      if (init_NetHelper() != null && target instanceof Player && read_Set.contains(target.getId())) {
         flush_Set.add(target.getId());
      }
   }

   public static void init(ClientboundPlayerInfoUpdatePacket packet) {
      close_I++;
      if (init_NetHelper() != null) {
         EnumSet<Action> actions = packet.actions();
         boolean added = actions.contains(Action.ADD_PLAYER);
         boolean gameMode = actions.contains(Action.UPDATE_GAME_MODE);

         for (Entry entry : packet.entries()) {
            UUID uuid = entry.profileId();
            if (added && open_Set.size() < 4096) {
               open_Set.add(uuid);
               poll.remove(uuid);
            }

            if (gameMode) {
               poll.add(uuid);
            }
         }
      }
   }

   public static void init(List<UUID> uuids) {
      close_I++;

      for (UUID uuid : uuids) {
         open_Set.remove(uuid);
         poll.remove(uuid);
      }
   }

   public static void load_V(Entity entity) {
      AntiBotModule m = init_NetHelper();
      if (m != null
         && m.init_Z(m.init_BoolSetting)
         && init_Minecraft.level != null
         && entity instanceof Player p
         && p != init_Minecraft.player
         && p.level() == init_Minecraft.level) {
         int id = p.getId();
         if (!p.isPassenger() && !p.isSpectator() && !p.isFallFlying() && !p.isSleeping() && !p.onClimbable()) {
            Vec3 at = p.getPositionCodec().getBase();
            AABB box = p.getBoundingBox().move(at.x - p.getX(), at.y - p.getY(), at.z - p.getZ());
            if (init_Minecraft.level.isLoaded(BlockPos.containing(box.minX, at.y, box.minZ))
               && init_Minecraft.level.isLoaded(BlockPos.containing(box.maxX, at.y, box.maxZ))) {
               Double lastY = update_Map.put(id, at.y);
               if (update_Map.size() > 4096) {
                  update_Map.clear();
               }

               if (!init(box) && (lastY == null || !(at.y < lastY - 0.001) || !p.onGround())) {
                  int vl = ConfigStore.init_I_I(write_Map.getOrDefault(id, 0), p.onGround(), init(p, box));
                  if (vl <= 0) {
                     write_Map.remove(id);
                  } else if (write_Map.size() < 4096 || write_Map.containsKey(id)) {
                     write_Map.put(id, vl);
                  }
               }
            }
         }
      }
   }

   private static boolean init(Player p, AABB box) {
      AABB slice = new AABB(box.minX - 0.03, box.minY - 0.3, box.minZ - 0.03, box.maxX + 0.03, box.minY + 0.001, box.maxZ + 0.03);
      CollisionContext context = CollisionContext.of(p);
      MutableBlockPos pos = new MutableBlockPos();

      for (int x = Mth.floor(slice.minX); x <= Mth.floor(slice.maxX); x++) {
         for (int z = Mth.floor(slice.minZ); z <= Mth.floor(slice.maxZ); z++) {
            for (int y = Mth.floor(slice.minY) - 1; y <= Mth.floor(slice.maxY); y++) {
               pos.set(x, y, z);
               BlockState state = init_Minecraft.level.getBlockState(pos);
               if (!state.isAir()) {
                  VoxelShape shape = state.getCollisionShape(init_Minecraft.level, pos, context);
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

      return !init_Minecraft.level.getEntities(p, slice, other -> other.canBeCollidedWith(p)).isEmpty();
   }

   private static boolean init(AABB box) {
      MutableBlockPos pos = new MutableBlockPos();

      for (int x = Mth.floor(box.minX); x <= Mth.floor(box.maxX); x++) {
         for (int y = Mth.floor(box.minY) - 1; y <= Mth.floor(box.maxY); y++) {
            for (int z = Mth.floor(box.minZ); z <= Mth.floor(box.maxZ); z++) {
               if (!init_Minecraft.level.getFluidState(pos.set(x, y, z)).isEmpty()) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   private static boolean read(Player player) {
      ClientPacketListener conn = init_Minecraft.getConnection();
      return conn == null ? false : player.tickCount >= 40 && conn.getPlayerInfo(player.getUUID()) == null;
   }

   private static Map<Integer, Integer> init_Map() {
      ClientLevel level = init_Minecraft.level;
      ClientPacketListener conn = init_Minecraft.getConnection();
      if (level != null && conn != null) {
         if (level != init_Obj || level.players().size() != update_I || conn.getOnlinePlayers().size() != sync_I || close_I != flush_I) {
            update();
         }

         return load_Map;
      } else {
         return load_Map;
      }
   }

   private static void update() {
      ClientLevel level = init_Minecraft.level;
      ClientPacketListener conn = init_Minecraft.getConnection();
      load_Map.clear();
      if (level != null && conn != null) {
         init_Obj = level;
         update_I = level.players().size();
         sync_I = conn.getOnlinePlayers().size();
         flush_I = close_I;
         Map<String, List<Player>> entitiesByName = new HashMap<>();

         for (Player p : level.players()) {
            if (p != init_Minecraft.player) {
               GameProfile profile = p.getGameProfile();
               if (profile != null && profile.name() != null) {
                  entitiesByName.computeIfAbsent(profile.name(), k -> new ArrayList<>()).add(p);
               }
            }
         }

         if (!entitiesByName.isEmpty()) {
            List<ConfigStore.Inner_DataUtil> identities = new ArrayList<>();
            Set<UUID> withEntity = new HashSet<>();
            Collection<PlayerInfo> listed = conn.getListedOnlinePlayers();

            for (List<Player> players : entitiesByName.values()) {
               for (Player p : players) {
                  withEntity.add(p.getUUID());
                  identities.add(init(p.getUUID(), p.getGameProfile().name(), p.getId(), conn.getPlayerInfo(p.getUUID()), listed));
               }
            }

            GameProfile self = init_Minecraft.player.getGameProfile();
            boolean selfNamed = self != null && entitiesByName.containsKey(self.name());
            if (selfNamed) {
               identities.add(new ConfigStore.Inner_DataUtil(self.id(), self.name(), -1, true, true, true, true, false));
            }

            for (PlayerInfo info : conn.getOnlinePlayers()) {
               GameProfile profile = info.getProfile();
               if (profile != null
                  && profile.name() != null
                  && !withEntity.contains(profile.id())
                  && (!selfNamed || !profile.id().equals(self.id()))
                  && entitiesByName.containsKey(profile.name())) {
                  identities.add(init(profile.id(), profile.name(), -1, info, listed));
               }
            }

            load_Map.putAll(ConfigStore.init_Map_List(identities));
         }
      } else {
         init_Obj = null;
         update_I = -1;
      }
   }

   private static ConfigStore.Inner_DataUtil init(UUID uuid, String name, int entityId, PlayerInfo info, Collection<PlayerInfo> listed) {
      boolean proven = info != null && info.hasVerifiableChat();
      boolean isListed = info != null && listed.contains(info);
      boolean gameMode = info != null && poll.contains(uuid);
      boolean latency = info != null && info.getLatency() > 0;
      boolean known = init_Set.contains(uuid) || ConfigStore.init_Z_UUID(uuid);
      return new ConfigStore.Inner_DataUtil(uuid, name, entityId, proven, isListed, gameMode, latency, known);
   }
}
