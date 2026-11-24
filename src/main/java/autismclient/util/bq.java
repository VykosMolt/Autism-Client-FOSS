package autismclient.util;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.HashedStack;
import net.minecraft.network.HashedStack.ActualItem;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundDisconnectPacket;
import net.minecraft.network.protocol.common.ClientboundResourcePackPopPacket;
import net.minecraft.network.protocol.common.ClientboundResourcePackPushPacket;
import net.minecraft.network.protocol.common.ClientboundTransferPacket;
import net.minecraft.network.protocol.configuration.ClientboundFinishConfigurationPacket;
import net.minecraft.network.protocol.game.ClientboundContainerClosePacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetDataPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundForgetLevelChunkPacket;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.network.protocol.game.ClientboundSetChunkCacheCenterPacket;
import net.minecraft.network.protocol.game.ClientboundSetChunkCacheRadiusPacket;
import net.minecraft.network.protocol.game.ClientboundSetCursorItemPacket;
import net.minecraft.network.protocol.game.ClientboundSetHeldSlotPacket;
import net.minecraft.network.protocol.game.ClientboundSetPlayerInventoryPacket;
import net.minecraft.network.protocol.game.ClientboundSetSimulationDistancePacket;
import net.minecraft.network.protocol.game.ClientboundStartConfigurationPacket;
import net.minecraft.network.protocol.game.ServerboundAcceptTeleportationPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public final class bq {
   private static final Minecraft init_Minecraft = Minecraft.getInstance();
   public static final bq.Inner_DataUtil init_bq_DataUtil = new bq.Inner_DataUtil(
      bq.Inner_CachePool.init_bq_CachePool, bq.Inner_CachePool.init_bq_CachePool, List.of(), false
   );
   private boolean init_Z;
   private int init_I = -1;
   private int load_I = -1;
   private String init_Str = "";
   private String load_Str = "";
   private int read_I = -1;
   private int write_I = -1;
   private String read_Str = "unknown";
   private int update_I = -1;
   private bq.Inner_StreamUtil init_bq_StreamUtil;
   private bq.Inner_StreamUtil load_bq_StreamUtil;
   private int sync = -1;
   private String write_Str = "";
   private Integer init_Integer;
   private Integer load_Integer;
   private Integer read_Integer;
   private Integer write_Integer;
   private String update_Str = "unknown";
   private final Map<Integer, String> init_Map = new LinkedHashMap<>();
   private final Map<Integer, String> load_Map = new LinkedHashMap<>();

   public synchronized void init_V() {
      this.init_Z = false;
      this.init_I = -1;
      this.load_I = -1;
      this.init_Str = "";
      this.load_Str = "";
      this.read_I = -1;
      this.write_I = -1;
      this.read_Str = "unknown";
      this.update_I = -1;
      this.init_bq_StreamUtil = null;
      this.load_bq_StreamUtil = null;
      this.sync = -1;
      this.write_Str = "";
      this.init_Integer = null;
      this.load_Integer = null;
      this.read_Integer = null;
      this.write_Integer = null;
      this.update_Str = "unknown";
      this.init_Map.clear();
      this.load_Map.clear();
   }

   public synchronized bq.Inner_DataUtil init_bq_DataUtil_Packet_Str(Packet<?> packet, String direction) {
      if (!init_Z_Packet(packet)) {
         return init_bq_DataUtil;
      }

      this.load();
      bq.Inner_CachePool before = this.init_bq_CachePool();
      List<String> changes = new ArrayList<>();
      this.init_V_Packet(packet, direction == null ? "" : direction.toUpperCase(Locale.ROOT), changes);
      bq.Inner_CachePool after = this.init_bq_CachePool();
      this.init_Z = true;
      return new bq.Inner_DataUtil(before, after, List.copyOf(changes), true);
   }

   public static boolean init_Z_Packet(Packet<?> packet) {
      return packet == null
         ? false
         : packet instanceof ClientboundOpenScreenPacket
            || packet instanceof ClientboundContainerClosePacket
            || packet instanceof ClientboundContainerSetContentPacket
            || packet instanceof ClientboundContainerSetSlotPacket
            || packet instanceof ClientboundContainerSetDataPacket
            || packet instanceof ClientboundSetCursorItemPacket
            || packet instanceof ClientboundSetPlayerInventoryPacket
            || packet instanceof ClientboundSetHeldSlotPacket
            || packet instanceof ServerboundSetCarriedItemPacket
            || packet instanceof ServerboundContainerClickPacket
            || packet instanceof ServerboundContainerClosePacket
            || packet instanceof ServerboundMovePlayerPacket
            || packet instanceof ClientboundPlayerPositionPacket
            || packet instanceof ServerboundAcceptTeleportationPacket
            || packet instanceof ClientboundLoginPacket
            || packet instanceof ClientboundRespawnPacket
            || packet instanceof ClientboundTransferPacket
            || packet instanceof ClientboundStartConfigurationPacket
            || packet instanceof ClientboundFinishConfigurationPacket
            || packet instanceof ClientboundSetChunkCacheCenterPacket
            || packet instanceof ClientboundSetChunkCacheRadiusPacket
            || packet instanceof ClientboundSetSimulationDistancePacket
            || packet instanceof ClientboundForgetLevelChunkPacket
            || packet instanceof ClientboundLevelChunkWithLightPacket
            || packet instanceof ClientboundGameEventPacket
            || packet instanceof ClientboundResourcePackPushPacket
            || packet instanceof ClientboundResourcePackPopPacket
            || packet instanceof ClientboundDisconnectPacket;
   }

   private void init_V_Packet(Packet<?> packet, String direction, List<String> changes) {
      if (packet instanceof ClientboundLoginPacket login) {
         this.read();
         this.update_Str = "play";
         this.write_Str = String.valueOf(login.commonPlayerSpawnInfo().dimension().identifier());
         this.read_Integer = login.chunkRadius();
         this.write_Integer = login.simulationDistance();
         changes.add("Entered world " + this.write_Str + " as entity #" + login.playerId());
      } else if (packet instanceof ClientboundRespawnPacket respawn) {
         this.read();
         this.update_Str = "play";
         this.write_Str = String.valueOf(respawn.commonPlayerSpawnInfo().dimension().identifier());
         changes.add("Respawned / switched world to " + this.write_Str);
      } else if (packet instanceof ClientboundTransferPacket transfer) {
         this.read();
         this.update_Str = "transfer " + transfer.host() + ":" + transfer.port();
         changes.add("Server transfer to " + transfer.host() + ":" + transfer.port());
      } else if (packet instanceof ClientboundStartConfigurationPacket) {
         this.update_Str = "configuration";
         changes.add("Server moved connection into configuration phase");
      } else if (packet instanceof ClientboundFinishConfigurationPacket) {
         this.update_Str = "configuration finished";
         changes.add("Configuration phase finished");
      } else if (packet instanceof ClientboundDisconnectPacket) {
         changes.add("Disconnected; tracked packet context cleared");
         this.init_V();
      } else if (packet instanceof ClientboundOpenScreenPacket open) {
         this.init_I = open.getContainerId();
         this.load_I = -1;
         this.init_Str = load(open.getType());
         this.load_Str = open.getTitle() == null ? "" : open.getTitle().getString();
         this.read_I = -1;
         this.write_I = -1;
         this.init_Map.clear();
         changes.add("Opened container #" + this.init_I + " " + init_Str_Str(this.load_Str));
      } else if (packet instanceof ClientboundContainerSetContentPacket content) {
         this.init_I = content.containerId();
         this.load_I = content.stateId();
         this.read_I = content.items().size();
         this.write_I = init_I_I_I(this.init_I, this.read_I);
         this.init_Map.clear();

         for (int i = 0; i < content.items().size(); i++) {
            this.init_Map.put(i, init_Str_ItemStack(content.items().get(i)));
         }

         this.read_Str = init_Str_ItemStack(content.carriedItem());
         changes.add("Synced " + this.read_I + " slots (" + init_I_List(content.items()) + " non-empty)");
      } else if (packet instanceof ClientboundContainerSetSlotPacket slot) {
         this.load_I = slot.getStateId();
         String previous = this.init_Str_I(slot.getSlot());
         String next = init_Str_ItemStack(slot.getItem());
         if (slot.getContainerId() == -1) {
            this.read_Str = next;
            changes.add("Cursor changed " + previous + " -> " + next);
         } else if (slot.getContainerId() == -2) {
            this.load_Map.put(slot.getSlot(), next);
            changes.add("Player inventory slot " + slot.getSlot() + " changed " + previous + " -> " + next);
         } else {
            if (this.init_I < 0) {
               this.init_I = slot.getContainerId();
            }

            this.init_Map.put(slot.getSlot(), next);
            changes.add("Slot " + slot.getSlot() + " changed " + previous + " -> " + next);
         }
      } else if (packet instanceof ClientboundSetCursorItemPacket cursor) {
         String previous = this.read_Str;
         this.read_Str = init_Str_ItemStack(cursor.contents());
         changes.add("Cursor changed " + previous + " -> " + this.read_Str);
      } else if (packet instanceof ClientboundSetPlayerInventoryPacket inventory) {
         String previous = this.load_Map.getOrDefault(inventory.slot(), "unknown");
         String next = init_Str_ItemStack(inventory.contents());
         this.load_Map.put(inventory.slot(), next);
         changes.add("Player inventory slot " + inventory.slot() + " changed " + previous + " -> " + next);
      } else if (packet instanceof ClientboundSetHeldSlotPacket held) {
         int previous = this.update_I;
         this.update_I = held.slot();
         changes.add("Held hotbar slot changed " + previous + " -> " + this.update_I);
      } else if (packet instanceof ServerboundSetCarriedItemPacket carried) {
         int previous = this.update_I;
         this.update_I = carried.getSlot();
         changes.add("Client selected hotbar slot " + previous + " -> " + this.update_I);
      } else if (packet instanceof ServerboundContainerClickPacket click) {
         this.init_I = click.containerId();
         this.load_I = click.stateId();
         this.init_V_Int2ObjectMap(click.changedSlots());
         this.read_Str = init_Str_HashedStack(click.carriedItem());
         changes.add(
            "Client " + init_Str_ContainerInput(click.containerInput()) + " on slot " + click.slotNum() + " changed " + click.changedSlots().size() + " slots"
         );
      } else if (packet instanceof ServerboundContainerClosePacket || packet instanceof ClientboundContainerClosePacket) {
         changes.add("Closed container #" + this.init_I);
         this.read();
      } else if (packet instanceof ServerboundMovePlayerPacket move) {
         bq.Inner_StreamUtil before = this.init_bq_StreamUtil == null ? init_bq_StreamUtil() : this.init_bq_StreamUtil;
         double x = move.getX(before == null ? 0.0 : before.init_D());
         double y = move.getY(before == null ? 0.0 : before.load_D());
         double z = move.getZ(before == null ? 0.0 : before.read());
         float yaw = move.getYRot(before == null ? 0.0F : before.init_F());
         float pitch = move.getXRot(before == null ? 0.0F : before.load_F());
         this.init_bq_StreamUtil = new bq.Inner_StreamUtil(x, y, z, yaw, pitch, move.hasPosition(), move.hasRotation());
         if (before != null) {
            changes.add("Client move delta " + init_Str_D(before.init(this.init_bq_StreamUtil)) + " blocks");
         }
      } else if (packet instanceof ClientboundPlayerPositionPacket position) {
         bq.Inner_StreamUtil base = this.load_bq_StreamUtil == null ? init_bq_StreamUtil() : this.load_bq_StreamUtil;
         bq.Inner_StreamUtil next = init_bq_StreamUtil_bq_StreamUtil(base, position.change(), position.relatives());
         this.load_bq_StreamUtil = next;
         this.sync = position.id();
         changes.add("Server correction/teleport #" + this.sync + " -> " + init_Str_bq_StreamUtil(next));
      } else if (packet instanceof ServerboundAcceptTeleportationPacket ack) {
         changes.add("Client acknowledged teleport #" + ack.id() + (ack.id() == this.sync ? " (matches pending)" : " (tracked pending " + this.sync + ")"));
         if (ack.id() == this.sync) {
            this.sync = -1;
         }
      } else if (packet instanceof ClientboundSetChunkCacheCenterPacket center) {
         this.init_Integer = center.getX();
         this.load_Integer = center.getZ();
         changes.add("Chunk cache center -> " + this.init_Integer + ", " + this.load_Integer);
      } else if (packet instanceof ClientboundSetChunkCacheRadiusPacket radius) {
         this.read_Integer = radius.getRadius();
         changes.add("Chunk cache radius -> " + this.read_Integer);
      } else if (packet instanceof ClientboundSetSimulationDistancePacket sim) {
         this.write_Integer = sim.simulationDistance();
         changes.add("Simulation distance -> " + this.write_Integer);
      } else if (packet instanceof ClientboundForgetLevelChunkPacket forget) {
         changes.add("Unload chunk " + forget.pos().x() + ", " + forget.pos().z());
      } else if (packet instanceof ClientboundLevelChunkWithLightPacket chunk) {
         changes.add("Load chunk " + chunk.x() + ", " + chunk.z() + " with light");
      } else if (packet instanceof ClientboundGameEventPacket gameEvent) {
         changes.add("Game event " + gameEvent.getEvent() + " value=" + gameEvent.getParam());
      } else if (packet instanceof ClientboundResourcePackPushPacket pack) {
         changes.add("Resource pack push " + pack.id() + " required=" + pack.required());
      } else if (packet instanceof ClientboundResourcePackPopPacket pack) {
         changes.add("Resource pack pop " + pack.id().map(Object::toString).orElse("latest"));
      }
   }

   private void load() {
      if (this.init_bq_StreamUtil == null) {
         this.init_bq_StreamUtil = init_bq_StreamUtil();
      }

      if (this.load_bq_StreamUtil == null) {
         this.load_bq_StreamUtil = init_bq_StreamUtil();
      }

      if (this.update_I < 0 && init_Minecraft != null && init_Minecraft.player != null) {
         try {
            this.update_I = init_Minecraft.player.getInventory().getSelectedSlot();
         } catch (Throwable var2) {
         }
      }
   }

   private bq.Inner_CachePool init_bq_CachePool() {
      return new bq.Inner_CachePool(
         this.init_Z,
         this.init_I,
         this.load_I,
         this.init_Str,
         this.load_Str,
         this.read_I,
         this.write_I,
         this.read_Str,
         this.update_I,
         this.init_bq_StreamUtil,
         this.load_bq_StreamUtil,
         this.sync,
         this.write_Str,
         this.init_Integer,
         this.load_Integer,
         this.read_Integer,
         this.write_Integer,
         this.update_Str,
         Collections.unmodifiableMap(new LinkedHashMap<>(this.init_Map)),
         Collections.unmodifiableMap(new LinkedHashMap<>(this.load_Map))
      );
   }

   private void read() {
      this.init_I = -1;
      this.load_I = -1;
      this.init_Str = "";
      this.load_Str = "";
      this.read_I = -1;
      this.write_I = -1;
      this.read_Str = "unknown";
      this.init_Map.clear();
   }

   private void init_V_Int2ObjectMap(Int2ObjectMap<HashedStack> changedSlots) {
      if (changedSlots != null) {
         for (Entry<HashedStack> entry : changedSlots.int2ObjectEntrySet()) {
            this.init_Map.put(entry.getIntKey(), init_Str_HashedStack(entry.getValue()));
         }
      }
   }

   private String init_Str_I(int slot) {
      if (slot < 0) {
         return "outside";
      }

      String value = this.init_Map.get(slot);
      return value != null ? value : this.load_Map.getOrDefault(slot, "unknown");
   }

   private static int init_I_I_I(int containerId, int totalSlots) {
      if (totalSlots < 0) {
         return -1;
      } else if (containerId == 0) {
         return Math.max(0, totalSlots - 36);
      } else {
         return totalSlots >= 36 ? Math.max(0, totalSlots - 36) : -1;
      }
   }

   private static int init_I_List(List<ItemStack> stacks) {
      int count = 0;
      if (stacks != null) {
         for (ItemStack stack : stacks) {
            if (stack != null && !stack.isEmpty()) {
               count++;
            }
         }
      }

      return count;
   }

   public static String init_Str_ItemStack(ItemStack stack) {
      if (stack != null && !stack.isEmpty()) {
         String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
         StringBuilder out = new StringBuilder();
         out.append(stack.getCount()).append('x').append(' ').append(id);

         try {
            String display = stack.getHoverName() == null ? "" : stack.getHoverName().getString();
            String vanilla = stack.getItem().getName(stack).getString();
            if (!display.isBlank() && !Objects.equals(display, vanilla)) {
               out.append(" [").append(init_Str_Str_I(display, 48)).append(']');
            }
         } catch (Throwable var6) {
         }

         try {
            if (stack.isDamageableItem()) {
               int max = stack.getMaxDamage();
               int damage = stack.getDamageValue();
               out.append(" dur=").append(Math.max(0, max - damage)).append('/').append(max);
            }
         } catch (Throwable var5) {
         }

         return out.toString();
      } else {
         return "empty";
      }
   }

   public static String init_Str_HashedStack(HashedStack stack) {
      if (stack == null || stack == HashedStack.EMPTY) {
         return "empty";
      } else if (stack instanceof ActualItem actual) {
         String id = actual.item().unwrapKey().map(key -> key.identifier().toString()).orElseGet(() -> load(actual.item()));
         return actual.count() + "x " + id + init_Str_Obj(actual.components());
      } else {
         return load(stack);
      }
   }

   private static String init_Str_Obj(Object components) {
      String value = load(components);
      return value != null && !value.isBlank() && !"HashedPatchMap[hashedPatches={}]".equals(value) ? " components" : "";
   }

   private static bq.Inner_StreamUtil init_bq_StreamUtil_bq_StreamUtil(bq.Inner_StreamUtil base, PositionMoveRotation change, Set<Relative> relatives) {
      Vec3 pos = change.position();
      Vec3 delta = change.deltaMovement();
      float yaw = change.yRot();
      float pitch = change.xRot();
      if (base != null && relatives != null) {
         if (relatives.contains(Relative.X)) {
            pos = new Vec3(base.init_D() + pos.x, pos.y, pos.z);
         }

         if (relatives.contains(Relative.Y)) {
            pos = new Vec3(pos.x, base.load_D() + pos.y, pos.z);
         }

         if (relatives.contains(Relative.Z)) {
            pos = new Vec3(pos.x, pos.y, base.read() + pos.z);
         }

         if (relatives.contains(Relative.Y_ROT)) {
            yaw += base.init_F();
         }

         if (relatives.contains(Relative.X_ROT)) {
            pitch += base.load_F();
         }
      }

      return new bq.Inner_StreamUtil(pos.x, pos.y, pos.z, yaw, pitch, true, true);
   }

   private static bq.Inner_StreamUtil init_bq_StreamUtil() {
      if (init_Minecraft != null && init_Minecraft.player != null) {
         try {
            return new bq.Inner_StreamUtil(
               init_Minecraft.player.getX(),
               init_Minecraft.player.getY(),
               init_Minecraft.player.getZ(),
               init_Minecraft.player.getYRot(),
               init_Minecraft.player.getXRot(),
               true,
               true
            );
         } catch (Throwable ignored) {
            return null;
         }
      } else {
         return null;
      }
   }

   private static String init_Str_ContainerInput(ContainerInput input) {
      if (input == null) {
         return "clicked";
      }

      String var10000;
      switch (input) {
         case PICKUP:
            var10000 = "picked up / placed";
            break;
         case QUICK_MOVE:
            var10000 = "quick-moved";
            break;
         case SWAP:
            var10000 = "hotbar-swapped";
            break;
         case CLONE:
            var10000 = "cloned";
            break;
         case THROW:
            var10000 = "threw";
            break;
         case QUICK_CRAFT:
            var10000 = "drag-crafted";
            break;
         case PICKUP_ALL:
            var10000 = "picked up all matching";
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   private static String init_Str_D(double value) {
      return String.format(Locale.ROOT, "%.3f", value);
   }

   public static String init_Str_bq_StreamUtil(bq.Inner_StreamUtil position) {
      return position == null
         ? "unknown"
         : String.format(
            Locale.ROOT,
            "x=%.3f, y=%.3f, z=%.3f, yaw=%.2f, pitch=%.2f",
            position.init_D(),
            position.load_D(),
            position.read(),
            position.init_F(),
            position.load_F()
         );
   }

   public static String init_Str_bq_CachePool_I(bq.Inner_CachePool snapshot, int slot) {
      if (slot < 0) {
         return "outside / carried item area";
      }

      if (snapshot != null && snapshot.read_I() > 0 && snapshot.write_I() >= 0) {
         int containerSlots = snapshot.write_I();
         if (slot < containerSlots) {
            return "container slot " + slot;
         } else {
            int playerRelative = slot - containerSlots;
            if (playerRelative >= 0 && playerRelative < 27) {
               return "player inventory slot " + (playerRelative + 9);
            } else {
               return playerRelative >= 27 && playerRelative < 36 ? "hotbar slot " + (playerRelative - 27) : "handler slot " + slot;
            }
         }
      } else {
         return "handler slot " + slot;
      }
   }

   private static String init_Str_Str(String value) {
      return value != null && !value.isBlank() ? "\"" + init_Str_Str_I(value, 72) + "\"" : "\"\"";
   }

   private static String init_Str_Str_I(String value, int max) {
      if (value == null) {
         return "";
      } else {
         return value.length() <= max ? value : value.substring(0, Math.max(0, max - 3)) + "...";
      }
   }

   private static String load(Object value) {
      if (value == null) {
         return "";
      }

      try {
         return String.valueOf(value);
      } catch (Throwable ignored) {
         return value.getClass().getSimpleName();
      }
   }

   public record Inner_CachePool(
      boolean init_Z,
      int init_I,
      int load_I,
      String init_Str,
      String load_Str,
      int read_I,
      int write_I,
      String read_Str,
      int update_I,
      bq.Inner_StreamUtil init_bq_StreamUtil,
      bq.Inner_StreamUtil load_bq_StreamUtil,
      int sync,
      String write_Str,
      Integer init_Integer,
      Integer load_Integer,
      Integer read_Integer,
      Integer write_Integer,
      String update_Str,
      Map<Integer, String> init_Map,
      Map<Integer, String> load_Map
   ) {
      public static final bq.Inner_CachePool init_bq_CachePool = new bq.Inner_CachePool(
         false, -1, -1, "", "", -1, -1, "unknown", -1, null, null, -1, "", null, null, null, null, "unknown", Map.of(), Map.of()
      );

      public Inner_CachePool {
         init_Str = init_Str == null ? "" : init_Str;
         load_Str = load_Str == null ? "" : load_Str;
         read_Str = read_Str == null ? "unknown" : read_Str;
         write_Str = write_Str == null ? "" : write_Str;
         update_Str = update_Str == null ? "unknown" : update_Str;
         init_Map = init_Map == null ? Map.of() : Map.copyOf(init_Map);
         load_Map = load_Map == null ? Map.of() : Map.copyOf(load_Map);
      }

      public String init(int slot) {
         if (slot < 0) {
            return "outside";
         }

         String value = this.init_Map.get(slot);
         return value != null ? value : this.load_Map.getOrDefault(slot, "unknown");
      }
   }

   public record Inner_DataUtil(bq.Inner_CachePool init_bq_CachePool, bq.Inner_CachePool load, List<String> init_List, boolean init_Z) {
      public Inner_DataUtil {
         init_List = init_List == null ? List.of() : List.copyOf(init_List);
         init_bq_CachePool = init_bq_CachePool == null ? bq.Inner_CachePool.init_bq_CachePool : init_bq_CachePool;
         load = load == null ? bq.Inner_CachePool.init_bq_CachePool : load;
      }
   }

   public record Inner_StreamUtil(double init_D, double load_D, double read, float init_F, float load_F, boolean init_Z, boolean load_Z) {
      public double init(bq.Inner_StreamUtil other) {
         if (other == null) {
            return 0.0;
         }

         double dx = this.init_D - other.init_D;
         double dy = this.load_D - other.load_D;
         double dz = this.read - other.read;
         return Math.sqrt(dx * dx + dy * dy + dz * dz);
      }
   }
}
