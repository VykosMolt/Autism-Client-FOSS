package autismclient.util;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.HashedStack;
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
import net.minecraft.network.protocol.game.ServerboundContainerButtonClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundSelectBundleItemPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public final class PacketDetailFormatter {
   private static final int init = 16;
   private static final int load = 12;

   private PacketDetailFormatter() {
   }

   public static PacketDetailFormatter.Inner_DataUtil init_bh_DataUtil_AutismPacketLogger(AutismPacketLoggerOverlay.LoggedPacketEntry entry) {
      PacketDetailFormatter.Inner_StreamUtil out = new PacketDetailFormatter.Inner_StreamUtil();
      if (entry == null) {
         return out.init(false, "fallback");
      }

      init_V_AutismPacketLogger_bh_StreamUtil(entry, out);
      PacketDetailFormatter.Inner_CachePool wireDecode = init_bh_CachePool_AutismPacketLogger(entry);
      Packet<?> packet = wireDecode.init_Packet != null ? wireDecode.init_Packet : entry.init_Packet;
      if (packet == null) {
         if (wireDecode.init_Str != null && !wireDecode.init_Str.isBlank()) {
            out.init("Wire Decode: " + wireDecode.init_Str, wireDecode.init_Z ? UiColorPalette.mint() : UiColorPalette.warningYellow());
         }

         return out.init(false, "fallback");
      } else {
         boolean complete = init_Z_Packet(packet, out, wireDecode);
         return out.init(complete, complete ? "complete" : (out.init() ? wireDecode.init_Str_2() : "fallback"));
      }
   }

   public static String init_Str_AutismPacketLogger(AutismPacketLoggerOverlay.LoggedPacketEntry entry) {
      PacketDetailFormatter.Inner_DataUtil analysis = init_bh_DataUtil_AutismPacketLogger(entry);
      if (!analysis.init_Z() && !analysis.load_Z()) {
         return "";
      }

      StringBuilder out = new StringBuilder();
      if (analysis.init_Z()) {
         out.append("[Reality]\n");
         init_V_StringBuilder_List(out, analysis.init_List());
      }

      if (analysis.load_Z()) {
         out.append("\n[Meaning]\n");
         init_V_StringBuilder_List(out, analysis.load_List());
      }

      return out.toString().stripTrailing();
   }

   private static void init_V_StringBuilder_List(StringBuilder out, List<PacketInspectorEngine.ColoredLine> lines) {
      for (PacketInspectorEngine.ColoredLine line : lines) {
         if (line != null) {
            out.append(line.init_Str()).append('\n');
         }
      }
   }

   private static PacketDetailFormatter.Inner_CachePool init_bh_CachePool_AutismPacketLogger(AutismPacketLoggerOverlay.LoggedPacketEntry entry) {
      if (entry == null || entry.init_Packet == null || entry.init_Class == null) {
         return PacketDetailFormatter.Inner_CachePool.init_bh_CachePool();
      }

      if (!Packet.class.isAssignableFrom(entry.init_Class)) {
         return PacketDetailFormatter.Inner_CachePool.init_bh_CachePool();
      }

      PacketWireCaptureStore.Inner_StreamUtil snapshot = PacketWireCaptureStore.init(entry.init_Packet);
      if (snapshot != null && snapshot.init_BArr().length != 0) {
         try {
            Class<? extends Packet<?>> packetClass = (Class<? extends Packet<?>>)entry.init_Class;
            PacketStreamCodecDecoder.Inner_CachePool decoded = PacketStreamCodecDecoder.init_bp_CachePool(packetClass, snapshot.init_BArr(), snapshot.load());
            return PacketDetailFormatter.Inner_CachePool.init(decoded.init_Packet(), "decoded from captured plaintext with Minecraft STREAM_CODEC");
         } catch (Throwable t) {
            return PacketDetailFormatter.Inner_CachePool.init("captured plaintext was kept, but STREAM_CODEC decode failed: " + load(t));
         }
      } else {
         return PacketDetailFormatter.Inner_CachePool.init_bh_CachePool();
      }
   }

   private static boolean init_Z_Packet(Packet<?> packet, PacketDetailFormatter.Inner_StreamUtil out, PacketDetailFormatter.Inner_CachePool wireDecode) {
      if (wireDecode != null && wireDecode.init_Str != null && !wireDecode.init_Str.isBlank()) {
         out.init("Wire Decode: " + wireDecode.init_Str, wireDecode.init_Z ? UiColorPalette.mint() : UiColorPalette.warningYellow());
      }

      PacketSchemaSnapshot view = PacketSchemaSnapshot.init_br_Packet(packet);
      init_V_br_bh_StreamUtil(view, out);
      init_V_Packet(packet, view, out);
      return view.read();
   }

   private static void init_V_br_bh_StreamUtil(PacketSchemaSnapshot view, PacketDetailFormatter.Inner_StreamUtil out) {
      if (view == null) {
         out.init("Status: fallback", UiColorPalette.warningYellow());
         out.init("No packet object was available.", UiColorPalette.errorText());
      } else {
         PacketSchemaRegistry.Inner_StreamUtil schema = view.init_bx_StreamUtil();
         out.init("Status: " + view.init_Str(), view.read() ? UiColorPalette.mint() : UiColorPalette.warningYellow());
         if (schema != null) {
            out.init("Schema: " + (view.load_Z() ? "Minecraft source-backed schema" : "reflection fallback"), UiColorPalette.textBright());
            out.init("Protocol: " + schema.read() + " | Direction: " + schema.write(), UiColorPalette.textBody());
            out.init("Codec: " + schema.update() + " | Source: " + schema.flush(), UiColorPalette.textBody());
            if (!schema.sync().isBlank()) {
               out.init("Packet Type: " + schema.sync(), UiColorPalette.textBody());
            }

            if (schema.load_Z()) {
               out.init("Field Note: using parent packet schema for nested packet variant", UiColorPalette.warningYellow());
            }
         } else {
            out.init("Schema: reflection fallback", UiColorPalette.warningYellow());
         }

         if (!view.load_Str().isBlank()) {
            out.init("Fallback Note: " + view.load_Str(), UiColorPalette.textMuted());
         }

         if (view.init_List().isEmpty()) {
            out.init("Fields: none", UiColorPalette.textMuted());
         } else {
            out.init("Fields: " + view.init_List().size(), UiColorPalette.textBright());

            for (PacketFieldSnapshot field : view.init_List()) {
               int color = field.init_Z() ? UiColorPalette.softWhite() : UiColorPalette.errorText();
               out.init(field.init_Str() + " (" + field.load_Str() + "): " + field.write(), color);

               for (String detail : field.init_List()) {
                  out.init("  " + detail, UiColorPalette.textBody());
               }
            }
         }
      }
   }

   private static void init_V_Packet(Packet<?> packet, PacketSchemaSnapshot view, PacketDetailFormatter.Inner_StreamUtil out) {
      if (packet instanceof ClientboundOpenScreenPacket open) {
         out.load("Server opens a handled screen.", UiColorPalette.textBright());
         out.load("Container: #" + open.getContainerId() + " type=" + load(open.getType()), UiColorPalette.blue());
         out.load("Title: " + init_Str_Str(open.getTitle() == null ? "" : open.getTitle().getString()), UiColorPalette.successText());
      } else if (packet instanceof ClientboundContainerSetContentPacket content) {
         out.load("Server replaces the full contents of a container.", UiColorPalette.textBright());
         out.load("Container: #" + content.containerId() + " state=" + content.stateId(), UiColorPalette.blue());
         out.load("Slots: " + content.items().size() + " total, " + init_I_Collection(content.items()) + " non-empty", UiColorPalette.textBright());
         out.load("Carried Item: " + PacketStateDeltaTracker.init_Str_ItemStack(content.carriedItem()), UiColorPalette.successText());
         init_V_List_bh_StreamUtil(content.items(), out);
      } else if (packet instanceof ClientboundContainerSetSlotPacket slot) {
         out.load("Server updates one container slot.", UiColorPalette.textBright());
         out.load("Container: #" + slot.getContainerId() + " state=" + slot.getStateId(), UiColorPalette.blue());
         out.load("Slot: " + slot.getSlot(), UiColorPalette.textBright());
         out.load("Item: " + PacketStateDeltaTracker.init_Str_ItemStack(slot.getItem()), UiColorPalette.successText());
      } else if (packet instanceof ClientboundSetCursorItemPacket cursor) {
         out.load("Server sets the carried cursor item.", UiColorPalette.textBright());
         out.load("Cursor: " + PacketStateDeltaTracker.init_Str_ItemStack(cursor.contents()), UiColorPalette.successText());
      } else if (packet instanceof ClientboundSetPlayerInventoryPacket inventory) {
         out.load("Server updates a player inventory slot.", UiColorPalette.textBright());
         out.load("Inventory Slot: " + inventory.slot(), UiColorPalette.blue());
         out.load("Item: " + PacketStateDeltaTracker.init_Str_ItemStack(inventory.contents()), UiColorPalette.successText());
      } else if (packet instanceof ClientboundSetHeldSlotPacket held) {
         out.load("Server changes the selected hotbar slot.", UiColorPalette.textBright());
         out.load("Selected Slot: " + held.slot(), UiColorPalette.successText());
      } else if (packet instanceof ServerboundSetCarriedItemPacket carried) {
         out.load("Client changes the selected hotbar slot.", UiColorPalette.textBright());
         out.load("Selected Slot: " + carried.getSlot(), UiColorPalette.successText());
      } else if (packet instanceof ServerboundContainerClickPacket click) {
         out.load("Client clicks inside a handled screen.", UiColorPalette.textBright());
         out.load("Container: #" + click.containerId() + " state=" + click.stateId(), UiColorPalette.blue());
         out.load("Input: " + init_Str_ContainerInput(click.containerInput()) + " (" + click.containerInput() + ")", UiColorPalette.successText());
         out.load("Slot: " + click.slotNum() + " | Button: " + init_Str_ContainerInput_I(click.containerInput(), click.buttonNum()), UiColorPalette.textBright());
         out.load("Changed Slots: " + click.changedSlots().size(), UiColorPalette.textBright());
         int shown = 0;

         for (Entry<HashedStack> slot : click.changedSlots().int2ObjectEntrySet()) {
            if (shown >= 16) {
               out.load("  ... +" + (click.changedSlots().size() - shown) + " more", UiColorPalette.textMuted());
               break;
            }

            out.load("  #" + slot.getIntKey() + " -> " + PacketStateDeltaTracker.init_Str_HashedStack(slot.getValue()), UiColorPalette.textBody());
            shown++;
         }

         out.load("Carried Item: " + PacketStateDeltaTracker.init_Str_HashedStack(click.carriedItem()), UiColorPalette.successText());
      } else if (packet instanceof ServerboundContainerButtonClickPacket button) {
         out.load("Client presses a handled-screen button.", UiColorPalette.textBright());
         out.load("Container: #" + load(init_Obj_Obj_StrArr(button, "containerId", "getContainerId", "getSyncId")), UiColorPalette.blue());
         out.load("Button Id: " + load(init_Obj_Obj_StrArr(button, "buttonId", "getButtonId")), UiColorPalette.textBright());
      } else if (packet instanceof ServerboundContainerClosePacket || packet instanceof ClientboundContainerClosePacket) {
         out.load(packet instanceof ServerboundContainerClosePacket ? "Client closes a handled screen." : "Server closes a handled screen.", UiColorPalette.textBright());
         Object id = packet instanceof ClientboundContainerClosePacket close
            ? close.getContainerId()
            : init_Obj_Obj_StrArr(packet, "containerId", "getContainerId");
         out.load("Container: #" + load(id), UiColorPalette.blue());
      } else if (packet instanceof ClientboundContainerSetDataPacket data) {
         out.load("Server updates a container property/data slot.", UiColorPalette.textBright());
         out.load("Container: #" + load(init_Obj_Obj_StrArr(data, "containerId", "getContainerId", "getSyncId")), UiColorPalette.blue());
         out.load(
            "Property: "
               + load(init_Obj_Obj_StrArr(data, "id", "getId", "propertyId", "getPropertyId"))
               + " = "
               + load(init_Obj_Obj_StrArr(data, "value", "getValue")),
            UiColorPalette.successText()
         );
      } else if (packet instanceof ServerboundSetCreativeModeSlotPacket creative) {
         out.load("Client sets a creative inventory slot.", UiColorPalette.textBright());
         out.load("Slot: " + load(init_Obj_Obj_StrArr(creative, "slot", "getSlot")), UiColorPalette.blue());
         out.load("Stack: " + init_Str_Obj(init_Obj_Obj_StrArr(creative, "itemStack", "stack", "getItem", "getStack")), UiColorPalette.successText());
      } else if (packet instanceof ServerboundSelectBundleItemPacket bundle) {
         out.load("Client selects an item inside a bundle.", UiColorPalette.textBright());
         out.load(
            "Slot: "
               + load(init_Obj_Obj_StrArr(bundle, "slot", "getSlot"))
               + " | Index: "
               + load(init_Obj_Obj_StrArr(bundle, "selectedItemIndex", "index", "getIndex")),
            UiColorPalette.blue()
         );
      } else if (packet instanceof ClientboundPlayerPositionPacket position) {
         PositionMoveRotation change = position.change();
         out.load("Server sends an authoritative player position/rotation correction.", UiColorPalette.textBright());
         out.load("Teleport Id: " + position.id(), UiColorPalette.blue());
         out.load("Relative Flags: " + init_Str_Set(position.relatives()), UiColorPalette.textBright());
         out.load("Encoded Position: " + init_Str_Vec3(change.position()), UiColorPalette.textBright());
         out.load("Encoded Velocity Delta: " + init_Str_Vec3(change.deltaMovement()), UiColorPalette.textBright());
         out.load("Encoded Rotation: yaw=" + init_Str_F(change.yRot()) + ", pitch=" + init_Str_F(change.xRot()), UiColorPalette.textBright());
         out.load("Expected Reply: ServerboundAcceptTeleportation #" + position.id(), UiColorPalette.amber());
      } else if (packet instanceof ServerboundMovePlayerPacket move) {
         out.load("Client sends player movement state.", UiColorPalette.textBright());
         out.load("Variant: " + move.getClass().getSimpleName().replace("ServerboundMovePlayerPacket$", ""), UiColorPalette.textBright());
         out.load("Has Position: " + move.hasPosition() + " | Has Rotation: " + move.hasRotation(), UiColorPalette.textBright());
         out.load("On Ground: " + move.isOnGround() + " | Horizontal Collision: " + move.horizontalCollision(), UiColorPalette.textBright());
         out.load("Encoded Position: x=" + move.getX(0.0) + ", y=" + move.getY(0.0) + ", z=" + move.getZ(0.0), UiColorPalette.successText());
         out.load("Encoded Rotation: yaw=" + init_Str_F(move.getYRot(0.0F)) + ", pitch=" + init_Str_F(move.getXRot(0.0F)), UiColorPalette.successText());
      } else if (packet instanceof ServerboundAcceptTeleportationPacket ack) {
         out.load("Client acknowledges a server teleport/correction.", UiColorPalette.textBright());
         out.load("Teleport Id: " + ack.id(), UiColorPalette.blue());
      } else if (packet instanceof ClientboundLoginPacket login) {
         out.load("Server enters the play world.", UiColorPalette.textBright());
         out.load("Player Entity Id: " + login.playerId(), UiColorPalette.blue());
         out.load("Dimension: " + login.commonPlayerSpawnInfo().dimension().identifier(), UiColorPalette.successText());
         out.load("Game Mode: " + login.commonPlayerSpawnInfo().gameType(), UiColorPalette.textBright());
         out.load("Chunk Radius: " + login.chunkRadius() + " | Simulation Distance: " + login.simulationDistance(), UiColorPalette.textBright());
      } else if (packet instanceof ClientboundRespawnPacket respawn) {
         out.load("Server respawns player or changes dimension/world.", UiColorPalette.textBright());
         out.load("Dimension: " + respawn.commonPlayerSpawnInfo().dimension().identifier(), UiColorPalette.successText());
         out.load("Game Mode: " + respawn.commonPlayerSpawnInfo().gameType(), UiColorPalette.textBright());
         out.load("Keep Data Mask: " + respawn.dataToKeep(), UiColorPalette.textBright());
      } else if (packet instanceof ClientboundTransferPacket transfer) {
         out.load("Server requests client transfer.", UiColorPalette.textBright());
         out.load("Target: " + transfer.host() + ":" + transfer.port(), UiColorPalette.successText());
      } else if (packet instanceof ClientboundStartConfigurationPacket) {
         out.load("Server starts configuration phase.", UiColorPalette.textBright());
      } else if (packet instanceof ClientboundFinishConfigurationPacket) {
         out.load("Server finishes configuration phase.", UiColorPalette.textBright());
      } else if (packet instanceof ClientboundSetChunkCacheCenterPacket center) {
         out.load("Server changes chunk cache center.", UiColorPalette.textBright());
         out.load("Chunk Center: " + center.getX() + ", " + center.getZ(), UiColorPalette.successText());
      } else if (packet instanceof ClientboundSetChunkCacheRadiusPacket radius) {
         out.load("Server changes chunk render/cache radius.", UiColorPalette.textBright());
         out.load("Radius: " + radius.getRadius(), UiColorPalette.successText());
      } else if (packet instanceof ClientboundSetSimulationDistancePacket simulation) {
         out.load("Server changes simulation distance.", UiColorPalette.textBright());
         out.load("Simulation Distance: " + simulation.simulationDistance(), UiColorPalette.successText());
      } else if (packet instanceof ClientboundForgetLevelChunkPacket forget) {
         out.load("Client should unload a chunk.", UiColorPalette.textBright());
         out.load("Chunk: " + forget.pos().x() + ", " + forget.pos().z(), UiColorPalette.successText());
      } else if (packet instanceof ClientboundLevelChunkWithLightPacket chunk) {
         out.load("Server sends chunk data with light data.", UiColorPalette.textBright());
         out.load("Chunk: " + chunk.x() + ", " + chunk.z(), UiColorPalette.successText());
      } else if (packet instanceof ClientboundGameEventPacket gameEvent) {
         out.load("Server sends a game/world event.", UiColorPalette.textBright());
         out.load("Event: " + load(gameEvent.getEvent()) + " | Value: " + gameEvent.getParam(), UiColorPalette.successText());
      } else if (packet instanceof ClientboundResourcePackPushPacket pack) {
         out.load("Server requests a resource pack.", UiColorPalette.textBright());
         out.load("Id: " + pack.id(), UiColorPalette.blue());
         out.load("Url: " + pack.url(), UiColorPalette.successText());
         out.load("Required: " + pack.required() + " | Hash: " + pack.hash(), UiColorPalette.textBright());
      } else if (packet instanceof ClientboundResourcePackPopPacket pack) {
         out.load("Server removes a resource pack.", UiColorPalette.textBright());
         out.load("Id: " + pack.id().map(Object::toString).orElse("latest / top"), UiColorPalette.blue());
      } else if (packet instanceof ClientboundDisconnectPacket disconnect) {
         out.load("Server disconnects the client.", UiColorPalette.textBright());
         out.load("Reason: " + load(disconnect.reason()), UiColorPalette.errorText());
      } else if (view != null && view.read()) {
         out.load("No specialist meaning layer yet; Reality shows the exact source-backed packet fields.", UiColorPalette.textMuted());
      }
   }

   private static void init_V_List_bh_StreamUtil(List<ItemStack> items, PacketDetailFormatter.Inner_StreamUtil out) {
      int shown = 0;

      for (int i = 0; i < items.size(); i++) {
         ItemStack stack = items.get(i);
         if (stack != null && !stack.isEmpty()) {
            if (shown == 0) {
               out.load("Non-empty Preview:", UiColorPalette.textBright());
            }

            if (shown >= 12) {
               out.load("  ... more non-empty slots hidden", UiColorPalette.textMuted());
               return;
            }

            out.load("  #" + i + " " + PacketStateDeltaTracker.init_Str_ItemStack(stack), UiColorPalette.textBody());
            shown++;
         }
      }
   }

   private static boolean init_Z_AutismPacketLogger(AutismPacketLoggerOverlay.LoggedPacketEntry entry, Packet<?> packet, PacketDetailFormatter.Inner_StreamUtil out) {
      PacketStateDeltaTracker.Inner_DataUtil capture = entry.init_bq_DataUtil;
      PacketStateDeltaTracker.Inner_CachePool before = capture == null ? PacketStateDeltaTracker.Inner_CachePool.init_bq_CachePool : capture.init_bq_CachePool();
      PacketStateDeltaTracker.Inner_CachePool after = capture == null ? PacketStateDeltaTracker.Inner_CachePool.init_bq_CachePool : capture.load();
      if (packet instanceof ClientboundOpenScreenPacket open) {
         out.init("Status: complete", UiColorPalette.mint());
         out.init("Meaning: server opened a handled screen", UiColorPalette.textBright());
         out.init("Container Id: " + open.getContainerId(), UiColorPalette.blue());
         out.init("Screen Type: " + load(open.getType()), UiColorPalette.textBright());
         out.init("Title: " + init_Str_Str(open.getTitle() == null ? "" : open.getTitle().getString()), UiColorPalette.successText());
         return true;
      } else if (packet instanceof ClientboundContainerSetContentPacket content) {
         out.init("Status: context-aware", UiColorPalette.mint());
         out.init("Meaning: server replaced full container contents", UiColorPalette.textBright());
         out.init("Container Id: " + content.containerId(), UiColorPalette.blue());
         out.init("State Id: " + content.stateId(), UiColorPalette.textBody());
         out.init("Slots: " + content.items().size() + " total, " + init_I_Collection(content.items()) + " non-empty", UiColorPalette.textBright());
         out.init("Container Area: " + init_Str_bq_CachePool(after), UiColorPalette.textBright());
         out.init("Cursor: " + PacketStateDeltaTracker.init_Str_ItemStack(content.carriedItem()), UiColorPalette.successText());
         load(content.items(), out);
         return true;
      } else if (packet instanceof ClientboundContainerSetSlotPacket slot) {
         out.init("Status: context-aware", UiColorPalette.mint());
         out.init("Meaning: server updated one container slot", UiColorPalette.textBright());
         out.init("Container Id: " + slot.getContainerId(), UiColorPalette.blue());
         out.init("State Id: " + slot.getStateId(), UiColorPalette.textBody());
         out.init("Slot: " + slot.getSlot() + " (" + PacketStateDeltaTracker.init_Str_bq_CachePool_I(after, slot.getSlot()) + ")", UiColorPalette.textBright());
         out.init("Before: " + before.init(slot.getSlot()), UiColorPalette.textMuted());
         out.init("After: " + PacketStateDeltaTracker.init_Str_ItemStack(slot.getItem()), UiColorPalette.successText());
         return true;
      } else if (packet instanceof ClientboundSetCursorItemPacket cursor) {
         out.init("Status: context-aware", UiColorPalette.mint());
         out.init("Meaning: server updated carried cursor item", UiColorPalette.textBright());
         out.init("Before: " + before.read_Str(), UiColorPalette.textMuted());
         out.init("After: " + PacketStateDeltaTracker.init_Str_ItemStack(cursor.contents()), UiColorPalette.successText());
         return true;
      } else if (packet instanceof ClientboundSetPlayerInventoryPacket inventory) {
         out.init("Status: context-aware", UiColorPalette.mint());
         out.init("Meaning: server updated a player inventory slot", UiColorPalette.textBright());
         out.init("Inventory Slot: " + inventory.slot(), UiColorPalette.blue());
         out.init("Before: " + before.load_Map().getOrDefault(inventory.slot(), "unknown"), UiColorPalette.textMuted());
         out.init("After: " + PacketStateDeltaTracker.init_Str_ItemStack(inventory.contents()), UiColorPalette.successText());
         return true;
      } else if (packet instanceof ClientboundSetHeldSlotPacket held) {
         out.init("Status: context-aware", UiColorPalette.mint());
         out.init("Meaning: server selected held hotbar slot", UiColorPalette.textBright());
         out.init("Before: " + before.update_I(), UiColorPalette.textMuted());
         out.init("After: " + held.slot(), UiColorPalette.successText());
         return true;
      } else if (packet instanceof ServerboundSetCarriedItemPacket carried) {
         out.init("Status: context-aware", UiColorPalette.mint());
         out.init("Meaning: client selected hotbar slot", UiColorPalette.textBright());
         out.init("Before: " + before.update_I(), UiColorPalette.textMuted());
         out.init("After: " + carried.getSlot(), UiColorPalette.successText());
         return true;
      } else if (packet instanceof ServerboundContainerClickPacket click) {
         init_V_ServerboundContain(click, before, after, out);
         return false;
      } else if (packet instanceof ServerboundContainerButtonClickPacket button) {
         out.init("Status: complete", UiColorPalette.mint());
         out.init("Meaning: client clicked a handled-screen button", UiColorPalette.textBright());
         out.init("Container Id: " + init_Obj_Obj_StrArr(button, "containerId", "getContainerId", "getSyncId"), UiColorPalette.blue());
         out.init("Button Id: " + init_Obj_Obj_StrArr(button, "buttonId", "getButtonId"), UiColorPalette.textBright());
         return true;
      } else if (packet instanceof ServerboundContainerClosePacket || packet instanceof ClientboundContainerClosePacket) {
         out.init("Status: context-aware", UiColorPalette.mint());
         out.init(
            "Meaning: " + (packet instanceof ServerboundContainerClosePacket ? "client closed a handled screen" : "server closed a handled screen"), UiColorPalette.textBright()
         );
         Object id = packet instanceof ClientboundContainerClosePacket close
            ? close.getContainerId()
            : init_Obj_Obj_StrArr(packet, "containerId", "getContainerId");
         out.init("Container Id: " + load(id), UiColorPalette.blue());
         if (!before.load_Str().isBlank()) {
            out.init("Closed Screen: " + init_Str_Str(before.load_Str()), UiColorPalette.successText());
         }

         return true;
      } else if (packet instanceof ClientboundContainerSetDataPacket data) {
         out.init("Status: complete", UiColorPalette.mint());
         out.init("Meaning: server updated container property/data", UiColorPalette.textBright());
         out.init("Container Id: " + init_Obj_Obj_StrArr(data, "containerId", "getContainerId", "getSyncId"), UiColorPalette.blue());
         out.init("Property: " + init_Obj_Obj_StrArr(data, "id", "getId", "propertyId", "getPropertyId"), UiColorPalette.textBright());
         out.init("Value: " + init_Obj_Obj_StrArr(data, "value", "getValue"), UiColorPalette.successText());
         return true;
      } else if (packet instanceof ServerboundSetCreativeModeSlotPacket creative) {
         out.init("Status: complete", UiColorPalette.mint());
         out.init("Meaning: client set a creative inventory slot", UiColorPalette.textBright());
         out.init("Slot: " + init_Obj_Obj_StrArr(creative, "slot", "getSlot"), UiColorPalette.blue());
         out.init("Stack: " + init_Str_Obj(init_Obj_Obj_StrArr(creative, "itemStack", "stack", "getItem", "getStack")), UiColorPalette.successText());
         return true;
      } else if (packet instanceof ServerboundSelectBundleItemPacket bundle) {
         out.init("Status: complete", UiColorPalette.mint());
         out.init("Meaning: client selected an item inside a bundle", UiColorPalette.textBright());
         out.init("Slot: " + init_Obj_Obj_StrArr(bundle, "slot", "getSlot"), UiColorPalette.blue());
         out.init("Selected Item Index: " + init_Obj_Obj_StrArr(bundle, "selectedItemIndex", "index", "getIndex"), UiColorPalette.textBright());
         return true;
      } else if (packet instanceof ClientboundPlayerPositionPacket position) {
         init_V_ClientboundPlayerP(position, before, after, out);
         return true;
      } else if (packet instanceof ServerboundMovePlayerPacket move) {
         init_V_ServerboundMovePla(move, before, after, out);
         return true;
      } else if (packet instanceof ServerboundAcceptTeleportationPacket ack) {
         out.init("Status: context-aware", UiColorPalette.mint());
         out.init("Meaning: client acknowledged server teleport/correction", UiColorPalette.textBright());
         out.init("Teleport Id: " + ack.id(), UiColorPalette.blue());
         if (before.sync() >= 0) {
            out.init("Pending Before: " + before.sync() + (before.sync() == ack.id() ? " (matched)" : " (different)"), UiColorPalette.textBright());
         }

         return true;
      } else if (packet instanceof ClientboundLoginPacket login) {
         init_V_ClientboundLoginPa_bh_StreamUtil(login, out);
         return true;
      } else if (packet instanceof ClientboundRespawnPacket respawn) {
         out.init("Status: complete", UiColorPalette.mint());
         out.init("Meaning: respawn or dimension/world change", UiColorPalette.textBright());
         out.init("Dimension: " + respawn.commonPlayerSpawnInfo().dimension().identifier(), UiColorPalette.successText());
         out.init("Game Mode: " + respawn.commonPlayerSpawnInfo().gameType(), UiColorPalette.textBright());
         out.init("Previous Game Mode: " + respawn.commonPlayerSpawnInfo().previousGameType(), UiColorPalette.textBody());
         out.init("Keep Data Mask: " + respawn.dataToKeep(), UiColorPalette.textBright());
         return true;
      } else if (packet instanceof ClientboundTransferPacket transfer) {
         out.init("Status: complete", UiColorPalette.mint());
         out.init("Meaning: server requested client transfer", UiColorPalette.textBright());
         out.init("Target: " + transfer.host() + ":" + transfer.port(), UiColorPalette.successText());
         return true;
      } else if (packet instanceof ClientboundStartConfigurationPacket) {
         out.init("Status: complete", UiColorPalette.mint());
         out.init("Meaning: play connection is switching into configuration phase", UiColorPalette.textBright());
         return true;
      } else if (packet instanceof ClientboundFinishConfigurationPacket) {
         out.init("Status: complete", UiColorPalette.mint());
         out.init("Meaning: configuration phase finished", UiColorPalette.textBright());
         return true;
      } else if (packet instanceof ClientboundSetChunkCacheCenterPacket center) {
         out.init("Status: complete", UiColorPalette.mint());
         out.init("Meaning: server changed chunk cache center", UiColorPalette.textBright());
         out.init("Chunk Center: " + center.getX() + ", " + center.getZ(), UiColorPalette.successText());
         return true;
      } else if (packet instanceof ClientboundSetChunkCacheRadiusPacket radius) {
         out.init("Status: complete", UiColorPalette.mint());
         out.init("Meaning: server changed chunk render/cache radius", UiColorPalette.textBright());
         out.init("Radius: " + radius.getRadius(), UiColorPalette.successText());
         return true;
      } else if (packet instanceof ClientboundSetSimulationDistancePacket simulation) {
         out.init("Status: complete", UiColorPalette.mint());
         out.init("Meaning: server changed simulation distance", UiColorPalette.textBright());
         out.init("Simulation Distance: " + simulation.simulationDistance(), UiColorPalette.successText());
         return true;
      } else if (packet instanceof ClientboundForgetLevelChunkPacket forget) {
         out.init("Status: complete", UiColorPalette.mint());
         out.init("Meaning: client should unload a chunk", UiColorPalette.textBright());
         out.init("Chunk: " + forget.pos().x() + ", " + forget.pos().z(), UiColorPalette.successText());
         return true;
      } else if (packet instanceof ClientboundLevelChunkWithLightPacket chunk) {
         out.init("Status: fallback", UiColorPalette.warningYellow());
         out.init("Meaning: server sent chunk data with light data", UiColorPalette.textBright());
         out.init("Chunk: " + chunk.x() + ", " + chunk.z(), UiColorPalette.successText());
         out.init("Chunk Data: " + load(chunk.chunkData()), UiColorPalette.textBody());
         out.init("Light Data: " + load(chunk.lightData()), UiColorPalette.textBody());
         return false;
      } else if (packet instanceof ClientboundGameEventPacket gameEvent) {
         out.init("Status: complete", UiColorPalette.mint());
         out.init("Meaning: game/world event", UiColorPalette.textBright());
         out.init("Event: " + load(gameEvent.getEvent()), UiColorPalette.textBright());
         out.init("Value: " + gameEvent.getParam(), UiColorPalette.successText());
         return true;
      } else if (packet instanceof ClientboundResourcePackPushPacket pack) {
         out.init("Status: complete", UiColorPalette.mint());
         out.init("Meaning: server requested resource pack", UiColorPalette.textBright());
         out.init("Id: " + pack.id(), UiColorPalette.blue());
         out.init("Url: " + pack.url(), UiColorPalette.successText());
         out.init("Hash: " + pack.hash(), UiColorPalette.textBody());
         out.init("Required: " + pack.required(), UiColorPalette.textBright());
         pack.prompt().ifPresent(component -> out.init("Prompt: " + init_Str_Str(component.getString()), UiColorPalette.textBright()));
         return true;
      } else if (packet instanceof ClientboundResourcePackPopPacket pack) {
         out.init("Status: complete", UiColorPalette.mint());
         out.init("Meaning: server removed resource pack", UiColorPalette.textBright());
         out.init("Id: " + pack.id().map(Object::toString).orElse("latest / top"), UiColorPalette.blue());
         return true;
      } else if (packet instanceof ClientboundDisconnectPacket disconnect) {
         out.init("Status: complete", UiColorPalette.mint());
         out.init("Meaning: server disconnected the client", UiColorPalette.textBright());
         out.init("Reason: " + load(disconnect.reason()), UiColorPalette.errorText());
         return true;
      } else {
         return false;
      }
   }

   private static void init_V_ServerboundContain(
      ServerboundContainerClickPacket click, PacketStateDeltaTracker.Inner_CachePool before, PacketStateDeltaTracker.Inner_CachePool after, PacketDetailFormatter.Inner_StreamUtil out
   ) {
      out.init("Status: context-aware", UiColorPalette.mint());
      out.init("Meaning: client clicked inside a handled screen", UiColorPalette.textBright());
      out.init("Container Id: " + click.containerId(), UiColorPalette.blue());
      out.init("State Id: " + click.stateId(), UiColorPalette.textBody());
      out.init("Input: " + init_Str_ContainerInput(click.containerInput()) + " (" + click.containerInput() + ")", UiColorPalette.successText());
      out.init("Slot: " + click.slotNum() + " (" + PacketStateDeltaTracker.init_Str_bq_CachePool_I(before, click.slotNum()) + ")", UiColorPalette.textBright());
      out.init("Button: " + init_Str_ContainerInput_I(click.containerInput(), click.buttonNum()), UiColorPalette.textBright());
      out.init("Slot Before: " + before.init(click.slotNum()), UiColorPalette.textMuted());
      out.init("Slot After: " + after.init(click.slotNum()), UiColorPalette.successText());
      out.init("Cursor Before: " + before.read_Str(), UiColorPalette.textMuted());
      out.init("Cursor After: " + PacketStateDeltaTracker.init_Str_HashedStack(click.carriedItem()), UiColorPalette.successText());
      out.init("Changed Slots: " + click.changedSlots().size(), UiColorPalette.textBright());
      int shown = 0;

      for (Entry<HashedStack> slot : click.changedSlots().int2ObjectEntrySet()) {
         if (shown >= 16) {
            out.init("  ... +" + (click.changedSlots().size() - shown) + " more", UiColorPalette.textMuted());
            break;
         }

         out.init(
            "  #" + slot.getIntKey() + " " + PacketStateDeltaTracker.init_Str_bq_CachePool_I(before, slot.getIntKey()) + " -> " + PacketStateDeltaTracker.init_Str_HashedStack(slot.getValue()),
            UiColorPalette.textBody()
         );
         shown++;
      }
   }

   private static void init_V_ClientboundPlayerP(
      ClientboundPlayerPositionPacket position, PacketStateDeltaTracker.Inner_CachePool before, PacketStateDeltaTracker.Inner_CachePool after, PacketDetailFormatter.Inner_StreamUtil out
   ) {
      PositionMoveRotation change = position.change();
      out.init("Status: context-aware", UiColorPalette.mint());
      out.init("Meaning: server corrected/teleported local player", UiColorPalette.textBright());
      out.init("Teleport Id: " + position.id(), UiColorPalette.blue());
      out.init("Relative Flags: " + init_Str_Set(position.relatives()), UiColorPalette.textBright());
      out.init("Raw Position Change: " + init_Str_Vec3(change.position()), UiColorPalette.textBright());
      out.init("Raw Delta Movement: " + init_Str_Vec3(change.deltaMovement()), UiColorPalette.textBright());
      out.init("Raw Rotation: yaw=" + init_Str_F(change.yRot()) + ", pitch=" + init_Str_F(change.xRot()), UiColorPalette.textBright());
      if (before.load_bq_StreamUtil() != null) {
         out.init("Before Server Pos: " + PacketStateDeltaTracker.init_Str_bq_StreamUtil(before.load_bq_StreamUtil()), UiColorPalette.textMuted());
      }

      if (after.load_bq_StreamUtil() != null) {
         out.init("Computed After: " + PacketStateDeltaTracker.init_Str_bq_StreamUtil(after.load_bq_StreamUtil()), UiColorPalette.successText());
      }

      out.init("Ack Expected: ServerboundAcceptTeleportation #" + position.id(), UiColorPalette.amber());
   }

   private static void init_V_ServerboundMovePla(ServerboundMovePlayerPacket move, PacketStateDeltaTracker.Inner_CachePool before, PacketStateDeltaTracker.Inner_CachePool after, PacketDetailFormatter.Inner_StreamUtil out) {
      out.init("Status: context-aware", UiColorPalette.mint());
      out.init("Meaning: client player movement update", UiColorPalette.textBright());
      out.init("Variant: " + move.getClass().getSimpleName().replace("ServerboundMovePlayerPacket$", ""), UiColorPalette.textBright());
      out.init("Has Position: " + move.hasPosition(), UiColorPalette.textBright());
      out.init("Has Rotation: " + move.hasRotation(), UiColorPalette.textBright());
      out.init("On Ground: " + move.isOnGround(), UiColorPalette.textBright());
      out.init("Horizontal Collision: " + move.horizontalCollision(), UiColorPalette.textBright());
      if (before.init_bq_StreamUtil() != null) {
         out.init("Before: " + PacketStateDeltaTracker.init_Str_bq_StreamUtil(before.init_bq_StreamUtil()), UiColorPalette.textMuted());
      }

      if (after.init_bq_StreamUtil() != null) {
         out.init("After: " + PacketStateDeltaTracker.init_Str_bq_StreamUtil(after.init_bq_StreamUtil()), UiColorPalette.successText());
         if (before.init_bq_StreamUtil() != null) {
            out.init("Distance Delta: " + String.format(Locale.ROOT, "%.4f blocks", before.init_bq_StreamUtil().init(after.init_bq_StreamUtil())), UiColorPalette.warningYellow());
         }
      }
   }

   private static void init_V_ClientboundLoginPa_bh_StreamUtil(ClientboundLoginPacket login, PacketDetailFormatter.Inner_StreamUtil out) {
      out.init("Status: complete", UiColorPalette.mint());
      out.init("Meaning: entered play world", UiColorPalette.textBright());
      out.init("Player Entity Id: " + login.playerId(), UiColorPalette.blue());
      out.init("Dimension: " + login.commonPlayerSpawnInfo().dimension().identifier(), UiColorPalette.successText());
      out.init("Game Mode: " + login.commonPlayerSpawnInfo().gameType(), UiColorPalette.textBright());
      out.init("Hardcore: " + login.hardcore(), UiColorPalette.textBright());
      out.init("Known Levels: " + login.levels().size(), UiColorPalette.textBright());
      out.init("Chunk Radius: " + login.chunkRadius(), UiColorPalette.textBright());
      out.init("Simulation Distance: " + login.simulationDistance(), UiColorPalette.textBright());
      out.init("Reduced Debug: " + login.reducedDebugInfo(), UiColorPalette.textBright());
      out.init("Secure Chat Enforced: " + login.enforcesSecureChat(), UiColorPalette.textBright());
   }

   private static void init_V_bq_DataUtil_bh_StreamUtil(PacketStateDeltaTracker.Inner_DataUtil capture, PacketDetailFormatter.Inner_StreamUtil out) {
      if (capture != null && capture.init_Z()) {
         PacketStateDeltaTracker.Inner_CachePool before = capture.init_bq_CachePool();
         PacketStateDeltaTracker.Inner_CachePool after = capture.load();
         out.load("Context Status: " + (before.init_Z() ? "tracked" : "started mid-stream / partial"), before.init_Z() ? UiColorPalette.mint() : UiColorPalette.warningYellow());
         if (!after.update_Str().isBlank()) {
            out.load("Protocol State: " + after.update_Str(), UiColorPalette.textBright());
         }

         if (after.init_I() >= 0 || !after.load_Str().isBlank()) {
            out.load("Screen: #" + after.init_I() + " " + init_Str_Str(after.load_Str()) + " " + after.init_Str(), UiColorPalette.successText());
         }

         if (!after.write_Str().isBlank()) {
            out.load("Dimension: " + after.write_Str(), UiColorPalette.successText());
         }

         if (after.init_Integer() != null && after.load_Integer() != null) {
            out.load("Chunk Center: " + after.init_Integer() + ", " + after.load_Integer(), UiColorPalette.textBright());
         }

         if (after.read_Integer() != null) {
            out.load("Chunk Radius: " + after.read_Integer(), UiColorPalette.textBright());
         }

         if (after.write_Integer() != null) {
            out.load("Simulation Distance: " + after.write_Integer(), UiColorPalette.textBright());
         }

         if (after.sync() >= 0) {
            out.load("Pending Teleport Ack: " + after.sync(), UiColorPalette.amber());
         }

         if (!capture.init_List().isEmpty()) {
            out.load("Changes:", UiColorPalette.textBright());

            for (String change : capture.init_List()) {
               out.load("  - " + change, UiColorPalette.textBody());
            }
         }
      }
   }

   private static void init_V_AutismPacketLogger_bh_StreamUtil(AutismPacketLoggerOverlay.LoggedPacketEntry entry, PacketDetailFormatter.Inner_StreamUtil out) {
      if (entry != null && entry.init_Packet != null) {
         PacketWireCaptureStore.Inner_StreamUtil snapshot = PacketWireCaptureStore.init(entry.init_Packet);
         if (snapshot != null) {
            out.read("Protocol: " + snapshot.load(), UiColorPalette.textBody());
            out.read("Direction: " + snapshot.init_Str(), "C2S".equalsIgnoreCase(snapshot.init_Str()) ? UiColorPalette.amber() : UiColorPalette.cyan());
            if (snapshot.init_I() >= 0) {
               out.read("Packet ID: " + snapshot.init_I(), UiColorPalette.textBody());
            }

            if (snapshot.write() != null && !snapshot.write().isBlank()) {
               out.read("Packet Type: " + snapshot.write(), UiColorPalette.textBody());
            }

            byte[] bytes = snapshot.init_BArr();
            out.read("Plaintext: " + bytes.length + " bytes " + PacketWireCaptureStore.init(bytes, 48), UiColorPalette.textMuted());
         }
      }
   }

   private static void load(List<ItemStack> items, PacketDetailFormatter.Inner_StreamUtil out) {
      int shown = 0;

      for (int i = 0; i < items.size(); i++) {
         ItemStack stack = items.get(i);
         if (stack != null && !stack.isEmpty()) {
            if (shown == 0) {
               out.init("Non-empty Preview:", UiColorPalette.textBright());
            }

            if (shown >= 12) {
               out.init("  ... more non-empty slots hidden", UiColorPalette.textMuted());
               return;
            }

            out.init("  #" + i + " " + PacketStateDeltaTracker.init_Str_ItemStack(stack), UiColorPalette.textBody());
            shown++;
         }
      }
   }

   private static String init_Str_bq_CachePool(PacketStateDeltaTracker.Inner_CachePool snapshot) {
      if (snapshot != null && snapshot.read_I() >= 0) {
         int containerSlots = Math.max(0, snapshot.write_I());
         int playerSlots = Math.max(0, snapshot.read_I() - containerSlots);
         return containerSlots + " container slots + " + playerSlots + " player slots";
      } else {
         return "unknown";
      }
   }

   private static int init_I_Collection(Collection<ItemStack> items) {
      int count = 0;
      if (items != null) {
         for (ItemStack item : items) {
            if (item != null && !item.isEmpty()) {
               count++;
            }
         }
      }

      return count;
   }

   private static String init_Str_ContainerInput(ContainerInput input) {
      if (input == null) {
         return "unknown";
      }

      String var10000;
      switch (input) {
         case PICKUP:
            var10000 = "PICKUP left/right click pickup/place";
            break;
         case QUICK_MOVE:
            var10000 = "QUICK_MOVE shift-click transfer";
            break;
         case SWAP:
            var10000 = "SWAP hotbar/offhand swap";
            break;
         case CLONE:
            var10000 = "CLONE creative middle-click";
            break;
         case THROW:
            var10000 = "THROW drop from slot";
            break;
         case QUICK_CRAFT:
            var10000 = "QUICK_CRAFT drag distribute";
            break;
         case PICKUP_ALL:
            var10000 = "PICKUP_ALL gather matching carried stack";
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   private static String init_Str_ContainerInput_I(ContainerInput input, int button) {
      if (input == ContainerInput.PICKUP) {
         return button == 0 ? "0 left click" : (button == 1 ? "1 right click" : String.valueOf(button));
      } else if (input == ContainerInput.QUICK_MOVE) {
         return button == 0 ? "0 shift-left" : (button == 1 ? "1 shift-right" : String.valueOf(button));
      } else if (input == ContainerInput.SWAP) {
         return button == 40 ? "40 offhand" : button + " hotbar slot";
      } else if (input == ContainerInput.THROW) {
         return button == 0 ? "0 drop one" : (button == 1 ? "1 drop stack" : String.valueOf(button));
      } else {
         return String.valueOf(button);
      }
   }

   private static String init_Str_Obj(Object value) {
      return value instanceof ItemStack stack ? PacketStateDeltaTracker.init_Str_ItemStack(stack) : load(value);
   }

   private static String init_Str_Set(Set<Relative> relatives) {
      return relatives != null && !relatives.isEmpty() ? relatives.toString() : "none / absolute";
   }

   private static String init_Str_Vec3(Vec3 vec) {
      return vec == null ? "unknown" : String.format(Locale.ROOT, "x=%.3f, y=%.3f, z=%.3f", vec.x, vec.y, vec.z);
   }

   private static String init_Str_F(float value) {
      return String.format(Locale.ROOT, "%.2f", value);
   }

   private static Object init_Obj_Obj_StrArr(Object target, String... methodNames) {
      if (target != null && methodNames != null) {
         for (String methodName : methodNames) {
            if (methodName != null && !methodName.isBlank()) {
               try {
                  Method method = target.getClass().getMethod(methodName);
                  if (method.getParameterCount() == 0) {
                     return method.invoke(target);
                  }
               } catch (Throwable var7) {
               }
            }
         }

         return null;
      } else {
         return null;
      }
   }

   private static String load(Object value) {
      if (value == null) {
         return "unknown";
      } else if (value instanceof ItemStack stack) {
         return PacketStateDeltaTracker.init_Str_ItemStack(stack);
      } else {
         try {
            if (value instanceof Optional<?> optional) {
               return optional.<String>map(PacketDetailFormatter::load).orElse("empty");
            } else if (value instanceof Holder<?> holder) {
               return holder.unwrapKey().map(key -> key.identifier().toString()).orElse(String.valueOf(holder.value()));
            } else {
               return value instanceof Item item ? BuiltInRegistries.ITEM.getKey(item).toString() : String.valueOf(value);
            }
         } catch (Throwable ignored) {
            return value.getClass().getSimpleName();
         }
      }
   }

   private static String init_Str_Str(String value) {
      if (value == null) {
         return "\"\"";
      }

      String trimmed = value.length() > 96 ? value.substring(0, 93) + "..." : value;
      return "\"" + trimmed + "\"";
   }

   private record Inner_CachePool(Packet<?> init_Packet, boolean init_Z, String init_Str) {
      static PacketDetailFormatter.Inner_CachePool init_bh_CachePool() {
         return new PacketDetailFormatter.Inner_CachePool(null, false, "");
      }

      static PacketDetailFormatter.Inner_CachePool init(Packet<?> packet, String message) {
         return new PacketDetailFormatter.Inner_CachePool(packet, true, message);
      }

      static PacketDetailFormatter.Inner_CachePool init(String message) {
         return new PacketDetailFormatter.Inner_CachePool(null, false, message);
      }

      String init_Str_2() {
         return this.init_Z ? "wire-codec" : "fallback";
      }

      public String load() {
         return this.init_Str;
      }
   }

   public record Inner_DataUtil(
      List<PacketInspectorEngine.ColoredLine> init_List, List<PacketInspectorEngine.ColoredLine> load, List<PacketInspectorEngine.ColoredLine> read, boolean init_Z, String init_Str
   ) {
      public Inner_DataUtil {
         init_List = init_List == null ? List.of() : List.copyOf(init_List);
         load = load == null ? List.of() : List.copyOf(load);
         read = read == null ? List.of() : List.copyOf(read);
         init_Str = init_Str == null ? "fallback" : init_Str;
      }

      public boolean init_Z() {
         return !this.init_List.isEmpty();
      }

      public boolean load_Z() {
         return !this.load.isEmpty();
      }

      public boolean read_Z() {
         return !this.read.isEmpty();
      }

      public List<PacketInspectorEngine.ColoredLine> load_List() {
         return this.load;
      }

      public List<PacketInspectorEngine.ColoredLine> read_List() {
         return this.read;
      }

      public boolean write() {
         return this.init_Z;
      }
   }

   private static final class Inner_StreamUtil {
      private final List<PacketInspectorEngine.ColoredLine> init = new ArrayList<>();
      private final List<PacketInspectorEngine.ColoredLine> load = new ArrayList<>();
      private final List<PacketInspectorEngine.ColoredLine> read = new ArrayList<>();

      void init(String text, int color) {
         this.init.add(new PacketInspectorEngine.ColoredLine(text, color));
      }

      void load(String text, int color) {
         this.load.add(new PacketInspectorEngine.ColoredLine(text, color));
      }

      void read(String text, int color) {
         this.read.add(new PacketInspectorEngine.ColoredLine(text, color));
      }

      boolean init() {
         return !this.init.isEmpty();
      }

      PacketDetailFormatter.Inner_DataUtil init(boolean complete, String status) {
         return new PacketDetailFormatter.Inner_DataUtil(this.init, this.load, this.read, complete, status);
      }
   }
}
