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

public final class PacketStateDeltaTracker {
   private static final Minecraft MINECRAFT = Minecraft.getInstance();
   public static final PacketStateDeltaTracker.DeltaCapture EMPTY_CAPTURE = new PacketStateDeltaTracker.DeltaCapture(
      PacketStateDeltaTracker.StateSnapshot.EMPTY, PacketStateDeltaTracker.StateSnapshot.EMPTY, List.of(), false
   );
   private boolean init_Z;
   private int containerId = -1;
   private int stateId = -1;
   private String menuType = "";
   private String menuTitle = "";
   private int totalSlots = -1;
   private int inventoryStart = -1;
   private String cursorItem = "unknown";
   private int selectedSlot = -1;
   private PacketStateDeltaTracker.PlayerPose init_bq_StreamUtil;
   private PacketStateDeltaTracker.PlayerPose load_bq_StreamUtil;
   private int lastPositionId = -1;
   private String dimension = "";
   private Integer chunkCenterX;
   private Integer chunkCenterZ;
   private Integer chunkRadius;
   private Integer simulationDistance;
   private String phase = "unknown";
   private final Map<Integer, String> containerSlots = new LinkedHashMap<>();
   private final Map<Integer, String> load_Map = new LinkedHashMap<>();

   public synchronized void reset() {
      this.init_Z = false;
      this.containerId = -1;
      this.stateId = -1;
      this.menuType = "";
      this.menuTitle = "";
      this.totalSlots = -1;
      this.inventoryStart = -1;
      this.cursorItem = "unknown";
      this.selectedSlot = -1;
      this.init_bq_StreamUtil = null;
      this.load_bq_StreamUtil = null;
      this.lastPositionId = -1;
      this.dimension = "";
      this.chunkCenterX = null;
      this.chunkCenterZ = null;
      this.chunkRadius = null;
      this.simulationDistance = null;
      this.phase = "unknown";
      this.containerSlots.clear();
      this.load_Map.clear();
   }

   public synchronized PacketStateDeltaTracker.DeltaCapture capture(Packet<?> packet, String direction) {
      if (!isTrackedPacket(packet)) {
         return EMPTY_CAPTURE;
      }

      this.capturePoses();
      PacketStateDeltaTracker.StateSnapshot before = this.snapshot();
      List<String> changes = new ArrayList<>();
      this.applyPacket(packet, direction == null ? "" : direction.toUpperCase(Locale.ROOT), changes);
      PacketStateDeltaTracker.StateSnapshot after = this.snapshot();
      this.init_Z = true;
      return new PacketStateDeltaTracker.DeltaCapture(before, after, List.copyOf(changes), true);
   }

   public static boolean isTrackedPacket(Packet<?> packet) {
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

   private void applyPacket(Packet<?> packet, String direction, List<String> changes) {
      if (packet instanceof ClientboundLoginPacket login) {
         this.resetMenuState();
         this.phase = "play";
         this.dimension = String.valueOf(login.commonPlayerSpawnInfo().dimension().identifier());
         this.chunkRadius = login.chunkRadius();
         this.simulationDistance = login.simulationDistance();
         changes.add("Entered world " + this.dimension + " as entity #" + login.playerId());
      } else if (packet instanceof ClientboundRespawnPacket respawn) {
         this.resetMenuState();
         this.phase = "play";
         this.dimension = String.valueOf(respawn.commonPlayerSpawnInfo().dimension().identifier());
         changes.add("Respawned / switched world to " + this.dimension);
      } else if (packet instanceof ClientboundTransferPacket transfer) {
         this.resetMenuState();
         this.phase = "transfer " + transfer.host() + ":" + transfer.port();
         changes.add("Server transfer to " + transfer.host() + ":" + transfer.port());
      } else if (packet instanceof ClientboundStartConfigurationPacket) {
         this.phase = "configuration";
         changes.add("Server moved connection into configuration phase");
      } else if (packet instanceof ClientboundFinishConfigurationPacket) {
         this.phase = "configuration finished";
         changes.add("Configuration phase finished");
      } else if (packet instanceof ClientboundDisconnectPacket) {
         changes.add("Disconnected; tracked packet context cleared");
         this.reset();
      } else if (packet instanceof ClientboundOpenScreenPacket open) {
         this.containerId = open.getContainerId();
         this.stateId = -1;
         this.menuType = safeString(open.getType());
         this.menuTitle = open.getTitle() == null ? "" : open.getTitle().getString();
         this.totalSlots = -1;
         this.inventoryStart = -1;
         this.containerSlots.clear();
         changes.add("Opened container #" + this.containerId + " " + quote(this.menuTitle));
      } else if (packet instanceof ClientboundContainerSetContentPacket content) {
         this.containerId = content.containerId();
         this.stateId = content.stateId();
         this.totalSlots = content.items().size();
         this.inventoryStart = playerInventoryStart(this.containerId, this.totalSlots);
         this.containerSlots.clear();

         for (int i = 0; i < content.items().size(); i++) {
            this.containerSlots.put(i, describeStack(content.items().get(i)));
         }

         this.cursorItem = describeStack(content.carriedItem());
         changes.add("Synced " + this.totalSlots + " slots (" + countNonEmpty(content.items()) + " non-empty)");
      } else if (packet instanceof ClientboundContainerSetSlotPacket slot) {
         this.stateId = slot.getStateId();
         String previous = this.slotDescription(slot.getSlot());
         String next = describeStack(slot.getItem());
         if (slot.getContainerId() == -1) {
            this.cursorItem = next;
            changes.add("Cursor changed " + previous + " -> " + next);
         } else if (slot.getContainerId() == -2) {
            this.load_Map.put(slot.getSlot(), next);
            changes.add("Player inventory slot " + slot.getSlot() + " changed " + previous + " -> " + next);
         } else {
            if (this.containerId < 0) {
               this.containerId = slot.getContainerId();
            }

            this.containerSlots.put(slot.getSlot(), next);
            changes.add("Slot " + slot.getSlot() + " changed " + previous + " -> " + next);
         }
      } else if (packet instanceof ClientboundSetCursorItemPacket cursor) {
         String previous = this.cursorItem;
         this.cursorItem = describeStack(cursor.contents());
         changes.add("Cursor changed " + previous + " -> " + this.cursorItem);
      } else if (packet instanceof ClientboundSetPlayerInventoryPacket inventory) {
         String previous = this.load_Map.getOrDefault(inventory.slot(), "unknown");
         String next = describeStack(inventory.contents());
         this.load_Map.put(inventory.slot(), next);
         changes.add("Player inventory slot " + inventory.slot() + " changed " + previous + " -> " + next);
      } else if (packet instanceof ClientboundSetHeldSlotPacket held) {
         int previous = this.selectedSlot;
         this.selectedSlot = held.slot();
         changes.add("Held hotbar slot changed " + previous + " -> " + this.selectedSlot);
      } else if (packet instanceof ServerboundSetCarriedItemPacket carried) {
         int previous = this.selectedSlot;
         this.selectedSlot = carried.getSlot();
         changes.add("Client selected hotbar slot " + previous + " -> " + this.selectedSlot);
      } else if (packet instanceof ServerboundContainerClickPacket click) {
         this.containerId = click.containerId();
         this.stateId = click.stateId();
         this.recordChangedSlots(click.changedSlots());
         this.cursorItem = describeHashedStack(click.carriedItem());
         changes.add(
            "Client " + describeInput(click.containerInput()) + " on slot " + click.slotNum() + " changed " + click.changedSlots().size() + " slots"
         );
      } else if (packet instanceof ServerboundContainerClosePacket || packet instanceof ClientboundContainerClosePacket) {
         changes.add("Closed container #" + this.containerId);
         this.resetMenuState();
      } else if (packet instanceof ServerboundMovePlayerPacket move) {
         PacketStateDeltaTracker.PlayerPose before = this.init_bq_StreamUtil == null ? currentPlayerPose() : this.init_bq_StreamUtil;
         double x = move.getX(before == null ? 0.0 : before.x());
         double y = move.getY(before == null ? 0.0 : before.y());
         double z = move.getZ(before == null ? 0.0 : before.read());
         float yaw = move.getYRot(before == null ? 0.0F : before.yaw());
         float pitch = move.getXRot(before == null ? 0.0F : before.pitch());
         this.init_bq_StreamUtil = new PacketStateDeltaTracker.PlayerPose(x, y, z, yaw, pitch, move.hasPosition(), move.hasRotation());
         if (before != null) {
            changes.add("Client move delta " + format3(before.init(this.init_bq_StreamUtil)) + " blocks");
         }
      } else if (packet instanceof ClientboundPlayerPositionPacket position) {
         PacketStateDeltaTracker.PlayerPose base = this.load_bq_StreamUtil == null ? currentPlayerPose() : this.load_bq_StreamUtil;
         PacketStateDeltaTracker.PlayerPose next = applyRelative(base, position.change(), position.relatives());
         this.load_bq_StreamUtil = next;
         this.lastPositionId = position.id();
         changes.add("Server correction/teleport #" + this.lastPositionId + " -> " + formatPosition(next));
      } else if (packet instanceof ServerboundAcceptTeleportationPacket ack) {
         changes.add("Client acknowledged teleport #" + ack.id() + (ack.id() == this.lastPositionId ? " (matches pending)" : " (tracked pending " + this.lastPositionId + ")"));
         if (ack.id() == this.lastPositionId) {
            this.lastPositionId = -1;
         }
      } else if (packet instanceof ClientboundSetChunkCacheCenterPacket center) {
         this.chunkCenterX = center.getX();
         this.chunkCenterZ = center.getZ();
         changes.add("Chunk cache center -> " + this.chunkCenterX + ", " + this.chunkCenterZ);
      } else if (packet instanceof ClientboundSetChunkCacheRadiusPacket radius) {
         this.chunkRadius = radius.getRadius();
         changes.add("Chunk cache radius -> " + this.chunkRadius);
      } else if (packet instanceof ClientboundSetSimulationDistancePacket sim) {
         this.simulationDistance = sim.simulationDistance();
         changes.add("Simulation distance -> " + this.simulationDistance);
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

   private void capturePoses() {
      if (this.init_bq_StreamUtil == null) {
         this.init_bq_StreamUtil = currentPlayerPose();
      }

      if (this.load_bq_StreamUtil == null) {
         this.load_bq_StreamUtil = currentPlayerPose();
      }

      if (this.selectedSlot < 0 && MINECRAFT != null && MINECRAFT.player != null) {
         try {
            this.selectedSlot = MINECRAFT.player.getInventory().getSelectedSlot();
         } catch (Throwable var2) {
         }
      }
   }

   private PacketStateDeltaTracker.StateSnapshot snapshot() {
      return new PacketStateDeltaTracker.StateSnapshot(
         this.init_Z,
         this.containerId,
         this.stateId,
         this.menuType,
         this.menuTitle,
         this.totalSlots,
         this.inventoryStart,
         this.cursorItem,
         this.selectedSlot,
         this.init_bq_StreamUtil,
         this.load_bq_StreamUtil,
         this.lastPositionId,
         this.dimension,
         this.chunkCenterX,
         this.chunkCenterZ,
         this.chunkRadius,
         this.simulationDistance,
         this.phase,
         Collections.unmodifiableMap(new LinkedHashMap<>(this.containerSlots)),
         Collections.unmodifiableMap(new LinkedHashMap<>(this.load_Map))
      );
   }

   private void resetMenuState() {
      this.containerId = -1;
      this.stateId = -1;
      this.menuType = "";
      this.menuTitle = "";
      this.totalSlots = -1;
      this.inventoryStart = -1;
      this.cursorItem = "unknown";
      this.containerSlots.clear();
   }

   private void recordChangedSlots(Int2ObjectMap<HashedStack> changedSlots) {
      if (changedSlots != null) {
         for (Entry<HashedStack> entry : changedSlots.int2ObjectEntrySet()) {
            this.containerSlots.put(entry.getIntKey(), describeHashedStack(entry.getValue()));
         }
      }
   }

   private String slotDescription(int slot) {
      if (slot < 0) {
         return "outside";
      }

      String value = this.containerSlots.get(slot);
      return value != null ? value : this.load_Map.getOrDefault(slot, "unknown");
   }

   private static int playerInventoryStart(int containerId, int totalSlots) {
      if (totalSlots < 0) {
         return -1;
      } else if (containerId == 0) {
         return Math.max(0, totalSlots - 36);
      } else {
         return totalSlots >= 36 ? Math.max(0, totalSlots - 36) : -1;
      }
   }

   private static int countNonEmpty(List<ItemStack> stacks) {
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

   public static String describeStack(ItemStack stack) {
      if (stack != null && !stack.isEmpty()) {
         String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
         StringBuilder out = new StringBuilder();
         out.append(stack.getCount()).append('x').append(' ').append(id);

         try {
            String display = stack.getHoverName() == null ? "" : stack.getHoverName().getString();
            String vanilla = stack.getItem().getName(stack).getString();
            if (!display.isBlank() && !Objects.equals(display, vanilla)) {
               out.append(" [").append(truncate(display, 48)).append(']');
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

   public static String describeHashedStack(HashedStack stack) {
      if (stack == null || stack == HashedStack.EMPTY) {
         return "empty";
      } else if (stack instanceof ActualItem actual) {
         String id = actual.item().unwrapKey().map(key -> key.identifier().toString()).orElseGet(() -> safeString(actual.item()));
         return actual.count() + "x " + id + componentsSuffix(actual.components());
      } else {
         return safeString(stack);
      }
   }

   private static String componentsSuffix(Object components) {
      String value = safeString(components);
      return value != null && !value.isBlank() && !"HashedPatchMap[hashedPatches={}]".equals(value) ? " components" : "";
   }

   private static PacketStateDeltaTracker.PlayerPose applyRelative(PacketStateDeltaTracker.PlayerPose base, PositionMoveRotation change, Set<Relative> relatives) {
      Vec3 pos = change.position();
      Vec3 delta = change.deltaMovement();
      float yaw = change.yRot();
      float pitch = change.xRot();
      if (base != null && relatives != null) {
         if (relatives.contains(Relative.X)) {
            pos = new Vec3(base.x() + pos.x, pos.y, pos.z);
         }

         if (relatives.contains(Relative.Y)) {
            pos = new Vec3(pos.x, base.y() + pos.y, pos.z);
         }

         if (relatives.contains(Relative.Z)) {
            pos = new Vec3(pos.x, pos.y, base.read() + pos.z);
         }

         if (relatives.contains(Relative.Y_ROT)) {
            yaw += base.yaw();
         }

         if (relatives.contains(Relative.X_ROT)) {
            pitch += base.pitch();
         }
      }

      return new PacketStateDeltaTracker.PlayerPose(pos.x, pos.y, pos.z, yaw, pitch, true, true);
   }

   private static PacketStateDeltaTracker.PlayerPose currentPlayerPose() {
      if (MINECRAFT != null && MINECRAFT.player != null) {
         try {
            return new PacketStateDeltaTracker.PlayerPose(
               MINECRAFT.player.getX(),
               MINECRAFT.player.getY(),
               MINECRAFT.player.getZ(),
               MINECRAFT.player.getYRot(),
               MINECRAFT.player.getXRot(),
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

   private static String describeInput(ContainerInput input) {
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

   private static String format3(double value) {
      return String.format(Locale.ROOT, "%.3f", value);
   }

   public static String formatPosition(PacketStateDeltaTracker.PlayerPose position) {
      return position == null
         ? "unknown"
         : String.format(
            Locale.ROOT,
            "x=%.3f, y=%.3f, z=%.3f, yaw=%.2f, pitch=%.2f",
            position.x(),
            position.y(),
            position.read(),
            position.yaw(),
            position.pitch()
         );
   }

   public static String describeSlot(PacketStateDeltaTracker.StateSnapshot snapshot, int slot) {
      if (slot < 0) {
         return "outside / carried item area";
      }

      if (snapshot != null && snapshot.totalSlots() > 0 && snapshot.containerSlots() >= 0) {
         int containerSlots = snapshot.containerSlots();
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

   private static String quote(String value) {
      return value != null && !value.isBlank() ? "\"" + truncate(value, 72) + "\"" : "\"\"";
   }

   private static String truncate(String value, int max) {
      if (value == null) {
         return "";
      } else {
         return value.length() <= max ? value : value.substring(0, Math.max(0, max - 3)) + "...";
      }
   }

   private static String safeString(Object value) {
      if (value == null) {
         return "";
      }

      try {
         return String.valueOf(value);
      } catch (Throwable ignored) {
         return value.getClass().getSimpleName();
      }
   }

   public record StateSnapshot(
      boolean tracked,
      int containerId,
      int stateId,
      String menuType,
      String screenTitle,
      int totalSlots,
      int containerSlots,
      String carriedItem,
      int selectedSlot,
      PacketStateDeltaTracker.PlayerPose clientPosition,
      PacketStateDeltaTracker.PlayerPose serverPosition,
      int sync,
      String dimension,
      Integer chunkCenterX,
      Integer chunkCenterZ,
      Integer chunkRadius,
      Integer simulationDistance,
      String protocolState,
      Map<Integer, String> slotItems,
      Map<Integer, String> inventoryItems
   ) {
      public static final PacketStateDeltaTracker.StateSnapshot EMPTY = new PacketStateDeltaTracker.StateSnapshot(
         false, -1, -1, "", "", -1, -1, "unknown", -1, null, null, -1, "", null, null, null, null, "unknown", Map.of(), Map.of()
      );

      public StateSnapshot {
         menuType = menuType == null ? "" : menuType;
         screenTitle = screenTitle == null ? "" : screenTitle;
         carriedItem = carriedItem == null ? "unknown" : carriedItem;
         dimension = dimension == null ? "" : dimension;
         protocolState = protocolState == null ? "unknown" : protocolState;
         slotItems = slotItems == null ? Map.of() : Map.copyOf(slotItems);
         inventoryItems = inventoryItems == null ? Map.of() : Map.copyOf(inventoryItems);
      }

      public String itemAt(int slot) {
         if (slot < 0) {
            return "outside";
         }

         String value = this.slotItems.get(slot);
         return value != null ? value : this.inventoryItems.getOrDefault(slot, "unknown");
      }
   }

   public record DeltaCapture(PacketStateDeltaTracker.StateSnapshot before, PacketStateDeltaTracker.StateSnapshot load, List<String> changes, boolean captured) {
      public DeltaCapture {
         changes = changes == null ? List.of() : List.copyOf(changes);
         before = before == null ? PacketStateDeltaTracker.StateSnapshot.EMPTY : before;
         load = load == null ? PacketStateDeltaTracker.StateSnapshot.EMPTY : load;
      }
   }

   public record PlayerPose(double x, double y, double read, float yaw, float pitch, boolean hasPosition, boolean hasRotation) {
      public double init(PacketStateDeltaTracker.PlayerPose other) {
         if (other == null) {
            return 0.0;
         }

         double dx = this.x - other.x;
         double dy = this.y - other.y;
         double dz = this.read - other.read;
         return Math.sqrt(dx * dx + dy * dy + dz * dz);
      }
   }
}
